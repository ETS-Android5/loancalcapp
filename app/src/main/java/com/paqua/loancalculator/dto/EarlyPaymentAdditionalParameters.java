package com.paqua.loancalculator.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public enum EarlyPaymentAdditionalParameters implements Serializable {
    @SerializedName("repeat_to_month_number")
    REPEAT_TO_MONTH_NUMBER;
}
