package com.example.CafeteriaApp.Adapters;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.R;

import java.util.List;

/**
 * Adapter for the Shopping Cart RecyclerView.
 */
public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder>
{
    private final List<Product> items;
    private final Context context;

    public interface OnQuantityChangeListener
    {
        void onQuantityChange(int position, int newQuantity);
        void onEditClick(Product item, int position); // Added position
    }

    private final OnQuantityChangeListener quantityListener;

    public ShoppingCartAdapter(Context context, List<Product> items, OnQuantityChangeListener quantityListener)
    {
        this.context = context;
        this.items = items;
        this.quantityListener = quantityListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        Product item = items.get(position);

        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(String.format("₪%.2f", item.getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getAmount()));

        if (isNetworkAvailable()) {
            FBRef.loadProductImage(item, holder.ivImg);
        }

        if (item.getAmount() == 1) {
            holder.btnMinus.setImageResource(R.drawable.ic_bin);
        } else {
            holder.btnMinus.setImageResource(R.drawable.ic_minus_black);
        }

        holder.btnPlus.setOnClickListener(v ->
        {
            if (quantityListener != null && item.getAmount() < 9)
            {
                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    quantityListener.onQuantityChange(currentPos, items.get(currentPos).getAmount() + 1);
                }
            }
        });

        holder.btnMinus.setOnClickListener(v ->
        {
            if (quantityListener != null)
            {
                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    quantityListener.onQuantityChange(currentPos, items.get(currentPos).getAmount() - 1);
                }
            }
        });

        holder.btnEdit.setOnClickListener(v ->
        {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && quantityListener != null)
            {
                quantityListener.onEditClick(items.get(currentPos), currentPos);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView ivImg;
        TextView tvName, tvPrice, tvQuantity;
        ImageButton btnMinus, btnPlus, btnEdit;

        ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            ivImg = itemView.findViewById(R.id.item_img);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvPrice = itemView.findViewById(R.id.tv_item_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnMinus = itemView.findViewById(R.id.btn_minus);
            btnPlus = itemView.findViewById(R.id.btn_plus);
            btnEdit = itemView.findViewById(R.id.btn_edit); 
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = (cm != null) ? cm.getActiveNetworkInfo() : null;
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
