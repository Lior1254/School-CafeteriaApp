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
import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying a list of products in the menu.
 * Handles product details display, image loading from Firebase, and click interactions.
 */
public class CustomProductAdapterRV extends RecyclerView.Adapter<CustomProductAdapterRV.ProductViewHolder> {

    private final List<Product> productItems;
    private final Context context;
    private final OnProductClickListener clickListener;

    /**
     * Interface definition for a callback to be invoked when a product is clicked.
     */
    public interface OnProductClickListener {
        /**
         * Called when a product item has been clicked.
         *
         * @param product The product object associated with the clicked item.
         */
        void onProductClick(Product product);
    }

    /**
     * Constructs a new CustomProductAdapterRV.
     *
     * @param context       The context used for layout inflation and resources.
     * @param productItems  The list of products to display.
     * @param clickListener The callback that will run when a product is clicked.
     */
    public CustomProductAdapterRV(Context context, List<Product> productItems, OnProductClickListener clickListener) {
        this.context = context;
        this.productItems = (productItems != null) ? productItems : new ArrayList<>();
        this.clickListener = clickListener;
    }

    /**
     * Creates a new ProductViewHolder for a product item.
     *
     * @param parent   The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new ProductViewHolder that holds the View for each product.
     */
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_product_lv_layout, parent, false);
        return new ProductViewHolder(view);
    }

    /**
     * Binds the product data to the ProductViewHolder.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productItems.get(position);
        if (product != null) {
            holder.bind(product);
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of products.
     */
    @Override
    public int getItemCount() {
        return productItems.size();
    }

    /**
     * ViewHolder class for product items in the menu RecyclerView.
     */
    public class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImageView;
        private final TextView productNameTextView;
        private final TextView productDescriptionTextView;
        private final TextView productPriceTextView;

        /**
         * Initializes the ProductViewHolder and its UI components.
         *
         * @param itemView The root view of the product item layout.
         */
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.lv_item_img);
            productNameTextView = itemView.findViewById(R.id.lv_item_name);
            productDescriptionTextView = itemView.findViewById(R.id.lv_item_description);
            productPriceTextView = itemView.findViewById(R.id.lv_item_price);
        }

        /**
         * Binds the product data to the views and sets up the click listener.
         *
         * @param product The product object to display.
         */
        public void bind(Product product) {
            productNameTextView.setText(product.getName());
            productDescriptionTextView.setText(product.getDescription());
            
            // Format price with currency symbol
            String formattedPrice = context.getString(R.string.customize_price_format, product.getPrice());
            productPriceTextView.setText(formattedPrice);

            // Load product image via Firebase helper
            FBRef.loadProductImage(product, productImageView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onProductClick(product);
                }
            });
        }
    }
}
