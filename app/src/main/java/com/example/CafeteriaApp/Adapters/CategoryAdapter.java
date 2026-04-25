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

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the horizontal Category RecyclerView.
 * Manages the display and selection of menu categories.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<CategoryItem> categoryList;
    private final OnCategoryClickListener categoryClickListener;
    private int selectedPosition = 0;

    /**
     * Interface definition for a callback to be invoked when a category is clicked.
     */
    public interface OnCategoryClickListener {
        /**
         * Called when a category item has been clicked.
         *
         * @param categoryName The name of the clicked category.
         */
        void onCategoryClick(String categoryName);
    }

    /**
     * Constructs a new CategoryAdapter.
     *
     * @param categories List of category items to display.
     * @param listener   The callback that will run when a category is clicked.
     */
    public CategoryAdapter(List<CategoryItem> categories, OnCategoryClickListener listener) {
        this.categoryList = (categories != null) ? categories : new ArrayList<>();
        this.categoryClickListener = listener;
    }

    /**
     * Creates a new ViewHolder for a category item.
     *
     * @param parent   The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new CategoryViewHolder that holds the View for each category item.
     */
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(itemView);
    }

    /**
     * Binds the data to the ViewHolder and handles selection state and clicks.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryItem category = categoryList.get(position);

        holder.nameTextView.setText(category.getName());
        holder.iconImageView.setImageResource(category.getIconRes());

        // Update the visual selection state
        holder.itemView.setSelected(selectedPosition == position);

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            int previousPosition = selectedPosition;
            selectedPosition = currentPosition;

            // Refresh old and new items to reflect selection change in UI
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            if (categoryClickListener != null) {
                categoryClickListener.onCategoryClick(category.getName());
            }
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of categories.
     */
    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    /**
     * ViewHolder class for category items, containing the icon and name.
     */
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        final TextView nameTextView;
        final ImageView iconImageView;

        /**
         * Constructs a CategoryViewHolder.
         *
         * @param itemView The root view of the item layout.
         */
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tvCategoryName);
            iconImageView = itemView.findViewById(R.id.ivCategoryIcon);
        }
    }
}
