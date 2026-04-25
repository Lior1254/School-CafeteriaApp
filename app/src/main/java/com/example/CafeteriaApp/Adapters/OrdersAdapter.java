package com.example.CafeteriaApp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.BaseActivity;
import com.example.CafeteriaApp.CookOrderDetailsActivity;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Helpers.FileManager;
import com.example.CafeteriaApp.MainPage;
import com.example.CafeteriaApp.Models.Order;
import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.Models.User;
import com.example.CafeteriaApp.R;
import com.google.android.material.card.MaterialCardView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying orders in a RecyclerView.
 * Supports both active orders and historical orders with different layouts.
 */
public class OrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ACTIVE = 0;
    private static final int TYPE_HISTORY = 1;

    private List<Order> ordersList;
    private boolean isHistoryMode = false;
    private int currentUserRole = User.ROLE_USER;

    /**
     * Constructs a new OrdersAdapter.
     * @param ordersList Initial list of orders.
     */
    public OrdersAdapter(List<Order> ordersList) {
        this.ordersList = ordersList;
    }

    /**
     * Sets the current user role to determine click behavior.
     * @param role The role (User, Cook, Manager).
     */
    public void setCurrentUserRole(int role) {
        this.currentUserRole = role;
    }

    /**
     * Updates the data set and switches between active and history modes.
     * @param newOrders The updated order list.
     * @param isHistory True if showing history tab.
     */
    public void setOrders(List<Order> newOrders, boolean isHistory) {
        this.ordersList = newOrders;
        this.isHistoryMode = isHistory;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return isHistoryMode ? TYPE_HISTORY : TYPE_ACTIVE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HISTORY) {
            View view = inflater.inflate(R.layout.item_order_history, parent, false);
            return new HistoryViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.custom_rv_order_item_tracking, parent, false);
            return new ActiveViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Order currentOrder = ordersList.get(position);

        if (holder instanceof ActiveViewHolder) {
            bindActiveOrder((ActiveViewHolder) holder, currentOrder);
        } else if (holder instanceof HistoryViewHolder) {
            bindHistoryOrder((HistoryViewHolder) holder, currentOrder);
        }
    }

    /**
     * Binds an active order to the ViewHolder.
     * @param holder Active order ViewHolder.
     * @param order The order data.
     */
    private void bindActiveOrder(ActiveViewHolder holder, final Order order) {
        Context context = holder.itemView.getContext();
        holder.tvOrderNumber.setText(context.getString(R.string.order_number_format, order.getOrderCode()));
        
        holder.itemView.setOnClickListener(v -> handleOrderClick(v.getContext(), order));

        holder.tvOrderStatus.setText(BaseActivity.getStatusText(order.getOrderStatus()));
        holder.tvEstimatedTimeValue.setText(formatTimeOnly(order.getRequestedTime()));
        holder.tvOrderSummary.setText(order.getSummary());
        holder.tvTotalPrice.setText(String.format(Locale.getDefault(), "₪%.2f", order.getTotalPrice()));
        
        String received = order.getOrderReceivedTime();
        if (received != null && received.contains(" ")) {
            String[] parts = received.split(" ");
            String formattedTime = parts[1].length() > 5 ? parts[1].substring(0, 5) : parts[1];
            holder.tvOrderReceivedTime.setText(context.getString(R.string.order_placed_at, parts[0], formattedTime));
        }

        updateStepper(holder, order.getOrderStatus());
    }

    /**
     * Handles clicks on an order item.
     * @param context Context for UI operations.
     * @param order The clicked order.
     */
    private void handleOrderClick(Context context, final Order order) {
        // 1. Regular User logic
        if (currentUserRole == User.ROLE_USER) {
            showOrderCodeDialog(context, order.getOrderCode());
            return;
        }

        // 2. Staff Logic: Network check only if BaseActivity is identified
        Context activityContext = context;
        while (activityContext instanceof android.content.ContextWrapper) {
            if (activityContext instanceof BaseActivity) break;
            activityContext = ((android.content.ContextWrapper) activityContext).getBaseContext();
        }

        if (activityContext instanceof BaseActivity) {
            if (!((BaseActivity) activityContext).checkNetworkAndShowDialog()) return;
        }

        // 3. Status Update Logic: This now runs for STAFF regardless of Activity identification
        String currentStatus = order.getOrderStatus();
        if (Order.STATUS_PENDING.equals(currentStatus) || "0".equals(currentStatus)) {
            final String oldStatus = currentStatus;

            // Change status to Preparing (1)
            order.setOrderStatus(Order.STATUS_PREPARING);

            // Update student's personal record first
            FBRef.getUserOrdersRef(order.getUserId(), false)
                    .child(order.getOrderId())
                    .setValue(order)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Successfully updated student, now move in global dashboard
                            moveGlobalOrderNode(context, order, oldStatus);
                        } else {
                            Toast.makeText(context, "Error updating user node: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Order is already in progress, just navigate to details
            openCookDetails(context, order);
        }
    }

    /**
     * Synchronizes order status in the global dashboard.
     * Fixed: Path now correctly uses RequestedTime instead of duplicating status.
     */
    private void moveGlobalOrderNode(final Context context, final Order order, String oldStatus) {
        FBRef.refOrders.child(oldStatus)
                .child(order.getRequestedTime())
                .child(order.getOrderId())
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FBRef.refOrders.child(order.getOrderStatus())
                                .child(order.getRequestedTime()) // FIXED: Was duplicating status here
                                .child(order.getOrderId())
                                .setValue(order)
                                .addOnCompleteListener(innerTask -> {
                                    if (innerTask.isSuccessful()) {
                                        openCookDetails(context, order);
                                    } else {
                                        Toast.makeText(context, "Sync Error: " + innerTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(context, "Cleanup Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void openCookDetails(Context context, Order order) {
        Intent intent = new Intent(context, CookOrderDetailsActivity.class);
        intent.putExtra("order", order);
        context.startActivity(intent);
    }

    /**
     * Binds a history order to the ViewHolder.
     */
    private void bindHistoryOrder(HistoryViewHolder holder, final Order order) {
        Context context = holder.itemView.getContext();
        holder.tvOrderNumber.setText(context.getString(R.string.order_number_format, order.getOrderCode()));
        holder.tvItemsDetails.setText(order.getSummary());
        holder.tvOrderPrice.setText(String.format(Locale.getDefault(), "₪%.2f", order.getTotalPrice()));
        
        String received = order.getOrderReceivedTime();
        if (received != null && received.contains(" ")) {
            String[] parts = received.split(" ");
            String formattedTime = parts[1].length() > 5 ? parts[1].substring(0, 5) : parts[1];
            holder.tvOrderDate.setText(context.getString(R.string.order_placed_at_label, parts[0], formattedTime));
        }

        holder.btnOrderAgain.setOnClickListener(v -> handleOrderAgain(v.getContext(), order));
    }

    /**
     * Handles "Order Again" functionality by populating the cart.
     */
    private void handleOrderAgain(Context context, Order order) {
        if (context instanceof BaseActivity && !((BaseActivity) context).checkNetworkAndShowDialog()) return;

        List<Product> itemsToReorder = order.getProducts();
        if (itemsToReorder != null && !itemsToReorder.isEmpty()) {
            FileManager.saveCart(context, new ArrayList<>(itemsToReorder));
            Toast.makeText(context, R.string.order_again_success, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, MainPage.class);
            intent.putExtra("OPEN_CART", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, R.string.order_again_error, Toast.LENGTH_SHORT).show();
        }
    }

    private String formatTimeOnly(String fullTime) {
        if (fullTime != null && fullTime.contains(" ")) {
            String time = fullTime.split(" ")[1];
            return time.length() > 5 ? time.substring(0, 5) : time;
        }
        return fullTime;
    }

    /**
     * Displays a QR code dialog for the order.
     */
    private void showOrderCodeDialog(Context context, String code) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_order_code, null);
        TextView tvCode = dialogView.findViewById(R.id.tvDialogOrderCode);
        ImageView imgQRCode = dialogView.findViewById(R.id.imgDialogIcon);
        Button btnClose = dialogView.findViewById(R.id.btnDialogClose);

        tvCode.setText(String.format("#%s", code));
        generateQRCode(imgQRCode, code);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void generateQRCode(ImageView imgQRCode, String code) {
        MultiFormatWriter mWriter = new MultiFormatWriter();
        try {
            BitMatrix mMatrix = mWriter.encode(code, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder mEncoder = new BarcodeEncoder();
            Bitmap mBitmap = mEncoder.createBitmap(mMatrix);
            imgQRCode.setImageBitmap(mBitmap);
        } catch (WriterException e) { e.printStackTrace(); }
    }

    private void updateStepper(ActiveViewHolder holder, String status) {
        try {
            int statusInt = Integer.parseInt(status);
            holder.step1_circle.setAlpha(statusInt >= 0 ? 1.0f : 0.3f);
            holder.step2_circle.setAlpha(statusInt >= 1 ? 1.0f : 0.3f);
            holder.step3_circle.setAlpha(statusInt >= 2 ? 1.0f : 0.3f);
            holder.step4_circle.setAlpha(statusInt >= 3 ? 1.0f : 0.3f);
        } catch (Exception ignored) {}
    }

    @Override
    public int getItemCount() {
        return ordersList != null ? ordersList.size() : 0;
    }

    public static class ActiveViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber, tvOrderStatus, tvEstimatedTimeValue, tvOrderSummary, tvTotalPrice, tvOrderReceivedTime;
        MaterialCardView step1_circle, step2_circle, step3_circle, step4_circle;

        public ActiveViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvEstimatedTimeValue = itemView.findViewById(R.id.tvEstimatedTimeValue);
            tvOrderSummary = itemView.findViewById(R.id.tvOrderSummary);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvOrderReceivedTime = itemView.findViewById(R.id.tvOrderReceivedTime);
            step1_circle = itemView.findViewById(R.id.step1_circle);
            step2_circle = itemView.findViewById(R.id.step2_circle);
            step3_circle = itemView.findViewById(R.id.step3_circle);
            step4_circle = itemView.findViewById(R.id.step4_circle);
        }
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber, tvItemsDetails, tvOrderPrice, tvOrderDate;
        Button btnOrderAgain;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvItemsDetails = itemView.findViewById(R.id.tvItemsDetails);
            tvOrderPrice = itemView.findViewById(R.id.tvOrderPrice);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            btnOrderAgain = itemView.findViewById(R.id.btnOrderAgain);
        }
    }
}
