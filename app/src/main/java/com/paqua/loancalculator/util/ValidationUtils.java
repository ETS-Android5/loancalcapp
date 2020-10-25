package com.paqua.loancalculator.util;

import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;
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
     * Sets listener on key for restoring original background
     * @param view edit text
     */
    public static void setupRestoringBackgroundOnKey(View view) {
        final Drawable originBackground = view.getBackground();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (v.getBackground() != originBackground) {
                    v.setBackground(originBackground);
                }
                return false;
            }
        });
    }

}
