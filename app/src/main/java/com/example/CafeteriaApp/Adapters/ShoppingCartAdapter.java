package com.example.CafeteriaApp.Adapters;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the Shopping Cart RecyclerView.
 * Manages cart items, quantity updates, and item editing.
 */
public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.CartViewHolder> {

    private final List<Product> cartItems;
    private final Context context;
    private final OnCartInteractionListener interactionListener;

    /**
     * Interface for handling interactions within the shopping cart.
     */
    public interface OnCartInteractionListener {
        /**
         * Triggered when an item's quantity is changed.
         *
         * @param position    The position of the item in the list.
         * @param newQuantity The updated quantity value.
         */
        void onQuantityChange(int position, int newQuantity);

        /**
         * Triggered when the edit button of an item is clicked.
         *
         * @param item     The product to be edited.
         * @param position The position of the item in the list.
         */
        void onEditClick(Product item, int position);
    }

    /**
     * Constructs a new ShoppingCartAdapter.
     *
     * @param context             The context for resources and inflation.
     * @param cartItems           The list of products in the cart.
     * @param interactionListener Callback for quantity changes and edits.
     */
    public ShoppingCartAdapter(Context context, List<Product> cartItems, OnCartInteractionListener interactionListener) {
        this.context = context;
        this.cartItems = (cartItems != null) ? cartItems : new ArrayList<>();
        this.interactionListener = interactionListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item_order, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Product cartItem = cartItems.get(position);
        if (cartItem != null) {
            holder.bind(cartItem);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    /**
     * ViewHolder for items within the shopping cart.
     */
    class CartViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImageView;
        private final TextView productNameTextView;
        private final TextView productPriceTextView;
        private final TextView quantityTextView;
        private final ImageButton decreaseQuantityButton;
        private final ImageButton increaseQuantityButton;
        private final ImageButton editItemButton;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.ivProductImage);
            productNameTextView = itemView.findViewById(R.id.tvProductName);
            productPriceTextView = itemView.findViewById(R.id.tvProductPrice);
            quantityTextView = itemView.findViewById(R.id.tvQuantity);
            decreaseQuantityButton = itemView.findViewById(R.id.btnDecreaseQuantity);
            increaseQuantityButton = itemView.findViewById(R.id.btnIncreaseQuantity);
            editItemButton = itemView.findViewById(R.id.btnEditItem);
        }

        /**
         * Binds product data to the UI components and initializes click listeners.
         *
         * @param item The cart product to bind.
         */
        void bind(Product item) {
            productNameTextView.setText(item.getName());
            
            // Localized price formatting
            productPriceTextView.setText(context.getString(R.string.customize_price_format, item.getPrice()));
            quantityTextView.setText(String.valueOf(item.getAmount()));

            // Load product image via Firebase helper
            FBRef.loadProductImage(item, productImageView);

            // Toggle between minus and bin icon based on quantity
            if (item.getAmount() <= 1) {
                decreaseQuantityButton.setImageResource(R.drawable.ic_bin);
            } else {
                decreaseQuantityButton.setImageResource(R.drawable.ic_minus_black);
            }

            setupClickListeners(item);
        }

        /**
         * Sets up the interactive elements for the cart item.
         *
         * @param item The product associated with this ViewHolder.
         */
        private void setupClickListeners(Product item) {
            increaseQuantityButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && interactionListener != null) {
                    // Maximum limit for a single item is 9
                    if (item.getAmount() < 9) {
                        interactionListener.onQuantityChange(position, item.getAmount() + 1);
                    }
                }
            });

            decreaseQuantityButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && interactionListener != null) {
                    interactionListener.onQuantityChange(position, item.getAmount() - 1);
                }
            });

            editItemButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && interactionListener != null) {
                    interactionListener.onEditClick(item, position);
                }
            });
        }
    }
}
