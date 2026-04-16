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
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private OrdersAdapter adapter;
    private TabLayout tabLayout;
    private List<Order> orderList = new ArrayList<>();
    private boolean startWithHistory = false;

    public void setStartWithHistory(boolean startWithHistory) {
        this.startWithHistory = startWithHistory;
        if (tabLayout != null) {
            TabLayout.Tab tab = tabLayout.getTabAt(startWithHistory ? 1 : 0);
            if (tab != null) tab.select();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rvOrders);
        tabLayout = view.findViewById(R.id.tabLayoutOrders);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OrdersAdapter(orderList);
        recyclerView.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // If position is 1, it's History, otherwise it's Active Order
                boolean isHistory = (tab.getPosition() == 1);
                fetchOrders(isHistory);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Check if we should start with history
        if (startWithHistory) {
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            if (tab != null) tab.select();
            fetchOrders(true);
        } else {
            fetchOrders(false);
        }
    }

    private void fetchOrders(boolean isHistory)
    {
        if (adapter != null)
        {
            adapter.setOrders(new ArrayList<>(), isHistory);
        }

        FBRef.downloadOrderForUser(isHistory, new FBRef.FBListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onSuccess(Object data) {
                if (data instanceof List) {
                    List<Order> myOrders = (List<Order>) data;
                    if (adapter != null) {
                        adapter.setOrders(myOrders, isHistory);
                    }
                }
            }

            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "שגיאה בטעינת נתונים: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
