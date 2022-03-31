package com.example.test.pathpredictionnotification;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import static android.content.Context.LOCATION_SERVICE;

/*
GPS를 이용해 위치정보를 받는 클래스 입니다.
실내에서는 NETWORK_PROVIDER 이용해 받고 실외에서는 GPS_PROVIDER 이용해 받습니다.
또한 에뮬레이터에서는 GPS는 동작하지 않습니다.
수정해야될거: 이 어플에서는 딱히 필요 없지만 실내외에 있을시 네트워크로 받아지지 않는다.(고려사항)
 */

public class GpsManager implements LocationListener {
    private Gps gps;
    private Context context;
    private boolean isGPSEnabled = false;       //gps 상태
    private boolean isNetworkEnabled = false;   //네트워크 상태
    private boolean canGetLocation = false; //gps 사용 상태
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; //m
    private static final long MIN_TIME_BW_UPDATES = 1000 * 3 * 1;    //시간
    private LocationManager locationManager;
    private Location location;

    /*생성자*/
    public GpsManager(Context context) {
        this.context = context;
        this.gps = new Gps();
    }

    /*위치정보 업데이트*/
    public void Update(){
        getLocation();
    }

    /*위치정보를 받아 GPS에 위도, 경도 저장*/
    public Location getLocation() {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            return null;
        }
        try {
            locationManager = (LocationManager) context
                    .getSystemService(LOCATION_SERVICE);

            // GPS로 받을수 있으면 TRUE
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // 네트워크로 받을수 있으면 TRUE
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                this.canGetLocation = false;
                //둘다 받을수 없으면
            } else {
                this.canGetLocation = true;

                // GPS로 받을수 있을때(위치정보는 네트워크보다 정확하나 실내에서 받을수 없음
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,  this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                gps.setGpsLatitude(location.getLatitude());
                                gps.setGpsLongitude(location.getLongitude());
                                gps.setGpsAccuray(location.getAccuracy());
                                gps.setGpsProvider(location.getProvider());
                            }
                        }

                        // 네트워크로 받을수 있을때(네트워크로 받을시는 부정확)
                } else if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            gps.setGpsLatitude(location.getLatitude());
                            gps.setGpsLongitude(location.getLongitude());
                            gps.setGpsAccuray(location.getAccuracy());
                            gps.setGpsProvider(location.getProvider());
                            gps.setGpsAltitude(location.getAltitude());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    /*GPS 정보를 가져오지 못했을때 설정값으로 갈지 물어보는 창*/
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("GPS 사용유무셋팅");
        alertDialog.setMessage("GPS 설정이 되지 않았습니다.. \n 설정창으로 가시겠습니까?");
        // OK 를 누르게 되면 설정창으로 이동합니다.
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    }
                });
        // NO 하면 종료 합니다.
        alertDialog.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onProviderDisabled(String provider) {
    }

    /*get set*/
    public Gps getGps() {
        return gps;
    }
    public void setGps(Gps gps) {
        this.gps = gps;
    }
    public boolean canGetLocation() {
        return this.canGetLocation;
    }
}