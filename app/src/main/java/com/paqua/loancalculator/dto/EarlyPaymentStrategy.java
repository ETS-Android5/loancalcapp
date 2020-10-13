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

}
