package com.example.test.pathpredictionnotification;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
/*
* 도로 세팅 이나 경로 예측 알고리즘
* 2018/10/19(수정전)
*  데이터베이스 s13,s14,s15 포함하고 있는 road 데이터 문제 있음(차후에 해결 할것
* */
public class RoadManager {
    /*포인트 리스트*/
    public ArrayList<Point> getPointArr(JSONArray jsonArray) {
        ArrayList<Point> points = new ArrayList<Point>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jObject = jsonArray.getJSONObject(i);
                String id = jObject.getString("id");
                double latitude = jObject.getDouble("latitude");
                double longitude = jObject.getDouble("longitude");
                boolean croos_road = jObject.getBoolean("cross_road");
                Point point = new Point(id, latitude, longitude, croos_road);
                points.add(point);
            }
        }catch (Exception e) { }
        return points;
    }

    /*도로 리스트*/
    public ArrayList<Road> getRoadArr(JSONArray jsonArray, ArrayList<Point> points) {
        ArrayList<Road> roads = new ArrayList<Road>();
        Point point_left = null;
        Point point_right = null;
        try {
            for(int i=0; i <jsonArray.length(); i++) {
                JSONObject jObject = jsonArray.getJSONObject(i);
                String id = jObject.getString("id");
                String pid_left = jObject.getString("pid_left");
                String pid_right = jObject.getString("pid_right");
                String include_sid = jObject.getString("include_sid");
                for(int j=0; j< points.size(); j++) {
                    if(pid_left.equals(points.get(j).getPid())) {
                        point_left = points.get(j);
                    }
                    if(pid_right.equals(points.get(j).getPid())) {
                        point_right = points.get(j);
                    }
                }
                Road road = new Road(id,point_left,point_right,include_sid);
                roads.add(road);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return roads;
    }
    /*세그먼트 리스트*/
    public ArrayList<Segment> getSegmentArr(JSONArray jsonArray, ArrayList<Road> roads) {
        ArrayList<Segment> segments = new ArrayList<Segment>();
        Road road;
        try {
            for(int i=0; i <jsonArray.length(); i++) {
                JSONObject jObject = jsonArray.getJSONObject(i);
                String id = jObject.getString("id");
                String name = jObject.getString("name");
                Segment segment = new Segment();
                segment.setSid(id);
                segment.setName(name);
                for(int j =0; j < roads.size(); j++) {
                    if(id.equals(roads.get(j).getInclude_sid())) {
                        road = roads.get(j);
                        segment.getRoads().add(road);
                    }
                }
                segments.add(segment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return segments;
    }
    /*웨이트 리스트*/
    public ArrayList<WeightSegment> getWeightSegmentArr(JSONArray jsonArray) {
        ArrayList<WeightSegment> weightSegments = new ArrayList<WeightSegment>();
        try {
            for(int i=0; i <jsonArray.length(); i++) {
                JSONObject jObject = jsonArray.getJSONObject(i);
                String id = jObject.getString("id");
                String currentSegment = jObject.getString("current_segment");
                String arrivalSegment = jObject.getString("arrival_segment");
                double weight = jObject.getDouble("weight");
                WeightSegment weightSegment = new WeightSegment(id,currentSegment, arrivalSegment, weight);
                weightSegments.add(weightSegment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return weightSegments;
    }

    /*현재 유저가 포함한 도로*/
    public Segment userPath(double latitude, double longitude, ArrayList<Segment> segments) {
        Segment userSegment = null;
        double minH = 0;
        for(int i=0; i< segments.size(); i++) {
            for(int j=0; j < segments.get(i).getRoads().size(); j++) {
                Location locationA = new Location("A");
                Location locationB = new Location("B");
                Location user_locationC = new Location("C");
                locationA.setLatitude(segments.get(i).getRoads().get(j).getPoint_left().getLatitude());
                locationA.setLongitude(segments.get(i).getRoads().get(j).getPoint_left().getLongitude());
                locationB.setLatitude(segments.get(i).getRoads().get(j).getPoint_right().getLatitude());
                locationB.setLongitude(segments.get(i).getRoads().get(j).getPoint_right().getLongitude());
                user_locationC.setLatitude(latitude);
                user_locationC.setLongitude(longitude);

                double dist_AB = locationA.distanceTo(locationB);
                double dist_AC = locationA.distanceTo(user_locationC);
                double dist_CB = locationB.distanceTo(user_locationC);

                /*양쪽다 둔각이 아닐때(둔각일경우 버린다.)*/
                if(!(Math.pow(dist_AB,2)+Math.pow(dist_CB,2) <= Math.pow(dist_AC,2)) && !(Math.pow(dist_AC,2)+Math.pow(dist_AB,2) <= Math.pow(dist_CB,2))) {
                    double h = Math.sqrt(Math.pow(dist_AC,2) - Math.pow(((Math.pow(dist_AC,2) - Math.pow(dist_CB,2) + Math.pow(dist_AB,2))/(2*dist_AB)),2));    //user 와 도로의 직선 거리 구하기
                    if(minH == 0) {
                        minH = h;
                        userSegment = segments.get(i);
                    }else if(minH > h) {
                        minH = h;
                        userSegment = segments.get(i);
                    }
                }
            }
        }
        return userSegment;
    }

    /*사용자 경로의 왼쪽으로 갈수 있는 경로 리스트*/
    public ArrayList<Segment> userPathLefts(Segment userPath, ArrayList<Segment> segments ) {
        ArrayList<Segment> userPathLeftArr = new ArrayList<Segment>();
        Road userSegmentLeftRoad = userPath.getRoads().get(0);  //유저 세그먼트가 포함하고 있는 왼쪽 도로

        for(int i=0; i < segments.size(); i++) {
            //세그먼트들이 유저가 위치한 세그먼트랑 같지 안을때 세그먼트가 포함한 도로
            if(!segments.get(i).getSid().equals(userPath.getSid())) {
                for (int j = 0; j < segments.get(i).getRoads().size(); j++) {
                    //유저 도로의 맨 왼쪽 포인트와 연결되는 다른 도로 정보가 있으면
                    if ((userSegmentLeftRoad.getPoint_left().getPid().equals(segments.get(i).getRoads().get(j).getPoint_left().getPid())) ||
                            (userSegmentLeftRoad.getPoint_left().getPid().equals(segments.get(i).getRoads().get(j).getPoint_right().getPid()))) {

                        userPathLeftArr.add(segments.get(i));   //세그먼트를 추가
                    }
                }
            }
        }
        return userPathLeftArr;
    }

    /*사용자가 오른쪽으로 갈수 있는 경로 리스트*/
    public ArrayList<Segment> userPathRights(Segment userPath, ArrayList<Segment> segments ) {
        ArrayList<Segment> userPathRightArr = new ArrayList<Segment>();
        Road userSegmentRightRoad = userPath.getRoads().get(userPath.getRoads().size()-1);  //유저 세그먼트가 포함하고 있는 오른쪽 도로
        for(int i=0;  i < segments.size(); i++) {
            //세그먼트들이 유저가 위치한 세그먼트랑 같지 안을때 세그먼트가 포함한 도로
            if(!segments.get(i).getSid().equals(userPath.getSid())) {
                for (int j = 0; j < segments.get(i).getRoads().size(); j++) {
                    //유저의 왼쪽 도로 중 왼쪽,오른쪽 포인트가 세그먼트들의 도로중 포함하지 안을때
                    if((userSegmentRightRoad.getPoint_right().getPid().equals(segments.get(i).getRoads().get(j).getPoint_left().getPid())) ||
                            (userSegmentRightRoad.getPoint_right().getPid().equals(segments.get(i).getRoads().get(j).getPoint_right().getPid()))) {
                        userPathRightArr.add(segments.get(i));
                    }
                }
            }
        }
        return userPathRightArr;
    }

    /*경로 예측 하기*/
    public Segment pathPrediction(Segment userPath,ArrayList<Segment> userPaths, ArrayList<WeightSegment> weightSegments) {
        Segment predictionPath = null;
        double predictionWeight = 0;
        for (int i = 0; i < userPaths.size(); i++) {
            for (int j = 0; j < weightSegments.size(); j++) {
                //유저가 위치한 경로와 출발 지점 경로가 같으면
                if (userPath.getSid().equals(weightSegments.get(j).getCurrentSegment())) {
                    //유저가 갈수 있는 세그먼트들이랑 weight 도착 지점 경로가 같으면
                    if (weightSegments.get(j).getArrivalSegment().equals(userPaths.get(i).getSid())) {
                        if (predictionWeight == 0) {
                            predictionWeight = weightSegments.get(j).getWeight();
                            predictionPath = userPaths.get(i);
                        } else if (predictionWeight > weightSegments.get(j).getWeight()) {
                            predictionWeight = weightSegments.get(j).getWeight();
                            predictionPath = userPaths.get(i);
                        }
                    }
                }
            }
        }
        return predictionPath;
    }

    /*전에 유저경로가 왼쪽 경로들에 포함 될때*/
    public boolean includeUserPathLeft(Segment userPath, ArrayList<Segment> userPathLeft) {
        for(int i=0; i < userPathLeft.size(); i++) {
            if(userPath.getSid().equals(userPathLeft.get(i).getSid())) {
                return  true;
            }
        }
        return false;
    }

    /*전에 유저경로가 왼쪽 경로들에 포함 될때*/
    public boolean includeUserPathRight(Segment userPath, ArrayList<Segment> userPathRights) {
        for(int i=0; i < userPathRights.size(); i++) {
            if(userPath.getSid().equals(userPathRights.get(i).getSid())) {
                return  true;
            }
        }
        return false;
    }
}
