package com.paqua.loancalculator.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Represents strategies for repeating early payments
 */
public enum EarlyPaymentRepeatingStrategy implements Serializable {

    /**
     * Single payment, without repeating
     */
    @SerializedName("single")
    SINGLE,

    /**
     * Repeats early payment in each payment in schedule to end of loan term
     */
    @SerializedName("to_end")
    TO_END,

    /**
     * Repeats early payment between this date
     */
    @SerializedName("to_certain_month")
    TO_CERTAIN_MONTH;
}
