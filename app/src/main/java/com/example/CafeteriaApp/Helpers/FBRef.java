package com.example.CafeteriaApp.Helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FBRef
{
    public static FirebaseDatabase FBDB = FirebaseDatabase.getInstance();

    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();
    public static DatabaseReference refUsers = FBDB.getReference("Users");
    public static DatabaseReference refProducts = FBDB.getReference("Products");
    public static DatabaseReference refCarts = FBDB.getReference("Carts");
    public static DatabaseReference refOrders = FBDB.getReference("Orders");
    public static DatabaseReference refHistoryOrders = FBDB.getReference("HistoryOrders");

    public static FirebaseStorage storage = FirebaseStorage.getInstance();
    public static StorageReference refStorage = storage.getReference();

    public static void loadProductImage(Product product, ImageView imageView)
    {
        String productId = product.getId();
        imageView.setTag(productId);

        if (product.getImageBitmap() != null)
        {
            imageView.setImageBitmap(product.getImageBitmap());
            return;
        }

        imageView.setImageResource(R.drawable.ic_launcher_background);

        if (productId == null || productId.isEmpty()) return;

        final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

        // 1. Try "Products" folder first
        StorageReference productsRef = refStorage.child("Products").child(productId + ".jpg");
        
        productsRef.getBytes(MAX_SIZE).addOnSuccessListener(bytes ->
        {
            if (productId.equals(imageView.getTag()))
            {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                product.setImageBitmap(bitmap);
                imageView.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(e ->
        {
            Log.e("FBRef", "Failed to load from Products/" + productId + ".jpg: " + e.getMessage());

            // 2. Fallback: Try root folder
            StorageReference rootRef = refStorage.child(productId + ".jpg");
            rootRef.getBytes(MAX_SIZE).addOnSuccessListener(bytes ->
            {
                if (productId.equals(imageView.getTag()))
                {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    product.setImageBitmap(bitmap);
                    imageView.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(e2 ->
            {
                 Log.e("FBRef", "Failed to load from root " + productId + ".jpg: " + e2.getMessage());
                 if (productId.equals(imageView.getTag())) {
                     imageView.setImageResource(R.drawable.ic_launcher_background);
                 }
            });
        });
    }
}
