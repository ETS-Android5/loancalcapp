package com.paqua.loancalculator.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.paqua.loancalculator.dto.Loan;
import com.paqua.loancalculator.dto.LoanAmortization;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages storage for loans
 */
public class LoanStorage {
    private static final String LOAN_STORAGE_KEY = "LOAN_STORAGE_KEY";
    private static final String LOAN_HASH_MAP = "LOAN_HASH_MAP";

    private static final Gson GSON = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .create();

    public static final Type LOAN_HASH_MAP_TYPE = new TypeToken<HashMap<Loan, LoanAmortization>>() {}.getType();

    /**
     * Loads all loans from the shared preferences
     *
     * @param context application context
     * @return map of previously saved loans
     */
    public static Map<Loan, LoanAmortization> getAll(Context context) {
        String loaded = getSharedPreferences(context).getString(LOAN_HASH_MAP, "");

        return GSON.fromJson(loaded, LOAN_HASH_MAP_TYPE);
    }

    /**
     * Saves a loan to the shared preferences
     *
     * @param context application context
     * @param loan loan object
     * @param loanAmortization amortization object
     */
    public static void put(Context context, Loan loan, LoanAmortization loanAmortization) {
        Map<Loan, LoanAmortization> map = getAll(context);

        if (map == null) {
            map = new HashMap<>();
        }
        map.put(loan, loanAmortization);

        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        String json = GSON.toJson(map, LOAN_HASH_MAP_TYPE);
        editor.putString(LOAN_HASH_MAP, json);

        editor.apply();
    }

    /**
     * Removes loan from shared preferences
     *
     * @param context application context
     * @param loan loan object
     */
    public static void remove(Context context, Loan loan) {
        Map<Loan, LoanAmortization> all = getAll(context);

        all.remove(loan);

        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        String json = GSON.toJson(all, LOAN_HASH_MAP_TYPE);
        editor.putString(LOAN_HASH_MAP, json);

        editor.apply();
    }

    /**
     * Deletes all saved loans in the shared preferences
     *
     * @param context application context
     */
    public static void clearAll(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        preferences.edit()
                .clear()
                .apply();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(LOAN_STORAGE_KEY, Context.MODE_PRIVATE);
    }

}
