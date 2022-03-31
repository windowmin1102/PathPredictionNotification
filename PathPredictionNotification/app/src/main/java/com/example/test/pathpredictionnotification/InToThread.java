package com.example.test.pathpredictionnotification;

import android.os.Handler;

public class InToThread extends Thread{
    Handler handler;
    private int threadTime;
    private boolean canRun = true;

    public InToThread(Handler handler, int threadTime){
        this.handler = handler;
        this.threadTime = threadTime*1000;
    }

    public void stopForever(){
        synchronized (this) {
            this.canRun = false;
        }
    }

    public boolean isCanRun() {
        return this.canRun;
    }

    /*반복작업*/
    public void run(){
        while(canRun){
            handler.sendEmptyMessage(0);
            try{
                Thread.sleep(threadTime);
            }catch (Exception e) {}
        }
    }
}