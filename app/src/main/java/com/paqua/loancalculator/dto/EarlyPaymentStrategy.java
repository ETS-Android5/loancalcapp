package com.paqua.loancalculator.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Represents strategies that can be applied to a loan when there is an overpayment
 *
 * @author Artyom Panfutov
 */
public enum EarlyPaymentStrategy implements Serializable {
    /**
     * Early payment decreases the term
     */
    @SerializedName("decrease_term")
    DECREASE_TERM,

    /**
     * Early payment decreases the amount of monthly payments
     */
    @SerializedName("decrease_monthly_payment")
    DECREASE_MONTHLY_PAYMENT;
}
