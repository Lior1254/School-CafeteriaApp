package com.example.CafeteriaApp.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
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

        RV_items.setAdapter(new CustomProductAdapterRV(new ArrayList<>(), null));
        RV_categories.setAdapter(new CategoryAdapter(new ArrayList<>(), null));

        DownloadData();
    }

    private void chooseProducts(List<Product> products)
    {
        CustomProductAdapterRV adp = new CustomProductAdapterRV(
                products,
                item ->
                {
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

    private void UploadData()
    {
        int PH = R.drawable.ic_launcher_foreground;

        Addon dfa = new Addon("DFA", "DFA", -1, PH);
        List<Addon> generalAddons = Arrays.asList(dfa,
                new Addon("add_tahini", "טחינה", 0.00, PH),
                new Addon("add_spicy", "חריף", 0.00, PH),
                new Addon("add_garlic", "שום", 0.00, PH),
                new Addon("add_onion", "בצל מטוגן", 1.00, PH),
                new Addon("add_pickles", "חמוצים", 0.00, PH));

        List<Product> tempProducts = new ArrayList<>();

        // מוקפץ
        tempProducts.add(new Product("101", "מוקפץ (ירקות)", "", 12.00, "מוקפץ", generalAddons, PH, 0));
        tempProducts.add(new Product("102", "מוקפץ (עוף)", "", 16.00, "מוקפץ", generalAddons, PH, 0));

        // אורז
        tempProducts.add(new Product("201", "אורז בקערה", "", 10.00, "אורז", generalAddons, PH, 0));

        // קוסקוס
        tempProducts.add(new Product("301", "קוסקוס צמחוני קטן", "", 13.00, "קוסקוס", generalAddons, PH, 0));
        tempProducts.add(new Product("302", "קוסקוס צמחוני גדול", "", 19.00, "קוסקוס", generalAddons, PH, 0));

        // כריכים
        tempProducts.add(new Product("401", "כריך קטן", "חביתה / טונה / סביח", 10.00, "כריכים", generalAddons, PH, 0));
        tempProducts.add(new Product("402", "כריך גדול", "חביתה / טונה / סביח", 12.00, "כריכים", generalAddons, PH, 0));
        tempProducts.add(new Product("403", "באגט שקשוקה / חביתה", "", 15.00, "כריכים", generalAddons, PH, 0));
        tempProducts.add(new Product("404", "טוסט קטן (לחמניה)", "", 9.00, "כריכים", generalAddons, PH, 0));
        tempProducts.add(new Product("405", "טוסט גדול (באגט)", "", 15.00, "כריכים", generalAddons, PH, 0));

        // לחמניות
        tempProducts.add(new Product("501", "לחמניה 3 באגט 5", "", 3.00, "לחמניות", null, PH, 0));

        // תוספות
        tempProducts.add(new Product("601", "שקשוקה חמה", "", 7.00, "תוספות", null, PH, 0));
        tempProducts.add(new Product("602", "צ׳יפס קטן", "", 7.00, "תוספות", null, PH, 0));
        tempProducts.add(new Product("603", "צ׳יפס גדול", "", 13.00, "תוספות", null, PH, 0));

        // סלטים
        tempProducts.add(new Product("701", "סלט בהרכבה אישית", "ירקות, רטבים, טונה, ביצה, בולגרית, פטריות", 12.00, "סלטים", null, PH, 0));
        tempProducts.add(new Product("702", "סלט + רוטב ישראלי", "", 18.00, "סלטים", null, PH, 0));

        // מרק היום
        tempProducts.add(new Product("801", "מרק קטן (כוס)", "משתנה עם בורקסונים בצד", 7.00, "מרק היום", null, PH, 0));
        tempProducts.add(new Product("802", "מרק גדול (קערה)", "משתנה עם בורקסונים בצד", 10.00, "מרק היום", null, PH, 0));

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
