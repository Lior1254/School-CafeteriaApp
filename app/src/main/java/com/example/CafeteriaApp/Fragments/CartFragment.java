package com.example.CafeteriaApp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Adapters.ProductItem;
import com.example.CafeteriaApp.Adapters.ShoppingCartAdapter;
import com.example.CafeteriaApp.MainPage;
import com.example.CafeteriaApp.R;

import java.util.Arrays;
import java.util.List;

public class CartFragment extends Fragment {
    TextView tv_amount_of_items;
    Button btn_checkout;
    RecyclerView rv;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i,
                             @Nullable ViewGroup c,
                             @Nullable Bundle b) {
        return i.inflate(R.layout.fragment_cart, c, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //צריך לבדוק עם אלברט
        view.findViewById(R.id.btn_add_item).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MainPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        List<ProductItem> productItems = Arrays.asList(
                new ProductItem(R.drawable.ic_launcher_background, "ארוחת ריב", "צ'יפס • קולה זירו", 59.0, true, null),
                new ProductItem(R.drawable.images,  "צ'יפס",      "רגיל",              18.0, false, null)
        );


        Wedding(view);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        ShoppingCartAdapter adapter = new ShoppingCartAdapter(requireContext(), productItems, R.layout.recyclerview_item_order);
        rv.setAdapter(adapter);

        updateData(productItems);
    }

    public void updateData(List<ProductItem> data) {
        tv_amount_of_items.setText(data.size() + " מוצרים");

        double cartPrice = 0;
        for(ProductItem p : data)
        {
            cartPrice += p.price;
        }
        if(cartPrice != 0)
        {
            btn_checkout.setText("סיום ותשלום • ₪" + cartPrice);
        }
    }

    public void Wedding(@NonNull View view)
    {
        tv_amount_of_items = view.findViewById(R.id.tv_amount_of_items);
        btn_checkout = view.findViewById(R.id.btn_checkout);
        rv = view.findViewById(R.id.rvOrders);

    }
}
