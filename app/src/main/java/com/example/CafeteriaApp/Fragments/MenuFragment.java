package com.example.CafeteriaApp.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.example.CafeteriaApp.Adapters.CategoryAdapter;
import com.example.CafeteriaApp.Adapters.CustomProductAdapterRV;
import com.example.CafeteriaApp.CustomizeItemActivity;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Helpers.MenuDataUploader;
import com.example.CafeteriaApp.Models.CategoryItem;
import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Fragment responsible for displaying the cafeteria menu, filtering by categories,
 * and navigating to product customization.
 */
public class MenuFragment extends Fragment
{
    /**
     * Flag to control manual menu data upload to Firebase.
     * Set to true to trigger the upload process on view creation.
     */
    private static final boolean SHOULD_UPLOAD_MENU = false;

    private RecyclerView RV_items, RV_categories;
    private List<Product> allProducts = new ArrayList<>();
    private List<CategoryItem> categories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        RV_items = view.findViewById(R.id.RV_items);
        RV_categories = view.findViewById(R.id.RV_categories);

        RV_items.setLayoutManager(new LinearLayoutManager(requireContext()));
        RV_categories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        RV_items.setAdapter(new CustomProductAdapterRV(requireContext(), new ArrayList<>(), null));
        RV_categories.setAdapter(new CategoryAdapter(new ArrayList<>(), null));

        // Load existing menu data
        DownloadData();
        
        // Conditional upload based on internal flag
        if (SHOULD_UPLOAD_MENU) {
            UploadData(); 
        }
    }

    /**
     * Checks if the device is connected to the internet.
     * Shows an alert dialog if no connection is found.
     * @return true if connected, false otherwise.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = (cm != null) ? cm.getActiveNetworkInfo() : null;
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Connection Error")
                    .setMessage("This action requires an active internet connection.")
                    .setPositiveButton("Ok", null)
                    .show();
        }
        return isConnected;
    }

    /**
     * Sets up the products adapter and handles item click navigation.
     * @param products List of products to display.
     */
    private void chooseProducts(List<Product> products)
    {
        CustomProductAdapterRV adp = new CustomProductAdapterRV(
                requireContext(),
                products,
                item ->
                {
                    CustomizeItemActivity.selectedImageBitmap = item.getImageBitmap();
                    Intent intent = new Intent(requireContext(), CustomizeItemActivity.class);
                    intent.putExtra("item", item);
                    startActivity(intent);
                }
        );
        RV_items.setAdapter(adp);
    }

    /**
     * Downloads menu data from Firebase Realtime Database.
     * Clears existing lists and populates them with fresh data.
     */
    private void DownloadData()
    {
        if (isNetworkAvailable()) {
            ProgressDialog pd = new ProgressDialog(requireContext());
            pd.setTitle("Downloading Data");
            pd.setMessage("Please wait...");
            pd.show();

            FBRef.refProducts.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    allProducts.clear();
                    categories.clear();

                    int PH = R.drawable.ic_launcher_foreground;

                    List<String> categoryNames = new ArrayList<>();
                    for (DataSnapshot categorySnapshot : snapshot.getChildren())
                    {
                        String categoryName = categorySnapshot.getKey();
                        if (categoryName == null || categoryName.equals("Categories")) continue;
                        categoryNames.add(categoryName);

                        for (DataSnapshot productSnapshot : categorySnapshot.getChildren())
                        {
                            Product p = productSnapshot.getValue(Product.class);
                            if (p != null)
                            {
                                p.setCategory(categoryName);
                                allProducts.add(p);
                            }
                        }
                    }

                    // Sort categories by custom priority: All -> Sandwiches -> Cold Drinks -> Others
                    Collections.sort(categoryNames, new Comparator<String>() {
                        @Override
                        public int compare(String o1, String o2) {
                            List<String> order = Arrays.asList("כריכים וטוסטים", "שתייה קרה");
                            int index1 = order.indexOf(o1);
                            int index2 = order.indexOf(o2);
                            
                            if (index1 != -1 && index2 != -1) return Integer.compare(index1, index2);
                            if (index1 != -1) return -1;
                            if (index2 != -1) return 1;
                            return o1.compareTo(o2);
                        }
                    });

                    categories.add(new CategoryItem("הכל", PH));
                    for (String name : categoryNames) {
                        categories.add(new CategoryItem(name, PH));
                    }

                    pd.dismiss();
                    chooseProducts(allProducts);

                    CategoryAdapter categoryAdapter = new CategoryAdapter(categories, categoryName ->
                    {
                        if (categoryName.equals("הכל"))
                        {
                            chooseProducts(allProducts);
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
                            chooseProducts(filteredList);
                        }
                    });
                    RV_categories.setAdapter(categoryAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    pd.dismiss();
                    Toast.makeText(requireContext(), "Failed to download data: " + error.getMessage(),
                                   Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Triggers the menu data upload process using MenuDataUploader helper.
     */
    private void UploadData()
    {
        if (isNetworkAvailable()) {
            ProgressDialog pd = new ProgressDialog(requireContext());
            pd.setTitle("Updating Menu");
            pd.setMessage("Uploading new menu items...");
            pd.show();

            MenuDataUploader.uploadFullMenu().addOnCompleteListener(task -> {
                pd.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(requireContext(), "Menu updated successfully!", Toast.LENGTH_SHORT).show();
                    DownloadData();
                } else {
                    Toast.makeText(requireContext(), "Upload failed.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
