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
import com.example.CafeteriaApp.Models.Product;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i,
                             @Nullable ViewGroup c,
                             @Nullable Bundle b) {
        return i.inflate(R.layout.fragment_home, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        RecyclerView RV_items = v.findViewById(R.id.RV_items);
        RV_items.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));

        List<Product> data = new ArrayList<>(
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


}

