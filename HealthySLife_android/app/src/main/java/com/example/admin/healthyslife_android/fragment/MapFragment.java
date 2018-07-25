package com.example.admin.healthyslife_android.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.admin.healthyslife_android.R;
import com.example.admin.healthyslife_android.music.MusicActivity;
import com.example.admin.healthyslife_android.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * @author wu jingji
 */
public class MapFragment extends Fragment {

    public static final int STATE_STOP = 0;
    public static final int STATE_PAUSE = 1;
    public static final int STATE_START = 2;

    private double mTotalDistance;

    private int mState;
    private Button mStartButton;
    private Button mPauseButton;
    private Button mStopButton;

    private View mHealthyInfoView;
    private TextView mStepsTextView;
    private TextView mStepFrequencyTextView;
    private TextView mSpeedTextView;

    MapView mMapView;
    BaiduMap mBaiduMap;
    private boolean isFirstLocate  = true;
    public LocationClient mLocationClient = null;
    private List<LatLng> points = new ArrayList<>();

    private OnExerciseStateChangeListener onExerciseStateChangeListener;
    public LatLng mLastPoint = null;
    private double mVelocity = 0;

    private void setOnExerciseStateChangeListener(OnExerciseStateChangeListener onExerciseStateChangeListener) {
        this.onExerciseStateChangeListener = onExerciseStateChangeListener;
    }

    public MapFragment() {}

    public static MapFragment newInstance(OnExerciseStateChangeListener onExerciseStateChangeListener) {
        MapFragment mapFragment = new MapFragment();
        mapFragment.setOnExerciseStateChangeListener(onExerciseStateChangeListener);
        return mapFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        mLocationClient = new LocationClient(getActivity().getBaseContext());
 //       mLocationClient = new LocationClient(getContext());
        mLocationClient = new LocationClient(getActivity().getApplicationContext());

        mLocationClient.registerLocationListener(new MyLocationListener());
        mMapView = (MapView) view.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.FOLLOWING, true, null,
                0xAAFFFF88, 0xAA00FF00));

        mBaiduMap.setMyLocationEnabled(true);

        List<String>permissionList  = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(getActivity(),Manifest.
                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(getActivity(),Manifest.
                permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(getActivity(),Manifest.
                permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String [] permissions= permissionList.toArray(new String[permissionList.
                    size()]);
            /*使用ActivityCompat 统一申请权限 */
            ActivityCompat.requestPermissions(getActivity(),permissions,1);
        }else {
            /*开始定位*/
            requestLocation();
        }




        mState = STATE_STOP;
        final Animation fadeInAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        final Animation fadeOutAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        mStopButton = view.findViewById(R.id.btn_map_stopRun);
        mStopButton.setVisibility(View.GONE);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mState == STATE_START || mState == STATE_PAUSE) {
                    mStartButton.setText(R.string.main_map_startExercise);
                    onExerciseStateChangeListener.onStop();
                    mState = STATE_STOP;
                    mStopButton.startAnimation(fadeOutAnim);
                    mStopButton.setVisibility(View.GONE);
                    mPauseButton.startAnimation(fadeOutAnim);
                    mPauseButton.setVisibility(View.GONE);
                    points.clear();
                    mTotalDistance=0;
                    mVelocity = 0;
                    mLastPoint = null;
                }
            }
        });
        mPauseButton = view.findViewById(R.id.btn_map_pauseRun);
        mPauseButton.setVisibility(View.GONE);
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mState == STATE_START) {
                    mPauseButton.setText(R.string.main_map_continueExercise);
                    onExerciseStateChangeListener.onPause();
                    mState = STATE_PAUSE;
                } else if (mState == STATE_PAUSE) {
                    mPauseButton.setText(R.string.main_map_pauseExercise);
                    onExerciseStateChangeListener.onContinue();
                    mState = STATE_START;
                }
            }
        });
        mStartButton = view.findViewById(R.id.btn_map_startRun);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button button = (Button) view;
                if (mState == STATE_STOP) {
                    mState = STATE_START;
                    button.setText(getTimeText(0));
                    onExerciseStateChangeListener.onStart();
                    mPauseButton.setVisibility(View.VISIBLE);
                    mPauseButton.startAnimation(fadeInAnim);
                    mStopButton.setVisibility(View.VISIBLE);
                    mStopButton.startAnimation(fadeInAnim);
                    points = new ArrayList<>();
                    mTotalDistance=0;
                    mVelocity = 0;
                    mLastPoint = null;
                }
            }
        });
        view.findViewById(R.id.btn_map_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MusicActivity.class);
                startActivity(intent);
                //切换效果
                getActivity().overridePendingTransition(R.anim.leftin, R.anim.leftout);
            }
        });
        view.findViewById(R.id.btn_map_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                //切换效果
                getActivity().overridePendingTransition(R.anim.leftin, R.anim.leftout);
            }
        });
        mHealthyInfoView = view.findViewById(R.id.main_map_healthyInfo);
        mHealthyInfoView.setVisibility(View.GONE);
        mStepsTextView = view.findViewById(R.id.tv_map_stepCounter);
        mStepFrequencyTextView = view.findViewById(R.id.tv_map_stepFrequency);
        mSpeedTextView = view.findViewById(R.id.tv_map_speed);
    }

    public void updateTimeText(long time) {
        mStartButton.setText(getTimeText(time));
    }

    public void updateStepCounterText(int steps) {
        mStepsTextView.setText(String.valueOf(steps));
    }

    public void updateStepFrequencyText(double frequency) {
        mStepFrequencyTextView.setText(getString(R.string.main_healthy_stepFrequencyFormat, frequency));
    }

    public void updateSpeedText(double speed) {
        mSpeedTextView.setText(getString(R.string.main_healthy_avgSpeedFormat, speed));
    }

    public void showHealthyInfo() {
        mHealthyInfoView.setVisibility(View.VISIBLE);
    }

    public void hideHealthyInfo() {
        mHealthyInfoView.setVisibility(View.GONE);
    }

    private String getTimeText(long time) {
        int seconds = (int) (time / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        minutes = minutes % 60;
        seconds = seconds % 60;
        return getString(R.string.main_map_timeFormat, hours, minutes, seconds);
    }

    public interface OnExerciseStateChangeListener {
        /**
         * It will be execute after user clicks the start button
         */
        void onStart();

        /**
         * It will be execute after user clicks the pause button
         */
        void onPause();

        /**
         * It will be execute after user clicks the pause button
         */
        void onContinue();

        /**
         * It will be execute after user clicks the stop button
         */
        void onStop();
    }

    private void requestLocation()       {
        initLocation();
        /*开始定位*/
        mLocationClient.start();
    }
    /*设置1000ms更新一次坐标位置信息*/
    private void initLocation(){
        LocationClientOption option = new  LocationClientOption();
        option.setScanSpan(1000);option.setOpenGps(true);option.setLocationNotify(true);option.setCoorType("bd09ll");option.setIgnoreKillProcess(true);
        mLocationClient.setLocOption(option);

    }

    @Override /*重写Activity 方法返回申请权限结果*/
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        switch(requestCode){
            case 1:
                if(grantResults.length > 0) {
                    for (int result:grantResults) {
                        if(result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(getActivity(),"必须同意所有权限才能使用本程序",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(getActivity(),"发生未知错误",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }

    }
    /*将当前位置显示在地图上*/
    private void navigateTo(BDLocation location){
        if(isFirstLocate){
//            /*获取经纬度*/
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
            mBaiduMap.animateMapStatus(update);
            update=MapStatusUpdateFactory.zoomTo(16f);
            mBaiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        /*获取当前位置 并显示到地图上*/
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        mBaiduMap.setMyLocationData(locationData);
        /*draw*/
        if (mState == STATE_START) {
            LatLng newPoint = new LatLng(location.getLatitude(), location.getAltitude());
            double deltaDistance = 0;
            if (mLastPoint != null) {
                deltaDistance = getDistance(newPoint, mLastPoint);
            }
            mTotalDistance += deltaDistance;
            mLastPoint = newPoint;
            mVelocity = deltaDistance;
            points.add(new LatLng(location.getLatitude(),location.getLongitude()));
            OverlayOptions ooPolyline = new PolylineOptions().width(10).color(0xAAFF0000).points(points);
            mBaiduMap.addOverlay(ooPolyline);
        }

    }

    public double getTotalDistance() {
        return mTotalDistance;
    }

    private static double getDistance(LatLng p1, LatLng p2) {
        double lat1, lng1, lat2, lng2;
        lat1 = p1.latitude;
        lng1 = p1.longitude;
        lat2 = p2.latitude;
        lng2 = p2.longitude;
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double a = radLat1 - radLat2;
        double b = Math.toRadians(lng1) - Math.toRadians(lng2);

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
                Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));

        // 地球半径
        s = s * 6371004;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            navigateTo(bdLocation);
        }
    }
}
