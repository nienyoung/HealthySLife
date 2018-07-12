package com.example.admin.healthyslife_android;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
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

    private ViewPager mViewPager;
    private BottomNavigationView mBottomNavigationView;

    private List<Fragment> mFragmentList = new ArrayList<Fragment>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.main_viewPager);
        mBottomNavigationView = findViewById(R.id.bottomNavigation);

        MapFragment mapFragment = MapFragment.newInstance();
        HealthyFragment healthyFragment = HealthyFragment.newInstance();
        mFragmentList.clear();
        mFragmentList.add(mapFragment);
        mFragmentList.add(healthyFragment);
        mViewPager.setAdapter(new MainViewPagerAdapter(getSupportFragmentManager(), mFragmentList));
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mBottomNavigationView.setSelectedItemId(R.id.navigation_exercise);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_exercise:
                    mViewPager.setCurrentItem(0, false);
                    return true;
                case R.id.navigation_healthy:
                    mViewPager.setCurrentItem(1, false);
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
                case 0:
                    mBottomNavigationView.setSelectedItemId(R.id.navigation_exercise);
                    break;
                case 1:
                    mBottomNavigationView.setSelectedItemId(R.id.navigation_healthy);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {}
    };
}
