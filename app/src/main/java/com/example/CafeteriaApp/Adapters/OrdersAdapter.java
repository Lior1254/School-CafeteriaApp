package com.example.CafeteriaApp.Adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Models.Order;
import com.example.CafeteriaApp.R;

import java.util.List;

/**
 * Adapter for the Orders RecyclerView.
 * Binds order data to the view holder and manages the order status timeline UI.
 */
public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>
{

    private final Context context;
    private final List<Order> orderList;

    /**
     * Constructor for the OrdersAdapter.
     *
     * @param context   Context for inflating layouts and accessing resources.
     * @param orderList List of orders to display.
     */
    public OrdersAdapter(Context context, List<Order> orderList)
    {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_rv_order_item, parent,
                                                         false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position)
    {
        Order currentOrder = orderList.get(position);

        holder.tvOrderNumber.setText("הזמנה #" + currentOrder.getId());
        holder.tvOrderSummary.setText(currentOrder.getSummary());
        holder.tvTotalPrice.setText("סה\"כ: ₪" + String.format("%.2f", currentOrder.getPrice()));
        holder.tvOrderStatus.setText(currentOrder.getStatusText());
        holder.tvEstimatedTimeValue.setText(currentOrder.getEstimatedReadyTime());
        holder.tvOrderReceivedTime.setText("התקבלה ב-" + currentOrder.getOrderReceivedTime());

        updateStatusUI(holder, currentOrder.getStatusCode());

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v ->
                                           {
                                               Toast.makeText(context,
                                                              "נלחצה הזמנה מספר: " + currentOrder.getId(),
                                                              Toast.LENGTH_SHORT).show();
                                               // TODO: Open order details screen here
                                           });
    }

    @Override
    public int getItemCount()
    {
        return orderList.size();
    }

    /**
     * Updates the UI of the timeline steps based on the current order status.
     *
     * @param holder     The ViewHolder containing the UI elements.
     * @param statusCode The status code of the order (1-4).
     */
    private void updateStatusUI(OrderViewHolder holder, int statusCode)
    {
        // Define colors for active, completed, and inactive states
        int colorActive = ContextCompat.getColor(context, R.color.status_in_progress_yellow);
        int colorCompleted = ContextCompat.getColor(context, R.color.status_ready_green);
        int colorInactive = ContextCompat.getColor(context, R.color.status_received_grey);

        // --- Reset all steps to inactive state ---
        ((GradientDrawable) holder.step1_icon.getBackground()).setColor(colorInactive);
        ((GradientDrawable) holder.step2_icon.getBackground()).setColor(colorInactive);
        ((GradientDrawable) holder.step3_icon.getBackground()).setColor(colorInactive);
        ((GradientDrawable) holder.step4_icon.getBackground()).setColor(colorInactive);
        holder.step1_icon.setText("1");
        holder.step2_icon.setText("2");
        holder.step3_icon.setText("3");
        holder.step4_icon.setText("4");
        holder.line1.setBackgroundColor(colorInactive);
        holder.line2.setBackgroundColor(colorInactive);
        holder.line3.setBackgroundColor(colorInactive);
        holder.step1_text.setTextColor(colorInactive);
        holder.step2_text.setTextColor(colorInactive);
        holder.step3_text.setTextColor(colorInactive);
        holder.step4_text.setTextColor(colorInactive);

        // --- Update UI based on completed steps ---
        if (statusCode >= 1)
        {
            ((GradientDrawable) holder.step1_icon.getBackground()).setColor(colorCompleted);
            holder.step1_icon.setText("✓");
            holder.step1_text.setTextColor(colorCompleted);
        }
        if (statusCode >= 2)
        {
            holder.line1.setBackgroundColor(colorCompleted);
            ((GradientDrawable) holder.step2_icon.getBackground()).setColor(colorCompleted);
            holder.step2_icon.setText("✓");
            holder.step2_text.setTextColor(colorCompleted);
        }
        if (statusCode >= 3)
        {
            holder.line2.setBackgroundColor(colorCompleted);
            ((GradientDrawable) holder.step3_icon.getBackground()).setColor(colorCompleted);
            holder.step3_icon.setText("✓");
            holder.step3_text.setTextColor(colorCompleted);
        }
        if (statusCode >= 4)
        {
            holder.line3.setBackgroundColor(colorCompleted);
            ((GradientDrawable) holder.step4_icon.getBackground()).setColor(colorCompleted);
            holder.step4_icon.setText("✓");
            holder.step4_text.setTextColor(colorCompleted);
        }

        // --- Highlight current active step and set status label background ---
        GradientDrawable statusBackground = (GradientDrawable) holder.tvOrderStatus.getBackground();
        if (statusCode == 1)
        {
            ((GradientDrawable) holder.step1_icon.getBackground()).setColor(
                    colorActive); // Step 1 is active
            holder.step1_text.setTextColor(colorActive);
            statusBackground.setColor(colorInactive); // General status background gray
        } else if (statusCode == 2)
        {
            ((GradientDrawable) holder.step2_icon.getBackground()).setColor(
                    colorActive); // Step 2 is active
            holder.step2_text.setTextColor(colorActive);
            statusBackground.setColor(colorActive); // General status background yellow
        } else if (statusCode == 3)
        {
            ((GradientDrawable) holder.step3_icon.getBackground()).setColor(
                    colorActive); // Step 3 is active
            holder.step3_text.setTextColor(colorActive);
            statusBackground.setColor(colorCompleted); // General status background green
        } else if (statusCode >= 4)
        {
            // Order collected, set background to purple
            statusBackground.setColor(
                    ContextCompat.getColor(context, R.color.status_collected_purple));
        }
    }

    /**
     * ViewHolder class to hold references to the views for each list item.
     */
    public static class OrderViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvOrderNumber, tvOrderStatus, tvOrderSummary, tvTotalPrice;
        TextView step1_icon, step2_icon, step3_icon, step4_icon;
        TextView step1_text, step2_text, step3_text, step4_text;
        View line1, line2, line3;
        TextView tvEstimatedTimeValue, tvOrderReceivedTime;

        public OrderViewHolder(@NonNull View itemView)
        {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderSummary = itemView.findViewById(R.id.tvOrderSummary);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvEstimatedTimeValue = itemView.findViewById(R.id.tvEstimatedTimeValue);
            tvOrderReceivedTime = itemView.findViewById(R.id.tvOrderReceivedTime);

            step1_icon = itemView.findViewById(R.id.progress_step1_icon);
            step2_icon = itemView.findViewById(R.id.progress_step2_icon);
            step3_icon = itemView.findViewById(R.id.progress_step3_icon);
            step4_icon = itemView.findViewById(R.id.progress_step4_icon);
            step1_text = itemView.findViewById(R.id.progress_step1_text);
            step2_text = itemView.findViewById(R.id.progress_step2_text);
            step3_text = itemView.findViewById(R.id.progress_step3_text);
            step4_text = itemView.findViewById(R.id.progress_step4_text);
            line1 = itemView.findViewById(R.id.progress_line1);
            line2 = itemView.findViewById(R.id.progress_line2);
            line3 = itemView.findViewById(R.id.progress_line3);
        }
    }
}
