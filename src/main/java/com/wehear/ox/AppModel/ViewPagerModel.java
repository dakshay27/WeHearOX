package com.wehear.ox.AppModel;

import android.media.Image;
import android.os.Parcel;
import android.os.Parcelable;

public class ViewPagerModel implements Parcelable {
    private String displayText;
    private int image;


    public ViewPagerModel() {
    }

    public ViewPagerModel(String displayText, int image) {
        this.displayText = displayText;
        this.image = image;
    }

    protected ViewPagerModel(Parcel in) {
        displayText = in.readString();
        image = in.readInt();
    }

    public static final Creator<ViewPagerModel> CREATOR = new Creator<ViewPagerModel>() {
        @Override
        public ViewPagerModel createFromParcel(Parcel in) {
            return new ViewPagerModel(in);
        }

        @Override
        public ViewPagerModel[] newArray(int size) {
            return new ViewPagerModel[size];
        }
    };

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(displayText);
        dest.writeInt(image);
    }
}
