package com.example.CafeteriaApp.Models;

import android.graphics.Bitmap;
import com.google.firebase.database.Exclude;
import java.io.Serializable;
import java.util.List;

/**
 * Represents a product in the cafeteria menu.
 * Implements Serializable for easy data passing between Android components and Firebase compatibility.
 */
public class Product implements Serializable
{
    private String id;
    private String name;
    private String description;
    private double price;
    private String category;
    private List<Addon> addons; // List of available addons for this product
    private int imageRes; // Local drawable resource for placeholder
    private int amount; // Quantity of the product in cart/order

    @Exclude
    private transient Bitmap imageBitmap; // The downloaded image, excluded from Firebase

    // Empty constructor required for Firebase Realtime Database
    public Product()
    {
    }

    /**
     * Full constructor for creating a Product.
     */
    public Product(String id, String name, String description, double price, String category,
                   List<Addon> addons, int imageRes, int amount)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.addons = addons;
        this.imageRes = imageRes;
        this.amount = amount;
    }

    /**
     * Returns the formatted price string (e.g., "₪15.00").
     */
    public String getPriceText()
    {
        return "₪" + String.format("%.2f", price);
    }

    /**
     * Returns a string representation of selected addons for comparison.
     * This is used to determine if two product instances in the cart are identical.
     */
    public String getSelectedOptions() {
        if (addons == null || addons.isEmpty()) {
            return "";
        }
        StringBuilder selected = new StringBuilder();
        for (Addon addon : addons) {
            if (addon.isSelected()) {
                if (selected.length() > 0) {
                    selected.append(",");
                }
                selected.append(addon.getAddonId());
            }
        }
        return selected.toString();
    }

    //<editor-fold desc="Getters and Setters">
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public List<Addon> getAddons()
    {
        return addons;
    }

    public void setAddons(List<Addon> addons)
    {
        this.addons = addons;
    }

    public int getImageRes()
    {
        return imageRes;
    }

    public void setImageRes(int imageRes)
    {
        this.imageRes = imageRes;
    }

    public int getAmount()
    {
        return amount;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }

    @Exclude
    public Bitmap getImageBitmap()
    {
        return imageBitmap;
    }

    @Exclude
    public void setImageBitmap(Bitmap imageBitmap)
    {
        this.imageBitmap = imageBitmap;
    }
}
