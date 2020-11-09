package com.paqua.loancalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.paqua.loancalculator.dto.Loan;
import com.paqua.loancalculator.util.Constant;

import java.math.BigDecimal;

import static com.paqua.loancalculator.util.ValidationUtils.*;

public class MainActivity extends AppCompatActivity {
    private Button calculateButton;
    private InterstitialAd interstitialAd;

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

        if (isValidInput) isValidInput = hasEmptyText(loanAmount, getResources().getColor(R.color.coolRed));
        if (isValidInput) isValidInput = hasEmptyText(interestRate, getResources().getColor(R.color.coolRed));
        if (isValidInput) isValidInput = hasEmptyText(loanTerm, getResources().getColor(R.color.coolRed));
        if (isValidInput) isValidInput = hasValidTerm(this, loanTerm, monthTermType.isChecked(), getResources().getColor(R.color.coolRed));

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