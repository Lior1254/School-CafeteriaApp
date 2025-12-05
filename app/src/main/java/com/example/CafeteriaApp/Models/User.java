package com.example.CafeteriaApp.Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable
{
    private String uid;
    private List<Order> ordersHistory;
    private List<Product> favoriteProducts;
    private boolean darkMode;
    private boolean notificationsEnabled;

    public User() {
        this.ordersHistory = new ArrayList<>();
        this.favoriteProducts = new ArrayList<>();
    }

    public User(String uid, boolean darkMode, boolean notificationsEnabled) {
        this.uid = uid;
        this.darkMode = darkMode;
        this.notificationsEnabled = notificationsEnabled;
        this.ordersHistory = new ArrayList<>();
        this.favoriteProducts = new ArrayList<>();
    }

    public User(String uid, List<Order> ordersHistory, List<Product> favoriteProducts, boolean darkMode, boolean notificationsEnabled) {
        this.uid = uid;
        this.ordersHistory = ordersHistory;
        this.favoriteProducts = favoriteProducts;
        this.darkMode = darkMode;
        this.notificationsEnabled = notificationsEnabled;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<Order> getOrdersHistory() {
        return ordersHistory;
    }

    public void setOrdersHistory(List<Order> ordersHistory) {
        this.ordersHistory = ordersHistory;
    }

    public List<Product> getFavoriteProducts() {
        return favoriteProducts;
    }

    public void setFavoriteProducts(List<Product> favoriteProducts) {
        this.favoriteProducts = favoriteProducts;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}
