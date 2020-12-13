package com.paqua.loancalculator.util;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

public class ErrorDialogUtils {
    /**
     * Show common dialog in case of errors
     */
    public static void showSomethingWentWrongDialog(Context context) {
        getSomethingWentWrongDialog(context, null, null).show();
    }

    /**
     * Show common dialog in case of errors
     */
    public static void showSomethingWentWrongDialog(Context context, String title, String message) {
        getSomethingWentWrongDialog(context, title, message).show();
    }

    /**
     *
     * @return Common alert dialog in case of errors
     */
    public static AlertDialog getSomethingWentWrongDialog(Context context, String title, String message) {
        return new AlertDialog.Builder(context)
                .setTitle(title == null ? "Something went wrong :(" : title)
                .setMessage(message == null ? "Please, try again later" : message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO audit or something
                    }
                })
                .create();
    }

}
