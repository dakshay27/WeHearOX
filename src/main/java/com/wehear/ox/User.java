package com.wehear.ox;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Comparator;

import static com.wehear.ox.MinuteUsedService.i;

public class User  {
    private String fname;
    private String lname;
    private String number;
    private String username;
    private String coderefer;
    private String bday;
    private String imageLink;
    private int PHI;
    private String currentUserId;
    private String gender;
    private String userOxId;

   public User(){

   }

   public User(String fname, String lname, String number, String username,String coderefer, String bday,String imageLink,int PHI,String gender) {
       this.fname = fname;
       this.lname = lname;
       this.number = number;
       this.username = username;
       this.coderefer = coderefer;
       this.bday = bday;
       this.imageLink = imageLink;
       this.PHI = PHI;
       this.gender=gender;

   }

   public User(String fname, String lname,String username,String imageLink,int PHI,String currentUserID,String gender)
   {
       this.fname =fname;
       this.lname = lname;
       this.username=username;
       this.imageLink=imageLink;
       this.PHI= PHI;
       this.currentUserId=currentUserID;
       this.gender= gender;
   }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getUsername() {
        return username;
    }

    public String getCoderefer() {
        return coderefer;
    }

    public String getBday() {
        return bday;
    }

    public void setCoderefer(String coderefer) {
        this.coderefer = coderefer;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setBday(String coderefer) {
        this.bday = bday;
    }


    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public int getPHI() {
        return PHI;
    }

    public void setPHI(int  PHI) {
        this.PHI = PHI;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUserOxId() {
        return userOxId;
    }

    public void setUserOxId(String userOxId) {
        this.userOxId = userOxId;
    }
}
