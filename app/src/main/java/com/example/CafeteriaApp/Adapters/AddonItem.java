package com.example.CafeteriaApp.Adapters;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;

public class AddonItem implements Parcelable {
    @DrawableRes
    public final int iconRes;
    public final String name;
    public final double price;
    public boolean isAddonChecked;

    public AddonItem(int iconRes, String name, double price) {
        this.iconRes = iconRes;
        this.name = name;
        this.price = price;
        this.isAddonChecked = false;
    }

    public String getPriceText() {
        return "₪" + String.format("%.2f", price);
    }

    protected AddonItem(Parcel in) {
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

    public static final Creator<AddonItem> CREATOR = new Creator<AddonItem>() {
        @Override
        public AddonItem createFromParcel(Parcel in) {
            return new AddonItem(in);
        }

        @Override
        public AddonItem[] newArray(int size) {
            return new AddonItem[size];
        }
    };
}
