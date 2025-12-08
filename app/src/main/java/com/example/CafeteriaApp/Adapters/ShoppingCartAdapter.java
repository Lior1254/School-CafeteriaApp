package com.example.CafeteriaApp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.R;

import java.util.List;

/**
 * Adapter for the Shopping Cart RecyclerView.
 * Handles displaying products and their quantities.
 */
public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder>
{

    private final List<Product> items;

    /**
     * Interface for quantity change events.
     */
    public interface OnQuantityChangeListener
    {
        void onQuantityChange(int position, int newQuantity);
    }

    private final OnQuantityChangeListener quantityListener;

    /**
     * Constructor for ShoppingCartAdapter.
     *
     * @param items            List of products in the cart.
     * @param quantityListener Listener for quantity changes.
     */
    public ShoppingCartAdapter(List<Product> items, OnQuantityChangeListener quantityListener)
    {
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

        if (item.getImageRes() != 0)
        {
            holder.ivImg.setImageResource(item.getImageRes());
        } else
        {
            holder.ivImg.setImageResource(R.drawable.ic_launcher_background); // Placeholder
        }

        // Set the correct icon based on the current quantity
        if (item.getAmount() == 1) {
            holder.btnMinus.setImageResource(R.drawable.ic_bin);
        } else {
            holder.btnMinus.setImageResource(R.drawable.ic_minus_black);
        }

        // Plus button click
        holder.btnPlus.setOnClickListener(v ->
        {
            if (quantityListener != null && item.getAmount() < 9)
            {
                quantityListener.onQuantityChange(position, item.getAmount() + 1);
            }
        });

        // Minus button click
        holder.btnMinus.setOnClickListener(v ->
        {
            if (quantityListener != null)
            {
                if (item.getAmount() > 1)
                {
                    quantityListener.onQuantityChange(position, item.getAmount() - 1);
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    /**
     * ViewHolder for a single cart item.
     */
    static class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView ivImg;
        TextView tvName, tvPrice, tvQuantity;
        ImageButton btnMinus, btnPlus;

        ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            ivImg = itemView.findViewById(R.id.item_img);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvPrice = itemView.findViewById(R.id.tv_item_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnMinus = itemView.findViewById(R.id.btn_minus);
            btnPlus = itemView.findViewById(R.id.btn_plus);
        }
    }
}
