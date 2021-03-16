package com.wehear.ox.AppModel;

import android.os.Parcel;
import android.os.Parcelable;

public class ViewPagerPermissionModel implements Parcelable {
    private String permissionName;
    private int image;
    private String permissionDes;


    public ViewPagerPermissionModel(String permissionName, int image, String permissionDes) {
        this.permissionName = permissionName;
        this.image = image;
        this.permissionDes = permissionDes;
    }

    public ViewPagerPermissionModel() {
    }

    protected ViewPagerPermissionModel(Parcel in) {
    }

    public static final Creator<ViewPagerPermissionModel> CREATOR = new Creator<ViewPagerPermissionModel>() {
        @Override
        public ViewPagerPermissionModel createFromParcel(Parcel in) {
            return new ViewPagerPermissionModel(in);
        }

        @Override
        public ViewPagerPermissionModel[] newArray(int size) {
            return new ViewPagerPermissionModel[size];
        }
    };

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getPermissionDes() {
        return permissionDes;
    }

    public void setPermissionDes(String permissionDes) {
        this.permissionDes = permissionDes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(permissionName);
        dest.writeString(permissionDes);
        dest.writeInt(image);
    }
}
