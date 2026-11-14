package dev.citedqa.answer;

import java.math.BigDecimal;
import java.util.List;

import dev.citedqa.corpus.Article;
import dev.citedqa.web.AnswerResponse;

/**
 * Everything one answered question produced: the response sent to the client,
 * plus the retrieved articles and the token usage, used by the eval.
 * Token counts and cost are null when the provider reported no usage.
 */
public record AnswerResult(
        AnswerResponse response,
        List<Article> retrievedArticles,
        Integer promptTokens,
        Integer completionTokens,
        BigDecimal costEur) {
}
