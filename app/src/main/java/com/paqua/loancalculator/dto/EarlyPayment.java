package com.paqua.loancalculator.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Represents early pay off of the loan
 */
public class EarlyPayment implements Serializable {
    private static final long serialVersionUID = -4828623603301324540L;

    /**
     * Payment amount
     */
    private final BigDecimal amount;

    /**
     * Type of early payment
     */
    private final EarlyPaymentStrategy strategy;

    /**
     * Repeating strategy
     */
    private final EarlyPaymentRepeatingStrategy repeatingStrategy;

    /**
     * Additional parameters
     */
    private final Map<EarlyPaymentAdditionalParameters, String> additionalParameters;

    public EarlyPayment(BigDecimal amount, EarlyPaymentStrategy type, EarlyPaymentRepeatingStrategy repeatingStrategy, Map<EarlyPaymentAdditionalParameters, String> additionalParameters) {
        this.amount = amount;
        this.strategy = type;
        this.repeatingStrategy = repeatingStrategy;
        this.additionalParameters = additionalParameters;
    }

    /**
     * @return Amount of early payment
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * @return Strategy
     */
    public EarlyPaymentStrategy getStrategy() {
        return strategy;
    }

    /**
     * @return Repeating strategy
     */
    public EarlyPaymentRepeatingStrategy getRepeatingStrategy() {
        return repeatingStrategy;
    }

    /**
     * @return Additional parameters
     */
    public Map<EarlyPaymentAdditionalParameters, String> getAdditionalParameters() {
        return additionalParameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EarlyPayment that = (EarlyPayment) o;

        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        if (strategy != that.strategy) return false;
        if (repeatingStrategy != that.repeatingStrategy) return false;
        return additionalParameters != null ? additionalParameters.equals(that.additionalParameters) : that.additionalParameters == null;
    }

    @Override
    public int hashCode() {
        int result = amount != null ? amount.hashCode() : 0;
        result = 31 * result + (strategy != null ? strategy.hashCode() : 0);
        result = 31 * result + (repeatingStrategy != null ? repeatingStrategy.hashCode() : 0);
        result = 31 * result + (additionalParameters != null ? additionalParameters.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EarlyPayment{" +
                "amount=" + amount +
                ", strategy=" + strategy +
                ", repeatingStrategy=" + repeatingStrategy +
                ", additionalParameters=" + additionalParameters +
                '}';
    }
}
