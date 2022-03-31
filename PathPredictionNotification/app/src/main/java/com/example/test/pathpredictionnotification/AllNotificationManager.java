package com.example.test.pathpredictionnotification;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
/*
* 다양한 알림을 주는 클래스
* 더 추가 예정
* */

public class AllNotificationManager {


    public void getPupupActivity(Context context, String title , String text) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE); //진동
        Intent intent = new Intent(context, PopupActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        vibrator.vibrate(1000);
        context.startActivity(intent);

    }
}
