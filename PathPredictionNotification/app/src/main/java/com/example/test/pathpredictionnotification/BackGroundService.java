package com.example.test.pathpredictionnotification;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class BackGroundService extends Service {
    private IBinder serviceBinder = new ServiceBinder();
    private InToThread serviceThread;
    private ServiceHandler serviceHandler;
    private GpsManager gpsManager;
    private Callback callback;

    /*도로 관련 변수*/
    private boolean isRoad = true;
    private ArrayList<Segment> segmentArrayList;
    private RoadManager roadManager;
    private ArrayList<WeightSegment> weightSegmentArrayList;
    private Segment userSegment;        //유저 경로
    private Segment beforeUserSegment = null;
    private String userMobility;
    private Segment userPathLeftPredictionPath = null;
    private Segment userPathRightsPredictionPath = null;

    /*서비스가 실행될때*/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        gpsManager = new GpsManager(this);
        roadManager = new RoadManager();
        serviceHandler = new ServiceHandler();
        serviceThread = new InToThread(serviceHandler,3);
        serviceThread.start();

        return START_STICKY;
    }

    /*엑티비티로 데이터 넘겨줄때*/
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        return serviceBinder;
    }

    /*쓰레드 정지*/
    public void onDestroy() {
        serviceThread.stopForever();
        serviceThread = null;
    }

    /*메인으로 값을 넘겨주기 위한 클래스*/
    public class ServiceBinder extends Binder {
        public BackGroundService getBackGroundService(){
            return BackGroundService.this;
        }
    }

    /*알림정보를 받기 위한 클래스*/
    class NotificationTask extends AsyncTask<String, Void, String> {
        String sendMsg;
        String receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://203.234.62.92:8080/LBS_route/NotificationInformationData.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "sid=" + strings[0];
                osw.write(sendMsg);
                osw.flush();

                if(conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader inputStreamReader = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    StringBuffer stringBuffer = new StringBuffer();
                    while ((str = bufferedReader.readLine()) != null) {
                        stringBuffer.append(str);
                    }
                    receiveMsg = stringBuffer.toString();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return receiveMsg;
        }
    }

    /*핸들러*/
    class ServiceHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            //도로정보 한번민 가져오기
            if(isRoad) {
                segmentArrayList = callback.getSegmentArrayList();
                weightSegmentArrayList = callback.getWeightSegmentArrayList();
                isRoad = false;
            }
            gpsManager.Update();    //gps 정보 가져오기
            userSegment = roadManager.userPath(gpsManager.getGps().getGpsLatitude(),gpsManager.getGps().getGpsLongitude(), segmentArrayList);   //유저의 현재 위치한 도로

            ArrayList<Segment> userPathLefts = roadManager.userPathLefts(userSegment, segmentArrayList);                 //유저의 왼쪽으로 갈수 있는 경로 리스트
            ArrayList<Segment> userPathRight = roadManager.userPathRights(userSegment, segmentArrayList);                //유저의 오른쪽으로 갈수 있는 경로 리스트

            userPathLeftPredictionPath = null;
            userPathRightsPredictionPath = null;

            //처음 경로 예측을 켰을때 || 아무도 포함하지 안을때
            if(beforeUserSegment == null || (!roadManager.includeUserPathLeft(beforeUserSegment, userPathLefts) && !roadManager.includeUserPathRight(beforeUserSegment, userPathRight)
                    && !userSegment.getSid().equals(beforeUserSegment.getSid()))) {
                userPathLeftPredictionPath = roadManager.pathPrediction(userSegment,userPathLefts,weightSegmentArrayList);     //유저의 왼쪽 예측 경로
                userPathRightsPredictionPath = roadManager.pathPrediction(userSegment,userPathRight,weightSegmentArrayList);   //유저의 오른쪽 예측 경로
                pushNotification(userSegment.getSid(),"현재경로");
                pushNotification(userPathLeftPredictionPath.getSid(),"예측경로");
                pushNotification(userPathRightsPredictionPath.getSid(),"예측경로");
                userMobility = "all"; //유저의 방향성을 모두 둔다.

                //유저의 경로가 바뀔때
            }else if(!beforeUserSegment.getSid().equals(userSegment.getSid())) {
                //전에 유저경로가 지금 유저 경로 왼쪽 경로들이 포함할때
                if (roadManager.includeUserPathLeft(beforeUserSegment, userPathLefts)) {
                    userPathRightsPredictionPath = roadManager.pathPrediction(userSegment, userPathRight, weightSegmentArrayList);   //유저의 오른쪽 예측 경로
                    pushNotification(userSegment.getSid(),"현재경로");
                    pushNotification(userPathRightsPredictionPath.getSid(),"예측경로");
                    userMobility = "right"; //유저의 방향성을 오른쪽으로 둔다.
                    //전에 유저경로가 지금 유저 경로 오른쪽 경로들이 포함할때
                } else if (roadManager.includeUserPathRight(beforeUserSegment, userPathRight)) {
                    userPathLeftPredictionPath = roadManager.pathPrediction(userSegment, userPathLefts, weightSegmentArrayList);     //유저의 왼쪽 예측 경로
                    pushNotification(userSegment.getSid(),"현재경로");
                    pushNotification(userPathLeftPredictionPath.getSid(),"예측경로");
                    userMobility = "left";  //유저의 방향성을 왼쪽으로 둔다.
                }
                //유저의 경로가 바뀌지 안을때
            }else if(beforeUserSegment.getSid().equals(userSegment.getSid())) {
                userPathLeftPredictionPath = roadManager.pathPrediction(userSegment,userPathLefts,weightSegmentArrayList);     //유저의 왼쪽 예측 경로
                userPathRightsPredictionPath = roadManager.pathPrediction(userSegment,userPathRight,weightSegmentArrayList);   //유저의 오른쪽 예측 경로
            }

            beforeUserSegment = userSegment;    //전에 유저 경로
        }
    }

    /*엑비티비로 데이터 가져오기*/
    public interface Callback {
        public ArrayList<Segment> getSegmentArrayList();
        public ArrayList<WeightSegment> getWeightSegmentArrayList();
    }

    /*알림 보내기*/
    public void pushNotification(String notificationData, String location) {
        try {
            String result = notificationData;
            String data=  new NotificationTask(). execute(result).get();
            if(!data.equals("{\"sendNotification\":[]}")) {
                JsonData jsonData = new JsonData();
                jsonData.setJsonNotificationInformation(data);
                NotificationInformation notificationInformation = new NotificationInformation(jsonData.getJsonNotificationInformation());
                new AllNotificationManager().getPupupActivity(BackGroundService.this, location+" | "+notificationInformation.getKind()+" | "+notificationInformation.getDateTime(),notificationInformation.getContent());
            }
        } catch (Exception e) { }
    }

    /*get set 캡슐화*/
    public void registerCallback(Callback cb) { callback = cb; }
    public GpsManager getGpsManager() { return gpsManager; }
    public Segment getUserSegment() { return userSegment; }
    public Segment getUserPathLeftPredictionPath() { return userPathLeftPredictionPath; }
    public Segment getUserPathRightsPredictionPath() { return userPathRightsPredictionPath; }
    public String getUserMobility() { return userMobility; }
}
