package com.example.CafeteriaApp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Adapters.CustomProductOptionRvAdapter;
import com.example.CafeteriaApp.Helpers.BaseActivity;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Helpers.FileManager;
import com.example.CafeteriaApp.Models.Addon;
import com.example.CafeteriaApp.Models.Product;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;

/**
 * Activity for customizing a product before adding it to the cart.
 * Users can select addons, adjust quantity, and add personal notes.
 */
public class CustomizeItemActivity extends BaseActivity
{
    private TextView tvProductName, tvProductDescription, tvQuantity, tvPriceDisplay;
    private ImageView ivProductImage;
    private RecyclerView rvAddons;
    private Button btnAction;
    private ImageButton btnIncrement, btnDecrement;
    private TextInputEditText etNotes;
    private LinearLayout addonsContainer, notesContainer;

    private Product product;
    private int itemQuantity = 1;
    private double totalPrice = 0;
    private double unitPrice = 0;
    private String actionButtonLabel;
    private boolean isEditMode = false;
    private int editPosition = -1;

    /** Static field to hold the bitmap temporarily for transitions from MenuFragment */
    public static Bitmap selectedImageBitmap = null;

    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_item);

        Intent intent = getIntent();
        if (intent == null) {
            showErrorAndExit();
            return;
        }

        isEditMode = intent.getBooleanExtra("isEditMode", false);
        editPosition = intent.getIntExtra("position", -1);
        actionButtonLabel = getString(isEditMode ? R.string.customize_update_cart : R.string.customize_add_to_cart);

        initializeViews();
        setupUI(intent);
    }

    /**
     * Links UI components to their respective XML IDs.
     */
    private void initializeViews() {
        tvProductName = findViewById(R.id.tvProductName);
        tvProductDescription = findViewById(R.id.tvProductDescription);
        ivProductImage = findViewById(R.id.ivProductIMG);
        tvPriceDisplay = findViewById(R.id.tv_price);
        tvQuantity = findViewById(R.id.tv_amount_of_items);
        rvAddons = findViewById(R.id.Addons);
        btnIncrement = findViewById(R.id.ibtn_plus_item);
        btnDecrement = findViewById(R.id.ibtn_minus_item);
        btnAction = findViewById(R.id.btn_AddToCart);
        etNotes = findViewById(R.id.etNotes);
        addonsContainer = findViewById(R.id.addonsContainer);
        notesContainer = findViewById(R.id.notesContainer);
    }

    /**
     * Configures the UI with product data and handles visibility logic based on category.
     *
     * @param intent The intent containing the product object.
     */
    private void setupUI(@NonNull Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            product = intent.getSerializableExtra("item", Product.class);
        } else {
            product = (Product) intent.getSerializableExtra("item");
        }

        if (product == null) {
            showErrorAndExit();
            return;
        }

        // Hide notes for drink categories
        String coldDrink = getString(R.string.category_cold_drink);
        String hotDrink = getString(R.string.category_hot_drink);
        if (coldDrink.equals(product.getCategory()) || hotDrink.equals(product.getCategory())) {
            if (notesContainer != null) {
                notesContainer.setVisibility(View.GONE);
            }
        }

        // Setup addons list or hide container if empty
        List<Addon> addons = product.getAddons();
        if (addons == null || addons.isEmpty()) {
            if (addonsContainer != null) {
                addonsContainer.setVisibility(View.GONE);
            }
        } else {
            if (addonsContainer != null) {
                addonsContainer.setVisibility(View.VISIBLE);
            }
            rvAddons.setVisibility(View.VISIBLE);
            CustomProductOptionRvAdapter adapter = new CustomProductOptionRvAdapter(
                    this,
                    addons,
                    (addon, pos, isChecked) -> updatePriceOnAddonSelection(addon)
            );
            rvAddons.setLayoutManager(new LinearLayoutManager(this));
            rvAddons.setAdapter(adapter);
        }

        // Set initial values
        if (product.getAmount() > 0) {
            itemQuantity = product.getAmount();
        }

        unitPrice = product.getPrice();
        totalPrice = unitPrice * itemQuantity;

        tvProductName.setText(product.getName());
        tvProductDescription.setText(product.getDescription());
        tvQuantity.setText(String.valueOf(itemQuantity));
        tvPriceDisplay.setText(String.format(Locale.getDefault(), getString(R.string.customize_price_format), unitPrice));
        
        if (product.getNotes() != null) {
            etNotes.setText(product.getNotes());
        }

        // Load image from static reference if available, otherwise from Firebase
        if (product.getImageBitmap() != null) {
            ivProductImage.setImageBitmap(product.getImageBitmap());
        } else if (selectedImageBitmap != null) {
            ivProductImage.setImageBitmap(selectedImageBitmap);
        } else {
            FBRef.loadProductImage(product, ivProductImage);
        }

        updateActionButtons();
    }

    /**
     * Adjusts the unit price when an addon is selected or deselected.
     *
     * @param addon The selected/deselected addon.
     */
    private void updatePriceOnAddonSelection(@NonNull Addon addon) {
        if (addon.isSelected()) {
            unitPrice += addon.getPrice();
        } else {
            unitPrice -= addon.getPrice();
        }

        totalPrice = unitPrice * itemQuantity;
        updateActionButtons();
    }

    /**
     * Handles the click event for the increment quantity button.
     *
     * @param view The clicked view.
     */
    public void onIncrementQuantityClick(View view) {
        changeQuantity(true);
    }

    /**
     * Handles the click event for the decrement quantity button.
     *
     * @param view The clicked view.
     */
    public void onDecrementQuantityClick(View view) {
        changeQuantity(false);
    }

    /**
     * Updates the product quantity within allowed bounds and refreshes the UI.
     *
     * @param increment True to increase, false to decrease.
     */
    private void changeQuantity(boolean increment) {
        if (increment && itemQuantity < MAX_QUANTITY) {
            itemQuantity++;
        } else if (!increment && itemQuantity > MIN_QUANTITY) {
            itemQuantity--;
        } else {
            return;
        }

        tvQuantity.setText(String.valueOf(itemQuantity));
        totalPrice = unitPrice * itemQuantity;
        updateActionButtons();
    }

    /**
     * Refreshes the action button text and quantity button icons based on current state.
     */
    private void updateActionButtons() {
        if (btnDecrement != null) {
            btnDecrement.setImageResource(itemQuantity == MIN_QUANTITY ? R.drawable.ic_minus_gray : R.drawable.ic_minus_black);
        }
        if (btnIncrement != null) {
            btnIncrement.setImageResource(itemQuantity == MAX_QUANTITY ? R.drawable.ic_plus_gray : R.drawable.ic_plus_black);
        }
        if (btnAction != null) {
            btnAction.setText(String.format(Locale.getDefault(), getString(R.string.customize_button_format), actionButtonLabel, totalPrice));
        }
    }

    /**
     * Saves the customized product to the cart and exits the activity.
     *
     * @param view The clicked view.
     */
    public void onAddToCartClick(View view) {
        if (product == null) return;

        product.setPrice(unitPrice);
        product.setAmount(itemQuantity);
        product.setNotes(etNotes.getText() != null ? etNotes.getText().toString().trim() : "");

        List<Product> cart = FileManager.loadCart(this);

        if (isEditMode && editPosition != -1 && editPosition < cart.size()) {
            cart.set(editPosition, product);
            Toast.makeText(this, R.string.customize_cart_updated, Toast.LENGTH_SHORT).show();
        } else {
            cart.add(product);
            Toast.makeText(this, R.string.customize_added_to_cart, Toast.LENGTH_SHORT).show();
        }

        FileManager.saveCart(this, cart);
        finish();
    }

    /**
     * Closes the activity without saving changes.
     *
     * @param view The clicked view.
     */
    public void onBackClick(View view) {
        finish();
    }

    /**
     * Shows a localized error message and terminates the activity.
     */
    private void showErrorAndExit() {
        Toast.makeText(this, R.string.customize_error_loading, Toast.LENGTH_SHORT).show();
        finish();
    }
}
