package com.myhost.spyros.location_n_accelerationdemo;

public class PointOfInterest {

    private String title,description;
    private double latitude,longitude;
    private boolean home,work,other;

    public PointOfInterest(String title, String description, boolean home, boolean work, boolean other, double latitude, double longitude) {
        this.title = title;
        this.description = description;
        this.home = home;
        this.work = work;
        this.other = other;
        this.latitude = latitude;
        this.longitude = longitude;
    }

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

    public boolean isHome() {
        return home;
    }

    public void setHome(boolean home) {
        this.home = home;
    }

    public boolean isWork() { return work; }

    public void setWork(boolean work) {
        this.work = work;
    }

    public boolean isOther() {
        return other;
    }

    public void setOther(boolean other) {
        this.other = other;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
