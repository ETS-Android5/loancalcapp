package com.paqua.loancalculator.customshop;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.paqua.loancalculator.MainActivity;
import com.paqua.loancalculator.R;
import com.paqua.loancalculator.storage.LoanStorage;

import java.lang.reflect.Method;
import java.util.List;

/**
 * This class is wrong - it is coupled with activity and loans
 * TODO
 */
public class CustomLoanAdapter extends ArrayAdapter<String> {
    private InterstitialAd interstitialAd;
    private MainActivity activity;

    public CustomLoanAdapter(MainActivity activity,  List<String> items) {
        super(activity.getApplicationContext(), R.layout.spinner_item, items);
        this.activity = activity;
        this.interstitialAd = activity.getInterstitialAd();
        refreshLoanByIndex();
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getDropDownView(final int position, View convertView, ViewGroup parent) {
        return delegateGetView(position, parent);
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        return delegateGetView(position, parent);
    }

    /**
     * Sets up view for loan
     * @param position position
     * @param parent parent
     *
     * @return view
     */
    private View delegateGetView(int position, ViewGroup parent) {
        View convertView;
        if (position != 0) {
            convertView = getSavedLoanItemView(position, parent);
            initDeleteButtonCallback(position, convertView);
        } else {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item, parent, false);

            TextView loanName = (TextView) convertView.findViewById(R.id.first_item);
            loanName.setBackground(activity.getResources().getDrawable(R.drawable.saved_loans_background));
            loanName.setText(getItem(position));
        }
        return convertView;
    }

    /**
     * Initialize delete button for a view
     *
     * @param position position
     * @param convertView view
     */
    private void initDeleteButtonCallback(final int position, View convertView) {
        final Button deleteButton = convertView.findViewById(R.id.delete_loan_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(activity);

                dialog.setMessage(activity.getResources().getString(R.string.delete_loan_dialog_text));
                dialog.setPositiveButton(activity.getResources().getString(R.string.yes_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LoanStorage.remove(activity, activity.getLoanBySavedIndex().get(position).getKey());
                        remove(getItem(position));
                        refreshLoanByIndex();
                        notifyDataSetChanged();

                        if (activity.getLoanBySavedIndex().isEmpty()) {
                            hideAndDetachSpinner();
                        }
                    }
                });

                dialog.setNegativeButton(activity.getResources().getString(R.string.no_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.create().show();
            }
        });
    }

    private void hideAndDetachSpinner() {
        Spinner savedLoansSpinner = (Spinner) activity.findViewById(R.id.savedLoans);
        savedLoansSpinner.setVisibility(View.INVISIBLE);
        try {
            Method method = Spinner.class.getDeclaredMethod("onDetachedFromWindow");
            method.setAccessible(true);
            method.invoke(savedLoansSpinner);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes view for spinner item
     *
     * @param position position
     * @param parent parent
     *
     * @return view
     */
    private View getSavedLoanItemView(final int position, ViewGroup parent) {
        View convertView;
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.saved_loans_spinner_item, parent, false);
        TextView loanName = (TextView) convertView.findViewById(R.id.loanNameSpinnerText);

        loanName.setText(getItem(position));

        loanName.setClickable(true);
        loanName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position > 0 && activity.getLoanBySavedIndex().containsKey(position)) {
                    interstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            interstitialAd.loadAd(new AdRequest.Builder().build());
                            activity.showLoadedLoanAmortization(position);
                        }
                    });

                    if (interstitialAd.isLoaded() && !activity.isAdDisabled()) {
                        interstitialAd.show();
                    } else {
                        // If ads did not load show amortization anyway
                        activity.showLoadedLoanAmortization(position);
                    }
                }
            }
        });
        return convertView;
    }

    private void refreshLoanByIndex() {
        activity.refreshSavedLoans((Spinner) activity.findViewById(R.id.savedLoans));
    }
}