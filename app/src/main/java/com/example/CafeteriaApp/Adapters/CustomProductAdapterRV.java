package com.example.CafeteriaApp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.R;

import java.util.List;

/**
 * RecyclerView adapter with an anonymous ViewHolder.
 * No named "VH" class; uses your existing IDs & layout.
 */
public class CustomProductAdapterRV extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ProductItem> items;
    public interface OnItemClick { void onClick(ProductItem item); }
    private final OnItemClick listener;

    public CustomProductAdapterRV(List<ProductItem> items, OnItemClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_product_lv_layout, parent, false);
        // Anonymous ViewHolder (no separate VH class)
        return new RecyclerView.ViewHolder(v) {};
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Find views each bind (simple; you asked to avoid VH fields)
        View itemView = holder.itemView;
        ImageView iv  = itemView.findViewById(R.id.lv_item_img);
        TextView tvN  = itemView.findViewById(R.id.lv_item_name);
        TextView tvD  = itemView.findViewById(R.id.lv_item_description);
        TextView tvP  = itemView.findViewById(R.id.lv_item_price);

        ProductItem p = items.get(position);
        iv.setImageResource(p.imageRes);
        tvN.setText(p.name);
        tvD.setText(p.description);
        tvP.setText(p.getPriceText());

        // Item click (same behavior as ListView's OnItemClickListener)
        itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(p); });
    }

    @Override
    public int getItemCount() { return items.size(); }
}
