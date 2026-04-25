package com.example.CafeteriaApp.Models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data model representing extended user settings and historical data.
 * This class stores preferences like theme, notifications, and lists of user-specific entities.
 * Implements Serializable for passing via Intents or Bundles.
 */
public class UserData implements Serializable {

    private String userId;
    private List<Order> orderHistory;
    private List<Product> favoriteProducts;
    private boolean isDarkModeEnabled;
    private boolean isNotificationsEnabled;

    /**
     * Default constructor required for Firebase Realtime Database deserialization.
     * Initializes lists to prevent NullPointerExceptions.
     */
    public UserData() {
        this.orderHistory = new ArrayList<>();
        this.favoriteProducts = new ArrayList<>();
    }

    /**
     * Constructs a new UserData with basic preferences.
     *
     * @param userId                 The unique Firebase UID of the user.
     * @param isDarkModeEnabled      Initial preference for the dark theme.
     * @param isNotificationsEnabled Initial preference for push notifications.
     */
    public UserData(String userId, boolean isDarkModeEnabled, boolean isNotificationsEnabled) {
        this.userId = userId;
        this.isDarkModeEnabled = isDarkModeEnabled;
        this.isNotificationsEnabled = isNotificationsEnabled;
        this.orderHistory = new ArrayList<>();
        this.favoriteProducts = new ArrayList<>();
    }

    /**
     * Full constructor for cloning or manual creation.
     *
     * @param userId                 The unique Firebase UID.
     * @param orderHistory           List of past orders.
     * @param favoriteProducts       List of products marked as favorites.
     * @param isDarkModeEnabled      Dark theme preference.
     * @param isNotificationsEnabled Notifications preference.
     */
    public UserData(String userId, List<Order> orderHistory, List<Product> favoriteProducts,
                    boolean isDarkModeEnabled, boolean isNotificationsEnabled) {
        this.userId = userId;
        this.orderHistory = orderHistory != null ? orderHistory : new ArrayList<>();
        this.favoriteProducts = favoriteProducts != null ? favoriteProducts : new ArrayList<>();
        this.isDarkModeEnabled = isDarkModeEnabled;
        this.isNotificationsEnabled = isNotificationsEnabled;
    }

    /**
     * @return The unique identifier for the user.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId The unique identifier for the user.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return A list of the user\'s past orders. Never returns null.
     */
    @NonNull
    public List<Order> getOrderHistory() {
        return orderHistory != null ? orderHistory : new ArrayList<>();
    }

    /**
     * @param orderHistory The updated history of orders.
     */
    public void setOrderHistory(List<Order> orderHistory) {
        this.orderHistory = orderHistory;
    }

    /**
     * @return A list of favorite products. Never returns null.
     */
    @NonNull
    public List<Product> getFavoriteProducts() {
        return favoriteProducts != null ? favoriteProducts : new ArrayList<>();
    }

    /**
     * @param favoriteProducts The updated list of favorite products.
     */
    public void setFavoriteProducts(List<Product> favoriteProducts) {
        this.favoriteProducts = favoriteProducts;
    }

    /**
     * @return True if dark mode is enabled in settings.
     */
    public boolean isDarkModeEnabled() {
        return isDarkModeEnabled;
    }

    /**
     * @param darkModeEnabled New state for dark mode.
     */
    public void setDarkModeEnabled(boolean darkModeEnabled) {
        this.isDarkModeEnabled = darkModeEnabled;
    }

    /**
     * @return True if the user has opted into receiving notifications.
     */
    public boolean isNotificationsEnabled() {
        return isNotificationsEnabled;
    }

    /**
     * @param notificationsEnabled New state for notification preference.
     */
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        isNotificationsEnabled = notificationsEnabled;
    }
}
