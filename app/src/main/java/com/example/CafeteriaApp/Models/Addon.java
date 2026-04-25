package com.example.CafeteriaApp.Models;

import androidx.annotation.NonNull;
import com.google.firebase.database.Exclude;
import java.io.Serializable;
import java.util.Locale;

/**
 * Data model representing a product addon (e.g., extra cheese, egg).
 * Implements Serializable for intent passing between activities.
 * 
 * This class is designed to be compatible with Firebase Realtime Database.
 */
public class Addon implements Serializable {

    private String id;
    private String name;
    private double price;
    private boolean isSelected;
    private int imageResId;

    /**
     * Default constructor required for Firebase Realtime Database deserialization.
     */
    public Addon() {
    }

    /**
     * Constructs a new Addon with specified properties.
     *
     * @param id         Unique identifier for the addon.
     * @param name       Localized display name (Hebrew).
     * @param price      Additional cost for selecting this addon.
     * @param imageResId Resource ID for the addon icon/image.
     */
    public Addon(String id, String name, double price, int imageResId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.isSelected = false;
    }

    /**
     * @return The unique identifier of the addon, or an empty string if null.
     */
    public String getId() {
        return id != null ? id : "";
    }

    /**
     * @param id The unique identifier of the addon.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The localized name of the addon, or an empty string if null.
     */
    @NonNull
    public String getName() {
        return name != null ? name : "";
    }

    /**
     * @param name The localized name of the addon.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The additional price of the addon.
     */
    public double getPrice() {
        return price;
    }

    /**
     * @param price The additional price of the addon.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * @return True if the addon is currently selected by the user.
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * @param selected Selection state of the addon.
     */
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    /**
     * @return The drawable resource ID for the addon image.
     */
    public int getImageResId() {
        return imageResId;
    }

    /**
     * @param imageResId The drawable resource ID for the addon image.
     */
    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    /**
     * Alias for getId. Excluded from Firebase mapping to prevent warnings.
     * @return The unique identifier.
     */
    @Exclude
    public String getAddonId() {
        return getId();
    }

    /**
     * Alias for getName. Excluded from Firebase mapping to prevent warnings.
     * @return The localized name.
     */
    @Exclude
    public String getAddonName() {
        return getName();
    }

    /**
     * Formats the price for display with the currency symbol (₪).
     * Excluded from Firebase to prevent ClassMapper warnings.
     *
     * @return A formatted price string (e.g., "₪2.50").
     */
    @Exclude
    public String getFormattedPrice() {
        return String.format(Locale.getDefault(), "₪%.2f", price);
    }

    /**
     * Alias for getFormattedPrice. Excluded from Firebase mapping to prevent warnings.
     * @return A formatted price string.
     */
    @Exclude
    public String getPriceText() {
        return getFormattedPrice();
    }
}
