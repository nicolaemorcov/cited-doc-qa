package dev.citedqa.cost;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CostCalculatorImpl implements CostCalculator {

    private static final int TOKENS_PER_MILLION_DIGITS = 6;

    private final BigDecimal inputPerMillionEur;
    private final BigDecimal outputPerMillionEur;

    public CostCalculatorImpl(
            @Value("${citedqa.pricing.chat.input-per-million-eur}") BigDecimal inputPerMillionEur,
            @Value("${citedqa.pricing.chat.output-per-million-eur}") BigDecimal outputPerMillionEur) {
        this.inputPerMillionEur = inputPerMillionEur;
        this.outputPerMillionEur = outputPerMillionEur;
    }

    /**
     * tokens x price per million, divided by one million. movePointLeft(6) divides
     * by 10^6 exactly, so no rounding mode is needed.
     */
    @Override
    public BigDecimal cost(int promptTokens, int completionTokens) {
        BigDecimal inputCost = inputPerMillionEur.multiply(BigDecimal.valueOf(promptTokens));
        BigDecimal outputCost = outputPerMillionEur.multiply(BigDecimal.valueOf(completionTokens));
        return inputCost.add(outputCost).movePointLeft(TOKENS_PER_MILLION_DIGITS);
    }
}
