package com.example.CafeteriaApp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Adapters.ShoppingCartAdapter;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying the user's shopping cart.
 */
public class CartFragment extends Fragment
{
    private RecyclerView rvCartItems;
    private TextView tvSubtotal, tvTaxes, tvTotal;
    private Button btnCheckout;
    private ShoppingCartAdapter adapter;
    private final List<Product> cartItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        loadCartData();
    }

    /**
     * Initializes the views from the layout.
     */
    private void initializeViews(View view)
    {
        rvCartItems = view.findViewById(R.id.rvOrders);
        tvTotal = view.findViewById(R.id.tv_total);
        btnCheckout = view.findViewById(R.id.btn_checkout);
    }

    /**
     * Sets up the RecyclerView with its adapter.
     */
    private void setupRecyclerView()
    {
        rvCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ShoppingCartAdapter(cartItems, (position, newQuantity) -> {
            // Handle quantity changes
            Product item = cartItems.get(position);
            item.setAmount(newQuantity);
            // TODO: Update the item in Firebase if needed
            adapter.notifyItemChanged(position);
            updateOrderSummary();
        });
        rvCartItems.setAdapter(adapter);
    }

    /**
     * Loads cart data from Firebase Realtime Database.
     */
    private void loadCartData()
    {
        String uid = FBRef.refAuth.getUid();
        if (uid == null) return;

        FBRef.refCarts.child(uid).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                cartItems.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren())
                {
                    Product product = itemSnapshot.getValue(Product.class);
                    if (product != null)
                    {
                        cartItems.add(product);
                    }
                }
                adapter.notifyDataSetChanged();
                updateOrderSummary();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(getContext(), "Failed to load cart.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Recalculates and updates the order summary fields (subtotal, total, etc.).
     */
    private void updateOrderSummary()
    {
        double total = 0;
        for (Product item : cartItems)
        {
            total += item.getPrice() * item.getAmount();
        }

        tvTotal.setText(String.format("₪%.2f", total));

        btnCheckout.setText(String.format("%s - ₪%.2f", getString(R.string.cart_proceed_to_checkout), total));
    }
}
