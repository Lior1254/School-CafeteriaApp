package com.example.CafeteriaApp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Adapters.CookProductAdapter;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CookOrderDetailsActivity extends BaseActivity {

    private TextView tvOrderTitle, tvTimer, tvTargetTime, tvCustomerNotes;
    private RecyclerView rvOrderItems;
    private Spinner spinStatus;
    private Order order;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_order_details);

        order = (Order) getIntent().getSerializableExtra("order");
        if (order == null) {
            finish();
            return;
        }

        initializeViews();
        setupUI();
        startTimer();
    }

    private void initializeViews() {
        tvOrderTitle = findViewById(R.id.tvOrderTitle);
        tvTimer = findViewById(R.id.tvTimer);
        tvTargetTime = findViewById(R.id.tvTargetTime);
        tvCustomerNotes = findViewById(R.id.tvCustomerNotes);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        spinStatus = findViewById(R.id.spinStatus);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnUpdateOrder).setOnClickListener(v -> updateOrderStatus());
    }

    private void setupUI() {
        tvOrderTitle.setText("הזמנה #" + order.getOrderCode());
        tvCustomerNotes.setText(order.getGeneralNotes() != null && !order.getGeneralNotes().isEmpty() 
                ? order.getGeneralNotes() : "אין הערות מיוחדות");
        
        String targetTimeStr = order.getRequestedTime();
        if (targetTimeStr != null && targetTimeStr.contains(" ")) {
            tvTargetTime.setText("יעד: " + targetTimeStr.split(" ")[1].substring(0, 5));
        }

        // Setup the NEW Professional Cook Item List
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        CookProductAdapter adapter = new CookProductAdapter(this, order.getProducts());
        rvOrderItems.setAdapter(adapter);

        // Setup Status Spinner
        String[] statuses = {"ממתין", "בהכנה", "מוכן", "נאסף"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        spinStatus.setAdapter(statusAdapter);
        
        try {
            int currentStatus = Integer.parseInt(order.getOrderStatus());
            spinStatus.setSelection(currentStatus);
        } catch (Exception e) {
            spinStatus.setSelection(0);
        }
    }

    private void startTimer() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date targetDate = sdf.parse(order.getRequestedTime());
            long diff = targetDate.getTime() - System.currentTimeMillis();

            if (diff > 0) {
                countDownTimer = new CountDownTimer(diff, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long minutes = (millisUntilFinished / 1000) / 60;
                        long seconds = (millisUntilFinished / 1000) % 60;
                        tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

                        // Professional Color Logic
                        if (minutes < 2) {
                            tvTimer.setTextColor(Color.parseColor("#D32F2F")); // Deep Red
                        } else if (minutes < 5) {
                            tvTimer.setTextColor(Color.parseColor("#F57C00")); // Deep Orange
                        } else {
                            tvTimer.setTextColor(Color.parseColor("#388E3C")); // Deep Green
                        }
                    }

                    @Override
                    public void onFinish() {
                        tvTimer.setText("זמן עבר!");
                        tvTimer.setTextColor(Color.RED);
                    }
                }.start();
            } else {
                tvTimer.setText("באיחור");
                tvTimer.setTextColor(Color.RED);
            }
        } catch (Exception e) {
            tvTimer.setText("--:--");
        }
    }

    private void updateOrderStatus() {
        if (!checkNetworkAndShowDialog()) return;

        final String oldStatus = order.getOrderStatus();
        int selectedStatus = spinStatus.getSelectedItemPosition();
        final String newStatus = String.valueOf(selectedStatus);
        
        order.setOrderStatus(newStatus);

        // 1. Update in the UserOrders branch
        FBRef.getUserOrdersRef(order.getUserId(), false)
                .child(order.getOrderId())
                .setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 2. Update in the Orders branch (including moving node if status changed)
                        updateGlobalOrderNode(oldStatus, newStatus);
                    } else {
                        Toast.makeText(this, "עדכון נכשל בנתוני משתמש", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateGlobalOrderNode(String oldStatus, String newStatus) {
        // If status changed, we need to delete the old node and create a new one
        // because status is part of the path: Orders/{status}/{time}/{id}
        if (!oldStatus.equals(newStatus)) {
            FBRef.refOrders.child(oldStatus)
                    .child(order.getRequestedTime())
                    .child(order.getOrderId())
                    .removeValue()
                    .addOnCompleteListener(task -> {
                        saveNewGlobalNode();
                    });
        } else {
            saveNewGlobalNode();
        }
    }

    private void saveNewGlobalNode() {
        FBRef.refOrders.child(order.getOrderStatus())
                .child(order.getRequestedTime())
                .child(order.getOrderId())
                .setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "ההזמנה עודכנה בהצלחה!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "שגיאה בעדכון גלובלי", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy() ;
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
