package dev.citedqa.answer;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import dev.citedqa.corpus.Article;
import dev.citedqa.cost.CostCalculator;
import dev.citedqa.privacy.PiiMasker;
import dev.citedqa.retrieval.RetrievalService;
import dev.citedqa.web.AnswerResponse;
import dev.citedqa.web.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

@Service
public class AnswerServiceImpl implements AnswerService {

    private static final Logger log = LoggerFactory.getLogger(AnswerServiceImpl.class);

    static final String NOT_COVERED_ANSWER =
            "The document does not cover this question.";

    private static final String SYSTEM_PROMPT = """
            You answer questions about a legal document using only the articles in the user message.
            Rules:
            1. Use only the text of those articles. Do not use outside knowledge.
            2. In "articles", list the numbers of the articles your answer is based on.
            3. If the articles do not answer the question, say so in "answer" and return an empty "articles" list.
            """;

    private static final String USER_TEMPLATE = """
            Articles:

            {articles}

            Question: {question}
            """;

    private final PiiMasker piiMasker;
    private final RetrievalService retrievalService;
    private final ChatClient chatClient;
    private final CostCalculator costCalculator;

    public AnswerServiceImpl(PiiMasker piiMasker, RetrievalService retrievalService,
                             ChatClient.Builder chatClientBuilder, CostCalculator costCalculator) {
        this.piiMasker = piiMasker;
        this.retrievalService = retrievalService;
        this.chatClient = chatClientBuilder.build();
        this.costCalculator = costCalculator;
    }

    /**
     * Masking is the first line: both the embedding call (inside search) and the
     * chat call send the question to the provider, so both get the masked text.
     */
    @Override
    public AnswerResult answer(String question) {
        String maskedQuestion = piiMasker.mask(question);
        long startNanos = System.nanoTime();
        String requestId = UUID.randomUUID().toString();
        List<Article> articles = retrievalService.search(maskedQuestion);

        ResponseEntity<ChatResponse, ModelAnswer> responseEntity = chatClient.prompt()
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .system(SYSTEM_PROMPT)
                .user(userSpec -> userSpec.text(USER_TEMPLATE)
                        .param("articles", formatArticles(articles))
                        .param("question", maskedQuestion))
                .call()
                .responseEntity(ModelAnswer.class);
        ModelAnswer modelAnswer = responseEntity.entity();

        List<Article> citedArticles = validCitedArticles(modelAnswer, articles);
        List<Source> sources = citedArticles.stream().map(this::toSource).toList();
        AnswerResponse answerResponse = sources.isEmpty()
                ? new AnswerResponse(requestId, NOT_COVERED_ANSWER, List.of())
                : new AnswerResponse(requestId, modelAnswer.answer(), sources);
        Usage usage = responseEntity.response() == null ? null : responseEntity.response().getMetadata().getUsage();
        boolean usageReported = usage != null && !(usage instanceof EmptyUsage);
        Integer promptTokens = usageReported ? usage.getPromptTokens() : null;
        Integer completionTokens = usageReported ? usage.getCompletionTokens() : null;
        BigDecimal costEur = promptTokens == null || completionTokens == null
                ? null
                : costCalculator.cost(promptTokens, completionTokens);
        logCost(requestId, promptTokens, completionTokens, costEur, citedArticles, startNanos);
        return new AnswerResult(answerResponse, articles, promptTokens, completionTokens, costEur);
    }

    /**
     * Logs one line per request. Never logs the question text. Spring AI reports
     * missing token counts as EmptyUsage (zeros), so a null cost here means the
     * provider reported no usage; that is logged at WARN instead of a false zero.
     */
    private void logCost(String requestId, Integer promptTokens, Integer completionTokens, BigDecimal costEur,
                         List<Article> citedArticles, long startNanos) {
        long latencyMs = (System.nanoTime() - startNanos) / 1_000_000;
        List<Integer> articleNumbers = citedArticles.stream().map(Article::number).toList();
        if (costEur == null) {
            log.warn("requestId={}, promptTokens=unknown, completionTokens=unknown, costEur=unknown, latencyMs={}, sources={}",
                    requestId, latencyMs, articleNumbers);
            return;
        }
        log.info("requestId={}, promptTokens={}, completionTokens={}, costEur={}, latencyMs={}, sources={}",
                requestId, promptTokens, completionTokens, costEur.toPlainString(), latencyMs, articleNumbers);
    }

    /**
     * Labels each article with its number and title so the model can cite it.
     */
    private String formatArticles(List<Article> articles) {
        return articles.stream()
                .map(article -> "[Article " + article.number() + "] " + article.title() + "\n" + article.text())
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * Keeps only cited numbers that were among the retrieved articles, in the
     * order the model cited them, without duplicates.
     */
    private List<Article> validCitedArticles(ModelAnswer modelAnswer, List<Article> articles) {
        if (modelAnswer == null || modelAnswer.articles() == null) {
            return List.of();
        }
        Map<Integer, Article> retrievedByNumber = new HashMap<>();
        for (Article article : articles) {
            retrievedByNumber.put(article.number(), article);
        }
        return modelAnswer.articles().stream()
                .distinct()
                .filter(retrievedByNumber::containsKey)
                .map(retrievedByNumber::get)
                .toList();
    }

    private Source toSource(Article article) {
        return new Source(article.document(), "Article " + article.number(), article.title());
    }
}
