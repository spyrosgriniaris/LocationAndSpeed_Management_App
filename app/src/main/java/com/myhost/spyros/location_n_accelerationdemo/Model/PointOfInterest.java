package com.myhost.spyros.location_n_accelerationdemo.Model;

public class PointOfInterest {

    private String title;
    private String description;
    private String category;
    private double longitude;
    private double latitude;


    //=============CONSTRUCTOR==================================================================//
    public PointOfInterest(String title, String description, String category, double longitude, double latitude) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.longitude = longitude;
        this.latitude = latitude;
    }
    //==========================================================================================//


    //==========GETTERS & SETTERS================================================================//
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    //=======================================================//
}
