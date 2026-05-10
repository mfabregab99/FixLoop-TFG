package com.marcal.fixloop.model;

import com.google.gson.annotations.SerializedName;

public class Category {

    private int id;

    @SerializedName("nom")
    private String name;

    // Camp local per gestionar la UI
    private boolean isSelected = false;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public String toString() {
        return name;
    }
}