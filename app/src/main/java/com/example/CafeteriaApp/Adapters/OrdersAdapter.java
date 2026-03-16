package com.example.CafeteriaApp.Adapters;

import android.content.Context;

import com.example.CafeteriaApp.Models.Order;
import com.example.CafeteriaApp.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// 1. The Adapter class
public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.MyViewHolder> {

    private List<String> dataList;

    // Constructor
    public OrdersAdapter(List<String> dataList) {
        this.dataList = dataList;
    }

    // 2. Called when RecyclerView needs a new ViewHolder
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new MyViewHolder(view);
    }

    // 3. Called to display the data at the specified position
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String currentItem = dataList.get(position);
        holder.titleTextView.setText(currentItem);


        View itemView = holder.itemView;
        TextView tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
        MaterialCardView step2_circle = itemView.findViewById(R.id.step2_circle);
        MaterialCardView step3_circle = itemView.findViewById(R.id.step3_circle);
        MaterialCardView step4_circle = itemView.findViewById(R.id.step4_circle);
        TextView tvEstimatedTimeValue = itemView.findViewById(R.id.tvEstimatedTimeValue);
        TextView tvOrderSummary =  itemView.findViewById(R.id.tvOrderSummary);
        TextView tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
        TextView tvOrderReceivedTime = itemView.findViewById(R.id.tvOrderReceivedTime);







    }

    // 4. Returns the total number of items
    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    // 5. The ViewHolder class
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}