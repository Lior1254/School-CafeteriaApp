package com.example.CafeteriaApp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Models.Order;
import com.example.CafeteriaApp.Adapters.OrdersAdapter;
import com.example.CafeteriaApp.R;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment
{

    private RecyclerView recyclerView;
    private OrdersAdapter adapter;
    private List<Order> orderList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        // 1. ניפוח (inflate) של קובץ ה-XML של הפרגמנט
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        // 2. מציאת ה-RecyclerView בקובץ ה-XML
        recyclerView = view.findViewById(R.id.rvOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 3. יצירת רשימת הזמנות לדוגמה (בפועל, הנתונים יגיעו מבסיס נתונים)
        loadDummyData();

        // 4. יצירת ה-Adapter וחיבורו ל-RecyclerView
        adapter = new OrdersAdapter(requireContext(), orderList);
        recyclerView.setAdapter(adapter);
    }


    private void loadDummyData()
    {
        orderList = new ArrayList<>();

        orderList.add(new Order("1024", "ארוחת ריב, צ'יפס, קולה זירו", 77.00, 1, "התקבלה", "12:45",
                                "12:40", "12:35"));
        orderList.add(
                new Order("1025", "פיצה מרגריטה", 55.00, 2, "בהכנה", "13:00", "12:55", "12:48"));
        orderList.add(new Order("1026", "סלט קיסר, מים", 48.50, 3, "מוכנה לאיסוף", "13:15", "13:10",
                                "13:02"));
        orderList.add(
                new Order("1027", "קפה הפוך ומאפה", 24.00, 4, "נאספה", "13:20", "13:18", "13:15"));
    }

}