package com.example.CafeteriaApp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;

public class PaymentActivity extends BaseActivity {

    private TextView tvTotal, tvSubtotal;
    private MaterialCardView cardGPay, cardCredit, cardCounter;
    private RadioButton radioGPay, radioCredit, radioCounter;

    private double totalAmount = 0;
    private double subtotal = 0;
    private final double serviceFee = 3.50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Get total from intent
        totalAmount = getIntent().getDoubleExtra("total_amount", 0);
        subtotal = totalAmount - serviceFee;
        if (subtotal < 0) subtotal = 0;

        initializeViews();
        updateUI();
        
        // Initial style sync
        updateCardStyles(R.id.radio_credit); 
    }

    private void initializeViews() {
        tvTotal = findViewById(R.id.tv_total_payment);
        tvSubtotal = findViewById(R.id.tv_subtotal_payment);
        
        cardGPay = findViewById(R.id.card_gpay);
        cardCredit = findViewById(R.id.card_credit);
        cardCounter = findViewById(R.id.card_counter);
        
        radioGPay = findViewById(R.id.radio_gpay);
        radioCredit = findViewById(R.id.radio_credit);
        radioCounter = findViewById(R.id.radio_counter);
    }

    private void updateUI() {
        tvTotal.setText(String.format("₪%.2f", totalAmount));
        tvSubtotal.setText(String.format("₪%.2f", subtotal));
    }

    /**
     * XML OnClick for back button
     */
    public void onBackClick(View view) {
        finish();
    }

    /**
     * XML OnClick for all payment method cards
     */
    public void onPaymentMethodClick(View view) {
        int id = view.getId();
        int radioIdToSelect = -1;

        if (id == R.id.card_gpay) radioIdToSelect = R.id.radio_gpay;
        else if (id == R.id.card_credit) radioIdToSelect = R.id.radio_credit;
        else if (id == R.id.card_counter) radioIdToSelect = R.id.radio_counter;

        if (radioIdToSelect != -1) {
            updateCardStyles(radioIdToSelect);
        }
    }

    /**
     * XML OnClick for confirm order button
     */
    public void onConfirmOrderClick(View view) {
        String method = "";
        if (radioGPay.isChecked()) method = getString(R.string.payment_google_pay);
        else if (radioCredit.isChecked()) method = getString(R.string.payment_credit_card);
        else if (radioCounter.isChecked()) method = getString(R.string.payment_counter);

        Toast.makeText(this, "ההזמנה אושרה באמצעות: " + method, Toast.LENGTH_LONG).show();
        // Here you would send the order to Firebase
    }

    private void updateCardStyles(int selectedRadioId) {
        // Uncheck all radios
        radioGPay.setChecked(selectedRadioId == R.id.radio_gpay);
        radioCredit.setChecked(selectedRadioId == R.id.radio_credit);
        radioCounter.setChecked(selectedRadioId == R.id.radio_counter);

        // Reset all card styles
        resetStyle(cardGPay);
        resetStyle(cardCredit);
        resetStyle(cardCounter);

        // Highlight selected
        if (selectedRadioId == R.id.radio_gpay) highlight(cardGPay);
        else if (selectedRadioId == R.id.radio_credit) highlight(cardCredit);
        else if (selectedRadioId == R.id.radio_counter) highlight(cardCounter);
    }

    private void resetStyle(MaterialCardView card) {
        card.setStrokeColor(Color.parseColor("#E0E0E0"));
        card.setStrokeWidth(1);
    }

    private void highlight(MaterialCardView card) {
        card.setStrokeColor(Color.parseColor("#FF6B35"));
        card.setStrokeWidth(4);
    }
}
