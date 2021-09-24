package com.ztech.travelholic.Model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Location implements Serializable {
    String Category;
    String Description;
    String ID;
    String Lan;
    String Lon;
    long Rate;
    String Title;
    String Uri;

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public long getRate() {
        return Rate;
    }

    public void setRate(long rate) {
        Rate = rate;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getUri() {
        return Uri;
    }

    public void setUri(String uri) {
        Uri = uri;
    }

    public String getLan() {
        return Lan;
    }

    public void setLan(String lan) {
        Lan = lan;
    }
    public String getLon() {
        return Lon;
    }

    public void setLon(String lon) {
        Lon = lon;
    }

    public Location(String category, String description, String ID, String lan, String lon, long rate, String title, String uri) {
        Category = category;
        Description = description;
        this.ID = ID;
        Lan = lan;
        Lon = lon;
        Rate = rate;
        Title = title;
        Uri = uri;
    }

    public Location() {
    }
}
