package com.example.CafeteriaApp.Helpers;

import android.content.Context;
import android.util.Log;

import com.example.CafeteriaApp.Models.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final String CART_FILE_PREFIX = "cart_items_";
    private static final String CART_FILE_SUFFIX = ".json";
    private static final String TAG = "FileManager";

    // Helper method to get the correct filename based on UID
    private static String getCartFilename() {
        String uid = FBRef.refAuth.getUid();
        if (uid == null || uid.isEmpty()) {
            return "guest_cart.json"; // Fallback for guest or not logged in
        }
        return CART_FILE_PREFIX + uid + CART_FILE_SUFFIX;
    }

    public static void saveCart(Context context, List<Product> cartItems) {
        String filename = getCartFilename();
        Gson gson = new Gson();
        String jsonString = gson.toJson(cartItems);

        try (FileOutputStream fOS = context.openFileOutput(filename, Context.MODE_PRIVATE);
             OutputStreamWriter oSW = new OutputStreamWriter(fOS);
             BufferedWriter bW = new BufferedWriter(oSW)) {

            bW.write(jsonString);
            Log.d(TAG, "Saved cart with " + cartItems.size() + " items to " + filename);

        } catch (IOException e) {
            Log.e(TAG, "Error saving cart", e);
            e.printStackTrace();
        }
    }

    public static List<Product> loadCart(Context context) {
        String filename = getCartFilename();
        List<Product> cartList = new ArrayList<>();

        try (FileInputStream fIS = context.openFileInput(filename);
             InputStreamReader iSR = new InputStreamReader(fIS);
             BufferedReader bR = new BufferedReader(iSR)) {

            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Product>>(){}.getType();
            
            // Read directly from reader for better performance and reliability
            cartList = gson.fromJson(bR, listType);

            if (cartList == null) {
                cartList = new ArrayList<>();
            }
            
            Log.d(TAG, "Loaded cart with " + cartList.size() + " items from " + filename);

        } catch (FileNotFoundException e) {
            Log.d(TAG, "Cart file not found (" + filename + "), creating new empty list.");
            return new ArrayList<>();
        } catch (IOException e) {
            Log.e(TAG, "Error loading cart", e);
            e.printStackTrace();
            return new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error loading cart", e);
            return new ArrayList<>();
        }
        
        return cartList;
    }
}
