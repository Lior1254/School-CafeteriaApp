package com.example.CafeteriaApp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

public class OrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private OrdersAdapter adapter;
    private TabLayout tabLayout;
    private TextView tvTitle;
    private List<Order> orderList = new ArrayList<>();
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
        tvTitle = view.findViewById(R.id.tvOrdersTitle);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OrdersAdapter(orderList);
        recyclerView.setAdapter(adapter);

        if (tabLayout.getTabAt(0) != null) tabLayout.getTabAt(0).setText("פעילות (0)");
        if (tabLayout.getTabAt(1) != null) tabLayout.getTabAt(1).setText("היסטוריה (0)");

        checkUserRoleAndSetTitle();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isHistoryTab = (tab.getPosition() == 1);
                startListeningToOrders();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * Public method to switch tabs from outside (like MainPage)
     */
    public void switchToTab(int position) {
        if (tabLayout != null) {
            TabLayout.Tab tab = tabLayout.getTabAt(position);
            if (tab != null) {
                tab.select();
            }
        } else {
            // If tabLayout is not yet created, save it for later
            isHistoryTab = (position == 1);
        }
    }

    private void checkUserRoleAndSetTitle() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FBRef.refUsers.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    userRole = user.getRole();
                    if (userRole == User.ROLE_COOK || userRole == User.ROLE_MANAGER) {
                        if (tvTitle != null) tvTitle.setText("הזמנות");
                    }
                    
                    // IMPORTANT: Pass the role to the adapter so it knows how to handle clicks
                    if (adapter != null) {
                        adapter.setCurrentUserRole(userRole);
                    }

                    // Start listeners only after we know the role
                    startListeningToAllCounts();
                    startListeningToOrders();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void startListeningToAllCounts() {
        // Active count listener
        activeCountListener = FBRef.listenToOrdersByRoleLive(userRole, false, new FBRef.FBListener() {
            @Override
            public void onSuccess(Object data) {
                if (data instanceof List) updateTabTitles(false, ((List<?>) data).size());
            }
            @Override public void onSuccess() {}
            @Override public void onFailure(String error) {}
        });

        // History count listener
        historyCountListener = FBRef.listenToOrdersByRoleLive(userRole, true, new FBRef.FBListener() {
            @Override
            public void onSuccess(Object data) {
                if (data instanceof List) updateTabTitles(true, ((List<?>) data).size());
            }
            @Override public void onSuccess() {}
            @Override public void onFailure(String error) {}
        });
    }

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
                if (getContext() != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
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
        if (activeCountListener != null) {
            if (userRole == User.ROLE_COOK || userRole == User.ROLE_MANAGER) FBRef.refOrders.removeEventListener(activeCountListener);
            else {
                String uid = FirebaseAuth.getInstance().getUid();
                if (uid != null) FBRef.getUserOrdersRef(uid, false).removeEventListener(activeCountListener);
            }
            activeCountListener = null;
        }
        if (historyCountListener != null) {
            if (userRole == User.ROLE_COOK || userRole == User.ROLE_MANAGER) FBRef.refOrders.removeEventListener(historyCountListener);
            else {
                String uid = FirebaseAuth.getInstance().getUid();
                if (uid != null) FBRef.getUserOrdersRef(uid, true).removeEventListener(historyCountListener);
            }
            historyCountListener = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAllListeners();
    }

    private void updateTabTitles(boolean isHistory, int count) {
        if (!isAdded()) return;
        TabLayout.Tab activeTab = tabLayout.getTabAt(0);
        TabLayout.Tab historyTab = tabLayout.getTabAt(1);

        if (isHistory && historyTab != null) {
            historyTab.setText("היסטוריה (" + count + ")");
        } else if (!isHistory && activeTab != null) {
            activeTab.setText("פעילות (" + count + ")");
        }
    }
}
