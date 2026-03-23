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

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.MyViewHolder> {

    private List<Order> ordersList;

    public OrdersAdapter(List<Order> ordersList) {
        this.ordersList = ordersList;
    }

    public void setOrders(List<Order> newOrders) {
        this.ordersList = newOrders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_rv_order_item_tracking, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Order currentOrder = ordersList.get(position);

        holder.tvOrderNumber.setText("הזמנה #" + currentOrder.getOrderCode());

        holder.tvOrderNumber.setOnClickListener(v -> {
            showPrettyDialog(v.getContext(), currentOrder.getOrderCode());
        });

        String statusText = "";
        switch (currentOrder.getOrderStatus()) {
            case Order.STATUS_PENDING: statusText = "ממתין"; break;
            case Order.STATUS_PREPARING: statusText = "בהכנה"; break;
            case Order.STATUS_READY: statusText = "מוכן"; break;
            case Order.STATUS_COLLECTED: statusText = "נאסף"; break;
        }
        holder.tvOrderStatus.setText(statusText);

        String reqTime = currentOrder.getRequestedTime();
        if (reqTime != null && reqTime.contains(" ")) {
            String timePart = reqTime.split(" ")[1];
            holder.tvEstimatedTimeValue.setText(timePart.length() > 5 ? timePart.substring(0, 5) : timePart);
        } else {
            holder.tvEstimatedTimeValue.setText(reqTime);
        }

        holder.tvOrderSummary.setText(currentOrder.getSummary());
        holder.tvTotalPrice.setText(String.format(Locale.getDefault(), "₪%.2f", currentOrder.getTotalPrice()));

        String receivedTime = currentOrder.getOrderReceivedTime();
        if (receivedTime != null && receivedTime.contains(" ")) {
            String[] parts = receivedTime.split(" ");
            String date = parts[0];
            String time = parts[1].length() > 5 ? parts[1].substring(0, 5) : parts[1];
            holder.tvOrderReceivedTime.setText("הוזמן ב: " + time + " " + date);
        } else {
            holder.tvOrderReceivedTime.setText("הוזמן ב: " + receivedTime);
        }

        updateStepper(holder, currentOrder.getOrderStatus());
    }

    private void showPrettyDialog(Context context, String code) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_order_code, null);
        
        TextView tvCode = dialogView.findViewById(R.id.tvDialogOrderCode);
        ImageView imgQRCODE = dialogView.findViewById(R.id.imgDialogIcon);
        Button btnClose = dialogView.findViewById(R.id.btnDialogClose);

        tvCode.setText("#" + code);
        
        // יצירת ה-QR Code לתוך ה-ImageView
        generateQRCode(imgQRCODE, code);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnClose.setOnClickListener(v1 -> dialog.dismiss());
        dialog.show();
    }

    private void generateQRCode(ImageView imgQRCODE, String code) {
        MultiFormatWriter mWriter = new MultiFormatWriter();
        try {
            // יצירת מטריצת ה-QR (גודל 400x400)
            BitMatrix mMatrix = mWriter.encode(code, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder mEncoder = new BarcodeEncoder();
            Bitmap mBitmap = mEncoder.createBitmap(mMatrix);
            imgQRCODE.setImageBitmap(mBitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void updateStepper(MyViewHolder holder, String status) {
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

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber, tvOrderStatus, tvEstimatedTimeValue, tvOrderSummary, tvTotalPrice, tvOrderReceivedTime;
        MaterialCardView step1_circle, step2_circle, step3_circle, step4_circle;

        public MyViewHolder(@NonNull View itemView) {
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
}
