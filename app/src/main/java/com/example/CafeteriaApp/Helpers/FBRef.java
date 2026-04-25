package com.example.CafeteriaApp.Helpers;

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
 * Global helper class for Firebase operations.
 * Centralizes database references, authentication, and optimized image loading.
 */
public class FBRef {
    public static final FirebaseDatabase FBDB = FirebaseDatabase.getInstance();
    public static final FirebaseAuth refAuth = FirebaseAuth.getInstance();

    public static final DatabaseReference refUsers = FBDB.getReference("Users");
    public static final DatabaseReference refProducts = FBDB.getReference("Products");
    public static final DatabaseReference refOrders = FBDB.getReference("Orders");
    public static final DatabaseReference refUserOrders = FBDB.getReference("UserOrders");

    public static final FirebaseStorage storage = FirebaseStorage.getInstance();
    public static final StorageReference refStorage = storage.getReference();

    /** Cache to store download URLs to avoid redundant Firebase storage calls */
    private static final Map<String, Uri> urlCache = new HashMap<>();

    /**
     * Listener interface for Firebase operations.
     */
    public interface FBListener {
        void onSuccess();
        void onSuccess(Object data);
        void onFailure(String error);
    }

    /**
     * Gets the database reference for a specific user's orders.
     * @param uid The user's ID.
     * @param isHistory True if requesting history, false for active orders.
     * @return DatabaseReference for the requested node.
     */
    public static DatabaseReference getUserOrdersRef(String uid, boolean isHistory) {
        String subPath = isHistory ? "HistoryOrders" : "Orders";
        return refUserOrders.child(uid).child(subPath);
    }

    /**
     * Listens to order updates in real-time based on the user's role.
     * @param role User's role (User, Cook, Manager).
     * @param isHistory True if listening to history orders.
     * @param listener Callback listener.
     * @return The created ValueEventListener for cleanup.
     */
    public static ValueEventListener listenToOrdersByRoleLive(int role, boolean isHistory, FBListener listener) {
        if (role == User.ROLE_COOK || role == User.ROLE_MANAGER) {
            ValueEventListener vel = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Order> allOrders = new ArrayList<>();
                    for (DataSnapshot statusSnap : snapshot.getChildren()) {
                        String status = statusSnap.getKey();
                        boolean isOrderStatusHistory = Order.STATUS_COLLECTED.equals(status);
                        if (isHistory == isOrderStatusHistory) {
                            for (DataSnapshot timeSnap : statusSnap.getChildren()) {
                                for (DataSnapshot orderSnap : timeSnap.getChildren()) {
                                    Order order = orderSnap.getValue(Order.class);
                                    if (order != null) allOrders.add(order);
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
     * Uploads an order to the database.
     * @param order The order to upload.
     * @param listener Callback listener.
     */
    public static void uploadOrder(Order order, FBListener listener) {
        if (order == null || order.getOrderStatus() == null ||
                order.getRequestedTime() == null || order.getOrderId() == null) {
            if (listener != null) listener.onFailure("Missing required order fields");
            return;
        }

        refOrders.child(order.getOrderStatus())
                .child(order.getRequestedTime())
                .child(order.getOrderId())
                .setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        uploadToUserNode(order, listener);
                    } else if (listener != null) {
                        listener.onFailure(task.getException() != null ?
                                task.getException().getMessage() : "Upload failed");
                    }
                });
    }

    /**
     * Uploads the order to the user's specific order node.
     * @param order The order to upload.
     * @param listener Callback listener.
     */
    private static void uploadToUserNode(Order order, FBListener listener) {
        FirebaseUser currentUser = refAuth.getCurrentUser();
        if (currentUser == null) return;

        boolean isHistory = Order.STATUS_COLLECTED.equals(order.getOrderStatus());
        getUserOrdersRef(currentUser.getUid(), isHistory)
                .child(order.getOrderId())
                .setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (listener != null) listener.onSuccess();
                    } else if (listener != null) {
                        listener.onFailure(task.getException() != null ?
                                task.getException().getMessage() : "User node upload failed");
                    }
                });
    }

    /**
     * Loads product image with optimization and caching.
     * @param product The product containing image metadata.
     * @param imageView The target ImageView.
     */
    public static void loadProductImage(final Product product, final ImageView imageView) {
        if (product == null || imageView == null) return;

        if (product.getImageBitmap() != null) {
            imageView.setImageBitmap(product.getImageBitmap());
            return;
        }

        final String productId = product.getId();
        imageView.setTag(productId);
        imageView.setImageResource(R.drawable.ic_launcher_background);
        if (productId == null || productId.isEmpty()) return;

        if (urlCache.containsKey(productId)) {
            loadWithGlide(imageView, urlCache.get(productId), product, productId);
            return;
        }

        StorageReference imageRef = refStorage.child("Products").child(productId + ".jpg");
        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            urlCache.put(productId, uri);
            loadWithGlide(imageView, uri, product, productId);
        }).addOnFailureListener(e -> Log.e("FBRef", "Image fetch failed: " + productId));
    }

    /**
     * Helper to load image via Glide and cache as Bitmap.
     */
    private static void loadWithGlide(final ImageView imageView, Uri uri, final Product product, final String productId) {
        Glide.with(imageView.getContext())
                .asBitmap()
                .load(uri)
                .placeholder(R.drawable.ic_launcher_background)
                .fitCenter()
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
