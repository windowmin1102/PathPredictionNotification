package com.example.test.pathpredictionnotification;

/*
도로타입
 */

public class Road {
    private String rid;
    private Point point_left;
    private Point point_right;
    private String include_sid;

    public Road(String rid,Point point_left,Point point_right,String include_sid)
    {
        this.rid = rid;
        this.point_left = point_left;
        this.point_right = point_right;
        this.include_sid = include_sid;
    }

    /*get set*/
    public String getRid() {
        return rid;
    }
    public Point getPoint_left() {
        return point_left;
    }
    public Point getPoint_right() {
        return point_right;
    }
    public String getInclude_sid() {
        return include_sid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }
    public void setPoint_left(Point point_left) {
        this.point_left = point_left;
    }
    public void setPoint_right(Point point_right) {
        this.point_right = point_right;
    }
    public void setInclude_sid(String include_sid) {
        this.include_sid = include_sid;
    }
}
