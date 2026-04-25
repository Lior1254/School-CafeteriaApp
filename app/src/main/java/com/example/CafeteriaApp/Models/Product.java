package com.example.CafeteriaApp.Models;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Represents a product in the cafeteria menu.
 * Implements Serializable for data passing between components.
 * Annotated with IgnoreExtraProperties to silence Firebase mapping warnings.
 */
@IgnoreExtraProperties
public class Product implements Serializable {
    private String id;
    private String name;
    private String description;
    private double price;
    private String category;
    private List<Addon> addons;
    private int placeholderResId;
    private int quantity;
    private String personalNotes;

    @Exclude
    private transient Bitmap imageBitmap;

    /**
     * Default constructor required for Firebase Realtime Database deserialization.
     * Initializes the addons list to prevent NullPointerExceptions.
     */
    public Product() {
        this.addons = new ArrayList<>();
        this.quantity = 0;
    }

    /**
     * Comprehensive constructor for creating a Product instance.
     *
     * @param id               Unique product identifier.
     * @param name             Display name of the product.
     * @param description      Detailed description.
     * @param price            Unit price of the product.
     * @param category         The menu category this product belongs to.
     * @param addons           List of available addons for this product.
     * @param placeholderResId Local resource ID for fallback image display.
     * @param quantity          Initial quantity (usually for cart operations).
     */
    public Product(String id, String name, String description, double price, String category,
                   List<Addon> addons, int placeholderResId, int quantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.addons = addons != null ? addons : new ArrayList<>();
        this.placeholderResId = placeholderResId;
        this.quantity = quantity;
    }

    /**
     * Formats the product price for UI display.
     * Renamed from 'get' to 'fetch' to avoid Firebase ClassMapper warnings.
     *
     * @return Formatted price string (e.g., "₪15.00").
     */
    @Exclude
    public String fetchPriceText() {
        return String.format(Locale.getDefault(), "₪%.2f", price);
    }

    /**
     * Generates a comma-separated string of currently selected addon IDs.
     * Renamed from 'get' to 'fetch' to avoid Firebase ClassMapper warnings.
     *
     * @return A string representing selected addon configurations.
     */
    @Exclude
    public String fetchSelectedOptions() {
        if (addons == null || addons.isEmpty()) {
            return "";
        }
        StringBuilder selected = new StringBuilder();
        for (Addon addon : addons) {
            if (addon != null && addon.isSelected()) {
                if (selected.length() > 0) {
                    selected.append(",");
                }
                selected.append(addon.getId());
            }
        }
        return selected.toString();
    }

    /**
     * Returns the unique identifier of the product.
     * @return The product ID string.
     */
    public String getId() {
        return id != null ? id : "";
    }

    /**
     * Sets the unique identifier for the product.
     * @param id The product ID string.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the localized name of the product.
     * @return The product name.
     */
    @NonNull
    public String getName() {
        return name != null ? name : "";
    }

    /**
     * Sets the localized name of the product.
     * @param name The product name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the detailed description of the product.
     * @return The product description.
     */
    @NonNull
    public String getDescription() {
        return description != null ? description : "";
    }

    /**
     * Sets the detailed description of the product.
     * @param description The product description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the unit price of the product.
     * @return The product price as double.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets the unit price of the product.
     * @param price The product price as double.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Returns the category of the product.
     * @return The category string.
     */
    @NonNull
    public String getCategory() {
        return category != null ? category : "";
    }

    /**
     * Sets the category of the product.
     * @param category The category string.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Returns the list of addons associated with the product.
     * @return List of Addon objects.
     */
    @NonNull
    public List<Addon> getAddons() {
        return addons != null ? addons : new ArrayList<>();
    }

    /**
     * Sets the list of addons associated with the product.
     * @param addons List of Addon objects.
     */
    public void setAddons(List<Addon> addons) {
        this.addons = addons;
    }

    /**
     * Returns the local drawable resource ID for placeholder image.
     * @return Integer resource ID.
     */
    public int getImageRes() {
        return placeholderResId;
    }

    /**
     * Sets the local drawable resource ID for placeholder image.
     * @param imageRes Integer resource ID.
     */
    public void setImageRes(int imageRes) {
        this.placeholderResId = imageRes;
    }

    /**
     * Returns the quantity of this product (used for orders and cart).
     * @return Current quantity as integer.
     */
    public int getAmount() {
        return quantity;
    }

    /**
     * Sets the quantity of this product.
     * @param amount Current quantity as integer.
     */
    public void setAmount(int amount) {
        this.quantity = amount;
    }

    /**
     * Returns personal notes added by the user for this product.
     * @return Personal notes string.
     */
    @NonNull
    public String getNotes() {
        return personalNotes != null ? personalNotes : "";
    }

    /**
     * Sets personal notes for this product.
     * @param notes Personal notes string.
     */
    public void setNotes(String notes) {
        this.personalNotes = notes;
    }

    /**
     * Returns the downloaded image bitmap. This is not saved to Firebase.
     * @return Bitmap object or null.
     */
    @Exclude
    @Nullable
    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    /**
     * Sets the downloaded image bitmap.
     * @param imageBitmap Bitmap object.
     */
    @Exclude
    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }
}
