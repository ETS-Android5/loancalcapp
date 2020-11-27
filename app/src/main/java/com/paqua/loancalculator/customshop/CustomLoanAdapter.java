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
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.paqua.loancalculator.MainActivity;
import com.paqua.loancalculator.R;
import com.paqua.loancalculator.dto.Loan;
import com.paqua.loancalculator.dto.LoanAmortization;
import com.paqua.loancalculator.storage.LoanStorage;
import com.paqua.loancalculator.util.LoanCommonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is wrong - it is coupled with activity and loans
 */
public class CustomLoanAdapter extends ArrayAdapter<String> {
    private Map<Integer, Map.Entry<Loan, LoanAmortization>> loanBySavedIndex;
    private InterstitialAd interstitialAd;
    private MainActivity activity;

    public CustomLoanAdapter(MainActivity activity, InterstitialAd interstitialAd, Context context, List<String> items) {
        super(context, R.layout.spinner_item, items);
        this.interstitialAd = interstitialAd;
        this.activity = activity;
        refreshLoanByIndex();
    }

    private void refreshLoanByIndex() {
        Map<Loan, LoanAmortization> saved = LoanStorage.getAll(activity);

        loanBySavedIndex = new HashMap<>(); // Always new

        List<String> items = new ArrayList<>();
        items.add("Saved loans"); // TODO lang

        int i = 1;
        for (Map.Entry<Loan, LoanAmortization> item : saved.entrySet()) {
            Loan loan = item.getKey();
            items.add(loan.getName() != null && !loan.getName().isEmpty()
                    ? loan.getNameWithCount()
                    : LoanCommonUtils.getDefaultLoanName(activity)
            );

            loanBySavedIndex.put(i, item);

            i++;
        }
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

    private View delegateGetView(int position, ViewGroup parent) {
        View convertView;
        if (position != 0) {
            convertView = getView(position, parent);
            initDeleteButtonCallback(position, convertView);
        } else {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item, parent, false);

            TextView loanName = (TextView) convertView.findViewById(R.id.first_item);
            loanName.setText(getItem(position));
        }
        return convertView;
    }

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
                        LoanStorage.remove(activity, loanBySavedIndex.get(position).getKey());
                        remove(getItem(position));
                        refreshLoanByIndex();
                        if (loanBySavedIndex.isEmpty()) {
                            // TODO hide
                        }

                        notifyDataSetChanged();
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

    private View getView(final int position, ViewGroup parent) {
        View convertView;
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.saved_loans_spinner_adapter, parent, false);
        TextView loanName = (TextView) convertView.findViewById(R.id.loanNameSpinnerText);

        loanName.setText(getItem(position));

        loanName.setClickable(true);
        loanName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position > 0 && loanBySavedIndex.containsKey(position)) {
                    interstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            interstitialAd.loadAd(new AdRequest.Builder().build());
                            activity.showLoadedLoanAmortization(position);
                        }
                    });

                    if (interstitialAd.isLoaded()) {
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
}