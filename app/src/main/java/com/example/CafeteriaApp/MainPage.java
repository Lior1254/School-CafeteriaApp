package com.example.CafeteriaApp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
    private int userRole = User.ROLE_USER;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        bottomNav = findViewById(R.id.bottomNav);

        if (topAppBar == null || bottomNav == null) return;

        topAppBar.setTitle("");

        topAppBar.setNavigationOnClickListener(v ->
        {
            if (isFinishing()) return;
            PopupMenu popup = new PopupMenu(MainPage.this, v);
            popup.getMenuInflater().inflate(R.menu.menu_bottom_nav, popup.getMenu());

            Menu menu = popup.getMenu();
            if (userRole == User.ROLE_COOK || userRole == User.ROLE_MANAGER) {
                if (menu.findItem(R.id.nav_home) != null) menu.findItem(R.id.nav_home).setVisible(false);
                if (menu.findItem(R.id.nav_cart) != null) menu.findItem(R.id.nav_cart).setVisible(false);
            }

            popup.setOnMenuItemClickListener(item ->
            {
                bottomNav.setSelectedItemId(item.getItemId());
                return true;
            });
            popup.show();
        });

        menuFragment = new MenuFragment();
        cartFragment = new CartFragment();
        ordersFragment = new OrdersFragment();
        profileFragment = new ProfileFragment();

        checkUserRoleAndSetupNavigation();
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
    }
}
