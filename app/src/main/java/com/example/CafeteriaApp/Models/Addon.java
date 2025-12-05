package com.example.CafeteriaApp.Models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;

public class Addon implements Parcelable {
    @DrawableRes

    public final int iconRes;
    public final String name;
    public final double price;
    public boolean isAddonChecked;

    public Addon(int iconRes, String name, double price) {
        this.iconRes = iconRes;
        this.name = name;
        this.price = price;
        this.isAddonChecked = false;
    }

    public String getPriceText() {
        return "₪" + String.format("%.2f", price);
    }

    protected Addon(Parcel in) {
        iconRes = in.readInt();
        name = in.readString();
        price = in.readDouble();
        isAddonChecked = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(iconRes);
        dest.writeString(name);
        dest.writeDouble(price);
        dest.writeByte((byte) (isAddonChecked ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Addon> CREATOR = new Creator<Addon>() {
        @Override
        public Addon createFromParcel(Parcel in) {
            return new Addon(in);
        }

        @Override
        public Addon[] newArray(int size) {
            return new Addon[size];
        }
    };
}
