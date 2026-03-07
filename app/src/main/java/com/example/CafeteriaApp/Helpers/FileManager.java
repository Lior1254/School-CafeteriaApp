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
    private static final String KEY_AUTH_TIME = "user_authentication_time";
    private static final String TAG = "FileManager";
    
    /**
     * The number of days the user's authentication session remains valid.
     */
    public static final int AUTH_EXPIRY_DAYS = 7;

    // Helper method to get the correct key based on UID for the cart
    private static String getCartKey() {
        String uid = FBRef.refAuth.getUid();
        if (uid == null || uid.isEmpty()) {
            return "guest_cart";
        }
        return CART_KEY_PREFIX + uid;
    }

    private static String getAuthenticationUid() {
        String uid = FBRef.refAuth.getUid();
        if (uid == null || uid.isEmpty()) {
            return "";
        }
        return uid;
    }

    /**
     * Saves the authentication expiry time (current time + AUTH_EXPIRY_DAYS).
     */
    public static void saveUserAuthentication(Context context) {
        String uid = getAuthenticationUid();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Calculate and save the expiry time (7 days from now)
        String expiryTime = Utils.getDateStringWithOffset(AUTH_EXPIRY_DAYS);
        String value = uid + "#" + expiryTime;
        editor.putString(KEY_AUTH_TIME, value);
        
        // Using commit() to ensure immediate synchronous write
        boolean success = editor.commit();
        
        if (success) {
            Log.d(TAG, "Successfully saved auth expiry: " + value + " for key: " + KEY_AUTH_TIME);
        } else {
            Log.e(TAG, "Failed to save auth value for key: " + KEY_AUTH_TIME);
        }
    }

    /**
     * Loads the stored authentication time for the user.
     */
    public static String getUserAuthentication(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_AUTH_TIME, "");
    }

    /**
     * Clears the stored authentication data (for logout).
     */
    public static void clearUserAuthentication(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_AUTH_TIME);
        editor.commit();
        Log.d(TAG, "Cleared user authentication from SharedPreferences");
    }

    public static void saveCart(Context context, List<Product> cartItems) {
        String key = getCartKey();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
        Gson gson = new Gson();
        String jsonString = gson.toJson(cartItems);

        editor.putString(key, jsonString);
        
        // Using commit() for synchronous write
        boolean success = editor.commit();

        if (success) {
            Log.d(TAG, "Successfully saved cart with " + cartItems.size() + " items for key: " + key);
        } else {
            Log.e(TAG, "Failed to save cart for key: " + key);
        }
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
