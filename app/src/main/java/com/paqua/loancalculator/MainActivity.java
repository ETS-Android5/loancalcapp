package com.paqua.loancalculator;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.paqua.loancalculator.dto.Loan;
import com.paqua.loancalculator.dto.LoanAmortization;
import com.paqua.loancalculator.storage.LoanStorage;
import com.paqua.loancalculator.util.Constant;
import com.paqua.loancalculator.util.LoanCommon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.paqua.loancalculator.util.ValidationUtils.hasValidTerm;
import static com.paqua.loancalculator.util.ValidationUtils.setupRestoringBackgroundOnTextChange;
import static com.paqua.loancalculator.util.ValidationUtils.validateForEmptyText;
import static com.paqua.loancalculator.util.ValidationUtils.validateForZero;
import static com.paqua.loancalculator.util.ValidationUtils.validateInterestRate;

public class MainActivity extends AppCompatActivity {
    private Button calculateButton;
    private InterstitialAd interstitialAd;
    private Map<Integer, Map.Entry<Loan, LoanAmortization>> loanBySavedIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMobileAds();

        calculateButton = (Button) findViewById(R.id.calc);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateButtonOnClickCallback();
            }

        });

        setupRestoringBackgroundOnTextChange((EditText) findViewById(R.id.loanAmount));
        setupRestoringBackgroundOnTextChange((EditText) findViewById(R.id.interestRate));
        setupRestoringBackgroundOnTextChange((EditText) findViewById(R.id.term));

        setMonthTermTypeOnChangeListener();

        initSavedLoansView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initSavedLoansView();
    }

    /**
     * Fills spinner with saved loans and sets its callbacks
     */
    private void initSavedLoansView() {
        Spinner savedLoans = (Spinner)findViewById(R.id.savedLoans);

        loanBySavedIndex = new HashMap<>(); // Always new

        List<String> items = new ArrayList<>();
        items.add("Saved loans"); // TODO lang

        Map<Loan, LoanAmortization> saved = LoanStorage.getAll(this);

        if (saved != null && !saved.isEmpty()) {
           int i = 1;
           for (Map.Entry<Loan, LoanAmortization> item : saved.entrySet()) {
               Loan loan = item.getKey();
               items.add(loan.getName() != null && !loan.getName().isEmpty()
                       ? loan.getName()
                       : LoanCommon.getDefaultLoanName(getApplicationContext(), loan)
               );

               loanBySavedIndex.put(i, item);

               i++;
           }
        } else {
            savedLoans.setVisibility(View.INVISIBLE);
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
        savedLoans.setAdapter(spinnerArrayAdapter);

        savedLoans.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                if (position > 0 && loanBySavedIndex.containsKey(position)) {
                    interstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            interstitialAd.loadAd(new AdRequest.Builder().build());
                            showLoadedLoanAmortization(position);
                        }
                    });

                    if (interstitialAd.isLoaded()) {
                        interstitialAd.show();
                    } else {
                        // If ads did not load show amortization anyway
                        showLoadedLoanAmortization(position);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Shows loaded amortization from preferences
     * @param position position from spinner
     */
    private void showLoadedLoanAmortization(int position) {
        Intent intent = new Intent(MainActivity.this, ResultActivity.class);

        Loan loan = loanBySavedIndex.get(position).getKey();
        LoanAmortization amortization = loanBySavedIndex.get(position).getValue();

        intent.putExtra(Constant.LOAN_OBJECT, loan);
        intent.putExtra(Constant.LOAN_AMORTIZATION_OBJECT, amortization);
        intent.putExtra(Constant.USE_SAVED_DATA, Boolean.TRUE);

        startActivity(intent);
    }

    /**
     * Shows ads and calculates results
     */
    private void calculateButtonOnClickCallback() {
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                interstitialAd.loadAd(new AdRequest.Builder().build());
                calculateAndShow();
            }
        });

        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            // If ads did not load show amortization anyway
            calculateAndShow();
        }
    }

    /**
     * Starts result activity
     */
    private void calculateAndShow() {
        boolean isValidInput = true;

        EditText loanAmount = (EditText) findViewById(R.id.loanAmount);
        EditText interestRate = ((EditText) findViewById(R.id.interestRate));
        EditText loanTerm = ((EditText) findViewById(R.id.term));
        RadioButton monthTermType = (RadioButton) findViewById(R.id.monthTermType);

        int errorColor = getResources().getColor(R.color.coolRed);
        if (isValidInput) isValidInput = validateForEmptyText(loanAmount, errorColor);
        if (isValidInput) isValidInput = validateForZero(loanAmount, errorColor);
        if (isValidInput) isValidInput = validateForEmptyText(loanTerm, errorColor);
        if (isValidInput) isValidInput = validateForZero(loanTerm, errorColor);
        if (isValidInput) isValidInput = hasValidTerm(this, loanTerm, monthTermType.isChecked(), errorColor);
        if (isValidInput) isValidInput = validateInterestRate(interestRate, errorColor);
        if (isValidInput) isValidInput = validateForZero(interestRate, errorColor);

        if (isValidInput) {
            BigDecimal amount = new BigDecimal(loanAmount.getText().toString());
            BigDecimal rate = new BigDecimal(interestRate.getText().toString());
            int term = Integer.parseInt(loanTerm.getText().toString());

            RadioButton yearTerm = (RadioButton) findViewById(R.id.yearTermType);
            if (yearTerm.isChecked()) {
                term *= 12;
            }

            Intent intent = new Intent(MainActivity.this, ResultActivity.class);
            Loan loan = Loan.builder()
                    .amount(amount)
                    .rate(rate)
                    .term(term)
                    .build();

            intent.putExtra(Constant.LOAN_OBJECT, loan);
            intent.putExtra(Constant.USE_SAVED_DATA, Boolean.FALSE);

            startActivity(intent);
        }
    }

    /**
     * Sets max length for term on term type change
     */
    private void setMonthTermTypeOnChangeListener() {
        final RadioButton months = (RadioButton) findViewById(R.id.monthTermType);
        final EditText term = (EditText) findViewById(R.id.term);

        months.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int maxLength;
                if (isChecked) {
                    maxLength = 3;
                } else {
                    maxLength = 2;
                }
                term.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
            }
        });
    }


    private void initMobileAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-3031881558720361/4919481152");
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }
}