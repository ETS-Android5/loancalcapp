package com.paqua.loancalculator.util;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.paqua.loancalculator.R;

/**
 * Utility class for user-input validation
 *
 * @author Artyom Panfutov
 */
public class ValidationUtils {
    private static final int MAX_VALID_TERM_MONTHS = 600;
    /**
     * Checks edit text for empty content
     *
     * If it is empty - sets background color
     * @param view
     * @param color
     * @return validation result
     */
    public static boolean hasEmptyText(EditText view, int color) {
        if (view.getText().toString().length() == 0) {
            view.setBackgroundColor(color);
            view.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Validates loan term
     *
     * @return validation result
     */
    public static boolean hasValidTerm(Activity activity, EditText termView, boolean inMonths, int color) {
        int term = Integer.parseInt(termView.getText().toString());
        boolean isValid;

        if (inMonths) {
            isValid = term <= MAX_VALID_TERM_MONTHS;

            if (!isValid) {
                termView.setError(activity.getResources().getString(R.string.max_term_in_months_message) + " " + MAX_VALID_TERM_MONTHS);
                termView.setBackgroundColor(color);
                termView.requestFocus();
            }
        } else {
            isValid = (term * 12) <= MAX_VALID_TERM_MONTHS;
            if (!isValid) {
                termView.setError(activity.getResources().getString(R.string.max_term_in_months_message) + " " + MAX_VALID_TERM_MONTHS / 12);
                termView.setBackgroundColor(color);
                termView.requestFocus();
            }
        }

        return isValid;
    }

    /**
     * Validates spinner
     *
     * @param spinner
     * @param color
     * @return validation result
     */
    public static boolean hasValidSpinnerItem(Spinner spinner, int color) {
        if (spinner.getSelectedItemId() == AdapterView.INVALID_ROW_ID || spinner.getSelectedItemId() == 0) {
            spinner.setBackgroundColor(color);
            spinner.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Sets listener on text change for restoring original background
     * @param view edit text
     */
    public static void setupRestoringBackgroundOnTextChange(final EditText view) {
        final Drawable originBackground = view.getBackground();
            view.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!view.getBackground().equals(originBackground)) {
                        view.setBackground(originBackground);
                    }

                }
            });
    }
    /**
     * Sets listener on item click for restoring original background
     * @param spinner spinner
     */
    public static void setupRestoringBackgroundOnTextChange(final Spinner spinner) {
        final Drawable originBackground = spinner.getBackground();
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinner.getBackground().equals(originBackground)) {
                    spinner.setBackground(originBackground);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (!spinner.getBackground().equals(originBackground)) {
                    spinner.setBackground(originBackground);
                }
            }
        });
    }

}
