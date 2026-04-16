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
import com.example.CafeteriaApp.Helpers.FileManager;
import com.example.CafeteriaApp.MainPage;
import com.example.CafeteriaApp.Models.Order;
import com.example.CafeteriaApp.Models.Product;
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

    public OrdersAdapter(List<Order> ordersList) {
        this.ordersList = ordersList;
    }

    /**
     * Updates the data set and switches between active and history modes.
     */
    public void setOrders(List<Order> newOrders, boolean isHistory) {
        this.ordersList = newOrders;
        this.isHistoryMode = isHistory;
        notifyDataSetChanged();
    }
    
    public void setOrders(List<Order> newOrders) {
        setOrders(newOrders, false);
    }

    @Override
    public int getItemViewType(int position) {
        return isHistoryMode ? TYPE_HISTORY : TYPE_ACTIVE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HISTORY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
            return new HistoryViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_rv_order_item_tracking, parent, false);
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
     * Binds data for an active order, including real-time status tracking.
     */
    private void bindActiveOrder(ActiveViewHolder holder, Order order) {
        holder.tvOrderNumber.setText("הזמנה #" + order.getOrderCode());
        holder.tvOrderNumber.setOnClickListener(v -> showPrettyDialog(v.getContext(), order.getOrderCode()));

        String statusText = BaseActivity.getStatusText(order.getOrderStatus());
        holder.tvOrderStatus.setText(statusText);

        holder.tvEstimatedTimeValue.setText(formatTimeOnly(order.getRequestedTime()));
        holder.tvOrderSummary.setText(order.getSummary());
        holder.tvTotalPrice.setText(String.format(Locale.getDefault(), "₪%.2f", order.getTotalPrice()));
        
        String received = order.getOrderReceivedTime();
        if (received != null && received.contains(" ")) {
            String[] parts = received.split(" ");
            holder.tvOrderReceivedTime.setText("הוזמן ב: " + parts[1].substring(0, 5) + " " + parts[0]);
        }

        updateStepper(holder, order.getOrderStatus());
    }

    /**
     * Binds data for a historical order and handles the "Order Again" logic.
     */
    private void bindHistoryOrder(HistoryViewHolder holder, Order order) {
        holder.tvOrderNumber.setText("הזמנה #" + order.getOrderCode());
        holder.tvItemsDetails.setText(order.getSummary());
        holder.tvOrderPrice.setText(String.format(Locale.getDefault(), "₪%.2f", order.getTotalPrice()));
        
        String received = order.getOrderReceivedTime();
        if (received != null && received.contains(" ")) {
            String[] parts = received.split(" ");
            holder.tvOrderDate.setText("הוזמן בתאריך: " + parts[0] + ", בשעה " + parts[1].substring(0, 5));
        }

        // Implementation of "Order Again" functionality
        holder.btnOrderAgain.setOnClickListener(v -> {
            List<Product> itemsToReorder = order.getProducts();
            if (itemsToReorder != null && !itemsToReorder.isEmpty()) {
                Context context = v.getContext();
                
                // 1. Clear current cart and add items from the history order
                List<Product> newCart = new ArrayList<>(itemsToReorder);
                FileManager.saveCart(context, newCart);
                
                // 2. Notify the user
                Toast.makeText(context, "הסל עודכן עם פריטי ההזמנה!", Toast.LENGTH_SHORT).show();
                
                // 3. Navigate to the Cart tab using Intent
                Intent intent = new Intent(context, MainPage.class);
                intent.putExtra("OPEN_CART", true);
                // Ensure we don't create multiple instances of MainPage
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);

            } else {
                Toast.makeText(v.getContext(), "לא ניתן לשחזר את הפריטים מהזמנה זו.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatTimeOnly(String fullTime) {
        if (fullTime != null && fullTime.contains(" ")) {
            String time = fullTime.split(" ")[1];
            return time.length() > 5 ? time.substring(0, 5) : time;
        }
        return fullTime;
    }

    private void showPrettyDialog(Context context, String code) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_order_code, null);
        TextView tvCode = dialogView.findViewById(R.id.tvDialogOrderCode);
        ImageView imgQRCODE = dialogView.findViewById(R.id.imgDialogIcon);
        Button btnClose = dialogView.findViewById(R.id.btnDialogClose);

        tvCode.setText("#" + code);
        generateQRCode(imgQRCODE, code);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        btnClose.setOnClickListener(v1 -> dialog.dismiss());
        dialog.show();
    }

    private void generateQRCode(ImageView imgQRCODE, String code) {
        MultiFormatWriter mWriter = new MultiFormatWriter();
        try {
            BitMatrix mMatrix = mWriter.encode(code, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder mEncoder = new BarcodeEncoder();
            Bitmap mBitmap = mEncoder.createBitmap(mMatrix);
            imgQRCODE.setImageBitmap(mBitmap);
        } catch (WriterException e) { e.printStackTrace(); }
    }

    private void updateStepper(ActiveViewHolder holder, String status) {
        try {
            int statusInt = Integer.parseInt(status);
            holder.step1_circle.setAlpha(statusInt >= 0 ? 1.0f : 0.3f);
            holder.step2_circle.setAlpha(statusInt >= 1 ? 1.0f : 0.3f);
            holder.step3_circle.setAlpha(statusInt >= 2 ? 1.0f : 0.3f);
            holder.step4_circle.setAlpha(statusInt >= 3 ? 1.0f : 0.3f);
        } catch (Exception e) {}
    }

    @Override
    public int getItemCount() {
        return ordersList != null ? ordersList.size() : 0;
    }

    // --- ViewHolders ---

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
        TextView tvOrderNumber, tvOrderStatus, tvItemsDetails, tvOrderPrice, tvOrderDate;
        Button btnOrderAgain;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvItemsDetails = itemView.findViewById(R.id.tvItemsDetails);
            tvOrderPrice = itemView.findViewById(R.id.tvOrderPrice);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            btnOrderAgain = itemView.findViewById(R.id.btnOrderAgain);
        }
    }
}
