package com.example.CafeteriaApp.Fragments;

import android.os.Bundle;
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
import com.example.CafeteriaApp.Models.User;
import com.example.CafeteriaApp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for displaying user orders, separated into active and history tabs.
 * Manages real-time updates and synchronization with Firebase.
 */
public class OrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private OrdersAdapter adapter;
    private TabLayout tabLayout;
    private final List<Order> orderList = new ArrayList<>();
    private int userRole = User.ROLE_USER;

    private ValueEventListener currentOrdersListener;
    private ValueEventListener activeCountListener;
    private ValueEventListener historyCountListener;
    private boolean isHistoryTab = false;

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

        setupTabs();
        checkUserRoleAndInitialize();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isHistoryTab = (tab.getPosition() == 1);
                startListeningToOrders();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupTabs() {
        if (tabLayout.getTabAt(0) != null) {
            tabLayout.getTabAt(0).setText(getString(R.string.orders_active_tab, 0));
        }
        if (tabLayout.getTabAt(1) != null) {
            tabLayout.getTabAt(1).setText(getString(R.string.orders_history_tab, 0));
        }
    }

    /**
     * Public method to switch tabs from outside (like MainPage).
     * @param position 0 for Active, 1 for History.
     */
    public void switchToTab(int position) {
        if (tabLayout != null) {
            TabLayout.Tab tab = tabLayout.getTabAt(position);
            if (tab != null) {
                tab.select();
            }
        } else {
            isHistoryTab = (position == 1);
        }
    }

    /**
     * Verifies the user role and initializes the appropriate data listeners.
     */
    private void checkUserRoleAndInitialize() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FBRef.refUsers.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    userRole = user.getRole();
                    if (adapter != null) adapter.setCurrentUserRole(userRole);
                    startListeningToAllCounts();
                    startListeningToOrders();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Starts background listeners for tab badge counts.
     */
    private void startListeningToAllCounts() {
        activeCountListener = FBRef.listenToOrdersByRoleLive(userRole, false, new FBRef.FBListener() {
            @Override
            public void onSuccess(Object data) {
                if (data instanceof List) updateTabTitles(false, ((List<?>) data).size());
            }
            @Override public void onSuccess() {}
            @Override public void onFailure(String error) {}
        });

        historyCountListener = FBRef.listenToOrdersByRoleLive(userRole, true, new FBRef.FBListener() {
            @Override
            public void onSuccess(Object data) {
                if (data instanceof List) updateTabTitles(true, ((List<?>) data).size());
            }
            @Override public void onSuccess() {}
            @Override public void onFailure(String error) {}
        });
    }

    /**
     * Starts the main real-time listener for the currently selected tab.
     */
    private void startListeningToOrders() {
        stopOrdersListener();

        currentOrdersListener = FBRef.listenToOrdersByRoleLive(userRole, isHistoryTab, new FBRef.FBListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onSuccess(Object data) {
                if (data instanceof List) {
                    List<Order> orders = (List<Order>) data;
                    adapter.setOrders(orders, isHistoryTab);
                    updateTabTitles(isHistoryTab, orders.size());
                }
            }
            @Override public void onSuccess() {}
            @Override public void onFailure(String error) {
                if (isAdded()) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void stopOrdersListener() {
        if (currentOrdersListener != null) {
            if (userRole == User.ROLE_COOK || userRole == User.ROLE_MANAGER) {
                FBRef.refOrders.removeEventListener(currentOrdersListener);
            } else {
                String uid = FirebaseAuth.getInstance().getUid();
                if (uid != null) FBRef.getUserOrdersRef(uid, isHistoryTab).removeEventListener(currentOrdersListener);
            }
            currentOrdersListener = null;
        }
    }

    private void stopAllListeners() {
        stopOrdersListener();
        removeListenerByRole(activeCountListener, false);
        activeCountListener = null;
        removeListenerByRole(historyCountListener, true);
        historyCountListener = null;
    }

    private void removeListenerByRole(ValueEventListener listener, boolean isHistory) {
        if (listener == null) return;
        if (userRole == User.ROLE_COOK || userRole == User.ROLE_MANAGER) {
            FBRef.refOrders.removeEventListener(listener);
        } else {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid != null) FBRef.getUserOrdersRef(uid, isHistory).removeEventListener(listener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAllListeners();
    }

    /**
     * Updates tab labels with the current count.
     * @param isHistory Tab type.
     * @param count Order count.
     */
    private void updateTabTitles(boolean isHistory, int count) {
        if (!isAdded() || tabLayout == null) return;
        TabLayout.Tab tab = tabLayout.getTabAt(isHistory ? 1 : 0);
        if (tab != null) {
            tab.setText(getString(isHistory ? R.string.orders_history_tab : R.string.orders_active_tab, count));
        }
    }
}
