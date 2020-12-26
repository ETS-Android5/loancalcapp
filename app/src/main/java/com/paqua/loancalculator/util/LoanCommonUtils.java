package com.paqua.loancalculator.util;

import android.content.Context;
import android.os.Build;
import android.os.LocaleList;
import android.widget.DatePicker;

import com.paqua.loancalculator.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Garbage class
 */
@Deprecated
public class LoanCommonUtils {

    /*
     * !!! DO NOT READ FURTHER !!!
     */

    /**
     * Default loan name for saving into local storage
     *
     * @return loan name
     */
    @Deprecated
    public static String getDefaultLoanName(Context context) {
        return context.getResources().getString(R.string.my_loan_text);
    }

    /**
     * Constructs term string
     *
     * @param years years
     * @param months months
     *
     * @return text
     */
    @Deprecated
    public static String getTermString(long years, long months, Context context) {
        String retVal;
        if (years > 0 && months > 0) {
            retVal = getCurrentLanguage().equals("ru") ?
                    "for" + getRussianYearString(years) + " " + getRussianMonthString(months) :
                    "for " + years + " years " + months + " months";
        } else if (years <= 0 && months > 0){
            retVal = "for " + months + " months";
        } else if (years > 0) {
            retVal = "for " + years + " years";
        } else {
            retVal = "";
        }

        return retVal;
    }

    @Deprecated
    private static String getRussianYearString(long year) {
        String retVal = "";
        
        boolean exclusion = (year % 100 >= 11) && (year % 100 <= 14);
        if (exclusion)
            return retVal + " " + "лет";

        long yearLastNumber = year % 10;

        if (yearLastNumber == 1) {
            retVal = "год";
        } else if(yearLastNumber == 0 || yearLastNumber >= 5) {
            retVal = "лет";
        } else if(yearLastNumber >= 2) {
            retVal = "года";
        }

        return year + " " + retVal;
    }

    @Deprecated
    private static String getRussianMonthString(long months) {
        String retVal = "";

        boolean exclusion = (months % 100 >= 11) && (months % 100 <= 14);
        if (exclusion) {
            return months + " " + "месяцев";
        }
        long monthLastNumber = months % 10;

        if (monthLastNumber == 1) {
            retVal = "месяц";
        } else if (monthLastNumber >= 2 && monthLastNumber <= 4) {
            retVal = "месяца";
        } else {
            retVal = "месяцев";
        }

        return months + " " + retVal;
    }

    @Deprecated
    private static String getYearString(long years) {
        if (years == 1) {
            return years + " " + "year";
        } else {
            return years + " " + "years";
        }
    }

    @Deprecated
    private static String getMonthString(long months) {
        if (months == 1) {
            return months + " " + "month";
        } else {
            return months + " " + "months";
        }
    }

    @Deprecated
    private static String getCurrentLanguage(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return LocaleList.getDefault().get(0).getLanguage();
        } else{
            return Locale.getDefault().getLanguage();
        }
    }

}
