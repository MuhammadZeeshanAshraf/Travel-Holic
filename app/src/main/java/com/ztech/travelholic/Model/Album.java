package com.ztech.travelholic.Model;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class Album implements Serializable {
    String Description;
    String ID;
    String Title;
    String Category;
    ArrayList<String> Uri;
    User User;

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

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public ArrayList<String> getUri() {
        return Uri;
    }

    public void setUri(ArrayList<String> uri) {
        Uri = uri;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public com.ztech.travelholic.Model.User getUser() {
        return User;
    }

    public void setUser(com.ztech.travelholic.Model.User user) {
        User = user;
    }
}
