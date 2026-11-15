package dev.citedqa.eval;

import java.math.BigDecimal;
import java.util.List;

/**
 * The outcome of one eval question. retrievalHit is null for questions the
 * document does not answer (nothing to retrieve). error names the exception
 * class when the question could not be answered; such a row counts as a miss.
 * group is the expected document, or "uncovered"; expected, retrieved and cited
 * are labels such as "GDPR 30" or "AI Act 12", because article numbers repeat across documents.
 */
public record EvalResult(
        int id,
        String type,
        String group,
        List<String> expected,
        List<String> retrieved,
        List<String> cited,
        Boolean retrievalHit,
        boolean citationHit,
        Integer promptTokens,
        Integer completionTokens,
        BigDecimal costEur,
        String error) {
}
