package com.paqua.loancalculator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents strategies that can be applied to a loan when there is an overpayment
 *
 * @author Artyom Panfutov
 */
public enum EarlyPaymentStrategy {
    /**
     * Early payment decreases the term
     */
    @JsonProperty("decrease_term")
    DECREASE_TERM,

    /**
     * Early payment decreases the amount of monthly payments
     */
    @JsonProperty("decrease_monthly_payment")
    DECREASE_MONTHLY_PAYMENT;

    // TODO its for serialization - refactor
    @Override
    public String toString() {
        String retVal = "";
        switch (ordinal()) {
            case 0:
                return "decrease_term";
            case 1:
                return "decrease_monthly_payment";
        }
        throw new IllegalArgumentException("Unknown strategy");
    }
}
