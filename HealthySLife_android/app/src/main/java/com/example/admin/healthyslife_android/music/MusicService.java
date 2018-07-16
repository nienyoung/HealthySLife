package com.example.admin.healthyslife_android.music;

import android.animation.ObjectAnimator;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

/**
 * author:YangJie
 */
public class MusicService extends Service {

    public final IBinder binder = new MyBinder();
    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public static int isReturnTo = 0;
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    public MusicService() {

    }

   public void initPlayer(String path){
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playNewSong(String file_path) {
        try {
            mediaPlayer.setDataSource(file_path);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int flag = 0;
    public static String which = "";
    public void playOrPause() {
        flag++;
        if (flag >= 1000) flag = 2;

        which = "pause";

        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    public void resetPlayer(){
        mediaPlayer.reset();
    }


    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        super.onDestroy();
    }
    /**
     * onBind 是 Service 的虚方法
     * 返回 null，表示客服端不能建立到此服务的连接。
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}
