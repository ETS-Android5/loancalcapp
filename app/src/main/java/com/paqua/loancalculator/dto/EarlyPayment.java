package com.paqua.loancalculator.dto;

import java.io.Serializable;
import java.math.BigDecimal;

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

    public EarlyPayment(BigDecimal amount, EarlyPaymentStrategy type, EarlyPaymentRepeatingStrategy repeatingStrategy) {
        this.amount = amount;
        this.strategy = type;
        this.repeatingStrategy = repeatingStrategy;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EarlyPayment that = (EarlyPayment) o;

        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        if (strategy != that.strategy) return false;
        return repeatingStrategy == that.repeatingStrategy;
    }

    @Override
    public int hashCode() {
        int result = amount != null ? amount.hashCode() : 0;
        result = 31 * result + (strategy != null ? strategy.hashCode() : 0);
        result = 31 * result + (repeatingStrategy != null ? repeatingStrategy.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EarlyPayment{" +
                "amount=" + amount +
                ", strategy=" + strategy +
                ", repeatingStrategy=" + repeatingStrategy +
                '}';
    }
}
