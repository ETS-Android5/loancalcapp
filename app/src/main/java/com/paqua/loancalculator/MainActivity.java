package com.paqua.loancalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.paqua.loancalculator.dto.Loan;
import com.paqua.loancalculator.util.Constant;

import java.math.BigDecimal;

import static com.paqua.loancalculator.util.ValidationUtils.*;

public class MainActivity extends AppCompatActivity {
    private Button calculateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calculateButton = (Button) findViewById(R.id.calc);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateButtonOnClickCallback();
            }
        });

        setupRestoringBackgroundOnKey(findViewById(R.id.loanAmount));
        setupRestoringBackgroundOnKey(findViewById(R.id.interestRate));
        setupRestoringBackgroundOnKey(findViewById(R.id.term));
    }

    /**
     * Starts result activity
     */
    private void calculateButtonOnClickCallback() {
        boolean isValidInput = true;

        EditText loanAmount = (EditText) findViewById(R.id.loanAmount);
        EditText interestRate = ((EditText) findViewById(R.id.interestRate));
        EditText loanTerm = ((EditText) findViewById(R.id.term));

        isValidInput = hasEmptyText(loanAmount, getResources().getColor(R.color.coolRed));
        isValidInput = hasEmptyText(interestRate, getResources().getColor(R.color.coolRed));
        isValidInput = hasEmptyText(loanTerm, getResources().getColor(R.color.coolRed));

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

}