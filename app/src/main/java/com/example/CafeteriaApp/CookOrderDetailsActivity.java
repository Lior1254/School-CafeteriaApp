package com.example.CafeteriaApp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Adapters.CookProductAdapter;
import com.example.CafeteriaApp.Helpers.BaseActivity;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for the cook to view full order details and update its status.
 * Features a real-time countdown timer synchronized with the requested pickup time.
 */
public class CookOrderDetailsActivity extends BaseActivity
{

    private TextView tvOrderTitle, tvCountdownTimer, tvTargetTime, tvCustomerNotes;
    private RecyclerView rvOrderItems;
    private Spinner spinOrderStatus;
    private Order order;
    private CountDownTimer countDownTimer;

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final long TIMER_INTERVAL = 1000;

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
        startPickupTimer();
    }

    /**
     * Links UI components to their respective XML IDs and sets click listeners.
     */
    private void initializeViews() {
        tvOrderTitle = findViewById(R.id.tvOrderTitle);
        tvCountdownTimer = findViewById(R.id.tvTimer);
        tvTargetTime = findViewById(R.id.tvTargetTime);
        tvCustomerNotes = findViewById(R.id.tvCustomerNotes);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        spinOrderStatus = findViewById(R.id.spinStatus);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnUpdateOrder).setOnClickListener(v -> handleStatusUpdate());
    }

    /**
     * Populates the UI with order data and configures the status spinner.
     */
    private void setupUI() {
        tvOrderTitle.setText(getString(R.string.order_number_format, order.getOrderCode()));
        
        String notes = order.getGeneralNotes();
        tvCustomerNotes.setText(notes != null && !notes.isEmpty() ? notes : getString(R.string.cook_order_details_no_notes));
        
        String requestedTime = order.getRequestedTime();
        if (requestedTime != null && requestedTime.contains(" ")) {
            String timePart = requestedTime.split(" ")[1];
            tvTargetTime.setText(getString(R.string.cook_order_details_target_time_format, 
                    timePart.length() > 5 ? timePart.substring(0, 5) : timePart));
        }

        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        CookProductAdapter adapter = new CookProductAdapter(this, order.getProducts());
        rvOrderItems.setAdapter(adapter);

        String[] statuses = {
                getString(R.string.order_status_pending),
                getString(R.string.order_status_preparing),
                getString(R.string.order_status_ready),
                getString(R.string.order_status_collected)
        };
        
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        spinOrderStatus.setAdapter(statusAdapter);
        
        try {
            int currentStatus = Integer.parseInt(order.getOrderStatus());
            spinOrderStatus.setSelection(currentStatus);
        } catch (NumberFormatException e) {
            spinOrderStatus.setSelection(0);
        }
    }

    /**
     * Initializes and starts a countdown timer until the requested pickup time.
     * Updates text color based on remaining time to alert the cook.
     */
    private void startPickupTimer() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            Date targetDate = sdf.parse(order.getRequestedTime());
            if (targetDate == null) return;
            
            long timeRemaining = targetDate.getTime() - System.currentTimeMillis();

            if (timeRemaining > 0) {
                countDownTimer = new CountDownTimer(timeRemaining, TIMER_INTERVAL) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long totalSeconds = millisUntilFinished / 1000;
                        long minutes = totalSeconds / 60;
                        long seconds = totalSeconds % 60;
                        
                        tvCountdownTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

                        // Dynamic color feedback based on urgency
                        if (minutes < 2) {
                            tvCountdownTimer.setTextColor(Color.parseColor("#D32F2F")); // Red
                        } else if (minutes < 5) {
                            tvCountdownTimer.setTextColor(Color.parseColor("#F57C00")); // Orange
                        } else {
                            tvCountdownTimer.setTextColor(Color.parseColor("#388E3C")); // Green
                        }
                    }

                    @Override
                    public void onFinish() {
                        tvCountdownTimer.setText(getString(R.string.cook_order_details_timer_finished));
                        tvCountdownTimer.setTextColor(Color.RED);
                    }
                }.start();
            } else {
                tvCountdownTimer.setText(getString(R.string.cook_order_details_timer_late));
                tvCountdownTimer.setTextColor(Color.RED);
            }
        } catch (Exception e) {
            tvCountdownTimer.setText("--:--");
        }
    }

    /**
     * Validates connection and updates the order status in Firebase.
     * If the status is "Collected", the order is moved to the history node.
     */
    private void handleStatusUpdate() {
        if (!checkNetworkAndShowDialog()) return;

        final String previousStatus = order.getOrderStatus();
        int selectedStatusIndex = spinOrderStatus.getSelectedItemPosition();
        final String newStatusString = String.valueOf(selectedStatusIndex);
        
        order.setOrderStatus(newStatusString);

        if (Order.STATUS_COLLECTED.equals(newStatusString)) {
            archiveOrder(previousStatus);
        } else {
            updateActiveOrderNode(previousStatus, newStatusString);
        }
    }

    /**
     * Updates an active order status and synchronizes it across global nodes.
     *
     * @param previousStatus The previous status code.
     * @param newStatus      The new status code.
     */
    private void updateActiveOrderNode(String previousStatus, String newStatus) {
        FBRef.getUserOrdersRef(order.getUserId(), false)
                .child(order.getOrderId())
                .setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        syncGlobalOrderNode(previousStatus, newStatus);
                    }
                });
    }

    /**
     * Moves a completed order to the user\'s history node and updates the global list.
     *
     * @param previousStatus The previous status code.
     */
    private void archiveOrder(String previousStatus) {
        FBRef.getUserOrdersRef(order.getUserId(), false)
                .child(order.getOrderId())
                .removeValue();

        FBRef.getUserOrdersRef(order.getUserId(), true)
                .child(order.getOrderId())
                .setValue(order);

        syncGlobalOrderNode(previousStatus, Order.STATUS_COLLECTED);
    }

    /**
     * Moves the order node from the old status branch to the new one in the global list.
     *
     * @param oldStatus The old status branch name.
     * @param newStatus The new status branch name.
     */
    private void syncGlobalOrderNode(String oldStatus, String newStatus) {
        if (!oldStatus.equals(newStatus)) {
            FBRef.refOrders.child(oldStatus)
                    .child(order.getRequestedTime())
                    .child(order.getOrderId())
                    .removeValue()
                    .addOnCompleteListener(task -> persistToNewGlobalNode());
        } else {
            persistToNewGlobalNode();
        }
    }

    /**
     * Writes the order to its new global node and provides user feedback.
     */
    private void persistToNewGlobalNode() {
        FBRef.refOrders.child(order.getOrderStatus())
                .child(order.getRequestedTime())
                .child(order.getOrderId())
                .setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.cook_order_details_update_success, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
