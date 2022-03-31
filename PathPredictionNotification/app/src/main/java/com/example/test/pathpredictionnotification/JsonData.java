package com.example.test.pathpredictionnotification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonData {
    private JSONArray JsonPointArray;
    private JSONArray JsonRoadArray;
    private JSONArray JsonSegmentArray;
    private JSONArray JsonWeightSegmentArray;
    private JSONArray JsonNotificationInformation;

    public JsonData(){

    }

    public JsonData(String jsonPointData,String jsonRoadData,String jsonSegmentData, String weightSegmentData) {
        try {
            JSONObject jsonObject_point=new JSONObject(jsonPointData);
            JSONObject jsonObject_road=new JSONObject(jsonRoadData);
            JSONObject jsonObject_segment=new JSONObject(jsonSegmentData);
            JSONObject jsonObject_weight_segment=new JSONObject(weightSegmentData);

            this.JsonPointArray = (JSONArray)jsonObject_point.get("sendPoint");
            this.JsonRoadArray = (JSONArray)jsonObject_road.get("sendRoad");
            this.JsonSegmentArray = (JSONArray)jsonObject_segment.get("sendSegment");
            this.JsonWeightSegmentArray = (JSONArray)jsonObject_weight_segment.get("sendWeightSegment");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getJsonPointArray() {
        return JsonPointArray;
    }
    public JSONArray getJsonRoadArray() {
        return JsonRoadArray;
    }
    public JSONArray getJsonSegmentArray() {
        return JsonSegmentArray;
    }
    public JSONArray getJsonWeightSegmentArray() {
        return JsonWeightSegmentArray;
    }

    public JSONArray getJsonNotificationInformation() {
        return JsonNotificationInformation;
    }
    public void setJsonNotificationInformation(String NotificationInForData) {
        JSONObject jsonObjectNotificationInformation = null;
        try {
            jsonObjectNotificationInformation = new JSONObject(NotificationInForData);
            JsonNotificationInformation = (JSONArray)jsonObjectNotificationInformation.get("sendNotification");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
