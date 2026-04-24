package com.example.CafeteriaApp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.CafeteriaApp.Authentication.LoginPage;
import com.example.CafeteriaApp.Fragments.CartFragment;
import com.example.CafeteriaApp.Fragments.MenuFragment;
import com.example.CafeteriaApp.Fragments.OrdersFragment;
import com.example.CafeteriaApp.Fragments.ProfileFragment;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Helpers.FileManager;
import com.example.CafeteriaApp.Helpers.NotificationHelper;
import com.example.CafeteriaApp.Models.Order;
import com.example.CafeteriaApp.Models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Main Activity of the application. Manages the primary navigation drawer,
 * bottom navigation, and handles real-time notifications for order status updates.
 * Also dynamically updates the top action bar title and side drawer header based on the active user.
 */
public class MainPage extends AppCompatActivity
{
    private final FragmentManager fm = getSupportFragmentManager();
    private Fragment menuFragment, cartFragment, ordersFragment, profileFragment;
    private Fragment activeFragment;
    private BottomNavigationView bottomNav;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView tvPageTitle;
    private int userRole = User.ROLE_USER;
    private ValueEventListener userDetailsListener;
    
    /** Keeps track of previous status to trigger notifications only on change */
    private final Map<String, String> orderStatusMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        bottomNav = findViewById(R.id.bottomNav);
        tvPageTitle = findViewById(R.id.tvPageTitle);

        if (topAppBar == null || bottomNav == null) return;

        topAppBar.setTitle("");
        topAppBar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        menuFragment = new MenuFragment();
        cartFragment = new CartFragment();
        ordersFragment = new OrdersFragment();
        profileFragment = new ProfileFragment();

        requestNotificationPermission();
        setupNavigation();
        checkUserRoleAndSetupNavigation();
        startNotificationListener();
        
        // Ensure color is applied after the menu is fully created
        navigationView.post(this::customizeLogoutItem);
    }

    /**
     * Requests POST_NOTIFICATIONS permission for Android 13+.
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    /**
     * Listens for changes in the current user's orders and triggers notifications
     * for each status transition.
     */
    private void startNotificationListener() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FBRef.getUserOrdersRef(uid, false).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Order order = orderSnap.getValue(Order.class);
                    if (order != null) {
                        String orderId = order.getOrderId();
                        String currentStatus = order.getOrderStatus();
                        
                        if (orderStatusMap.containsKey(orderId)) {
                            String oldStatus = orderStatusMap.get(orderId);
                            if (!currentStatus.equals(oldStatus)) {
                                sendStatusNotification(order);
                            }
                        }
                        orderStatusMap.put(orderId, currentStatus);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Sends a personalized notification based on the order status.
     * @param order The updated order object.
     */
    private void sendStatusNotification(Order order) {
        String message = "";
        int notificationId = order.getOrderId().hashCode();
        String status = order.getOrderStatus();

        switch (status) {
            case Order.STATUS_PREPARING:
                String time = "";
                if (order.getRequestedTime() != null && order.getRequestedTime().contains(" ")) {
                    time = order.getRequestedTime().split(" ")[1].substring(0, 5);
                }
                message = getString(R.string.notif_order_preparing, order.getOrderCode(), time);
                break;
            case Order.STATUS_READY:
                message = getString(R.string.notif_order_ready, order.getOrderCode());
                break;
            case Order.STATUS_COLLECTED:
                message = getString(R.string.notif_order_collected);
                break;
        }

        if (!message.isEmpty()) {
            NotificationHelper.showOrderStatus(this, message, notificationId);
        }
    }

    /**
     * Customizes the logout menu item appearance (White text/icon on Red button shape).
     */
    private void customizeLogoutItem() {
        if (navigationView == null) return;

        MenuItem logoutItem = navigationView.getMenu().findItem(R.id.nav_logout);
        if (logoutItem != null) {
            // Set text to white
            SpannableString s = new SpannableString(logoutItem.getTitle());
            s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
            logoutItem.setTitle(s);

            // Set icon to white
            if (logoutItem.getIcon() != null) {
                logoutItem.getIcon().setTint(Color.WHITE);
            }

            // Find the specific View for nav_logout and style it as a red button
            navigationView.post(() -> {
                View logoutView = findViewById(R.id.nav_logout);
                if (logoutView != null) {
                    // Apply a red background tint to the item's background shape
                    // This matches the "sky" (blue) shape of other items, but in red.
                    int redColor = ContextCompat.getColor(this, R.color.logout_red);
                    logoutView.setBackgroundTintList(ColorStateList.valueOf(redColor));
                }
            });
        }
    }

    /**
     * Sets up the selection listeners for both bottom and drawer navigation.
     */
    private void setupNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            selectFragment(item.getItemId());
            return true;
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_logout) {
                logout();
            } else if (itemId == R.id.nav_history) {
                selectFragment(R.id.nav_orders);
                bottomNav.setSelectedItemId(R.id.nav_orders);
                if (ordersFragment instanceof OrdersFragment) ((OrdersFragment) ordersFragment).switchToTab(1);
            } else {
                selectFragment(itemId);
                bottomNav.setSelectedItemId(itemId);
                if (itemId == R.id.nav_orders && ordersFragment instanceof OrdersFragment) ((OrdersFragment) ordersFragment).switchToTab(0);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    /**
     * Switches to the Orders tab and resets to Active Orders sub-tab.
     * Can be called from Fragments.
     */
    public void switchToOrdersTab() {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_orders);
            if (ordersFragment instanceof OrdersFragment) {
                ((OrdersFragment) ordersFragment).switchToTab(0);
            }
        }
    }

    /**
     * Switches the active fragment and updates the Top Bar title based on the selection.
     * @param itemId The resource ID of the selected menu item.
     */
    private void selectFragment(int itemId) {
        Fragment targetFragment = null;
        String title = "Pick-Up";

        if (itemId == R.id.nav_home) {
            targetFragment = menuFragment;
            title = "התפריט שלנו";
        } else if (itemId == R.id.nav_cart) {
            targetFragment = cartFragment;
            title = "סל הקניות";
        } else if (itemId == R.id.nav_orders || itemId == R.id.nav_history) {
            targetFragment = ordersFragment;
            title = (userRole == User.ROLE_COOK || userRole == User.ROLE_MANAGER) ? "ניהול הזמנות" : "ההזמנות שלי";
        } else if (itemId == R.id.nav_profile) {
            targetFragment = profileFragment;
            title = "אזור אישי";
        }

        if (tvPageTitle != null) {
            tvPageTitle.setText(title);
        }

        if (targetFragment != null && targetFragment != activeFragment && !isFinishing()) {
            fm.beginTransaction().hide(activeFragment).show(targetFragment).commit();
            activeFragment = targetFragment;
        }
    }

    /**
     * Signs out the current user and returns to the Login page.
     */
    private void logout() {
        FileManager.clearUserAuthentication(this);
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginPage.class));
        finish();
    }

    /**
     * Verifies the user role from the database and adjusts the navigation UI.
     * Uses a live listener to update the UI on profile changes.
     */
    private void checkUserRoleAndSetupNavigation() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        userDetailsListener = FBRef.refUsers.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    boolean roleChanged = (userRole != user.getRole());
                    userRole = user.getRole();
                    
                    // Update side menu header with user details (Real-time)
                    updateNavHeader(user);

                    // Only reset navigation if it's the first load or role actually changed
                    if (activeFragment == null || roleChanged) {
                        if (userRole == User.ROLE_COOK || userRole == User.ROLE_MANAGER) {
                            bottomNav.getMenu().findItem(R.id.nav_orders).setTitle("הזמנות");
                            bottomNav.getMenu().findItem(R.id.nav_home).setVisible(false);
                            bottomNav.getMenu().findItem(R.id.nav_cart).setVisible(false);
                            if (activeFragment == null) setupFragments(ordersFragment);
                            bottomNav.setSelectedItemId(R.id.nav_orders);
                            if (tvPageTitle != null) tvPageTitle.setText("ניהול הזמנות");
                        } else {
                            bottomNav.getMenu().findItem(R.id.nav_orders).setTitle("הזמנות שלי");
                            if (activeFragment == null) setupFragments(menuFragment);
                            bottomNav.setSelectedItemId(R.id.nav_home);
                            if (tvPageTitle != null) tvPageTitle.setText("התפריט שלנו");
                        }
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { 
                if (activeFragment == null) setupFragments(menuFragment); 
            }
        });
    }

    /**
     * Updates the side navigation drawer header with user name and email.
     * @param user The current user model.
     */
    private void updateNavHeader(User user) {
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView tvUserName = headerView.findViewById(R.id.tvUserName);
            TextView tvUserEmail = headerView.findViewById(R.id.tvUserEmail);

            if (tvUserName != null) {
                tvUserName.setText("שלום, " + user.getName());
            }
            if (tvUserEmail != null) {
                tvUserEmail.setText(user.getEmail());
            }
        }
    }

    private void setupFragments(Fragment defaultFragment) {
        activeFragment = defaultFragment;
        fm.beginTransaction()
            .add(R.id.fragmentContainer, profileFragment, "4").hide(profileFragment)
            .add(R.id.fragmentContainer, ordersFragment, "3").hide(ordersFragment)
            .add(R.id.fragmentContainer, cartFragment, "2").hide(cartFragment)
            .add(R.id.fragmentContainer, menuFragment, "1").hide(menuFragment)
            .show(defaultFragment)
            .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDetailsListener != null) {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid != null) FBRef.refUsers.child(uid).removeEventListener(userDetailsListener);
        }
    }
}
