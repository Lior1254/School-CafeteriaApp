package com.example.CafeteriaApp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.CafeteriaApp.Fragments.CartFragment;
import com.example.CafeteriaApp.Fragments.MenuFragment;
import com.example.CafeteriaApp.Fragments.OrdersFragment;
import com.example.CafeteriaApp.Fragments.ProfileFragment;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MainPage extends AppCompatActivity
{
    final FragmentManager fm = getSupportFragmentManager();
    Fragment menuFragment, cartFragment, ordersFragment, profileFragment;
    private Fragment activeFragment;
    private BottomNavigationView bottomNav;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private int userRole = User.ROLE_USER;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        bottomNav = findViewById(R.id.bottomNav);

        if (topAppBar == null || bottomNav == null) return;

        topAppBar.setTitle("");

        // Open drawer on menu icon click
        topAppBar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        menuFragment = new MenuFragment();
        cartFragment = new CartFragment();
        ordersFragment = new OrdersFragment();
        profileFragment = new ProfileFragment();

        setupNavigation();
        checkUserRoleAndSetupNavigation();
        
        // Apply custom style to logout item
        navigationView.post(this::customizeLogoutItem);
    }

    private void customizeLogoutItem() {
        MenuItem logoutItem = navigationView.getMenu().findItem(R.id.nav_logout);
        if (logoutItem != null) {
            // Set white text
            SpannableString s = new SpannableString(logoutItem.getTitle());
            s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
            logoutItem.setTitle(s);
            
            // Set white icon
            if (logoutItem.getIcon() != null) {
                logoutItem.getIcon().setTint(Color.WHITE);
            }

            // Set red background to the item view
            View logoutView = findViewById(R.id.nav_logout);
            if (logoutView != null) {
                logoutView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.logout_red)));
            }
        }
    }

    private void setupNavigation() {
        // Bottom Nav Listener
        bottomNav.setOnItemSelectedListener(item ->
        {
            int itemId = item.getItemId();
            selectFragment(itemId);
            return true;
        });

        // Drawer Nav Listener
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_logout) {
                logout();
            } else if (itemId == R.id.nav_history) {
                selectFragment(R.id.nav_orders);
                bottomNav.setSelectedItemId(R.id.nav_orders);
                if (ordersFragment instanceof OrdersFragment) {
                    ((OrdersFragment) ordersFragment).switchToTab(1);
                }
            } else {
                selectFragment(itemId);
                bottomNav.setSelectedItemId(itemId);
                
                if (itemId == R.id.nav_orders && ordersFragment instanceof OrdersFragment) {
                    ((OrdersFragment) ordersFragment).switchToTab(0);
                }
            }
            
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void selectFragment(int itemId) {
        Fragment targetFragment = null;
        if (itemId == R.id.nav_home) targetFragment = menuFragment;
        else if (itemId == R.id.nav_cart) targetFragment = cartFragment;
        else if (itemId == R.id.nav_orders || itemId == R.id.nav_history) targetFragment = ordersFragment;
        else if (itemId == R.id.nav_profile) targetFragment = profileFragment;

        if (targetFragment != null && targetFragment != activeFragment && !isFinishing())
        {
            fm.beginTransaction().hide(activeFragment).show(targetFragment).commit();
            activeFragment = targetFragment;
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void checkUserRoleAndSetupNavigation() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FBRef.refUsers.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    userRole = user.getRole();
                    if (userRole == User.ROLE_COOK || userRole == User.ROLE_MANAGER) {
                        bottomNav.getMenu().findItem(R.id.nav_orders).setTitle("הזמנות");
                        bottomNav.getMenu().findItem(R.id.nav_home).setVisible(false);
                        bottomNav.getMenu().findItem(R.id.nav_cart).setVisible(false);
                        setupFragments(ordersFragment);
                        bottomNav.setSelectedItemId(R.id.nav_orders);
                    } else {
                        bottomNav.getMenu().findItem(R.id.nav_orders).setTitle("הזמנות שלי");
                        setupFragments(menuFragment);
                        bottomNav.setSelectedItemId(R.id.nav_home);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setupFragments(menuFragment);
            }
        });
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
}
