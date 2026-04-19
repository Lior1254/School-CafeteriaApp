package com.example.CafeteriaApp.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Adapters.ShoppingCartAdapter;
import com.example.CafeteriaApp.CustomizeItemActivity;
import com.example.CafeteriaApp.Helpers.FileManager;
import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.PaymentActivity;
import com.example.CafeteriaApp.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Fragment for displaying the user's shopping cart.
 */
public class CartFragment extends Fragment
{
    private static final int PAYMENT_REQUEST_CODE = 1001;

    private RecyclerView rvCartItems;
    private TextView tvTotal;
    private Button btnCheckout;
    private Spinner spinPickupTime;
    private TextInputEditText etGeneralNotes;
    private ShoppingCartAdapter adapter;
    private List<Product> cartItems = new ArrayList<>();
    private double currentTotal = 0;

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
        setupTimeSpinner();
        loadCartData();
    }

    /**
     * Local method to check internet connection within the fragment.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = (cm != null) ? cm.getActiveNetworkInfo() : null;
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("שגיאת חיבור")
                    .setMessage("פעולה זו דורשת חיבור לאינטרנט כדי לבצע תשלום.")
                    .setPositiveButton("Ok", null)
                    .show();
        }
        return isConnected;
    }

    private void initializeViews(View view)
    {
        rvCartItems = view.findViewById(R.id.rvOrders);
        tvTotal = view.findViewById(R.id.tv_total);
        btnCheckout = view.findViewById(R.id.btn_checkout);
        spinPickupTime = view.findViewById(R.id.spin_pickup_time);
        etGeneralNotes = view.findViewById(R.id.etGeneralNotes);

        if (etGeneralNotes != null) {
            etGeneralNotes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    FileManager.saveGeneralNotes(requireContext(), s.toString().trim());
                }
            });
        }

        btnCheckout.setOnClickListener(v -> {
            // 1. בדיקת אינטרנט
            if (!isNetworkAvailable()) {
                return;
            }

            // 2. בדיקת סל ריק
            if (cartItems.isEmpty() ) {
                Toast.makeText(getContext(), "הסל שלך ריק", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 3. בדיקת זמן איסוף
            String selectedTime = spinPickupTime.getSelectedItem().toString();
            if (selectedTime.equals("לא ניתן להזמין להיום")) {
                Toast.makeText(getContext(), "הקפיטריה סגורה כעת", Toast.LENGTH_SHORT).show();
                return;
            }

            // מעבר לתשלום
            Intent intent = new Intent(getActivity(), PaymentActivity.class);
            intent.putExtra("total_amount", currentTotal);
            intent.putExtra("pickup_time", selectedTime);
            intent.putExtra("general_notes", FileManager.loadGeneralNotes(requireContext()));
            startActivityForResult(intent, PAYMENT_REQUEST_CODE);
        });
    }

    private void setupTimeSpinner() {
        List<String> timeSlots = generateTimeSlots();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, timeSlots);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinPickupTime.setAdapter(spinnerAdapter);
    }

    private List<String> generateTimeSlots() {
        List<String> slots = new ArrayList<>();
        Calendar now = Calendar.getInstance();

        addBreakTime(slots, now, 9, 45, "(הפסקה ראשונה)");
        addBreakTime(slots, now, 11, 30, "(הפסקה שנייה)");
        addBreakTime(slots, now, 13, 30, "(הפסקת צהריים)");

        for (int hour = 8; hour <= 16; hour++) {
            for (int minute = 0; minute < 60; minute += 15) {
                if (hour == 16 && minute > 0) continue;
                Calendar slotTime = Calendar.getInstance();
                slotTime.set(Calendar.HOUR_OF_DAY, hour);
                slotTime.set(Calendar.MINUTE, minute);
                if (slotTime.after(now)) {
                    String timeStr = String.format("%02d:%02d", hour, minute);
                    if (!isTimeInSlots(slots, timeStr)) {
                        slots.add(timeStr);
                    }
                }
            }
        }

        if (slots.isEmpty()) {
            slots.add("לא ניתן להזמין להיום");
        }
        return slots;
    }

    private void addBreakTime(List<String> slots, Calendar now, int hour, int minute, String label) {
        Calendar breakTime = Calendar.getInstance();
        breakTime.set(Calendar.HOUR_OF_DAY, hour);
        breakTime.set(Calendar.MINUTE, minute);
        if (breakTime.after(now)) {
            slots.add(String.format("%02d:%02d %s", hour, minute, label));
        }
    }

    private boolean isTimeInSlots(List<String> slots, String time) {
        for (String slot : slots) {
            if (slot.startsWith(time)) return true;
        }
        return false;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            clearCart();
        }
    }

    private void clearCart() {
        cartItems.clear();
        FileManager.saveCart(requireContext(), new ArrayList<>());
        FileManager.saveGeneralNotes(requireContext(), "");
        if (etGeneralNotes != null) {
            etGeneralNotes.setText("");
        }
        adapter.notifyDataSetChanged();
        updateOrderSummary();
        Toast.makeText(getContext(), "תודה רבה! ההזמנה בוצעה בהצלחה.", Toast.LENGTH_LONG).show();
    }

    private void setupRecyclerView()
    {
        rvCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        adapter = new ShoppingCartAdapter(requireContext(), cartItems, new ShoppingCartAdapter.OnQuantityChangeListener() {
            @Override
            public void onQuantityChange(int position, int newQuantity) {
                if (newQuantity <= 0) {
                    cartItems.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, cartItems.size());
                } else {
                    Product item = cartItems.get(position);
                    item.setAmount(newQuantity);
                    adapter.notifyItemChanged(position);
                }
                FileManager.saveCart(requireContext(), cartItems);
                updateOrderSummary();
            }

            @Override
            public void onEditClick(Product item, int position) {
                Intent intent = new Intent(requireContext(), CustomizeItemActivity.class);
                intent.putExtra("item", item);
                intent.putExtra("position", position);
                intent.putExtra("isEditMode", true);
                startActivity(intent);
            }
        });
        
        rvCartItems.setAdapter(adapter);
    }

    private void loadCartData()
    {
        cartItems.clear();
        List<Product> loadedItems = FileManager.loadCart(requireContext());
        if (loadedItems != null) {
            cartItems.addAll(loadedItems);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        
        if (etGeneralNotes != null) {
            etGeneralNotes.setText(FileManager.loadGeneralNotes(requireContext()));
        }

        updateOrderSummary();
    }

    private void updateOrderSummary()
    {
        currentTotal = 0;
        for (Product item : cartItems)
        {
            currentTotal += item.getPrice() * item.getAmount();
        }
        if (tvTotal != null) {
            tvTotal.setText(String.format("₪%.2f", currentTotal));
        }
        if (btnCheckout != null) {
            btnCheckout.setText(String.format("%s - ₪%.2f", getString(R.string.cart_proceed_to_checkout), currentTotal));
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadCartData();
        setupTimeSpinner();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadCartData();
        }
    }
}
