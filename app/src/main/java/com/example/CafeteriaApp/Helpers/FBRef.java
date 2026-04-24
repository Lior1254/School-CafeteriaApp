package com.example.CafeteriaApp.Helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.CafeteriaApp.Models.Order;
import com.example.CafeteriaApp.Models.Product;
import com.example.CafeteriaApp.Models.User;
import com.example.CafeteriaApp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for Firebase references and operations.
 * Manages database paths, authentication, and optimized image loading with memory caching.
 */
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

    public static Boolean OrderFlag = false;

    /** Cache to store download URLs to avoid redundant Firebase storage calls */
    private static final Map<String, Uri> urlCache = new HashMap<>();
    
    /**
     * Returns the database reference for a specific user's orders or history.
     */
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
     * Listens to order updates in real-time based on the user's role.
     */
    public static ValueEventListener listenToOrdersByRoleLive(int role, boolean isHistory, FBListener listener) {
        if (role == User.ROLE_COOK || role == User.ROLE_MANAGER) {
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

    /**
     * Uploads an order to both global and user-specific nodes in the database.
     */
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

    /**
     * Loads product image with maximum optimization.
     * Uses memory cache and Bitmap storage within the Product object for a "static" feel.
     */
    public static void loadProductImage(final Product product, final ImageView imageView)
    {
        if (product == null || imageView == null) return;

        // 1. If we already have the Bitmap in memory, show it immediately (Static display)
        if (product.getImageBitmap() != null) {
            imageView.setImageBitmap(product.getImageBitmap());
            return;
        }

        final String productId = product.getId();
        imageView.setTag(productId);
        imageView.setImageResource(R.drawable.ic_launcher_background);
        if (productId == null || productId.isEmpty()) return;

        // 2. Check if we have the URL cached to skip Firebase call
        if (urlCache.containsKey(productId)) {
            loadWithGlide(imageView, urlCache.get(productId), product, productId);
            return;
        }

        // 3. Fetch from Firebase Storage
        StorageReference imageRef = refStorage.child("Products").child(productId + ".jpg");
        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            urlCache.put(productId, uri);
            loadWithGlide(imageView, uri, product, productId);
        }).addOnFailureListener(e -> {
            Log.e("FBRef", "Failed to get image for: " + productId);
        });
    }

    /**
     * Internal helper to load image via Glide and store result as Bitmap.
     */
    private static void loadWithGlide(final ImageView imageView, Uri uri, final Product product, final String productId) {
        Glide.with(imageView.getContext())
                .asBitmap()
                .load(uri)
                .placeholder(R.drawable.ic_launcher_background)
                .fitCenter() // CHANGED: Ensuring entire image fits within the box
                .override(300, 300)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        product.setImageBitmap(resource);
                        if (productId.equals(imageView.getTag())) {
                            imageView.setImageBitmap(resource);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        if (productId.equals(imageView.getTag())) {
                            imageView.setImageDrawable(placeholder);
                        }
                    }
                });
    }
}
