package dev.citedqa.cost;

import java.math.BigDecimal;

/**
 * Computes the cost of one chat call from its token counts.
 */
public interface CostCalculator {

    /**
     * @return cost in EUR, exact (no rounding)
     */
    BigDecimal cost(int promptTokens, int completionTokens);
}
