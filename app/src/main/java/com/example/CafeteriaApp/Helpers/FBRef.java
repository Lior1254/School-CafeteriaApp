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
     * Listens specifically for changes in orders (status updates, etc.)
     * This triggers ONLY for the specific order that was modified.
     */
    public static void observeOrderUpdates(Context context, FBListener listener) {
        if (context == null) return;
        FirebaseUser currentUser = refAuth.getCurrentUser();
        if (currentUser == null) return;

        getUserOrdersRef(currentUser.getUid(), false)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        // Triggers when a new order is created
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        // THIS IS WHAT YOU NEED: Triggers only when an existing order is updated
                        Order updatedOrder = snapshot.getValue(Order.class);
                        if (updatedOrder != null && context != null) {
                            if (listener != null) listener.onSuccess(updatedOrder);

                            // Construct the Hebrew message: "ההזמנה שלך היא [סטטוס]"
                            String statusText = BaseActivity.getStatusText(updatedOrder.getOrderStatus());
                            String message = "ההזמנה שלך היא: " + statusText;

                            try {
                                Intent intent = new Intent(context, AlarmReceiver.class);
                                intent.setPackage(context.getPackageName()); // Ensure it reaches your app
                                intent.putExtra("Type", AlarmReceiver.OrderStatus);
                                intent.putExtra("text", message);
                                
                                // Convert String ID to int for notification compatibility if possible
                                String orderIdStr = updatedOrder.getOrderId();
                                if (orderIdStr != null) {
                                    try {
                                        intent.putExtra("orderID", Integer.parseInt(orderIdStr));
                                    } catch (NumberFormatException e) {
                                        intent.putExtra("orderID", (int) System.currentTimeMillis());
                                    }
                                } else {
                                    intent.putExtra("orderID", (int) System.currentTimeMillis());
                                }

                                context.sendBroadcast(intent);
                            } catch (Exception e) {
                                Log.e("FBRef", "Failed to send broadcast", e);
                            }
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (listener != null) listener.onFailure(error.getMessage());
                    }
                });
    }

    /**
     * Legacy method: Listens for the whole list of orders (good for RecyclerView).
     */
    public static void listenToUserOrders(FBListener listener) {
        FirebaseUser currentUser = refAuth.getCurrentUser();
        if (currentUser == null) return;

        getUserOrdersRef(currentUser.getUid(), false)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Order> orders = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Order order = child.getValue(Order.class);
                            if (order != null) orders.add(order);
                        }
                        if (listener != null) listener.onSuccess(orders);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (listener != null) listener.onFailure(error.getMessage());
                    }
                });
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

        String uid = currentUser.getUid();
        String requestedTime = order.getRequestedTime();
        
        if (requestedTime == null) {
            if (listener != null) listener.onFailure("Requested time is null");
            return;
        }
        
        getUserOrdersRef(uid, isHistory)
                .child(requestedTime)
                .setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (listener != null) listener.onSuccess();
                    } else {
                        if (listener != null) listener.onFailure(task.getException() != null ? 
                                task.getException().getMessage() : "Unknown user order upload error");
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
                        if (snapshot != null && snapshot.exists()) {
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
        }).addOnFailureListener(e ->
        {
            StorageReference rootRef = refStorage.child(productId + ".jpg");
            rootRef.getBytes(MAX_SIZE).addOnSuccessListener(bytes ->
            {
                if (productId.equals(imageView.getTag()))
                {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (bitmap != null) {
                        product.setImageBitmap(bitmap);
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }).addOnFailureListener(e2 -> {
                 if (productId.equals(imageView.getTag())) {
                     imageView.setImageResource(R.drawable.ic_launcher_background);
                 }
            });
        });
    }
}
