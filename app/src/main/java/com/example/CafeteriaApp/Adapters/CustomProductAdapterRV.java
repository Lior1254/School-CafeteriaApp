package com.example.CafeteriaApp.Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.firebase.storage.StorageReference;

import java.util.List;

/**
 * RecyclerView adapter for displaying a list of products.
 * Handles loading images from Firebase Storage and displaying them.
 */
public class CustomProductAdapterRV extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{

    private final List<Product> items;

    public interface OnItemClick
    {
        void onClick(Product item);
    }

    private final OnItemClick listener;

    public CustomProductAdapterRV(List<Product> items, OnItemClick listener)
    {
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

        // --- Image Loading Logic ---
        if (p.getImageBitmap() != null)
        {
            iv.setImageBitmap(p.getImageBitmap());
        } else if (p.getId() != null && !p.getId().isEmpty())
        {
            iv.setImageResource(R.drawable.ic_launcher_background); // Placeholder (Green)

            // Changed to look inside "Products" folder
            StorageReference imageRef = FBRef.refStorage.child("Products").child(p.getId() + ".jpg");

            final long MAX_SIZE = 5 * 1024 * 1024;
            imageRef.getBytes(MAX_SIZE).addOnSuccessListener(bytes ->
            {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                p.setImageBitmap(bitmap);
                iv.setImageBitmap(bitmap);
            }).addOnFailureListener(e -> {
                // On failure, set the green default image
                iv.setImageResource(R.drawable.ic_launcher_background);
            });
        } else
        {
            iv.setImageResource(R.drawable.ic_launcher_background);
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
}
