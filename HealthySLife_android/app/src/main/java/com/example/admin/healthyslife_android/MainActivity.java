package com.example.admin.healthyslife_android;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.admin.healthyslife_android.adapter.MainViewPagerAdapter;
import com.example.admin.healthyslife_android.fragment.HealthyFragment;
import com.example.admin.healthyslife_android.fragment.MapFragment;

import java.util.ArrayList;
import java.util.List;

import static com.example.admin.healthyslife_android.utils.HealthyUils.calorieCalculator;

/**
 * @author wu jingji
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int MAP_FRAGMENT_POSITION = 0;
    private static final int HEALTHY_FRAGMENT_POSITION = 1;

    /**
     * State of application, used to register for sensors when app is restored
     */
    public static final int STATE_STOP = 0;
    public static final int STATE_PAUSE = 1;
    public static final int STATE_START = 2;

    private static final String BUNDLE_STATE = "state";
    private static final String BUNDLE_LATENCY = "latency";
    private static final String BUNDLE_STEPS = "steps";
    private static final String BUNDLE_TIME = "time";

    private ViewPager mViewPager;
    private MainViewPagerAdapter mPagerAdapter;
    private BottomNavigationView mBottomNavigationView;

    private int mState;
    /**
     * Max batch latency is specified in microseconds
     */
    private int mMaxDelay = 0;
    /**
     * Steps counter
     */
    private int mSteps;
    /**
     * Total steps are calculated from this value
     */
    private int mCounterSteps = 0;
    /**
     * Record last timer time
     */
    private long mLastTime = 0;
    /**
     * Total run time
     */
    private long mTotalTime = 0;
    /**
     * Runner's velocity in X-axis
     */
    private double mVelX = 0;
    /**
     * Runner's velocity in Y-axis
     */
    private double mVelY = 0;
    /**
     * Runner's velocity in Z-axis
     */
    private double mVelZ = 0;
    /**
     * Timestamp the latest event happened in microseconds
     */
    private long mLastEventTime = 0;
    /**
     * Total distance the user run
     */
    private double mToTalDis = 0;

    /**
     * permanent data
     */
    private SharedPreferences settings;

    private Handler mTimerHandler = new Handler();
    private Runnable mTimerRunnable = new Runnable() {

        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            mTotalTime += currentTime - mLastTime;
            mLastTime = currentTime;

            // update time text view
            MapFragment mapFragment = getMapFragment();
            if (mapFragment == null) {
                return;
            }
            mapFragment.updateTimeText(mTotalTime);
            if (mTotalTime != 0) {
                mapFragment.updateStepFrequencyText((double) (mSteps * 60000) / mTotalTime);
                double velocity = mToTalDis * 1000 / mTotalTime;
                float calorie = calorieCalculator(getWeight(), mTotalTime / 1000, (float) velocity);
                mapFragment.updateSpeedText(velocity);
                HealthyFragment healthyFragment = getHealthyFragment();
                if (healthyFragment != null) {
                    healthyFragment.updateStepFrequencyText((double) (mSteps * 60000) / mTotalTime);
                    healthyFragment.updateSpeedText(velocity);
                    healthyFragment.updateCalorie(calorie);
                }
            }

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

        //get height and weight from settings
        settings = this.getSharedPreferences("mySettings", MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        verifyStoragePermissions(this);
        super.onResume();
        Log.d("hint", "handler post runnable");
    }

    @Override
    protected void onDestroy() {
        if (mState == STATE_START) {
            unregisterListeners();
            mTimerHandler.removeCallbacks(mTimerRunnable);
        }

        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
        outState.putInt(BUNDLE_STATE, mState);
        outState.putInt(BUNDLE_LATENCY, mMaxDelay);
        outState.putInt(BUNDLE_STEPS, mSteps);
        outState.putLong(BUNDLE_TIME, mTotalTime);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            resetCounter();
            mSteps = savedInstanceState.getInt(BUNDLE_STEPS);
            mMaxDelay = savedInstanceState.getInt(BUNDLE_LATENCY);
            mState = savedInstanceState.getInt(BUNDLE_STATE);
            mTotalTime = savedInstanceState.getLong(BUNDLE_TIME);

            if (mState == STATE_START) {
                registerEventListener();
            }
        }
    }

    @Nullable
    private MapFragment getMapFragment() {
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager()
                .findFragmentByTag(mPagerAdapter.getFragmentTag(MAP_FRAGMENT_POSITION));
        if (mapFragment == null) {
            Log.e(TAG, "Get map fragment fail");
            return null;
        }
        return mapFragment;
    }

    @Nullable
    private HealthyFragment getHealthyFragment() {
        HealthyFragment healthyFragment = (HealthyFragment) getSupportFragmentManager()
                .findFragmentByTag(mPagerAdapter.getFragmentTag(HEALTHY_FRAGMENT_POSITION));
        if (healthyFragment == null) {
            Log.e(TAG, "Get healthy fragment fail");
            return null;
        }
        return healthyFragment;
    }

    private float getHeight() {
        return Float.parseFloat(settings.getString("pref_key_user_height", "0"));
    }

    private float getWeight() {
        return Float.parseFloat(settings.getString("pref_key_user_weight", "0"));
    }

    private void resetCounter() {
        mSteps = 0;
        mCounterSteps = 0;
    }

    private void registerEventListener() {
        mCounterSteps = 0;
        // Get the default sensor for the sensor type from the SenorManager
        SensorManager sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        if (sensorManager == null) {
            Log.e(TAG, "Get sensor manager fail while registering listener");
            return;
        }
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor == null) {
            Toast.makeText(getApplicationContext(), R.string.main_map_getStepSensorFail, Toast.LENGTH_SHORT).show();
        } else {
            // Register the listener for this sensor in batch mode.
            // If the max delay is 0, events will be delivered in continuous mode without batching.
            final boolean stepBatchMode = sensorManager.registerListener(
                    mStepsListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL, mMaxDelay);

            if (!stepBatchMode) {
                // Batch mode could not be enabled, show a warning message and switch to continuous mode
                Log.w(TAG, "Could not register sensor listener in batch mode, " +
                        "falling back to continuous mode.");
                sensorManager.registerListener(
                        mStepsListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL, 0);
            }
        }

        Sensor accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (accelerationSensor == null) {
            Toast.makeText(getApplicationContext(), R.string.main_map_getAccelerationSensorFail, Toast.LENGTH_SHORT).show();
        } else {
            sensorManager.registerListener(
                    mAccelerateListener, accelerationSensor, SensorManager.SENSOR_DELAY_GAME);
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
        sensorManager.unregisterListener(mStepsListener);
        sensorManager.unregisterListener(mAccelerateListener);
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
            mState = STATE_START;
            mCounterSteps = 0;
            mSteps = 0;
            mLastTime = System.currentTimeMillis();
            mTotalTime = 0;
            mVelX = 0;
            mVelY = 0;
            mVelZ = 0;
            mLastEventTime = 0;
            mToTalDis = 0;
            mTimerHandler.postDelayed(mTimerRunnable, 0);
            registerEventListener();
            MapFragment mapFragment = getMapFragment();
            if (mapFragment != null) {
                mapFragment.showHealthyInfo();
            }
        }

        @Override
        public void onPause() {
            mState = STATE_PAUSE;
            mTimerHandler.removeCallbacks(mTimerRunnable);
        }

        @Override
        public void onContinue() {
            mState = STATE_START;
            mLastTime = System.currentTimeMillis();
            mVelX = 0;
            mVelY = 0;
            mVelZ = 0;
            mLastEventTime = 0;
            mTimerHandler.postDelayed(mTimerRunnable, 0);
        }

        @Override
        public void onStop() {
            MapFragment mapFragment = getMapFragment();
            if (mapFragment != null) {
                mapFragment.hideHealthyInfo();
            }
            unregisterListeners();
            mTimerHandler.removeCallbacks(mTimerRunnable);
            mLastTime = 0;
            mSteps = 0;
            mState = STATE_STOP;
        }
    };

    /**
     * Listener that handles step sensor events for step detector and step counter sensors.
     */
    private final SensorEventListener mStepsListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                if (mCounterSteps < 1) {
                    // initial value
                    mCounterSteps = (int) event.values[0];
                }

                // Calculate steps taken based on first counter value received.
                if (mState == STATE_START) {
                    mSteps += (int) event.values[0] - mCounterSteps;
                }
                mCounterSteps = (int) event.values[0];

                // Update the text view with the latest step count
                HealthyFragment healthyFragment = getHealthyFragment();
                if (healthyFragment == null) {
                    return;
                }
                healthyFragment.updateStepCounterText(mSteps);

                MapFragment mapFragment = getMapFragment();
                if (mapFragment == null) {
                    return;
                }
                mapFragment.updateStepCounterText(mSteps);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    /**
     * Listener that handles accelerometer events for accelerometer.
     */
    private final SensorEventListener mAccelerateListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                final long t = event.timestamp / 1000;
                if (mLastEventTime != 0) {
                    final float dT = (float) (t - mLastEventTime) / 1000000.f;

                    final float aX = event.values[0];
                    final float aY = event.values[1];
                    final float aZ = event.values[2];

                    // s = vt + 1/2 * a * t^2
                    double mDisX = mVelX * dT + aX * dT * dT / 2;
                    double mDisY = mVelY * dT + aY * dT * dT / 2;
                    double mDisZ = mVelZ * dT + aZ * dT * dT / 2;

                    // v1 = v0 + a * t
                    mVelX += aX * dT;
                    mVelY += aY * dT;
                    mVelZ += aZ * dT;

                    // s = (sx^2 + sy^2 + sz^2)^0.5
                    mToTalDis += Math.sqrt(mDisX * mDisX + mDisY * mDisY + mDisZ * mDisZ);
                }
                mLastEventTime = t;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    /**
     * Storage Permissions
     */
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.exit(0);
    }
}
