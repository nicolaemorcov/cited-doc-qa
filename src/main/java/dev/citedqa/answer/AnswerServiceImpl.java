package dev.citedqa.answer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import dev.citedqa.corpus.Article;
import dev.citedqa.retrieval.RetrievalService;
import dev.citedqa.web.AnswerResponse;
import dev.citedqa.web.Source;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AnswerServiceImpl implements AnswerService {

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

    private final RetrievalService retrievalService;
    private final ChatClient chatClient;

    public AnswerServiceImpl(RetrievalService retrievalService, ChatClient.Builder chatClientBuilder) {
        this.retrievalService = retrievalService;
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public AnswerResponse answer(String question) {
        String requestId = UUID.randomUUID().toString();
        List<Article> articles = retrievalService.search(question);

        ModelAnswer modelAnswer = chatClient.prompt()
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .system(SYSTEM_PROMPT)
                .user(userSpec -> userSpec.text(USER_TEMPLATE)
                        .param("articles", formatArticles(articles))
                        .param("question", question))
                .call()
                .responseEntity(ModelAnswer.class)
                .entity();

        List<Source> sources = validSources(modelAnswer, articles);
        if (sources.isEmpty()) {
            return new AnswerResponse(requestId, NOT_COVERED_ANSWER, List.of());
        }
        return new AnswerResponse(requestId, modelAnswer.answer(), sources);
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
    private List<Source> validSources(ModelAnswer modelAnswer, List<Article> articles) {
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
                .map(number -> toSource(retrievedByNumber.get(number)))
                .toList();
    }

    private Source toSource(Article article) {
        return new Source(article.document(), "Article " + article.number(), article.title());
    }
}
