package com.example.test.pathpredictionnotification;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/*
 에플리케이션 실행시 로딩창
 JsonPointData.jsp json 형식 데이터를 받아
 MainActivity 로 전송하고 onCreate() 실행이 끝날때까지 기다린다.
*/

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        new NetWorkData().execute();
    }
    /**/
    class NetWorkData extends AsyncTask<Void, String, String> {
        private String point_data;

        private String road_data;
        private String segment_data;
        private String weight_segment_data;

        @Override
        protected String doInBackground(Void... params) {
            try {
                return (String)jspConn();
            } catch (Exception ex) {
                return "불러오기 실패";
            }
        }
        @Override
        protected void onPostExecute(String jspData) {
            try {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra("jspPointData",point_data);
                intent.putExtra("jspRoadData",road_data);
                intent.putExtra("jspSegmentData",segment_data);
                intent.putExtra("jspWeightSegmentData",weight_segment_data);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public String jspConn() {
            String body = "";
            try {
                /*포인트 정보 가져오기*/
                HttpClient  httpClient = new DefaultHttpClient();
                HttpPost httpPost_point = new HttpPost("http://203.234.62.92:8080/LBS_route/JsonPointData.jsp");
                HttpResponse httpResponse_point = httpClient.execute(httpPost_point);
                point_data = EntityUtils.toString(httpResponse_point.getEntity());
                /*도로 정보 가져오기*/
                HttpPost httpPost_road = new HttpPost("http://203.234.62.92:8080/LBS_route/JsonRoadData.jsp");
                HttpResponse httpResponse_road = httpClient.execute(httpPost_road);
                road_data = EntityUtils.toString(httpResponse_road.getEntity());
                /*세그먼트 정보 가져오기*/
                HttpPost httpPost_segment = new HttpPost("http://203.234.62.92:8080/LBS_route/JsonSegmentData.jsp");
                HttpResponse httpResponse_segment = httpClient.execute(httpPost_segment);
                segment_data = EntityUtils.toString(httpResponse_segment.getEntity());
                /*가중치 가져오기*/
                HttpPost httpPost_weight_segment = new HttpPost("http://203.234.62.92:8080/LBS_route/JsonWeightSegmentData.jsp");
                HttpResponse httpResponse_weight_segment = httpClient.execute(httpPost_weight_segment);
                weight_segment_data = EntityUtils.toString(httpResponse_weight_segment.getEntity());
            }catch (Exception e) {
                e.getStackTrace();
            }
            return body;
        }
    }
}