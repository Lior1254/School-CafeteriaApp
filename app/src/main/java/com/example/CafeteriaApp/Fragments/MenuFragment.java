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
import com.example.CafeteriaApp.Models.Addon;
import com.example.CafeteriaApp.Models.CategoryItem;
import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.R;
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
                             @Nullable Bundle b)
    {
        return i.inflate(R.layout.fragment_menu, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b)
    {
        super.onViewCreated(v, b);
        RV_items = v.findViewById(R.id.RV_items);
        RV_categories = v.findViewById(R.id.RV_categories);

        RV_items.setLayoutManager(new LinearLayoutManager(requireContext()));
        RV_categories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        RV_items.setAdapter(new CustomProductAdapterRV(requireContext(), new ArrayList<>(), null));
        RV_categories.setAdapter(new CategoryAdapter(new ArrayList<>(), null));

        DownloadData();
    }

    /**
     * Local method to check internet connection within the fragment.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = (cm != null) ? cm.getActiveNetworkInfo() : null;
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("שגיאת חיבור")
                    .setMessage("פעולה זו דורשת חיבור לאינטרנט.")
                    .setPositiveButton("Ok", null)
                    .show();
        }
        return isConnected;
    }

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

    private void DownloadData()
    {
        // Use the local fragment check
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
                    categories.add(new CategoryItem("הכל", PH));

                    for (DataSnapshot categorySnapshot : snapshot.getChildren())
                    {
                        String categoryName = categorySnapshot.getKey();
                        if (categoryName == null || categoryName.equals("Categories")) continue;
                        categories.add(new CategoryItem(categoryName, PH));

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

    private void UploadData()
    {
        if (isNetworkAvailable()) {
            int PH = R.drawable.ic_launcher_foreground;

            Addon dfa = new Addon("DFA", "DFA", -1, PH);
            List<Addon> generalAddons = Arrays.asList(dfa,
                    new Addon("add_tahini", "טחינה", 0.00, PH),
                    new Addon("add_spicy", "חריף", 0.00, PH),
                    new Addon("add_garlic", "שום", 0.00, PH),
                    new Addon("add_onion", "בצל מטוגן", 1.00, PH),
                    new Addon("add_pickles", "חמוצים", 0.00, PH));

            List<Product> tempProducts = new ArrayList<>();
            // ... (rest of products)

            ProgressDialog pd = new ProgressDialog(requireContext());
            pd.setTitle("Uploading Data");
            pd.setMessage("Please wait...");
            pd.show();

            List<Task<Void>> tasks = new ArrayList<>();
            for (Product p : tempProducts)
            {
                tasks.add(FBRef.refProducts.child(p.getCategory()).child(p.getId()).setValue(p));
            }

            Tasks.whenAll(tasks).addOnCompleteListener(task ->
            {
                pd.dismiss();
                if (task.isSuccessful())
                {
                    Toast.makeText(requireContext(), "Data uploaded successfully", Toast.LENGTH_SHORT).show();
                } else
                {
                    Toast.makeText(requireContext(), "Failed to upload data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
