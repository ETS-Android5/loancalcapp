package com.paqua.loancalculator.util;

import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Utility class for user-input validation
 *
 * @author Artyom Panfutov
 */
public class ValidationUtils {
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

}
