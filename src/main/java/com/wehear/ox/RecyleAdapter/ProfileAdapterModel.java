package com.wehear.ox.RecyleAdapter;

public class ProfileAdapterModel {
    private int imgIcon;
    private String txtHeading;

    public ProfileAdapterModel(int imgIcon, String txtHeading) {
        this.imgIcon = imgIcon;
        this.txtHeading = txtHeading;
    }

    public int getImgIcon() {
        return imgIcon;
    }

    public void setImgIcon(int imgIcon) {
        this.imgIcon = imgIcon;
    }

    public String getTxtHeading() {
        return txtHeading;
    }

    public void setTxtHeading(String txtHeading) {
        this.txtHeading = txtHeading;
    }
}
