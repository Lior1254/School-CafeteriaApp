package com.example.CafeteriaApp.Helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.CafeteriaApp.BaseActivity;
import com.example.CafeteriaApp.Models.Order;
import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.Models.User;
import com.example.CafeteriaApp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.CafeteriaApp.Receivers.AlarmReceiver;

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
        void onSuccess(Object data);
        void onFailure(String error);
    }

    /**
     * Real-time listener for orders based on role.
     */
    public static ValueEventListener listenToOrdersByRoleLive(int role, boolean isHistory, FBListener listener) {
        if (role == User.ROLE_COOK || role == User.ROLE_MANAGER) {
            // Admin/Cook listens to ALL orders
            ValueEventListener vel = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Order> allOrders = new ArrayList<>();
                    for (DataSnapshot statusSnap : snapshot.getChildren()) {
                        String status = statusSnap.getKey();
                        boolean isOrderStatusHistory = "3".equals(status);
                        if (isHistory == isOrderStatusHistory) {
                            for (DataSnapshot timeSnap : statusSnap.getChildren()) {
                                for (DataSnapshot orderSnap : timeSnap.getChildren()) {
                                    Order o = orderSnap.getValue(Order.class);
                                    if (o != null) allOrders.add(o);
                                }
                            }
                        }
                    }
                    listener.onSuccess(allOrders);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    listener.onFailure(error.getMessage());
                }
            };
            refOrders.addValueEventListener(vel);
            return vel;
        } else {
            // Regular user listens only to their own orders
            FirebaseUser currentUser = refAuth.getCurrentUser();
            if (currentUser == null) return null;

            ValueEventListener vel = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Order> orderList = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Order order = child.getValue(Order.class);
                        if (order != null) orderList.add(order);
                    }
                    listener.onSuccess(orderList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    listener.onFailure(error.getMessage());
                }
            };
            getUserOrdersRef(currentUser.getUid(), isHistory).addValueEventListener(vel);
            return vel;
        }
    }

    public static void uploadOrder(Order order, FBListener listener)
    {
        if (order == null || order.getOrderStatus() == null || 
            order.getRequestedTime() == null || order.getOrderId() == null) {
            if (listener != null) listener.onFailure("Order data is missing required fields");
            return;
        }

        refOrders.child(order.getOrderStatus())
                .child(order.getRequestedTime())
                .child(order.getOrderId())
                .setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        uploadUserOrder(order, listener, OrderFlag);
                    } else {
                        if (listener != null) listener.onFailure(task.getException() != null ? 
                                task.getException().getMessage() : "Unknown upload error");
                    }
                });
    }

    private static void uploadUserOrder(Order order, FBListener listener, boolean isHistory)
    {
        FirebaseUser currentUser = refAuth.getCurrentUser();
        if (currentUser == null) return;

        // FIX: Use orderId instead of requestedTime to allow multiple orders at the same time
        getUserOrdersRef(currentUser.getUid(), isHistory)
                .child(order.getOrderId())
                .setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (listener != null) listener.onSuccess();
                    } else {
                        if (listener != null) listener.onFailure(task.getException() != null ? 
                                task.getException().getMessage() : "Error");
                    }
                });
    }

    public static void loadProductImage(Product product, ImageView imageView)
    {
        if (product == null || imageView == null) return;
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
                if (bitmap != null) {
                    product.setImageBitmap(bitmap);
                    imageView.setImageBitmap(bitmap);
                }
            }
        }).addOnFailureListener(e -> {});
    }
}
