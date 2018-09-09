package com.example.admin.healthyslife_android.bean;

import android.graphics.Bitmap;

public class Song {
    /**
     * 歌手
     */
    public String singer;
    /**
     * 歌曲名
     */
    public String song;
    /**
     * 歌曲的地址
     */
    public String path;
    /**
     * 歌曲长度
     */
    public int duration;
    /**
     * 歌曲的大小
     */
    public long size;
    /*
    *专辑图片
     */
    public String album;
    public long albumId;
    public Bitmap thumBitmap;

    public long songId;
}
