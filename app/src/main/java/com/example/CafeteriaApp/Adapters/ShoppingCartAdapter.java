package com.example.CafeteriaApp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Models.Addon;
import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the Shopping Cart RecyclerView.
 * Handles displaying products added to the cart, including their addons.
 */
public class ShoppingCartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{

    private final LayoutInflater inflater;
    private final List<Product> items = new ArrayList<>();
    @LayoutRes
    private final int rowLayoutId;

    boolean isFavorit = false;

    /**
     * Constructor for ShoppingCartAdapter.
     *
     * @param ctx         The context of the calling activity/fragment.
     * @param start       Initial list of products in the cart.
     * @param rowLayoutId Layout resource ID for a single row (cart item).
     */
    public ShoppingCartAdapter(@NonNull Context ctx, @NonNull List<Product> start,
                               @LayoutRes int rowLayoutId)
    {
        this.inflater = LayoutInflater.from(ctx);
        this.items.addAll(start);
        this.rowLayoutId = rowLayoutId;
    }

    /**
     * Updates the list of items in the adapter.
     *
     * @param data The new list of products.
     */
    public void setItems(List<Product> data)
    {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = inflater.inflate(rowLayoutId, parent, false);
        return new RecyclerView.ViewHolder(v)
        {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position)
    {
        Product it = items.get(position);
        View v = h.itemView;

        // Bind UI components
        TextView tvName = v.findViewById(R.id.tv_item_name);
        TextView tvAddon = v.findViewById(R.id.tv_item_addon);
        TextView tvPrice = v.findViewById(R.id.tv_item_price);
        ImageView ivImg = v.findViewById(R.id.item_img);
        ImageButton btnFav = v.findViewById(R.id.item_favorit_img_btn);

        if (tvName != null) tvName.setText(it.getName());
        if (tvAddon != null) tvAddon.setText(buildAddonsSummary(it));
        if (tvPrice != null) tvPrice.setText(it.getPriceText());
        if (ivImg != null)
        {
            if (it.getImageRes() != 0) ivImg.setImageResource(it.getImageRes());
            else ivImg.setImageResource(android.R.color.transparent);
        }

        // Handle favorite button click with animation
        if (btnFav != null)
        {
            btnFav.setOnClickListener(v1 ->
                                      {
                                          isFavorit = !isFavorit;

                                          if (isFavorit)
                                          {
                                              btnFav.setImageResource(R.drawable.favoirt_turn_on);
                                          } else
                                          {
                                              btnFav.setImageResource(R.drawable.favorit_turn_off);
                                          }

                                          // Add bounce animation
                                          btnFav.setScaleX(0.85f);
                                          btnFav.setScaleY(0.85f);
                                          btnFav.setAlpha(0.9f);

                                          btnFav.animate()
                                                  .scaleX(1.15f)
                                                  .scaleY(1.15f)
                                                  .alpha(1f)
                                                  .setDuration(120)
                                                  .withEndAction(() -> btnFav.animate()
                                                          .scaleX(1f)
                                                          .scaleY(1f)
                                                          .setDuration(120)
                                                          .setInterpolator(
                                                                  new android.view.animation.OvershootInterpolator())
                                                          .start())
                                                  .start();
                                      });
        }

        v.setOnClickListener(v12 ->
                             {
                                 // Optional: Handle click on the entire row
                             });
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    /**
     * Builds a string summary of selected addons for a product.
     *
     * @param p The product to check for addons.
     * @return A string listing the names of selected addons, separated by dots.
     */
    private static String buildAddonsSummary(@NonNull Product p)
    {
        if (p.getAddons() == null || p.getAddons().isEmpty()) return "ללא תוספות";

        StringBuilder sb = new StringBuilder();
        for (Addon a : p.getAddons())
        {
            if (a != null && a.isSelected())
            {
                if (sb.length() > 0) sb.append(" • ");
                sb.append(a.getAddonName());
            }
        }
        return sb.length() == 0 ? "ללא תוספות" : sb.toString();
    }
}
