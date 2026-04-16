package com.example.CafeteriaApp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
import com.example.CafeteriaApp.Models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Main Activity of the application.
 * Handles the main navigation using a BottomNavigationView, a Navigation Drawer, and a FragmentManager.
 */
public class MainPage extends AppCompatActivity
{

    final FragmentManager fm = getSupportFragmentManager();
    Fragment menuFragment;
    Fragment cartFragment;
    Fragment ordersFragment;
    Fragment profileFragment;
    private Fragment activeFragment;
    
    private BottomNavigationView bottomNav;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        // Initialize Views
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        bottomNav = findViewById(R.id.bottomNav);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        if (topAppBar == null || bottomNav == null) return;

        // --- Start Real-time Notifications ---
        FBRef.observeOrderUpdates(this, new FBRef.FBListener() {
            @Override
            public void onSuccess(Object data) {}
            @Override
            public void onSuccess() {}
            @Override
            public void onFailure(String error) {
                Log.e("MainPage", "Order listener failed: " + error);
            }
        });

        // --- Toolbar Setup ---
        topAppBar.setTitle("");

        // Handle Search Action
        MenuItem searchItem = topAppBar.getMenu().findItem(R.id.action_search);
        if (searchItem != null)
        {
            SearchView searchView = (SearchView) searchItem.getActionView();
            if (searchView != null)
            {
                searchView.setQueryHint("Search menu...");
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
                {
                    @Override
                    public boolean onQueryTextSubmit(String query)
                    {
                        Toast.makeText(MainPage.this, "Searching: " + query,
                                       Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText)
                    {
                        return false;
                    }
                });
            }
        }

        // Handle Hamburger Navigation Click (Open Drawer)
        topAppBar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // --- Drawer Navigation Listener ---
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            } else if (itemId == R.id.nav_cart) {
                bottomNav.setSelectedItemId(R.id.nav_cart);
            } else if (itemId == R.id.nav_orders) {
                if (ordersFragment instanceof OrdersFragment) {
                    ((OrdersFragment) ordersFragment).setStartWithHistory(false);
                }
                bottomNav.setSelectedItemId(R.id.nav_orders);
            } else if (itemId == R.id.nav_profile) {
                bottomNav.setSelectedItemId(R.id.nav_profile);
            } else if (itemId == R.id.nav_history) {
                // Ensure Orders Fragment is loaded and set to History tab
                if (ordersFragment instanceof OrdersFragment) {
                    ((OrdersFragment) ordersFragment).setStartWithHistory(true);
                }
                bottomNav.setSelectedItemId(R.id.nav_orders);
            } else if (itemId == R.id.nav_logout) {
                logout();
            }
            
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        setupDrawerHeader();
        customizeDrawerMenu();

        // --- Fragment Management ---
        if (savedInstanceState == null)
        {
            menuFragment = new MenuFragment();
            cartFragment = new CartFragment();
            ordersFragment = new OrdersFragment();
            profileFragment = new ProfileFragment();
            activeFragment = menuFragment;

            fm.beginTransaction()
                .add(R.id.fragmentContainer, profileFragment, "4").hide(profileFragment)
                .add(R.id.fragmentContainer, ordersFragment, "3").hide(ordersFragment)
                .add(R.id.fragmentContainer, cartFragment, "2").hide(cartFragment)
                .add(R.id.fragmentContainer, menuFragment, "1")
                .commit();

        } else
        {
            menuFragment = fm.findFragmentByTag("1");
            cartFragment = fm.findFragmentByTag("2");
            ordersFragment = fm.findFragmentByTag("3");
            profileFragment = fm.findFragmentByTag("4");

            // Safe restore
            activeFragment = menuFragment;
            if (cartFragment != null && cartFragment.isVisible()) activeFragment = cartFragment;
            else if (ordersFragment != null && ordersFragment.isVisible()) activeFragment = ordersFragment;
            else if (profileFragment != null && profileFragment.isVisible()) activeFragment = profileFragment;
        }

        // --- Bottom Navigation Listener ---
        bottomNav.setOnItemSelectedListener(item ->
                                            {
                                                int itemId = item.getItemId();
                                                Fragment targetFragment = null;

                                                if (itemId == R.id.nav_home) targetFragment = menuFragment;
                                                else if (itemId == R.id.nav_cart) targetFragment = cartFragment;
                                                else if (itemId == R.id.nav_orders) targetFragment = ordersFragment;
                                                else if (itemId == R.id.nav_profile) targetFragment = profileFragment;

                                                if (targetFragment != null && targetFragment != activeFragment && !isFinishing())
                                                {
                                                    fm.beginTransaction().hide(activeFragment).show(targetFragment).commit();
                                                    activeFragment = targetFragment;
                                                    // Sync drawer selection
                                                    navigationView.setCheckedItem(itemId);
                                                    return true;
                                                }
                                                return targetFragment == activeFragment;
                                            });

        handleIntent(getIntent());
    }

    private void setupDrawerHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvUserName = headerView.findViewById(R.id.tvUserName);
        TextView tvUserEmail = headerView.findViewById(R.id.tvUserEmail);

        FirebaseUser currentUser = FBRef.refAuth.getCurrentUser();
        if (currentUser != null) {
            FBRef.refUsers.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        tvUserName.setText("שלום, " + user.getFirstName());
                        tvUserEmail.setText(user.getEmail());
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void customizeDrawerMenu() {
        Menu menu = navigationView.getMenu();
        MenuItem logoutItem = menu.findItem(R.id.nav_logout);
        if (logoutItem != null) {
            SpannableString s = new SpannableString(logoutItem.getTitle());
            s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
            logoutItem.setTitle(s);
            logoutItem.getIcon().setTint(Color.RED);
        }
    }

    private void logout() {
        FBRef.refAuth.signOut();
        FileManager.clearUserAuthentication(this);
        Intent intent = new Intent(this, LoginPage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra("OPEN_CART", false)) {
            navigateToCart();
        }
    }

    public void navigateToCart() {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_cart);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
