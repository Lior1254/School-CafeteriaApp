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

/**
 * Helper class to manage local file storage and SharedPreferences.
 * Handles user sessions and shopping cart persistence.
 */
public class FileManager {
    private static final String PREFS_NAME = "CartPrefs";
    private static final String CART_KEY_PREFIX = "cart_items_";
    private static final String KEY_AUTH_TIME = "user_authentication_time";
    private static final String KEY_GENERAL_NOTES = "cart_general_notes_";
    private static final String TAG = "FileManager";
    
    public static final int AUTH_EXPIRY_DAYS = 7;

    private static String getCartKey() {
        String uid = FBRef.refAuth.getUid();
        return (uid == null || uid.isEmpty()) ? "guest_cart" : CART_KEY_PREFIX + uid;
    }

    private static String getGeneralNotesKey() {
        String uid = FBRef.refAuth.getUid();
        return (uid == null || uid.isEmpty()) ? "guest_notes" : KEY_GENERAL_NOTES + uid;
    }

    private static String getAuthenticationUid() {
        String uid = FBRef.refAuth.getUid();
        return (uid == null || uid.isEmpty()) ? "" : uid;
    }

    /**
     * Saves user authentication info with an expiry date if rememberMe is true.
     * If rememberMe is false, it clears any existing authentication.
     */
    public static void saveUserAuthentication(Context context, boolean rememberMe) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (rememberMe) {
            String uid = getAuthenticationUid();
            if (!uid.isEmpty()) {
                String expiryTime = Utils.getDateStringWithOffset(AUTH_EXPIRY_DAYS);
                String value = uid + "#" + expiryTime;
                editor.putString(KEY_AUTH_TIME, value);
                Log.d(TAG, "Saved auth for auto-login: " + uid);
            }
        } else {
            editor.remove(KEY_AUTH_TIME);
            Log.d(TAG, "Remember Me disabled, clearing auth info.");
        }
        editor.apply(); // Using apply() is safer/asynchronous compared to commit()
    }

    /**
     * Retrieves stored authentication data.
     */
    public static String getUserAuthentication(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_AUTH_TIME, "");
    }

    /**
     * Clears authentication data for logout.
     */
    public static void clearUserAuthentication(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_AUTH_TIME);
        editor.apply();
    }

    /**
     * Saves the current list of products to the cart.
     */
    public static void saveCart(Context context, List<Product> cartItems) {
        String key = getCartKey();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
        String jsonString = new Gson().toJson(cartItems);
        editor.putString(key, jsonString);
        editor.apply();
    }

    /**
     * Saves general order notes.
     */
    public static void saveGeneralNotes(Context context, String notes) {
        String key = getGeneralNotesKey();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, notes);
        editor.apply();
    }

    /**
     * Loads general order notes.
     */
    public static String loadGeneralNotes(Context context) {
        String key = getGeneralNotesKey();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }

    /**
     * Replaces the entire cart with a new list of items.
     * Useful for "Order Again" functionality.
     */
    public static void replaceCart(Context context, List<Product> newItems) {
        saveCart(context, newItems);
        Log.d(TAG, "Cart replaced with " + (newItems != null ? newItems.size() : 0) + " items.");
    }

    /**
     * Loads the saved cart items from SharedPreferences.
     */
    public static List<Product> loadCart(Context context) {
        String key = getCartKey();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        String jsonString = sharedPreferences.getString(key, null);
        if (jsonString == null) return new ArrayList<>();

        try {
            Type listType = new TypeToken<ArrayList<Product>>(){}.getType();
            List<Product> cartList = new Gson().fromJson(jsonString, listType);
            return cartList != null ? cartList : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error loading cart", e);
            return new ArrayList<>();
        }
    }
}
