package com.example.admin.healthyslife_android;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.example.admin.healthyslife_android.fragment.HealthyFragment;
import com.example.admin.healthyslife_android.fragment.MapFragment;
import com.example.admin.healthyslife_android.viewpager.MainViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wu jingji
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int MAP_FRAGMENT_POSITION = 0;
    private static final int HEALTHY_FRAGMENT_POSITION = 1;

    private static final String BUNDLE_LATENCY = "latency";
    private static final String BUNDLE_STEPS = "steps";

    private ViewPager mViewPager;
    private MainViewPagerAdapter mPagerAdapter;
    private BottomNavigationView mBottomNavigationView;

    private int mMaxDelay = 5000;
    private int mSteps;

    private long mStartTime = 0;

    Handler mTimerHandler = new Handler();
    Runnable mTimerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - mStartTime;

            // update text view
            MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(mPagerAdapter.getFragmentTag(MAP_FRAGMENT_POSITION));
            if (mapFragment == null) {
                Log.e(TAG, "Get map fragment fail");
                return;
            }
            mapFragment.updateTimeText(millis);

            mTimerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.main_viewPager);
        mBottomNavigationView = findViewById(R.id.bottomNavigation);

        MapFragment mapFragment = MapFragment.newInstance(onExerciseStateChangeListener);
        HealthyFragment healthyFragment = HealthyFragment.newInstance();
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.clear();
        fragmentList.add(mapFragment);
        fragmentList.add(healthyFragment);
        mPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), fragmentList);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mBottomNavigationView.setSelectedItemId(R.id.navigation_exercise);
    }

    /**
     * Records the state of the application into the {@link android.os.Bundle}.
     *
     * @param outState current state
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Store all variables required to restore the state of the application
        outState.putInt(BUNDLE_LATENCY, mMaxDelay);
        outState.putInt(BUNDLE_STEPS, mSteps);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            resetCounter();
            mSteps = savedInstanceState.getInt(BUNDLE_STEPS);
            mMaxDelay = savedInstanceState.getInt(BUNDLE_LATENCY);
        }
    }

    private void resetCounter() {
        mSteps = 0;
    }

    private void registerEventListener() {
        // Get the default sensor for the sensor type from the SenorManager
        SensorManager sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        if (sensorManager == null) {
            Log.e(TAG, "Get sensor manager fail while registering listener");
            return;
        }
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        // Register the listener for this sensor in batch mode.
        // If the max delay is 0, events will be delivered in continuous mode without batching.
        final boolean batchMode = sensorManager.registerListener(
                mListener, sensor, SensorManager.SENSOR_DELAY_NORMAL, mMaxDelay);

        if (!batchMode) {
            // Batch mode could not be enabled, show a warning message and switch to continuous mode
            Log.w(TAG, "Could not register sensor listener in batch mode, " +
                    "falling back to continuous mode.");
            sensorManager.registerListener(
                    mListener, sensor, SensorManager.SENSOR_DELAY_NORMAL, 0);
        }
    }

    /**
     * Unregisters the sensor listener if it is registered.
     */
    private void unregisterListeners() {
        SensorManager sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        if (sensorManager == null) {
            Log.e(TAG, "Get sensor manager fail while unregister");
            return;
        }
        sensorManager.unregisterListener(mListener);
        Log.i(TAG, "Sensor listener unregistered.");
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_exercise:
                    mViewPager.setCurrentItem(MAP_FRAGMENT_POSITION, false);
                    return true;
                case R.id.navigation_healthy:
                    mViewPager.setCurrentItem(HEALTHY_FRAGMENT_POSITION, false);
                    return true;
                default:
                    return false;
            }
        }
    };

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case MAP_FRAGMENT_POSITION:
                    mBottomNavigationView.setSelectedItemId(R.id.navigation_exercise);
                    break;
                case HEALTHY_FRAGMENT_POSITION:
                    mBottomNavigationView.setSelectedItemId(R.id.navigation_healthy);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {}
    };

    private MapFragment.OnExerciseStateChangeListener onExerciseStateChangeListener = new MapFragment.OnExerciseStateChangeListener() {
        @Override
        public void onStart() {
            mStartTime = System.currentTimeMillis();
            mTimerHandler.postDelayed(mTimerRunnable, 0);
//            registerEventListener();
        }

        @Override
        public void onStop() {
//            unregisterListeners();
            mTimerHandler.removeCallbacks(mTimerRunnable);
            MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(mPagerAdapter.getFragmentTag(MAP_FRAGMENT_POSITION));
            if (mapFragment == null) {
                Log.e(TAG, "Get map fragment fail");
                return;
            }
            mapFragment.updateTimeText(0);
        }
    };

    /**
     * Listener that handles step sensor events for step detector and step counter sensors.
     */
    private final SensorEventListener mListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                mSteps += event.values.length;

                // Update the text view with the latest step count
                HealthyFragment healthyFragment = (HealthyFragment) getSupportFragmentManager()
                        .findFragmentByTag(mPagerAdapter.getFragmentTag(HEALTHY_FRAGMENT_POSITION));
                if (healthyFragment == null) {
                    Log.e(TAG, "Get healthy fragment fail");
                    return;
                }
                healthyFragment.updateStepCounterText(mSteps);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public interface OnStepsChangeListener {
        void onStepsChange(int steps);
    }
}
