package com.example.CafeteriaApp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Adapters.CategoryAdapter;
import com.example.CafeteriaApp.Adapters.CustomProductAdapterRV;
import com.example.CafeteriaApp.CustomizeItemActivity;
import com.example.CafeteriaApp.Models.Addon;
import com.example.CafeteriaApp.Models.CategoryItem;
import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.R;

import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment {
    private RecyclerView RV_items, RV_categories;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i,
                             @Nullable ViewGroup c,
                             @Nullable Bundle b) {
        return i.inflate(R.layout.fragment_menu, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        RV_items = v.findViewById(R.id.RV_items);
        RV_categories = v.findViewById(R.id.RV_categories);

        RV_items.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Set up horizontal layout manager for categories
        RV_categories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // List of all products
        List<Product> allProducts = new ArrayList<>();
        // List of categories
        List<CategoryItem> categories = new ArrayList<>();

        // Placeholders
        int PH = R.drawable.ic_launcher_foreground;
        int TOAST = R.drawable.toast;

        // Create categories
        categories.add(new CategoryItem("הכל", PH));
        categories.add(new CategoryItem("כריכים", PH));
        categories.add(new CategoryItem("פסטה", PH));
        categories.add(new CategoryItem("משקאות קרים", PH));
        categories.add(new CategoryItem("קינוחים", PH));
        categories.add(new CategoryItem("מאפים", PH));

        // --- הגדרת תוספות (Addons) ---
        Addon dfa = new Addon("DFA", "DFA", -1, PH);
        // תוספות לטוסטים
        Addon[] toastAddons = new Addon[] {
                dfa,
                new Addon("add_olives", "זיתים", 1.00, PH),
                new Addon("add_corn", "תירס", 1.00, PH),
                new Addon("add_onion", "בצל", 0.00, PH),
                new Addon("add_bulgarit", "גבינה בולגרית", 2.00, PH),
                new Addon("add_extra_cheese", "תוספת גבינה", 2.50, PH),
                new Addon("add_pizza_sauce", "רוטב פיצה", 0.00, PH)
        };

        // תוספות לכריכים (טונה/שקשוקה)
        Addon[] sandwichAddons = new Addon[] {
                dfa,
                new Addon("add_egg", "ביצה קשה", 1.50, PH),
                new Addon("add_pickles", "מלפפון חמוץ", 0.00, PH),
                new Addon("add_tomato", "עגבנייה", 0.00, PH),
                new Addon("add_lettuce", "חסה", 0.00, PH),
                new Addon("add_spicy", "חריף", 0.00, PH)
        };

        // תוספות לפסטה
        Addon[] pastaAddons = new Addon[] {
                dfa,
                new Addon("add_parmesan", "תוספת פרמזן", 3.00, PH),
                new Addon("add_extra_sauce", "תוספת רוטב", 1.00, PH),
                new Addon("add_spicy_sauce", "רוטב חריף", 0.00, PH)
        };

        // תוספות למאפים (בורקס)
        Addon[] pastryAddons = new Addon[] {
                dfa,
                new Addon("add_egg", "ביצה קשה", 1.50, PH),
                new Addon("add_pickles", "מלפפון חמוץ", 0.50, PH),
                new Addon("add_tahini", "טחינה בצד", 0.00, PH)
        };

        Addon[] dfaArr = new Addon[] {
                dfa
        };

        // --- הוספת מוצרים (Add products) ---

        // כריכים
        allProducts.add(new Product("101", "טוסט", "לחם טרי, גבינה צהובה", 8.00, "כריכים", toastAddons, PH, 0));
        allProducts.add(new Product("102", "טוסט גדול", "לחם כפול, גבינה נדיבה", 10.00, "כריכים", toastAddons, TOAST, 0));
        allProducts.add(new Product("103", "כריך טונה", "לבחירה: לבן/מלא", 10.00, "כריכים", sandwichAddons, TOAST, 0));
        allProducts.add(new Product("104", "כריך שקשוקה", "שקשוקה עדינה בלחם טרי", 10.00, "כריכים", sandwichAddons, PH, 0));

        // פסטה
        allProducts.add(new Product("201", "פסטה פנה ברוטב עגבניות", "פנה, רוטב עגבניות עדין", 12.00, "פסטה", pastaAddons, PH, 0));

        // משקאות (ללא תוספות)
        allProducts.add(new Product("301", "קולה זירו (פחית)", "330 מ״ל", 6.00, "משקאות קרים", dfaArr, PH, 0));
        allProducts.add(new Product("302", "קוקה-קולה (פחית)", "330 מ״ל", 6.00, "משקאות קרים", dfaArr, PH, 0));
        allProducts.add(new Product("303", "ספרייט (פחית)", "330 מ״ל", 6.00, "משקאות קרים", dfaArr, PH, 0));
        allProducts.add(new Product("304", "פאנטה תפוז (פחית)", "330 מ״ל", 6.00, "משקאות קרים", dfaArr, PH, 0));
        allProducts.add(new Product("305", "סודה (פחית)", "330 מ״ל", 6.00, "משקאות קרים", dfaArr, PH, 0));
        allProducts.add(new Product("306", "ברד", "טעמים לבחירה", 9.00, "משקאות קרים", dfaArr, PH, 0));

        // קינוחים (ללא תוספות)
        allProducts.add(new Product("401", "קינדר", "שוקולד חלב", 5.00, "קינוחים (שוקולדים)", dfaArr, PH, 0));
        allProducts.add(new Product("402", "בואנו", "שוקולד/וופלים", 5.00, "קינוחים (שוקולדים)", dfaArr, PH, 0));

        // מאפים
        allProducts.add(new Product("501", "בורקס תפוחי אדמה", "פריך וחם", 8.00, "מאפים", pastryAddons, PH, 0));

        // Show all products initially
        showProducts(allProducts);

        // Set up CategoryAdapter with filtering logic
        CategoryAdapter categoryAdapter = new CategoryAdapter(categories, categoryName -> {
            if (categoryName.equals("הכל")) {
                showProducts(allProducts);
            } else {
                List<Product> filteredList = new ArrayList<>();
                for (Product p : allProducts) {
                    // Check if product category contains selected category name
                    if (p.getCategory() != null && p.getCategory().contains(categoryName)) {
                        filteredList.add(p);
                    }
                }
                showProducts(filteredList);
            }
        });
        RV_categories.setAdapter(categoryAdapter);
    }

    private void showProducts(List<Product> products) {
        CustomProductAdapterRV adp = new CustomProductAdapterRV(
                products,
                item -> {
                    Intent intent = new Intent(requireContext(), CustomizeItemActivity.class);
                    intent.putExtra("item", item);
                    startActivity(intent);
                }
        );
        RV_items.setAdapter(adp);
    }
}
