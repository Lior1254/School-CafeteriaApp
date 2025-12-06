package com.example.CafeteriaApp.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Adapters.CategoryAdapter;
import com.example.CafeteriaApp.Adapters.CustomProductAdapterRV;
import com.example.CafeteriaApp.CustomizeItemActivity;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Models.Addon;
import com.example.CafeteriaApp.Models.CategoryItem;
import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuFragment extends Fragment
{
    private RecyclerView RV_items, RV_categories;
    private List<Product> allProducts = new ArrayList<>();
    private List<CategoryItem> categories = new ArrayList<>();
    
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

        DownloadData();
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

    private void DownloadData()
    {
        ProgressDialog pd = new ProgressDialog(requireContext());
        pd.setTitle("Downloading Data");
        pd.setMessage("Please wait...");
        pd.show();

        FBRef.refProducts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allProducts.clear();
                categories.clear();

                int PH = R.drawable.ic_launcher_foreground;
                categories.add(new CategoryItem("הכל", PH));

                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String categoryName = categorySnapshot.getKey();

                    if (categoryName == null || categoryName.equals("Categories")) continue;

                    categories.add(new CategoryItem(categoryName, PH));

                    for (DataSnapshot productSnapshot : categorySnapshot.getChildren()) {
                        Product p = productSnapshot.getValue(Product.class);
                        if (p != null) {
                            p.setCategory(categoryName);
                            allProducts.add(p);
                        }
                    }
                }

                pd.dismiss();

                showProducts(allProducts);

                CategoryAdapter categoryAdapter = new CategoryAdapter(categories, categoryName -> {
                    if (categoryName.equals("הכל"))
                    {
                        showProducts(allProducts);
                    } else
                    {
                        List<Product> filteredList = new ArrayList<>();
                        for (Product p : allProducts)
                        {
                            if (p.getCategory() != null && p.getCategory().contains(categoryName))
                            {
                                filteredList.add(p);
                            }
                        }
                        showProducts(filteredList);
                    }
                });
                RV_categories.setAdapter(categoryAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pd.dismiss();
                Toast.makeText(requireContext(), "Failed to download data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void UploadData()
    {
        // Placeholders
        int PH = R.drawable.ic_launcher_foreground;
        int TOAST = R.drawable.toast;

        // Create categories
        List<CategoryItem> tempCategories = new ArrayList<>();
        tempCategories.add(new CategoryItem("הכל", PH));
        tempCategories.add(new CategoryItem("כריכים", PH));
        tempCategories.add(new CategoryItem("פסטה", PH));
        tempCategories.add(new CategoryItem("משקאות קרים", PH));
        tempCategories.add(new CategoryItem("קינוחים", PH));
        tempCategories.add(new CategoryItem("מאפים", PH));

        Addon dfa = new Addon("DFA", "DFA", -1, PH);
        
        List<Addon> toastAddons = Arrays.asList(
                dfa,
                new Addon("add_olives", "זיתים", 1.00, PH),
                new Addon("add_corn", "תירס", 1.00, PH),
                new Addon("add_onion", "בצל", 0.00, PH),
                new Addon("add_bulgarit", "גבינה בולגרית", 2.00, PH),
                new Addon("add_extra_cheese", "תוספת גבינה", 2.50, PH),
                new Addon("add_pizza_sauce", "רוטב פיצה", 0.00, PH)
        );

        List<Addon> sandwichAddons = Arrays.asList(
                dfa,
                new Addon("add_egg", "ביצה קשה", 1.50, PH),
                new Addon("add_pickles", "מלפפון חמוץ", 0.00, PH),
                new Addon("add_tomato", "עגבנייה", 0.00, PH),
                new Addon("add_lettuce", "חסה", 0.00, PH),
                new Addon("add_spicy", "חריף", 0.00, PH)
        );

        List<Addon> pastaAddons = Arrays.asList(
                dfa,
                new Addon("add_parmesan", "תוספת פרמזן", 3.00, PH),
                new Addon("add_extra_sauce", "תוספת רוטב", 1.00, PH),
                new Addon("add_spicy_sauce", "רוטב חריף", 0.00, PH)
        );

        List<Addon> pastryAddons = Arrays.asList(
                dfa,
                new Addon("add_egg", "ביצה קשה", 1.50, PH),
                new Addon("add_pickles", "מלפפון חמוץ", 0.50, PH),
                new Addon("add_tahini", "טחינה בצד", 0.00, PH)
        );

        List<Addon> dfaArr = Arrays.asList(dfa);

        List<Product> tempProducts = new ArrayList<>();

        // כריכים
        tempProducts.add(new Product("101", "טוסט", "לחם טרי, גבינה צהובה", 8.00, "כריכים", toastAddons, PH, 0));
        tempProducts.add(new Product("102", "טוסט גדול", "לחם כפול, גבינה נדיבה", 10.00, "כריכים", toastAddons, TOAST, 0));
        tempProducts.add(new Product("103", "כריך טונה", "לבחירה: לבן/מלא", 10.00, "כריכים", sandwichAddons, TOAST, 0));
        tempProducts.add(new Product("104", "כריך שקשוקה", "שקשוקה עדינה בלחם טרי", 10.00, "כריכים", sandwichAddons, PH, 0));

        // פסטה
        tempProducts.add(new Product("201", "פסטה פנה ברוטב עגבניות", "פנה, רוטב עגבניות עדין", 12.00, "פסטה", pastaAddons, PH, 0));

        // משקאות
        tempProducts.add(new Product("301", "קולה זירו (פחית)", "330 מ״ל", 6.00, "משקאות קרים", dfaArr, PH, 0));
        tempProducts.add(new Product("302", "קוקה-קולה (פחית)", "330 מ״ל", 6.00, "משקאות קרים", dfaArr, PH, 0));
        tempProducts.add(new Product("303", "ספרייט (פחית)", "330 מ״ל", 6.00, "משקאות קרים", dfaArr, PH, 0));
        tempProducts.add(new Product("304", "פאנטה תפוז (פחית)", "330 מ״ל", 6.00, "משקאות קרים", dfaArr, PH, 0));
        tempProducts.add(new Product("305", "סודה (פחית)", "330 מ״ל", 6.00, "משקאות קרים", dfaArr, PH, 0));
        tempProducts.add(new Product("306", "ברד", "טעמים לבחירה", 9.00, "משקאות קרים", dfaArr, PH, 0));

        // קינוחים
        tempProducts.add(new Product("401", "קינדר", "שוקולד חלב", 5.00, "קינוחים (שוקולדים)", dfaArr, PH, 0));
        tempProducts.add(new Product("402", "בואנו", "שוקולד/וופלים", 5.00, "קינוחים (שוקולדים)", dfaArr, PH, 0));

        // מאפים
        tempProducts.add(new Product("501", "בורקס תפוחי אדמה", "פריך וחם", 8.00, "מאפים", pastryAddons, PH, 0));


        ProgressDialog pd = new ProgressDialog(requireContext());
        pd.setTitle("Uploading Data");
        pd.setMessage("Please wait...");
        pd.show();

        List<Task<Void>> tasks = new ArrayList<>();

        for(Product p : tempProducts) {
            tasks.add(FBRef.refProducts.child(p.getCategory()).child(p.getId()).setValue(p));
        }

        Tasks.whenAll(tasks).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(requireContext(), "Data uploaded successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to upload data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
