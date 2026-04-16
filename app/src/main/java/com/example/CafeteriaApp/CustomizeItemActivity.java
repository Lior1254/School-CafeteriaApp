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

import java.util.List;

public class CustomizeItemActivity extends BaseActivity
{
    Intent intent;
    TextView tvProductName, tvProductDescription, tv_amount_of_items, tv_price;
    ImageView ivProductIMG;
    RecyclerView Addons;
    Button btn_AddToCart;
    ImageButton ibtn_plus_item, ibtn_minus_item;
    TextInputEditText etNotes;
    Product item;

    private int amount_of_products = 1;
    private double totalPrice = 0, price = 0;
    private String AddBtnText = "הוסף לסל";
    private boolean isEditMode = false;
    private int editPosition = -1;

    // Static field to hold the bitmap temporarily if needed
    public static android.graphics.Bitmap selectedImageBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_item);

        intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode", false);
        editPosition = intent.getIntExtra("position", -1);
        
        if (isEditMode) {
            AddBtnText = "חזור לסל";
        }

        initializeViews();
        setupUI();
    }

    private void initializeViews()
    {
        tvProductName = findViewById(R.id.tvProductName);
        tvProductDescription = findViewById(R.id.tvProductDescription);
        ivProductIMG = findViewById(R.id.ivProductIMG);
        tv_price = findViewById(R.id.tv_price);

        tv_amount_of_items = findViewById(R.id.tv_amount_of_items);
        Addons = findViewById(R.id.Addons);
        ibtn_plus_item = findViewById(R.id.ibtn_plus_item);
        ibtn_minus_item = findViewById(R.id.ibtn_minus_item);
        btn_AddToCart = findViewById(R.id.btn_AddToCart);
        etNotes = findViewById(R.id.etNotes);
    }

    private void setupUI()
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

        if(item.getAmount() != 0)
        {
            amount_of_products = item.getAmount();
        }
        
        price = item.getPrice();
        // Calculate subtotal price based on addons currently selected in the object
        if (item.getAddons() != null) {
            price = calculateInitialPrice(item);
        }
        
        totalPrice = price * amount_of_products;
        
        tvProductName.setText(item.getName());
        tvProductDescription.setText(item.getDescription());
        tv_amount_of_items.setText(String.valueOf(amount_of_products));
        tv_price.setText(item.getPriceText());
        btn_AddToCart.setText(AddBtnText + "   " + "₪" + String.format("%.2f", totalPrice));

        // Load existing notes if in edit mode
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
            Addons.setVisibility(View.VISIBLE);
            CustomProductOptionRvAdapter ad = new CustomProductOptionRvAdapter(
                    this,
                    item.getAddons(),
                    (addon, pos, isChecked) -> updatePriceBasedOnAddons(addon)
            );
            Addons.setLayoutManager(new LinearLayoutManager(this));
            Addons.setAdapter(ad);
        } else
        {
            Addons.setVisibility(View.GONE);
        }
    }

    /**
     * Calculates the price of a single product based on its selected addons.
     */
    private double calculateInitialPrice(Product p) {
        // Start with base price from DB logic (need to ensure product object has base price)
        // For now, let's assume we use current logic but handle the addons correctly
        double pPrice = p.getPrice(); 
        return pPrice;
    }

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
        btn_AddToCart.setText(AddBtnText + "   " + "₪" + String.format("%.2f", totalPrice));
    }

    public void Plus_btn_Click(View view)
    {
        updateItemQuantity(true);
    }

    public void Minus_btn_Click(View view)
    {
        updateItemQuantity(false);
    }

    private void updateItemQuantity(boolean increment)
    {
        if ((amount_of_products == 1 && !increment) || (amount_of_products == 9 && increment))
            return;

        if (increment)
        {
            amount_of_products++;
        } else
        {
            amount_of_products--;
        }

        updateItemIcons();

        tv_amount_of_items.setText(String.valueOf(amount_of_products));
        totalPrice = price * amount_of_products;
        btn_AddToCart.setText(AddBtnText + "   " + "₪" + String.format("%.2f", totalPrice));
    }

    private void updateItemIcons() {
        if (amount_of_products == 1)
        {
            ibtn_minus_item.setImageResource(R.drawable.ic_minus_gray);
        } else
        {
            ibtn_minus_item.setImageResource(R.drawable.ic_minus_black);
        }

        if (amount_of_products == 9)
        {
            ibtn_plus_item.setImageResource(R.drawable.ic_plus_gray);
        } else
        {
            ibtn_plus_item.setImageResource(R.drawable.ic_plus_black);
        }
    }

    public void AddToCart_Click(View view)
    {
        item.setPrice(price);
        item.setAmount(amount_of_products);
        item.setNotes(etNotes.getText() != null ? etNotes.getText().toString().trim() : "");

        List<Product> currentCart = FileManager.loadCart(this);
        
        if (isEditMode && editPosition != -1 && editPosition < currentCart.size()) {
            // Update existing item at specified position
            currentCart.set(editPosition, item);
            Toast.makeText(this, "הסל עודכן!", Toast.LENGTH_SHORT).show();
        } else {
            // Add as new item
            currentCart.add(item);
            Toast.makeText(this, "התווסף לסל!", Toast.LENGTH_SHORT).show();
        }
        
        FileManager.saveCart(this, currentCart);
        finish();
    }

    public void GoBack_Click(View view)
    {
        finish();
    }
}
