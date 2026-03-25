package com.example.CafeteriaApp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Models.Order;
import com.example.CafeteriaApp.R;
import com.google.android.material.card.MaterialCardView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ACTIVE = 0;
    private static final int TYPE_HISTORY = 1;

    private List<Order> ordersList;
    private boolean isHistoryMode = false;

    public OrdersAdapter(List<Order> ordersList) {
        this.ordersList = ordersList;
    }

    public void setOrders(List<Order> newOrders, boolean isHistory) {
        this.ordersList = newOrders;
        this.isHistoryMode = isHistory;
        notifyDataSetChanged();
    }
    
    // Support for the old method call in case it's used elsewhere
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

    private void bindActiveOrder(ActiveViewHolder holder, Order order) {
        holder.tvOrderNumber.setText("הזמנה #" + order.getOrderCode());
        holder.tvOrderNumber.setOnClickListener(v -> showPrettyDialog(v.getContext(), order.getOrderCode()));

        String statusText = getStatusText(order.getOrderStatus());
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

    private void bindHistoryOrder(HistoryViewHolder holder, Order order) {
        holder.tvOrderNumber.setText("הזמנה #" + order.getOrderCode());
        holder.tvItemsDetails.setText(order.getSummary());
        holder.tvOrderPrice.setText(String.format(Locale.getDefault(), "₪%.2f", order.getTotalPrice()));
        
        String received = order.getOrderReceivedTime();
        if (received != null && received.contains(" ")) {
            String[] parts = received.split(" ");
            holder.tvOrderDate.setText("הוזמן בתאריך: " + parts[0] + ", בשעה " + parts[1].substring(0, 5));
        }

        // Button action if needed
        holder.btnOrderAgain.setOnClickListener(v -> {
            // Implement logic to add these items back to cart
        });
    }

    private String getStatusText(String status) {
        switch (status) {
            case Order.STATUS_PENDING: return "ממתין";
            case Order.STATUS_PREPARING: return "בהכנה";
            case Order.STATUS_READY: return "מוכן";
            case Order.STATUS_COLLECTED: return "נאסף";
            default: return "לא ידוע";
        }
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
