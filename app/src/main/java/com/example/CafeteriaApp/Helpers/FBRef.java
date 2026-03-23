package com.example.CafeteriaApp.Helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.CafeteriaApp.Models.Order;
import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class FBRef
{
    public static FirebaseDatabase FBDB = FirebaseDatabase.getInstance();

    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();
    public static DatabaseReference refUsers = FBDB.getReference("Users");
    public static DatabaseReference refProducts = FBDB.getReference("Products");
    public static DatabaseReference refOrders = FBDB.getReference("Orders");
    public static DatabaseReference refUserOrders = FBDB.getReference("UserOrders");

    public static FirebaseStorage storage = FirebaseStorage.getInstance();
    public static StorageReference refStorage = storage.getReference();

    public static Boolean HistoryFlag = true;
    public static Boolean OrderFlag = false;
    
    public static DatabaseReference getUserOrdersRef(String uid, boolean isHistory) {
        String subPath = isHistory ? "HistoryOrders" : "Orders";
        return refUserOrders.child(uid).child(subPath);
    }

    public interface FBListener
    {
        void onSuccess();
        void onSuccess(Object data); // Added to support returning data
        void onFailure(String error);
    }

    public static void uploadOrder(Order order, FBListener listener)
    {
        if (order == null) return;

        refOrders.child(order.getOrderStatus())
                .child(order.getRequestedTime())
                .child(order.getOrderId())
                .setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        uploadUserOrder(order, listener, OrderFlag);
                    } else {
                        if (listener != null) listener.onFailure(task.getException().getMessage());
                    }
                });
    }

    private static void uploadUserOrder(Order order, FBListener listener, boolean isHistory)
    {
        FirebaseUser currentUser = refAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        
        getUserOrdersRef(uid, isHistory)
                .child(order.getRequestedTime())
                .setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (listener != null) listener.onSuccess();
                    } else {
                        if (listener != null) listener.onFailure(task.getException().getMessage());
                    }
                });
    }

    public static void downloadOrderForUser(boolean isHistory, FBListener listener)
    {
        FirebaseUser currentUser = refAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        getUserOrdersRef(uid, isHistory)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Order> orderList = new ArrayList<>();
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                Order order = child.getValue(Order.class);
                                if (order != null) {
                                    orderList.add(order);
                                }
                            }
                        }
                        if (listener != null) listener.onSuccess(orderList);
                    } 
                    else {
                        if (listener != null && task.getException() != null) 
                            listener.onFailure(task.getException().getMessage());
                    }
                });
    }
    
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

        final long MAX_SIZE = 5 * 1024 * 1024;
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
            StorageReference rootRef = refStorage.child(productId + ".jpg");
            rootRef.getBytes(MAX_SIZE).addOnSuccessListener(bytes ->
            {
                if (productId.equals(imageView.getTag()))
                {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    product.setImageBitmap(bitmap);
                    imageView.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(e2 -> {
                 if (productId.equals(imageView.getTag())) {
                     imageView.setImageResource(R.drawable.ic_launcher_background);
                 }
            });
        });
    }
}
