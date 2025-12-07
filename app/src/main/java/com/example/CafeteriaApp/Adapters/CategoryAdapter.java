package com.example.CafeteriaApp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Models.CategoryItem;
import com.example.CafeteriaApp.R;

import java.util.List;

/**
 * Adapter for the horizontal Category RecyclerView.
 * Handles displaying category items and managing selection state.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>
{

    private final List<CategoryItem> categories;
    private int selectedPos = 0; // Default to the first selection
    private final OnCategoryClick listener;

    /**
     * Interface for handling category click events.
     */
    public interface OnCategoryClick
    {
        void onCategoryClick(String categoryName);
    }

    /**
     * Constructor for the CategoryAdapter.
     *
     * @param categories List of CategoryItem objects to display.
     * @param listener   Listener to handle click events.
     */
    public CategoryAdapter(List<CategoryItem> categories, OnCategoryClick listener)
    {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        CategoryItem item = categories.get(position);

        holder.tvName.setText(item.getName());
        holder.ivIcon.setImageResource(item.getIconRes());

        // Manage selected state
        boolean isSelected = (selectedPos == position);
        holder.itemView.setSelected(isSelected);

        holder.itemView.setOnClickListener(v ->
                                           {
                                               int prevPos = selectedPos;
                                               selectedPos = holder.getAdapterPosition();

                                               // Update the old and new View to change color
                                               notifyItemChanged(prevPos);
                                               notifyItemChanged(selectedPos);

                                               if (listener != null)
                                               {
                                                   listener.onCategoryClick(item.getName());
                                               }
                                           });
    }

    @Override
    public int getItemCount()
    {
        return categories.size();
    }

    /**
     * ViewHolder for category items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvName;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
        }
    }
}
