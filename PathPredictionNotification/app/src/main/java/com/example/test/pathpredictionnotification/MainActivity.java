package com.example.test.pathpredictionnotification;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/*
2018-10-14
스위치 상태 저장하기 해야된다.
더이상 갈수 없을 길일때 버그 수정해야됨
데이터베이스 road 테이블 데이터 s12~s16 문제 있음 수정해야댐
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    /*탭 호스트 관련 변수*/
    private TabHost tabHost;             //탭 호스트
    private TabHost.TabSpec tabMap;      //맵
    private TabHost.TabSpec tabProFile; //내정보
    private TabHost.TabSpec tabBoard;   //게시판
    private TabHost.TabSpec tabOption;  //옵션

    /*웹뷰에 관련 변수*/
    private WebView webView;
    private WebSettings webSettings;

    /*구글맵 관련 변수*/
    private GoogleMap map;
    private boolean isCamera = true;
    private Marker userMarker;  //사용자 위치 마커
    private ArrayList<Polyline> polylines = new ArrayList<Polyline>();  //경로 표시

    /*내정보 관련 변수*/
    private TextView textProvider;  //위치제공
    private TextView textLatitude;  //위도
    private TextView textLongitude; //경도
    private TextView textAltitude;  //고도
    private TextView textRoadId;    //도로 아이디
    private TextView textRoadName;  //도로 이름

    /*옵션 관련 변수*/
    private Switch startSwitch;

    /*도로관련 변수*/
    private JsonData jsonData;        //서버 DB JSON
    private RoadManager roadManager;
    private ArrayList<Point> pointArrayList;
    private ArrayList<Road> roadArrayList;

    /*클릭 이벤트 테스트 삭제 예정*/
    private ArrayList<Segment> segmentArrayList;
    private ArrayList<WeightSegment> weightSegmentArrayList;
    private Segment beforeUserSegment = null;
    private String userMobility;

    /*백그라운드 서비스 관련 변수*/
    private Intent backGroundIntent;
    private BackGroundService backGroundService;
    private  boolean isService = false;
    private InToThread backGroundThread;
    private BackGroundHandler backGroundHandler;


    ServiceConnection serviceConnection = new ServiceConnection() {
        /*서비스와 연결되었을 때 호출되는 메서드*/
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BackGroundService.ServiceBinder serviceBinder = (BackGroundService.ServiceBinder) service;
            backGroundService = serviceBinder.getBackGroundService();
            backGroundService.registerCallback(callback);
            isService = true;
        }
        /*서비스와 연결이 끊기거나 종료할때*/
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isService = false;
        }
    };
    /*서비스 인터베이스 콜백 구현 서비스로 전달*/
    private BackGroundService.Callback callback = new BackGroundService.Callback() {
        public ArrayList<Segment> getSegmentArrayList() {
            return roadManager.getSegmentArr(jsonData.getJsonSegmentArray(), roadArrayList);
        }
        public ArrayList<WeightSegment> getWeightSegmentArrayList() {
            return roadManager.getWeightSegmentArr(jsonData.getJsonWeightSegmentArray());
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);

        /*탭 호스트*/
        tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        tabMap = tabHost.newTabSpec("tab1");    //탭 맵
        tabMap.setIndicator(null, getApplicationContext().getResources().getDrawable(R.drawable.tab_map));
        tabMap.setContent(R.id.tab1);
        tabHost.addTab(tabMap);

        tabProFile = tabHost.newTabSpec("tab2"); //탭 정보
        tabProFile.setIndicator(null,getApplicationContext().getResources().getDrawable(R.drawable.tab_profile));
        tabProFile.setContent(R.id.tab2);
        tabHost.addTab(tabProFile);

        tabBoard = tabHost.newTabSpec("tab3"); //탭 게시판
        tabBoard.setIndicator(null, getApplicationContext().getResources().getDrawable(R.drawable.tab_board));
        tabBoard.setContent(R.id.tab3);
        tabHost.addTab(tabBoard);

        tabOption = tabHost.newTabSpec("tab4"); //탭 옵션
        tabOption.setIndicator(null, getApplicationContext().getResources().getDrawable(R.drawable.tab_option));
        tabOption.setContent(R.id.tab4);
        tabHost.addTab(tabOption);

        /*탭 게시판 관련 웹뷰*/
        webView = (WebView)findViewById(R.id.webViewBoard);
        webView.setWebViewClient(new WebViewClient());
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl("http://203.234.62.92:8080/LBS_route/list.jsp");

        /*도로 데이터*/
        Intent intent = getIntent();
        String jsonPointData= intent.getStringExtra("jspPointData");
        String jsonRoadData = intent.getStringExtra("jspRoadData");
        String jsonSegmentData = intent.getStringExtra("jspSegmentData");
        String jsonWeightSegmentData = intent.getStringExtra("jspWeightSegmentData");
        jsonData = new JsonData(jsonPointData,jsonRoadData,jsonSegmentData,jsonWeightSegmentData);
        roadManager = new RoadManager();
        pointArrayList = roadManager.getPointArr(jsonData.getJsonPointArray());
        roadArrayList = roadManager.getRoadArr(jsonData.getJsonRoadArray(), pointArrayList);

        /*구글맵 클릭 이벤트 처리 테스트용 끝나는 즉시 삭제*/
        segmentArrayList = roadManager.getSegmentArr(jsonData.getJsonSegmentArray(), roadArrayList);
        weightSegmentArrayList = roadManager.getWeightSegmentArr(jsonData.getJsonWeightSegmentArray());


        /*구글맵*/
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*스위치*/
        startSwitch = (Switch)findViewById(R.id.startSwitch);
        startSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckState();
            }
        });

        /*내정보*/
        textProvider = (TextView) findViewById(R.id.textProvider);
        textLatitude = (TextView) findViewById(R.id.textLatitude);
        textLongitude = (TextView) findViewById(R.id.textLongitude);
        textAltitude = (TextView) findViewById(R.id.textAltitude);
        textRoadId  = (TextView) findViewById(R.id.textRoadId);
        textRoadName = (TextView) findViewById(R.id.textRoadName);
        /*백그라운드 서비스*/
        backGroundIntent = new Intent(MainActivity.this, BackGroundService.class);
        backGroundHandler = new BackGroundHandler();
        backGroundThread = new InToThread(backGroundHandler, 1);
        backGroundThread.start();

        /*GPS 퍼미션 허가요청*/
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( MainActivity.this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  }, 0 );
        }
    }

    /*구글맵 콜백*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        this.map.setMapType(GoogleMap.MAP_TYPE_NORMAL); //지도 타입 일반
        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.9452395, 126.6830059), 17));    //초기 구글맵 카메라 위치

        /*도로 찍기*/
        for(int i=0; i< roadArrayList.size(); i++) {
            LatLng start = new LatLng(roadArrayList.get(i).getPoint_left().getLatitude(), roadArrayList.get(i).getPoint_left().getLongitude());
            LatLng end = new LatLng(roadArrayList.get(i).getPoint_right().getLatitude(), roadArrayList.get(i).getPoint_right().getLongitude());
            if (start != null && end != null) {
                map.addPolygon(new PolygonOptions().add(start, end).strokeWidth(2).strokeColor(Color.BLACK).geodesic(true).visible(true));
            }
        }

        /*포인트 원 찍기*/
        for (int i = 0; i < pointArrayList.size(); i++) {
            double pointLatitude = pointArrayList.get(i).getLatitude();
            double pointLongitude = pointArrayList.get(i).getLongitude();
            CircleOptions circlePoint = new CircleOptions().center(new LatLng(pointLatitude, pointLongitude)).radius(1)
                    .fillColor(Color.parseColor("#000000")).strokeColor(Color.parseColor("#000000"));
            map.addCircle(circlePoint);
        }


        /*경로 예측 알고리즘 테스트 하기 위한 구글맵 클릭 이벤트 처리
        * 테스트 끝나면 삭제 예정이다.
        * 백그라운드 서비스와 같은 일을 한다(중복) 하지만 시뮬레이션 테스트 목적
        */
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            public void onMapClick(LatLng point) {
                double testLatitude = point.latitude;
                double testLongitude = point.longitude;

                removeUserMarker();     //유저마커 초기화
                removeUserPolyline();   //경로 초기화

                /*유저 마커 표시하기*/
                MarkerOptions makerOptions = new MarkerOptions();
                makerOptions.position(new LatLng(testLatitude, testLongitude));
                BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.usersmarker);
                Bitmap bitmap = bitmapDrawable.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 75, 75, false);
                makerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                userMarker = map.addMarker(makerOptions);

                Segment userSegment = roadManager.userPath(testLatitude,testLongitude,segmentArrayList);   //현재 경로 받기

                /*사용자 경로 표시하기*/
                drawPath(userSegment,"BLUE", 12);

                ArrayList<Segment> userPathLefts = roadManager.userPathLefts(userSegment, segmentArrayList);                 //유저의 왼쪽으로 갈수 있는 경로 리스트
                ArrayList<Segment> userPathRight = roadManager.userPathRights(userSegment, segmentArrayList);                //유저의 오른쪽으로 갈수 있는 경로 리스트
                Segment userPathLeftPredictionPath = null;
                Segment userPathRightsPredictionPath = null;

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

                //방향성 대로 경로 표시
               if(userMobility.equals("all")) {
                    drawPath(userPathLeftPredictionPath,"GREEN", 6);
                    drawPath(userPathRightsPredictionPath,"GREEN", 6);
                }else if(userMobility.equals("left")) {
                    drawPath(userPathLeftPredictionPath,"GREEN", 6);
                }else if(userMobility.equals("right")) {
                    drawPath(userPathRightsPredictionPath,"GREEN", 6);
                }

                beforeUserSegment = userSegment;    //전에 유저 경로

                /*텝 레이아웃 옵션에 있느 텍스트 뷰에 표시하기*/
                textRoadId.setText("도로아이디: "+userSegment.getSid());
                textRoadName.setText("도로이름: " + userSegment.getName());
            }
        });
    }

    /*핸들러*/
    class BackGroundHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            //서비스가 실행 되면
            if (isService) {
                //gps 로 위치정보를 받을수 있으면
                if (backGroundService.getGpsManager().canGetLocation()) {
                    removeUserMarker();     //유저마커 초기화
                    removeUserPolyline();   //경로 초기화

                    Segment userSegment = backGroundService.getUserSegment();   //현재 경로 받기

                    /*유저 마커 표시하기*/
                    MarkerOptions makerOptions = new MarkerOptions();
                    makerOptions.position(new LatLng(backGroundService.getGpsManager().getGps().getGpsLatitude(), backGroundService.getGpsManager().getGps().getGpsLongitude()));
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.usersmarker);
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 75, 75, false);
                    makerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                    userMarker = map.addMarker(makerOptions);

                    /*경로 표시하기*/
                    drawPath(userSegment,"BLUE",12);

                    /*딱 한번만 현재 위치에 카메라 놓기*/
                    if (isCamera && backGroundService.getGpsManager().getGps().getGpsLongitude() != 0 && backGroundService.getGpsManager().getGps().getGpsLatitude() != 0) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(backGroundService.getGpsManager().getGps().getGpsLatitude(),backGroundService.getGpsManager().getGps().getGpsLongitude()), 17));
                        isCamera = false;
                    }

                    /*예측 경로 가져오기*/
                    Segment userPathLeftPredictionPath = backGroundService.getUserPathLeftPredictionPath();
                    Segment userPathRightsPredictionPath = backGroundService.getUserPathRightsPredictionPath();

                    /*방향성 대로 경로 그리기*/
                    if(backGroundService.getUserMobility().equals("all")) {
                        drawPath(userPathLeftPredictionPath,"GREEN", 6);
                        drawPath(userPathRightsPredictionPath,"GREEN", 6);
                    }else if(backGroundService.getUserMobility().equals("left")) {
                        drawPath(userPathLeftPredictionPath,"GREEN", 6);
                    }else if(backGroundService.getUserMobility().equals("right")) {
                        drawPath(userPathRightsPredictionPath,"GREEN", 6);
                    }

                    /*텝 레이아웃 옵션에 있느 텍스트 뷰에 표시하기*/
                    textRoadId.setText("도로아이디: " + userSegment.getSid());
                    textRoadName.setText("도로이름: " + userSegment.getName());
                    textProvider.setText("위치제공: " + backGroundService.getGpsManager().getGps().getGpsProvider());
                    textLatitude.setText("위도: " + backGroundService.getGpsManager().getGps().getGpsLatitude());
                    textLongitude.setText("경도: " + backGroundService.getGpsManager().getGps().getGpsLongitude());
                    textAltitude.setText("고도: " + backGroundService.getGpsManager().getGps().getGpsAltitude());
                }
            }
        }
    }

    /*스타트 스위치 함수*/
    private void CheckState(){
        if (startSwitch.isChecked()) {
            startSwitch.setText("ON   ");
            Toast.makeText(getApplicationContext(),"경로예측을 시작합니다.", Toast.LENGTH_LONG).show();
            bindService(backGroundIntent, serviceConnection, Context.BIND_AUTO_CREATE); //인텐츠, 서비스연결객체, 플래그 전달
            startService(backGroundIntent);  //서비스 시작

        }else {
            startSwitch.setText("OFF   ");
            Toast.makeText(getApplicationContext(),"경로예측을 종료합니다.", Toast.LENGTH_LONG).show();
            unbindService(serviceConnection);
            isService = false;
            stopService(backGroundIntent);  //서비스종료
            //사용자 마커, 경로 지우기
            removeUserMarker();
            removeUserPolyline();

            isCamera = true;    //다시 실행시 카메라 1번 위치하게 하기

            /*옵션 정보 초기화*/
            textRoadId.setText("도로아이디: ");
            textRoadName.setText("도로이름: ");
            textProvider.setText("위치제공: 없음" );
            textLatitude.setText("위도: " );
            textLongitude.setText("경도: " );
            textAltitude.setText("고도: " );
        }
    }

    /*사용자 마커 지우기*/
    public void removeUserMarker(){
        if (userMarker != null) {
            userMarker.remove();
        }
    }
    /*경로 표시 지우기*/
    public void removeUserPolyline() {
        if (polylines != null) {
            for (int j = 0; j < polylines.size(); j++) {
                polylines.get(j).remove();
            }
        }
    }

    /*경로 표시*/
    public  void drawPath(Segment segment, String color, int width) {
        for(int i=0; i< segment.getRoads().size(); i++) {
            LatLng start = new LatLng(segment.getRoads().get(i).getPoint_left().getLatitude(), segment.getRoads().get(i).getPoint_left().getLongitude());   //왼쪽 포인트
            LatLng end = new LatLng(segment.getRoads().get(i).getPoint_right().getLatitude(), segment.getRoads().get(i).getPoint_right().getLongitude());   //오른쪽 포인트
            PolylineOptions polylineOptions = null;
            if(color.equals("BLUE")) {
                polylineOptions = new PolylineOptions().add(start, end).width(width).color(Color.BLUE).geodesic(true);
            }else if(color.equals("GREEN")){
                polylineOptions = new PolylineOptions().add(start, end).width(width).color(Color.GREEN).geodesic(true);
            }
            Polyline polyline = map.addPolyline(polylineOptions);
            polyline.setVisible(true);
            polylines.add(polyline);
        }
    }

    /*알림을 받기 위한 클래스
    * 구글맵 클릭 이벤트 처리 테스트용 끝나는 즉시 삭제
    * */
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
    /*
     * 구글맵 클릭 이벤트 처리 테스트용 끝나는 즉시 삭제
     * */
    public void pushNotification(String notificationData, String location) {
        try {
            String result = notificationData;
            String data=  new NotificationTask(). execute(result).get();
            if(!data.equals("{\"sendNotification\":[]}")) {
                jsonData.setJsonNotificationInformation(data);
                NotificationInformation notificationInformation = new NotificationInformation(jsonData.getJsonNotificationInformation());
                new AllNotificationManager().getPupupActivity(MainActivity.this, location+" | "+notificationInformation.getKind()+" | "+notificationInformation.getDateTime(),notificationInformation.getContent());
            }
        } catch (Exception e) { }
    }
}





