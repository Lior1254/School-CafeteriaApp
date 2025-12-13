package com.example.CafeteriaApp.Helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    /**
     * Helper function to load a product image into an ImageView.
     * Uses the cached bitmap if available, otherwise downloads from Firebase Storage.
     */
    public static void loadProductImage(Product product, ImageView imageView)
    {
        String productId = product.getId();
        // Tag the ImageView with the ID to verify identity upon async completion
        imageView.setTag(productId);

        // 1. If we already have the bitmap in memory, use it
        if (product.getImageBitmap() != null)
        {
            imageView.setImageBitmap(product.getImageBitmap());
            return;
        }

        // 2. Set default placeholder (green background) initially
        imageView.setImageResource(R.drawable.ic_launcher_background);

        // 3. If product has no ID, we can't download anything
        if (productId == null || productId.isEmpty())
        {
            return;
        }

        // 4. Try to download from Storage: Products/<id>.jpg
        StorageReference imageRef = refStorage.child("Products").child(productId + ".jpg");
        final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

        imageRef.getBytes(MAX_SIZE).addOnSuccessListener(bytes ->
        {
            // Verify that the ImageView is still waiting for THIS product
            if (productId.equals(imageView.getTag()))
            {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                product.setImageBitmap(bitmap); // Cache it
                imageView.setImageBitmap(bitmap); // Display it
            }
        }).addOnFailureListener(e ->
        {
            // On failure, ensure placeholder is shown if still relevant
            if (productId.equals(imageView.getTag()))
            {
                imageView.setImageResource(R.drawable.ic_launcher_background);
            }
        });
    }
}
