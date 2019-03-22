package com.myhost.spyros.location_n_accelerationdemo;

public class ViolationClass {
    private String title;// an einai gia poi
    private long timestamp;
    private double speed;// an einai gia taxutita
    private double latitude;
    private double longitude;
    private String identifier;
    private int times;//se periptwsi pou einai gia poi na apothikeuei poses fores irthe konta


    public ViolationClass(String title, long timestamp, double speed, double latitude, double longitude, String identifier, int times) {
        this.title = title;
        this.timestamp = timestamp;
        this.speed = speed;
        this.latitude = latitude;
        this.longitude = longitude;
        this.identifier = identifier;
        this.times = times;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }



}
