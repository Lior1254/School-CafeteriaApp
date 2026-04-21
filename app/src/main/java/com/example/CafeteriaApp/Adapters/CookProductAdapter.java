package com.example.CafeteriaApp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Models.Addon;
import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.R;

import java.util.List;

public class CookProductAdapter extends RecyclerView.Adapter<CookProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> products;

    public CookProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cook_product_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        holder.tvProductName.setText(product.getName());
        holder.tvQuantity.setText("x" + product.getAmount());

        // Load image
        FBRef.loadProductImage(product, holder.ivProduct);

        // Build addons string
        StringBuilder addonsText = new StringBuilder("תוספות: ");
        if (product.getAddons() != null && !product.getAddons().isEmpty()) {
            boolean first = true;
            for (Addon addon : product.getAddons()) {
                if (addon.isSelected()) {
                    if (!first) addonsText.append(", ");
                    addonsText.append(addon.getAddonName());
                    first = false;
                }
            }
            if (first) addonsText.append("ללא");
        } else {
            addonsText.append("ללא");
        }
        holder.tvAddons.setText(addonsText.toString());

        // Handle notes
        if (product.getNotes() != null && !product.getNotes().isEmpty()) {
            holder.tvProductNotes.setVisibility(View.VISIBLE);
            holder.tvProductNotes.setText("הערה למנה: " + product.getNotes());
        } else {
            holder.tvProductNotes.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvProductName, tvQuantity, tvAddons, tvProductNotes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvAddons = itemView.findViewById(R.id.tvAddons);
            tvProductNotes = itemView.findViewById(R.id.tvProductNotes);
        }
    }
}
