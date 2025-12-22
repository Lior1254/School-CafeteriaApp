package com.example.CafeteriaApp.Adapters;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.R;

import java.util.List;

/**
 * RecyclerView adapter for displaying a list of products.
 * Handles loading images from Firebase Storage and displaying them.
 */
public class CustomProductAdapterRV extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{

    private final List<Product> items;
    private final Context context;

    public interface OnItemClick
    {
        void onClick(Product item);
    }

    private final OnItemClick listener;

    public CustomProductAdapterRV(Context context, List<Product> items, OnItemClick listener)
    {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_product_lv_layout, parent, false);
        return new RecyclerView.ViewHolder(v)
        {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        View itemView = holder.itemView;
        ImageView iv = itemView.findViewById(R.id.lv_item_img);
        TextView tvN = itemView.findViewById(R.id.lv_item_name);
        TextView tvD = itemView.findViewById(R.id.lv_item_description);
        TextView tvP = itemView.findViewById(R.id.lv_item_price);

        Product p = items.get(position);

        // --- Use Centralized Image Loading ---
        if (isNetworkAvailable()) {
            FBRef.loadProductImage(p, iv);
        }

        tvN.setText(p.getName());
        tvD.setText(p.getDescription());
        tvP.setText("₪" + String.format("%.2f", p.getPrice()));

        itemView.setOnClickListener(v ->
                                    {
                                        if (listener != null) listener.onClick(p);
                                    });
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}
