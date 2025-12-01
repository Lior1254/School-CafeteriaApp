package com.example.CafeteriaApp;

import android.os.Bundle;
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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainPage extends AppCompatActivity {

    final FragmentManager fm = getSupportFragmentManager();
    Fragment menuFragment;
    Fragment cartFragment;
    Fragment ordersFragment;
    Fragment profileFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        // Initialize Views
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // --- Toolbar Setup ---
        // We do NOT call setSupportActionBar(topAppBar) to keep total control
        // and prevent default Activity title behavior.
        topAppBar.setTitle("");
        
        // Handle Search Action
        // The menu is inflated from XML (app:menu), so we can find the item immediately.
        MenuItem searchItem = topAppBar.getMenu().findItem(R.id.action_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            if (searchView != null) {
                searchView.setQueryHint("חפש בתפריט...");
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        Toast.makeText(MainPage.this, "מחפש: " + query, Toast.LENGTH_SHORT).show();
                        // Here you can implement transition to a search fragment if needed
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });
            }
        }

        // Handle Hamburger Navigation Click (Opens PopupMenu)
        topAppBar.setNavigationOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainPage.this, v);
            popup.getMenuInflater().inflate(R.menu.menu_bottom_nav, popup.getMenu());
            
            popup.setOnMenuItemClickListener(item -> {
                // Sync with BottomNavigationView
                bottomNav.setSelectedItemId(item.getItemId());
                return true;
            });
            popup.show();
        });

        // --- Fragment Management ---
        if (savedInstanceState == null) {
            menuFragment = new MenuFragment();
            cartFragment = new CartFragment();
            ordersFragment = new OrdersFragment();
            profileFragment = new ProfileFragment();
            activeFragment = menuFragment;

            // Add all fragments hidden, except the first one
            fm.beginTransaction().add(R.id.fragmentContainer, profileFragment, "4").hide(profileFragment).commit();
            fm.beginTransaction().add(R.id.fragmentContainer, ordersFragment, "3").hide(ordersFragment).commit();
            fm.beginTransaction().add(R.id.fragmentContainer, cartFragment, "2").hide(cartFragment).commit();
            fm.beginTransaction().add(R.id.fragmentContainer, menuFragment, "1").commit();

        } else {
            menuFragment = fm.findFragmentByTag("1");
            cartFragment = fm.findFragmentByTag("2");
            ordersFragment = fm.findFragmentByTag("3");
            profileFragment = fm.findFragmentByTag("4");

            // Restore active fragment state
            if (menuFragment != null && menuFragment.isVisible()) activeFragment = menuFragment;
            else if (cartFragment != null && cartFragment.isVisible()) activeFragment = cartFragment;
            else if (ordersFragment != null && ordersFragment.isVisible()) activeFragment = ordersFragment;
            else if (profileFragment != null && profileFragment.isVisible()) activeFragment = profileFragment;
        }

        // --- Bottom Navigation Listener ---
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment targetFragment = null;

            if (itemId == R.id.nav_home) {
                targetFragment = menuFragment;
            } else if (itemId == R.id.nav_cart) {
                targetFragment = cartFragment;
            } else if (itemId == R.id.nav_orders) {
                targetFragment = ordersFragment;
            } else if (itemId == R.id.nav_profile) {
                targetFragment = profileFragment;
            }

            if (targetFragment != null && targetFragment != activeFragment) {
                fm.beginTransaction().hide(activeFragment).show(targetFragment).commit();
                activeFragment = targetFragment;
                return true;
            }
            return targetFragment == activeFragment;
        });
    }
}