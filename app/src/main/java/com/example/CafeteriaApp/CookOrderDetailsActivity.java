package com.example.CafeteriaApp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Adapters.CookProductAdapter;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Models.Order;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for the cook to view full order details and update status.
 * Includes a countdown timer synchronized with requested pickup time.
 */
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

        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        CookProductAdapter adapter = new CookProductAdapter(this, order.getProducts());
        rvOrderItems.setAdapter(adapter);

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
            if (targetDate == null) return;
            long diff = targetDate.getTime() - System.currentTimeMillis();

            if (diff > 0) {
                countDownTimer = new CountDownTimer(diff, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long minutes = (millisUntilFinished / 1000) / 60;
                        long seconds = (millisUntilFinished / 1000) % 60;
                        tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

                        if (minutes < 2) {
                            tvTimer.setTextColor(Color.parseColor("#D32F2F"));
                        } else if (minutes < 5) {
                            tvTimer.setTextColor(Color.parseColor("#F57C00"));
                        } else {
                            tvTimer.setTextColor(Color.parseColor("#388E3C"));
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

    /**
     * Orchestrates the order status update across multiple Firebase nodes.
     * Moves order to history if status is "Collected" (3).
     */
    private void updateOrderStatus() {
        if (!checkNetworkAndShowDialog()) return;

        final String oldStatus = order.getOrderStatus();
        int selectedPosition = spinStatus.getSelectedItemPosition();
        final String newStatus = String.valueOf(selectedPosition);
        
        order.setOrderStatus(newStatus);

        if (Order.STATUS_COLLECTED.equals(newStatus)) {
            moveOrderToHistory(oldStatus);
        } else {
            updateActiveOrder(oldStatus, newStatus);
        }
    }

    /**
     * Updates an existing active order without moving branches.
     */
    private void updateActiveOrder(String oldStatus, String newStatus) {
        FBRef.getUserOrdersRef(order.getUserId(), false)
                .child(order.getOrderId())
                .setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        syncGlobalOrderNode(oldStatus, newStatus);
                    }
                });
    }

    /**
     * Logic for status 3 (Collected): Removes from active nodes and adds to history.
     */
    private void moveOrderToHistory(String oldStatus) {
        FBRef.getUserOrdersRef(order.getUserId(), false)
                .child(order.getOrderId())
                .removeValue();

        FBRef.getUserOrdersRef(order.getUserId(), true)
                .child(order.getOrderId())
                .setValue(order);

        syncGlobalOrderNode(oldStatus, Order.STATUS_COLLECTED);
    }

    private void syncGlobalOrderNode(String oldStatus, String newStatus) {
        if (!oldStatus.equals(newStatus)) {
            FBRef.refOrders.child(oldStatus)
                    .child(order.getRequestedTime())
                    .child(order.getOrderId())
                    .removeValue()
                    .addOnCompleteListener(task -> saveToNewGlobalNode());
        } else {
            saveToNewGlobalNode();
        }
    }

    private void saveToNewGlobalNode() {
        FBRef.refOrders.child(order.getOrderStatus())
                .child(order.getRequestedTime())
                .child(order.getOrderId())
                .setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "ההזמנה עודכנה בהצלחה!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy() ;
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
