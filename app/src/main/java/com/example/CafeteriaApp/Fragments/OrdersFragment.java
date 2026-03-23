package com.example.CafeteriaApp.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Adapters.OrdersAdapter;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Models.Order;
import com.example.CafeteriaApp.R;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private OrdersAdapter adapter;
    private List<Order> orderList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rvOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new OrdersAdapter(orderList);
        recyclerView.setAdapter(adapter);

        fetchOrders();
    }

    private void fetchOrders() {
        FBRef.downloadOrderForUser(FBRef.OrderFlag, new FBRef.FBListener() {
            @Override
            @SuppressWarnings("unchecked") // מעלים את האזהרה על ה-Casting
            public void onSuccess(Object data) {
                if (data instanceof List) {
                    List<Order> myOrders = (List<Order>) data;
                    if (adapter != null) {
                        adapter.setOrders(myOrders);
                    }
                }
            }

            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "שגיאה בטעינת הזמנות: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
