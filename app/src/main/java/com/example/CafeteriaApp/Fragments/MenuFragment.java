package com.example.CafeteriaApp.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Adapters.CustomProductAdapterRV;
import com.example.CafeteriaApp.Adapters.ProductItem;
import com.example.CafeteriaApp.CustomizeItemActivity;
import com.example.CafeteriaApp.MenuCategories;
import com.example.CafeteriaApp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuFragment extends Fragment {
    private ListView dialog_lv_options;
    private RecyclerView RV_items;
    private Button dialog_btnPay;
    private TextView dialog_tvProductName, dialog_tvTotalPrice;
    private ImageView dialog_ivProductIMG;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i,
                             @Nullable ViewGroup c,
                             @Nullable Bundle b)
    {
        return i.inflate(R.layout.fragment_home, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        RecyclerView RV_items = v.findViewById(R.id.RV_items);
        RV_items.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));

        List<ProductItem> data = new ArrayList<>(
                Arrays.asList(MenuCategories.SANDWICHES.products)
        );

        // 3) Create adapter with an item-click callback
        CustomProductAdapterRV adp = new CustomProductAdapterRV(
                data,
                item -> {
                    // Open details screen with Parcelable extra
                    Intent intent = new Intent(requireContext(), CustomizeItemActivity.class);
                    intent.putExtra("item", item);
                    startActivity(intent);
                }
        );

        RV_items.setAdapter(adp);
    }





    private void showProductDialog(ProductItem item) {
        View product_dialog = getLayoutInflater()
                .inflate(R.layout.custom_alertdialog_product_options, null, false);

        //Weddings
        dialog_ivProductIMG   = (ImageView) product_dialog.findViewById(R.id.ivProductIMG);
        dialog_tvProductName  = (TextView) product_dialog.findViewById(R.id.tvProductName);
        dialog_tvTotalPrice   = (TextView) product_dialog.findViewById(R.id.dialog_tvTotalPrice);
        dialog_btnPay         = (Button) product_dialog.findViewById(R.id.dialog_btnPay);

        dialog_lv_options = (ListView) product_dialog.findViewById(R.id.dialog_lv_options);

        if (dialog_ivProductIMG != null && item.imageRes != 0) dialog_ivProductIMG.setImageResource(item.imageRes);
        if (dialog_tvProductName != null) dialog_tvProductName.setText(item.name);
        if (dialog_tvTotalPrice != null)  dialog_tvTotalPrice.setText(item.getPriceText());

        if (item.supportAddon && item.addons != null && item.addons.length > 0)
        {
            Intent intent = new Intent(this.requireContext(), CustomizeItemActivity.class);
            intent.putExtra("item", item);
            startActivity(intent);

        }
        /*
        if (item.supportAddon && item.addons != null && item.addons.length > 0)
        {
            dialog_lv_options.setVisibility(View.VISIBLE);
            CustomProductOptionAdapter addonAdp = new CustomProductOptionAdapter(this.requireContext(), List.of(item.addons));
            dialog_lv_options.setAdapter(addonAdp);

        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(product_dialog)
                .create();
        dialog.show();



        dialog_btnPay.setOnClickListener(v -> {

            dialog.dismiss();
        });

         */



    }



    private void onPayClicked(ProductItem item, int qty, List<String> addons) {
    }

    }

