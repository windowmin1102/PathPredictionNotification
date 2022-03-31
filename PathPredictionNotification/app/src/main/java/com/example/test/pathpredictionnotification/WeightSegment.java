package com.example.test.pathpredictionnotification;

public class WeightSegment {
    private String wid;
    private String currentSegment;
    private String arrivalSegment;
    private double weight;

    public WeightSegment(String wid, String currentSegment,String arrivalSegment, double weight){
        this.wid = wid;
        this.currentSegment = currentSegment;
        this.arrivalSegment = arrivalSegment;
        this.weight = weight;
    }

    /*get set*/
    public String getWid() {
        return wid;
    }
    public String getCurrentSegment() {
        return currentSegment;
    }
    public String getArrivalSegment() {
        return arrivalSegment;
    }
    public double getWeight() {
        return weight;
    }

    public void setWid(String wid) {
        this.wid = wid;
    }
    public void setCurrentSegment(String currentSegment) {
        this.currentSegment = currentSegment;
    }
    public void setArrivalSegment(String arrivalSegment) {
        this.arrivalSegment = arrivalSegment;
    }
    public void setWeight(double weight) {
        this.weight = weight;
    }
}
