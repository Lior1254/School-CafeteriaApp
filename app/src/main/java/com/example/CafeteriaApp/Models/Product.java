package com.example.CafeteriaApp.Models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;

public class Product implements Parcelable {

    @DrawableRes
    public final int imageRes;
    public final String name;
    public final String description;
    public final double price;
    public final boolean supportAddon;
    public final Addon[] addons;

    public Product(int imageRes, String name, String description, double price,
                   boolean supportAddon, Addon[] addons) {
        this.imageRes = imageRes;
        this.name = name;
        this.description = description;
        this.price = price;
        this.supportAddon = supportAddon;
        this.addons = addons;
    }

    public String getPriceText() {
        return "₪" + String.format("%.2f", price);
    }

    // ---- Parcelable ----
    protected Product(Parcel in) {
        imageRes = in.readInt();
        name = in.readString();
        description = in.readString();
        price = in.readDouble();
        supportAddon = in.readByte() != 0;
        addons = in.createTypedArray(Addon.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(imageRes);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeDouble(price);
        dest.writeByte((byte) (supportAddon ? 1 : 0));
        dest.writeTypedArray(addons, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };
}
