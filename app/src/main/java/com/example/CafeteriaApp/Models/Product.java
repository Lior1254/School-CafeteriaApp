package com.example.CafeteriaApp.Models;

import java.io.Serializable;
import java.util.List;

public class Product implements Serializable
{
    private String id;
    private String name;
    private String description;
    private double price;
    private String category;
    private List<Addon> addons; // Changed from array to List
    private int imageRes;
    private int amount;

    public Product()
    {

    }
    
    public Product(String id, String name, String description, double price, String category, List<Addon> addons, int imageRes, int amount)
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

    public String getPriceText()
    {
        return "₪" + String.format("%.2f", price);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Addon> getAddons() {
        return addons;
    }

    public void setAddons(List<Addon> addons) {
        this.addons = addons;
    }

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
