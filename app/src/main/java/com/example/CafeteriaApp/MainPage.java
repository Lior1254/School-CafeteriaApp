package com.example.CafeteriaApp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.CafeteriaApp.Fragments.CartFragment;
import com.example.CafeteriaApp.Fragments.MenuFragment;
import com.example.CafeteriaApp.Fragments.OrdersFragment;
import com.example.CafeteriaApp.Fragments.ProfileFragment;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main Activity of the application.
 * Handles the main navigation using a BottomNavigationView and a FragmentManager.
 * Displays Menu, Cart, Orders, and Profile fragments.
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        // Initialize Views
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        bottomNav = findViewById(R.id.bottomNav);

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

        // Handle Hamburger Navigation Click
        topAppBar.setNavigationOnClickListener(v ->
                                               {
                                                   if (isFinishing()) return;
                                                   PopupMenu popup = new PopupMenu(MainPage.this, v);
                                                   popup.getMenuInflater().inflate(
                                                           R.menu.menu_bottom_nav, popup.getMenu());

                                                   popup.setOnMenuItemClickListener(item ->
                                                                                    {
                                                                                        bottomNav.setSelectedItemId(
                                                                                                item.getItemId());
                                                                                        return true;
                                                                                    });
                                                   popup.show();
                                               });

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

        // --- Navigation Listener ---
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
                                                    return true;
                                                }
                                                return targetFragment == activeFragment;
                                            });

        // Check for Intent to navigate to a specific tab
        handleIntent(getIntent());
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

    /**
     * Programmatically switches to the Cart tab.
     */
    public void navigateToCart() {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_cart);
        }
    }
}
