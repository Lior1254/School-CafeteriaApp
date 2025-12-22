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

import java.util.List;

public class CustomizeItemActivity extends BaseActivity
{
    Intent intent;
    TextView tvProductName, tvProductDescription, tv_amount_of_items, tv_price;
    ImageView ivProductIMG;
    RecyclerView Addons;
    Button btn_AddToCart;
    ImageButton ibtn_plus_item, ibtn_minus_item;
    Product item;

    private int amount_of_products = 1;
    private double totalPrice = 0, price = 0;
    private String AddBtnText = "הוסף לסל";

    // Static field to hold the bitmap temporarily if needed
    public static android.graphics.Bitmap selectedImageBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_item);

        intent = getIntent();
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

        price = item.getPrice();
        totalPrice = price * amount_of_products;

        tvProductName.setText(item.getName());
        tvProductDescription.setText(item.getDescription());

        if (item.getImageBitmap() == null && CustomizeItemActivity.selectedImageBitmap != null) {
             item.setImageBitmap(CustomizeItemActivity.selectedImageBitmap);
             CustomizeItemActivity.selectedImageBitmap = null;
        }
        executeFirebaseOperation(() -> FBRef.loadProductImage(item, ivProductIMG));

        tv_price.setText(item.getPriceText());
        btn_AddToCart.setText(AddBtnText + "   " + item.getPriceText());

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

        tv_amount_of_items.setText(String.valueOf(amount_of_products));
        totalPrice = price * amount_of_products;
        btn_AddToCart.setText(AddBtnText + "   " + "₪" + String.format("%.2f", totalPrice));
    }

    public void AddToCart_Click(View view)
    {
        item.setPrice(price);
        item.setAmount(amount_of_products);

        List<Product> currentCart = FileManager.loadCart(this);
        currentCart.add(item);
        FileManager.saveCart(this, currentCart);

        Toast.makeText(this, "Added! Cart size: " + currentCart.size(), Toast.LENGTH_SHORT).show();
        finish();
    }

    public void GoBack_Click(View view)
    {
        finish();
    }
}
