package com.example.admin.healthyslife_android.viewpager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author wu jingji
 */
public class MainViewPager extends ViewPager {

    public static final  String BAIDU_MAP_VIEW_NAME = "com.baidu.mapapi.map.MapView";

    public MainViewPager(@NonNull Context context) {
        super(context);
    }

    public MainViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        return v.getClass().getName().equals(BAIDU_MAP_VIEW_NAME) || super.canScroll(v, checkV, dx, x, y);
    }
}
