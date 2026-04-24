package com.example.CafeteriaApp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Adapters.CustomProductOptionRvAdapter;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Helpers.FileManager;
import com.example.CafeteriaApp.Models.Addon;
import com.example.CafeteriaApp.Models.Product;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

/**
 * Activity for customizing a product before adding it to the cart.
 * Allows selecting addons, quantity, and adding personal notes.
 */
public class CustomizeItemActivity extends BaseActivity
{
    private TextView tvProductName, tvProductDescription, tv_amount_of_items, tv_price;
    private ImageView ivProductIMG;
    private RecyclerView rvAddons;
    private Button btn_AddToCart;
    private ImageButton ibtn_plus_item, ibtn_minus_item;
    private TextInputEditText etNotes;
    private TextInputLayout tilNotes;
    private Product item;

    private int amount_of_products = 1;
    private double totalPrice = 0, price = 0;
    private String AddBtnText = "הוסף לסל";
    private boolean isEditMode = false;
    private int editPosition = -1;

    /** Static field to hold the bitmap temporarily for transitions */
    public static android.graphics.Bitmap selectedImageBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_item);

        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode", false);
        editPosition = intent.getIntExtra("position", -1);
        
        if (isEditMode) {
            AddBtnText = "חזור לסל";
        }

        initializeViews();
        setupUI(intent);
    }

    private void initializeViews()
    {
        tvProductName = findViewById(R.id.tvProductName);
        tvProductDescription = findViewById(R.id.tvProductDescription);
        ivProductIMG = findViewById(R.id.ivProductIMG);
        tv_price = findViewById(R.id.tv_price);
        tv_amount_of_items = findViewById(R.id.tv_amount_of_items);
        rvAddons = findViewById(R.id.Addons);
        ibtn_plus_item = findViewById(R.id.ibtn_plus_item);
        ibtn_minus_item = findViewById(R.id.ibtn_minus_item);
        btn_AddToCart = findViewById(R.id.btn_AddToCart);
        etNotes = findViewById(R.id.etNotes);
        tilNotes = findViewById(R.id.tilNotes); // Ensure this ID exists in your XML
    }

    /**
     * Initializes the UI components with product data.
     * @param intent The intent containing product details.
     */
    private void setupUI(Intent intent)
    {
        if (android.os.Build.VERSION.SDK_INT >= 33)
        {
            item = intent.getSerializableExtra("item", Product.class);
        } else
        {
            item = (Product) intent.getSerializableExtra("item");
        }

        if (item == null)
        {
            Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Hide notes section for drinks
        if ("שתייה קרה".equals(item.getCategory()) || "שתייה חמה".equals(item.getCategory())) {
            if (tilNotes != null) tilNotes.setVisibility(View.GONE);
            else if (etNotes != null) etNotes.setVisibility(View.GONE);
        }

        if(item.getAmount() != 0)
        {
            amount_of_products = item.getAmount();
        }
        
        price = item.getPrice();
        totalPrice = price * amount_of_products;
        
        tvProductName.setText(item.getName());
        tvProductDescription.setText(item.getDescription());
        tv_amount_of_items.setText(String.valueOf(amount_of_products));
        tv_price.setText(item.getPriceText());
        updateButtonText();

        if (item.getNotes() != null) {
            etNotes.setText(item.getNotes());
        }

        if (item.getImageBitmap() == null && CustomizeItemActivity.selectedImageBitmap != null) {
             item.setImageBitmap(CustomizeItemActivity.selectedImageBitmap);
             CustomizeItemActivity.selectedImageBitmap = null;
        }
        
        if (checkNetworkAndShowDialog())
        {
            FBRef.loadProductImage(item, ivProductIMG);
        }

        if (item.getAddons() != null && !item.getAddons().isEmpty())
        {
            rvAddons.setVisibility(View.VISIBLE);
            CustomProductOptionRvAdapter ad = new CustomProductOptionRvAdapter(
                    this,
                    item.getAddons(),
                    (addon, pos, isChecked) -> updatePriceBasedOnAddons(addon)
            );
            rvAddons.setLayoutManager(new LinearLayoutManager(this));
            rvAddons.setAdapter(ad);
        } else
        {
            rvAddons.setVisibility(View.GONE);
        }
    }

    /**
     * Updates the total price when an addon is selected or deselected.
     * @param addon The addon that was toggled.
     */
    private void updatePriceBasedOnAddons(Addon addon)
    {
        if (addon.isSelected())
        {
            price += addon.getAddonPrice();
        } else
        {
            price -= addon.getAddonPrice();
        }

        totalPrice = price * amount_of_products;
        updateButtonText();
    }

    public void Plus_btn_Click(View view)
    {
        updateItemQuantity(true);
    }

    public void Minus_btn_Click(View view)
    {
        updateItemQuantity(false);
    }

    /**
     * Updates the quantity of the product (limited between 1 and 9).
     * @param increment True to increase, false to decrease.
     */
    private void updateItemQuantity(boolean increment)
    {
        if ((amount_of_products == 1 && !increment) || (amount_of_products == 9 && increment))
            return;

        if (increment) amount_of_products++;
        else amount_of_products--;

        updateItemIcons();
        tv_amount_of_items.setText(String.valueOf(amount_of_products));
        totalPrice = price * amount_of_products;
        updateButtonText();
    }

    private void updateItemIcons() {
        if (ibtn_minus_item != null) {
            ibtn_minus_item.setImageResource(amount_of_products == 1 ? R.drawable.ic_minus_gray : R.drawable.ic_minus_black);
        }
        if (ibtn_plus_item != null) {
            ibtn_plus_item.setImageResource(amount_of_products == 9 ? R.drawable.ic_plus_gray : R.drawable.ic_plus_black);
        }
    }

    private void updateButtonText() {
        if (btn_AddToCart != null) {
            btn_AddToCart.setText(String.format("%s   ₪%.2f", AddBtnText, totalPrice));
        }
    }

    /**
     * Saves the customized product to the local cart and closes the activity.
     */
    public void AddToCart_Click(View view)
    {
        item.setPrice(price);
        item.setAmount(amount_of_products);
        item.setNotes(etNotes.getText() != null ? etNotes.getText().toString().trim() : "");

        List<Product> currentCart = FileManager.loadCart(this);
        
        if (isEditMode && editPosition != -1 && editPosition < currentCart.size()) {
            currentCart.set(editPosition, item);
            Toast.makeText(this, "The cart has been updated!", Toast.LENGTH_SHORT).show();
        } else {
            currentCart.add(item);
            Toast.makeText(this, "Added to cart!", Toast.LENGTH_SHORT).show();
        }
        
        FileManager.saveCart(this, currentCart);
        finish();
    }

    public void GoBack_Click(View view)
    {
        finish();
    }
}
