package com.example.test.pathpredictionnotification;

import java.util.ArrayList;
/*
* 세그먼트 -> 곡선 표현
*/
public class Segment {
    private String sid;
    private ArrayList<Road> roads;
    private String name;

    public Segment(){
        roads = new ArrayList<Road>();
    }

    /*get set*/
    public void setSid(String sid) {
        this.sid = sid;
    }
    public void setRoads(ArrayList<Road> roads) {
        this.roads = roads;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getSid() {
        return sid;
    }
    public ArrayList<Road> getRoads() {
        return roads;
    }
    public String getName() {
        return name;
    }
}
