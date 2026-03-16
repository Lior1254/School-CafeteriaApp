package com.example.CafeteriaApp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Helpers.FileManager;
import com.example.CafeteriaApp.Models.Order;
import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.Models.User;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class PaymentActivity extends BaseActivity {

    private TextView tvTotal, tvSubtotal;
    private MaterialCardView cardGPay, cardCredit, cardCounter;
    private RadioButton radioGPay, radioCredit, radioCounter;
    
    private double totalAmount = 0;
    private double subtotal = 0;
    private final double serviceFee = 3.50;
    private String pickupTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Get data from intent
        totalAmount = getIntent().getDoubleExtra("total_amount", 0);
        pickupTime = getIntent().getStringExtra("pickup_time");

        subtotal = totalAmount - serviceFee;
        if (subtotal < 0) subtotal = 0;

        initializeViews();
        updateUI();
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

    public void onBackClick(View view) { finish(); }

    public void onPaymentMethodClick(View view) {
        int id = view.getId();
        int radioId = -1;
        if (id == R.id.card_gpay) radioId = R.id.radio_gpay;
        else if (id == R.id.card_credit) radioId = R.id.radio_credit;
        else if (id == R.id.card_counter) radioId = R.id.radio_counter;
        if (radioId != -1) updateCardStyles(radioId);
    }

    public void onConfirmOrderClick(View view) {
        String method = "";
        if (radioGPay.isChecked()) method = getString(R.string.payment_google_pay);
        else if (radioCredit.isChecked()) method = getString(R.string.payment_credit_card);
        else if (radioCounter.isChecked()) method = getString(R.string.payment_counter);
        processOrder(method);
    }

    private void processOrder(String paymentMethod) {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("שולח הזמנה...");
        pd.show();

        // 1. Generate Identifiers
        String orderId = FBRef.refOrders.push().getKey();
        String timeKey = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String shortCode = String.valueOf(new Random().nextInt(9000) + 1000);

        List<Product> cartItems = FileManager.loadCart(this);
        String userId = FBRef.refAuth.getUid();
        User user = (User) getIntent().getSerializableExtra("user_data");

        // 2. Create Order
        // Here we set pickupTime as the orderCode, so FBRef uses it as the second-level node
        Order order = new Order(
                orderId,
                userId,
                shortCode,
                "0",
                timeKey, // orderReceivedTime
                cartItems,
                user,
                paymentMethod,
                !paymentMethod.equals(getString(R.string.payment_counter)),
                totalAmount
        );
        order.setRequestedTime(pickupTime);
        order.setSummary(shortCode); // Store the random numeric code in the summary field

        // 3. Upload via FBRef
        FBRef.uploadOrder(order, new FBRef.FBListener() {
            @Override
            public void onSuccess() {
                pd.dismiss();
                FileManager.saveCart(PaymentActivity.this, new ArrayList<>());
                finalizePayment(paymentMethod);
            }
            @Override
            public void onFailure(String error) {
                pd.dismiss();
                Toast.makeText(PaymentActivity.this, "שגיאה: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void finalizePayment(String method) {
        Toast.makeText(this, "ההזמנה אושרה! שעת איסוף: " + pickupTime, Toast.LENGTH_LONG).show();
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void updateCardStyles(int selectedRadioId) {
        radioGPay.setChecked(selectedRadioId == R.id.radio_gpay);
        radioCredit.setChecked(selectedRadioId == R.id.radio_credit);
        radioCounter.setChecked(selectedRadioId == R.id.radio_counter);
        resetStyle(cardGPay); resetStyle(cardCredit); resetStyle(cardCounter);
        if (selectedRadioId == R.id.radio_gpay) highlight(cardGPay);
        else if (selectedRadioId == R.id.radio_credit) highlight(cardCredit);
        else if (selectedRadioId == R.id.radio_counter) highlight(cardCounter);
    }
    private void resetStyle(MaterialCardView card) { card.setStrokeColor(Color.parseColor("#E0E0E0")); card.setStrokeWidth(1); }
    private void highlight(MaterialCardView card) { card.setStrokeColor(Color.parseColor("#FF6B35")); card.setStrokeWidth(4); }
}
