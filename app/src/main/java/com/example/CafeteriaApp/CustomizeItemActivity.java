package com.example.CafeteriaApp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Models.Addon;
import com.example.CafeteriaApp.Adapters.CustomProductOptionRvAdapter;
import com.example.CafeteriaApp.Models.Product;

import java.util.Arrays;

public class CustomizeItemActivity extends AppCompatActivity {

    Intent intent;
    TextView tvProductName,tvProductDescription, tv_amount_of_items, tv_price;
    ImageView ivProductIMG;
    RecyclerView Addons;
    Button btn_AddToCart;
    ImageButton ibtn_plus_item,ibtn_minus_item;
    Product item;

    private int amount_of_products = 1;
    private double totalPrice = 0 , price = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_item);

        intent = getIntent();
        weddings();
        setUI();
        price = item.getPrice();


    }

    private void weddings()
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


    private void setUI()
    {
        if (android.os.Build.VERSION.SDK_INT >= 33)
        {
            item = intent.getParcelableExtra("item", Product.class);
        } else
        {
            item = intent.getParcelableExtra("item");
        }
        
        if (item == null) return; 
        
        tvProductName.setText(item.getName());
        tvProductDescription.setText(item.getDescription());
        ivProductIMG.setImageResource(item.getImageRes());
        tv_price.setText(item.getPriceText());
        btn_AddToCart.setText("הוספה להזמנה   " + item.getPriceText());

        // Updated to use List<Addon> instead of array
        if(item.getAddons() != null && !item.getAddons().isEmpty())
        {
            CustomProductOptionRvAdapter ad = new CustomProductOptionRvAdapter(
                    this,
                    item.getAddons(), // List passed directly
                    (addon, pos, isChecked) -> updateUI(addon)
            );
            Addons.setLayoutManager(new LinearLayoutManager(this));
            Addons.setAdapter(ad);
        }
    }

    private void updateUI(Addon addon)
    {
        if(addon.isSelected())
        {
            price += addon.getAddonPrice();
        }
        else
        {
            price -= addon.getAddonPrice();
        }

        totalPrice = price * amount_of_products;
        btn_AddToCart.setText("הוספה להזמנה   " + "₪" + String.format("%.2f", totalPrice) );
    }


    public void Plus_btn_Click(View view) {
        updateAmountOfItems(true);
    }

    public void Minus_btn_Click(View view) {
        updateAmountOfItems(false);
    }

    private void updateAmountOfItems(boolean sign)
            //true - plus
            //false - minus
    {
        if((amount_of_products == 1 && !sign) || (amount_of_products == 9 && sign))
            return;

        if(sign) {
            amount_of_products++;
        }
        else {
            amount_of_products--;
        }

        if(amount_of_products == 1)
            {
                ibtn_minus_item.setImageResource(R.drawable.minus_gray);
            }
        else
            {
                ibtn_minus_item.setImageResource(R.drawable.minus_black);
            }

        if(amount_of_products == 9)
        {
            ibtn_plus_item.setImageResource(R.drawable.plus_gray);
        }
        else
        {
            ibtn_plus_item.setImageResource(R.drawable.plus_black);
        }

        tv_amount_of_items.setText(String.valueOf(amount_of_products));
        totalPrice = price * amount_of_products;
        btn_AddToCart.setText("הוספה להזמנה   " + "₪" + String.format("%.2f", totalPrice) );
    }

    public void Close_btn_Click(View view) {
        finish();
    }
}
