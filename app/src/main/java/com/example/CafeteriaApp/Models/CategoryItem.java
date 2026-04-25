package com.example.CafeteriaApp.Models;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

/**
 * Data model representing a food category in the menu (e.g., Drinks, Sandwiches).
 * Stores the localized name and a resource reference to its icon.
 */
public class CategoryItem {

    private String name;
    private int iconRes;

    /**
     * Default constructor required for Firebase Realtime Database deserialization.
     */
    public CategoryItem() {
    }

    /**
     * Constructs a new CategoryItem with specified properties.
     *
     * @param name    The display name of the category (Hebrew).
     * @param iconRes The drawable resource ID for the category icon.
     */
    public CategoryItem(@NonNull String name, @DrawableRes int iconRes) {
        this.name = name;
        this.iconRes = iconRes;
    }

    /**
     * @return The localized name of the category.
     */
    @NonNull
    public String getName() {
        return name != null ? name : "";
    }

    /**
     * @param name The localized name of the category.
     */
    public void setName(@NonNull String name) {
        this.name = name;
    }

    /**
     * @return The resource ID of the category icon.
     */
    @DrawableRes
    public int getIconRes() {
        return iconRes;
    }

    /**
     * @param iconRes The resource ID of the category icon.
     */
    public void setIconRes(@DrawableRes int iconRes) {
        this.iconRes = iconRes;
    }
}
