package com.example.CafeteriaApp.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.CafeteriaApp.Models.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final String PREFS_NAME = "CartPrefs";
    private static final String CART_KEY_PREFIX = "cart_items_";
    private static final String TAG = "FileManager";

    // Helper method to get the correct key based on UID
    private static String getCartKey() {
        String uid = FBRef.refAuth.getUid();
        if (uid == null || uid.isEmpty()) {
            return "guest_cart"; // Fallback for guest or not logged in
        }
        return CART_KEY_PREFIX + uid;
    }

    public static void saveCart(Context context, List<Product> cartItems) {
        String key = getCartKey();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String jsonString = gson.toJson(cartItems);

        editor.putString(key, jsonString);
        editor.apply();

        Log.d(TAG, "Saved cart with " + cartItems.size() + " items for key " + key);
    }

    public static List<Product> loadCart(Context context) {
        String key = getCartKey();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        String jsonString = sharedPreferences.getString(key, null);
        if (jsonString == null) {
            Log.d(TAG, "Cart not found for key (" + key + "), creating new empty list.");
            return new ArrayList<>();
        }

        List<Product> cartList = new ArrayList<>();
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Product>>(){}.getType();
            cartList = gson.fromJson(jsonString, listType);

            if (cartList == null) {
                cartList = new ArrayList<>();
            }
            
            Log.d(TAG, "Loaded cart with " + cartList.size() + " items for key " + key);

        } catch (Exception e) {
            Log.e(TAG, "Unexpected error loading cart", e);
            return new ArrayList<>();
        }
        
        return cartList;
    }
}
