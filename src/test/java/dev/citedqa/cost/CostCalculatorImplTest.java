package dev.citedqa.cost;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CostCalculatorImplTest {

    @Test
    void shouldComputeExactCostFromTokenCountsAndPricesPerMillion() {
        // Given: EUR 0.13 per 1M input tokens, EUR 0.51 per 1M output tokens
        CostCalculator costCalculator = new CostCalculatorImpl(new BigDecimal("0.13"), new BigDecimal("0.51"));

        // When
        BigDecimal costEur = costCalculator.cost(2_000, 120);

        // Then: 2000 x 0.13 / 1M = 0.00026, 120 x 0.51 / 1M = 0.0000612, total 0.0003212
        assertEquals(0, new BigDecimal("0.0003212").compareTo(costEur));
    }
}
