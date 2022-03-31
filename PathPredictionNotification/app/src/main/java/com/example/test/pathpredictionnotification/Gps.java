package com.example.test.pathpredictionnotification;

public class Gps {
    private double gpsLatitude;    //위도
    private double gpsLongitude;   //경도
    private double gpsAltitude;    //고도
    private float gpsAccuray;      //정확도
    private String gpsProvider;     //위치제공자

    /*get set*/
    public void setGpsAccuray(float gpsAccuray) {
        this.gpsAccuray = gpsAccuray;
    }
    public void setGpsLatitude(double gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
    }
    public void setGpsAltitude(double gpsAltitude) {
        this.gpsAltitude = gpsAltitude;
    }
    public void setGpsLongitude(double gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
    }
    public void setGpsProvider(String gpsProvider) {
        this.gpsProvider = gpsProvider;
    }

    public double getGpsLongitude() {
        return gpsLongitude;
    }
    public double getGpsLatitude() {
        return gpsLatitude;
    }
    public double getGpsAltitude() {
        return gpsAltitude;
    }
    public float getGpsAccuray() {
        return gpsAccuray;
    }
    public String getGpsProvider() {
        return gpsProvider;
    }
}
