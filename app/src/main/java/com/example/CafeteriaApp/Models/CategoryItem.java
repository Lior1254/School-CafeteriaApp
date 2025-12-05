package com.example.CafeteriaApp.Models;

public class CategoryItem {
    private String name;
    private int iconRes;

    public CategoryItem(String name, int iconRes) {
        this.name = name;
        this.iconRes = iconRes;
    }

    public String getName() {
        return name;
    }

    public int getIconRes() {
        return iconRes;
    }
}
