package com.example.test.pathpredictionnotification;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotificationInformation {
    private int num;
    private String segmentId;
    private String kind;
    private String content;
    private String dateTime;

    public NotificationInformation(JSONArray jsonArray) {
        try {
            for(int i=0; i <jsonArray.length(); i++) {
                JSONObject jObject = jsonArray.getJSONObject(i);
                num = jObject.getInt("num");
                segmentId = jObject.getString("segment_id");
                kind = jObject.getString("kind");
                content = jObject.getString("content");
                dateTime = jObject.getString("time");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getNum() {
        return num;
    }

    public String getContent() {
        return content;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getKind() {
        return kind;
    }

    public String getSegmentId() {
        return segmentId;
    }
}
