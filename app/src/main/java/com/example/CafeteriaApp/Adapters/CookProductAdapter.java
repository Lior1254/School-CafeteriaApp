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

import java.util.Collections;
import java.util.List;

/**
 * Adapter for displaying product details in the cook's order view.
 * Handles product name, quantity, image, selected addons, and special notes.
 */
public class CookProductAdapter extends RecyclerView.Adapter<CookProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<Product> productList;

    /**
     * Constructs a new CookProductAdapter.
     *
     * @param context  The context used for layout inflation and resources.
     * @param products The list of products to display.
     */
    public CookProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.productList = (products != null) ? products : Collections.emptyList();
    }

    /**
     * Creates a new ViewHolder for a product item.
     *
     * @param parent   The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new ProductViewHolder that holds the View for each product.
     */
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cook_product_detail, parent, false);
        return new ProductViewHolder(view);
    }

    /**
     * Binds the product data to the ViewHolder.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
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
        return productList.size();
    }

    /**
     * ViewHolder class for a single product item in the cook's view.
     */
    public class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImageView;
        private final TextView productNameTextView;
        private final TextView quantityTextView;
        private final TextView addonsTextView;
        private final TextView notesTextView;

        /**
         * Initializes the ViewHolder and its UI components.
         *
         * @param itemView The root view of the item layout.
         */
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.ivProduct);
            productNameTextView = itemView.findViewById(R.id.tvProductName);
            quantityTextView = itemView.findViewById(R.id.tvQuantity);
            addonsTextView = itemView.findViewById(R.id.tvAddons);
            notesTextView = itemView.findViewById(R.id.tvProductNotes);
        }

        /**
         * Binds the product data to the views, including formatting and visibility.
         *
         * @param product The product object to display.
         */
        public void bind(Product product) {
            productNameTextView.setText(product.getName());
            
            // Format quantity string
            String quantityText = "x" + product.getAmount();
            quantityTextView.setText(quantityText);

            // Load product image using helper
            FBRef.loadProductImage(product, productImageView);

            bindAddons(product);
            bindNotes(product);
        }

        /**
         * Processes and displays the list of selected addons.
         *
         * @param product The product containing the addons list.
         */
        private void bindAddons(Product product) {
            StringBuilder addonsBuilder = new StringBuilder(context.getString(R.string.cook_product_addons_label));
            List<Addon> addons = product.getAddons();
            
            boolean hasSelectedAddons = false;
            if (addons != null && !addons.isEmpty()) {
                for (Addon addon : addons) {
                    if (addon.isSelected()) {
                        if (hasSelectedAddons) {
                            addonsBuilder.append(", ");
                        }
                        addonsBuilder.append(addon.getName());
                        hasSelectedAddons = true;
                    }
                }
            }

            if (!hasSelectedAddons) {
                addonsBuilder.append(context.getString(R.string.cook_product_no_addons));
            }
            
            addonsTextView.setText(addonsBuilder.toString());
        }

        /**
         * Displays product-specific notes if available, otherwise hides the notes view.
         *
         * @param product The product containing optional notes.
         */
        private void bindNotes(Product product) {
            String notes = product.getNotes();
            if (notes != null && !notes.trim().isEmpty()) {
                notesTextView.setVisibility(View.VISIBLE);
                String notesFullText = context.getString(R.string.cook_product_notes_label) + notes;
                notesTextView.setText(notesFullText);
            } else {
                notesTextView.setVisibility(View.GONE);
            }
        }
    }
}
