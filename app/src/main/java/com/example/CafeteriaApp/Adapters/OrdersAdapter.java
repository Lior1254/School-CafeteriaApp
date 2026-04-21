package com.example.CafeteriaApp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private int currentUserRole = User.ROLE_USER; // Default to user

    public OrdersAdapter(List<Order> ordersList) {
        this.ordersList = ordersList;
    }

    public void setCurrentUserRole(int role) {
        this.currentUserRole = role;
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

    private void bindActiveOrder(ActiveViewHolder holder, final Order order) {
        holder.tvOrderNumber.setText("הזמנה #" + order.getOrderCode());
        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOrderClick(v.getContext(), order);
            }
        });

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

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = (cm != null) ? cm.getActiveNetworkInfo() : null;
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void showNoInternetDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("שגיאת חיבור")
                .setMessage("פעולה זו דורשת חיבור לאינטרנט. אנא בדוק את ההגדרות שלך.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Ok", null)
                .show();
    }

    /**
     * Logic for handling clicks on an order block.
     */
    private void handleOrderClick(final Context context, final Order order) {
        if (currentUserRole == User.ROLE_COOK || currentUserRole == User.ROLE_MANAGER) {
            // Check internet before opening/updating
            if (!isNetworkAvailable(context)) {
                showNoInternetDialog(context);
                return;
            }

            // Check if status is still Pending (0), if so, update to Preparing (1) automatically
            if (Order.STATUS_PENDING.equals(order.getOrderStatus())) {
                final String oldStatus = order.getOrderStatus();
                order.setOrderStatus(Order.STATUS_PREPARING);
                
                // Update in UserOrders branch first
                FBRef.getUserOrdersRef(order.getUserId(), false)
                    .child(order.getOrderId())
                    .setValue(order)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                moveGlobalOrderNode(context, order, oldStatus, Order.STATUS_PREPARING);
                            } else {
                                Toast.makeText(context, "שגיאה בעדכון הסטטוס", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            } else {
                openCookDetails(context, order);
            }
        } else {
             showPrettyDialog(context, order.getOrderCode());
        }
    }

    private void moveGlobalOrderNode(final Context context, final Order order, String oldStatus, String newStatus) {
        // Remove old node
        FBRef.refOrders.child(oldStatus)
                .child(order.getRequestedTime())
                .child(order.getOrderId())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Create new node
                        FBRef.refOrders.child(order.getOrderStatus())
                            .child(order.getRequestedTime())
                            .child(order.getOrderId())
                            .setValue(order)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        openCookDetails(context, order);
                                    } else {
                                        Toast.makeText(context, "שגיאה בסנכרון גלובלי", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    }
                });
    }

    private void openCookDetails(Context context, Order order) {
        Intent intent = new Intent(context, CookOrderDetailsActivity.class);
        intent.putExtra("order", order);
        context.startActivity(intent);
    }

    private void bindHistoryOrder(HistoryViewHolder holder, final Order order) {
        holder.tvOrderNumber.setText("הזמנה #" + order.getOrderCode());
        holder.tvItemsDetails.setText(order.getSummary());
        holder.tvOrderPrice.setText(String.format(Locale.getDefault(), "₪%.2f", order.getTotalPrice()));
        
        String received = order.getOrderReceivedTime();
        if (received != null && received.contains(" ")) {
            String[] parts = received.split(" ");
            holder.tvOrderDate.setText("הוזמן בתאריך: " + parts[0] + ", בשעה " + parts[1].substring(0, 5));
        }

        holder.btnOrderAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOrderAgain(v.getContext(), order);
            }
        });
    }

    private void handleOrderAgain(Context context, Order order) {
        if (!isNetworkAvailable(context)) {
            showNoInternetDialog(context);
            return;
        }

        List<Product> itemsToReorder = order.getProducts();
        if (itemsToReorder != null && !itemsToReorder.isEmpty()) {
            List<Product> newCart = new ArrayList<>(itemsToReorder);
            FileManager.saveCart(context, newCart);
            Toast.makeText(context, "הסל עודכן עם פריטי ההזמנה!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, MainPage.class);
            intent.putExtra("OPEN_CART", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "לא ניתן לשחזר את פריטים מהזמנה זו.", Toast.LENGTH_SHORT).show();
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
        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
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
