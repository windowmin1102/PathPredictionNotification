package com.example.test.pathpredictionnotification;

public class Point {
    private String pid;
    private double latitude;
    private double longitude;
    private boolean cross_road;

    public Point(String pid, double latitude, double longitude, boolean cross_road){
        this.pid = pid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.cross_road = cross_road;
    }

    public void setLatitude (double lat) {
        this.latitude = lat;
    }

    public void setLongitude (double lon) {
        this.longitude = lon;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public String getPid() {
        return this.pid;
    }
    public void setPid(String pid) {
        this.pid = pid;
    }
}
