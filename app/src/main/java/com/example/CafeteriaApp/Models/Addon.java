package com.example.CafeteriaApp.Models;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;

public class Addon implements Serializable {
    private String addonId;
    private String addonName;
    private double addonPrice;
    private boolean isSelected;
    private int imgRes;

    public Addon() {
    }

    public Addon(String addonId, String addonName, double addonPrice, int imgRes) {
        this.addonId = addonId;
        this.addonName = addonName;
        this.addonPrice = addonPrice;
        this.imgRes = imgRes;
        this.isSelected = false;
    }

    public Addon(String addonId, String addonName, double addonPrice, int imgRes, boolean isSelected) {
        this.addonId = addonId;
        this.addonName = addonName;
        this.addonPrice = addonPrice;
        this.imgRes = imgRes;
        this.isSelected = isSelected;
    }

    Addon[] withOutDefault(Addon[] addons)
    {
        Addon[] newAddon = new Addon[addons.length - 1];
        for(int i = 1; i < addons.length; i++)
        {
            newAddon[i - 1] = addons[i];
        }
        return newAddon;
    }

    // Getters and Setters
    public String getAddonId() { return addonId; }
    public void setAddonId(String addonId) { this.addonId = addonId; }

    public String getAddonName() { return addonName; }
    public void setAddonName(String addonName) { this.addonName = addonName; }

    public double getAddonPrice() { return addonPrice; }
    public void setAddonPrice(double addonPrice) { this.addonPrice = addonPrice; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    public int getImgRes() { return imgRes; }
    public void setImgRes(int imgRes) { this.imgRes = imgRes; }

    public String getPriceText() {
        return "₪" + String.format("%.2f", addonPrice);
    }


}
