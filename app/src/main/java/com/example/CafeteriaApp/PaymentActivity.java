package com.example.CafeteriaApp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.CafeteriaApp.Helpers.BaseActivity;
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

/**
 * Activity for selecting a payment method and finalizing the order.
 * Handles order processing, Firebase synchronization, and cart management.
 */
public class PaymentActivity extends BaseActivity
{

    private TextView tvTotal, tvSubtotal, tvVatAmount;
    private MaterialCardView cardGPay, cardCredit, cardCounter;
    private RadioButton radioGPay, radioCredit, radioCounter;
    
    private double totalAmount = 0;
    private double subtotal = 0;
    private double vatAmount = 0;
    private String pickupTime = "";
    private String generalNotes = "";

    private static final double VAT_RATE = 1.18;
    private static final int RANDOM_CODE_BOUND = 9000;
    private static final int RANDOM_CODE_OFFSET = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Intent intent = getIntent();
        if (intent != null) {
            totalAmount = intent.getDoubleExtra("total_amount", 0);
            pickupTime = intent.getStringExtra("pickup_time");
            generalNotes = intent.getStringExtra("general_notes");
        }

        if (pickupTime != null && pickupTime.contains(" ")) {
            pickupTime = pickupTime.split(" ")[0].trim();
        }

        // Calculation: Total = Subtotal * 1.18 => Subtotal = Total / 1.18
        subtotal = totalAmount / VAT_RATE;
        vatAmount = totalAmount - subtotal;

        initializeViews();
        updateUI();
        updatePaymentMethodStyles(R.id.radio_credit);
    }

    /**
     * Links UI components to their respective XML IDs.
     */
    private void initializeViews() {
        tvTotal = findViewById(R.id.tv_total_payment);
        tvSubtotal = findViewById(R.id.tv_subtotal_payment);
        tvVatAmount = findViewById(R.id.tv_vat_amount);
        cardGPay = findViewById(R.id.card_gpay);
        cardCredit = findViewById(R.id.card_credit);
        cardCounter = findViewById(R.id.card_counter);
        radioGPay = findViewById(R.id.radio_gpay);
        radioCredit = findViewById(R.id.radio_credit);
        radioCounter = findViewById(R.id.radio_counter);
    }

    /**
     * Populates price displays with calculated values.
     */
    private void updateUI() {
        tvTotal.setText(String.format(Locale.getDefault(), "₪%.2f", totalAmount));
        tvSubtotal.setText(String.format(Locale.getDefault(), "₪%.2f", subtotal));
        if (tvVatAmount != null) {
            tvVatAmount.setText(String.format(Locale.getDefault(), "₪%.2f", vatAmount));
        }
    }

    /**
     * Finishes activity when back button is clicked.
     */
    public void onBackClick(View view) {
        finish();
    }

    /**
     * Handles payment method card clicks to toggle radio buttons and styles.
     */
    public void onPaymentMethodClick(View view) {
        int id = view.getId();
        int radioId = -1;
        if (id == R.id.card_gpay) radioId = R.id.radio_gpay;
        else if (id == R.id.card_credit) radioId = R.id.radio_credit;
        else if (id == R.id.card_counter) radioId = R.id.radio_counter;
        
        if (radioId != -1) {
            updatePaymentMethodStyles(radioId);
        }
    }

    /**
     * Validates connection and starts the order submission process.
     */
    public void onConfirmOrderClick(View view) {
        if (!checkNetworkAndShowDialog()) return;

        String method;
        if (radioGPay.isChecked()) method = getString(R.string.payment_google_pay);
        else if (radioCredit.isChecked()) method = getString(R.string.payment_credit_card);
        else method = getString(R.string.payment_counter);
        
        submitOrder(method);
    }

    /**
     * Creates an Order object and uploads it to Firebase.
     *
     * @param paymentMethod The selected payment method name.
     */
    private void submitOrder(String paymentMethod) {
        executeFirebaseOperation(() -> {
            String orderId = FBRef.refOrders.push().getKey();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Date now = new Date();
            
            String receivedTime = dateFormat.format(now) + " " + timeFormat.format(now);
            String requestedTime = dateFormat.format(now) + " " + pickupTime + ":00";

            String shortCode = String.valueOf(new Random().nextInt(RANDOM_CODE_BOUND) + RANDOM_CODE_OFFSET);
            List<Product> cartItems = FileManager.loadCart(this);
            String userId = FBRef.refAuth.getUid();
            User user = (User) getIntent().getSerializableExtra("user_data");

            Order order = new Order(
                    orderId, userId, shortCode, Order.STATUS_PENDING,
                    receivedTime, requestedTime, cartItems, user,
                    paymentMethod, !paymentMethod.equals(getString(R.string.payment_counter)),
                    totalAmount
            );
            
            order.setSummary(generateOrderSummary(cartItems));
            order.setGeneralNotes(generalNotes);

            FBRef.uploadOrder(order, new FBRef.FBListener() {
                @Override
                public void onSuccess() {
                    FileManager.saveCart(PaymentActivity.this, new ArrayList<>());
                    FileManager.saveGeneralNotes(PaymentActivity.this, "");
                    handleOrderSuccess();
                }

                @Override
                public void onSuccess(Object data) {}

                @Override
                public void onFailure(String error) {
                    Toast.makeText(PaymentActivity.this, getString(R.string.payment_error_format, error), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    /**
     * Generates a comma-separated string of product names in the order.
     */
    @NonNull
    private String generateOrderSummary(List<Product> cartItems) {
        if (cartItems == null) return "";
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < cartItems.size(); i++) {
            summary.append(cartItems.get(i).getName());
            if (i < cartItems.size() - 1) summary.append(", ");
        }
        return summary.toString();
    }

    /**
     * Notifies user of success and returns to main screen.
     */
    private void handleOrderSuccess() {
        Toast.makeText(this, getString(R.string.payment_order_confirmed_format, pickupTime), Toast.LENGTH_LONG).show();
        setResult(Activity.RESULT_OK);
        finish();
    }

    /**
     * Updates radio button states and card stroke styles.
     */
    private void updatePaymentMethodStyles(int selectedRadioId) {
        radioGPay.setChecked(selectedRadioId == R.id.radio_gpay);
        radioCredit.setChecked(selectedRadioId == R.id.radio_credit);
        radioCounter.setChecked(selectedRadioId == R.id.radio_counter);
        
        applyCardStyle(cardGPay, selectedRadioId == R.id.radio_gpay);
        applyCardStyle(cardCredit, selectedRadioId == R.id.radio_credit);
        applyCardStyle(cardCounter, selectedRadioId == R.id.radio_counter);
    }

    private void applyCardStyle(MaterialCardView card, boolean isSelected) {
        card.setStrokeColor(isSelected ? Color.parseColor("#FF6B35") : Color.parseColor("#E0E0E0"));
        card.setStrokeWidth(isSelected ? 4 : 1);
    }
}
