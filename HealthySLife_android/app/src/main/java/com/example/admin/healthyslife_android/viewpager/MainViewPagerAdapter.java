package com.example.admin.healthyslife_android.viewpager;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.List;

/**
 * @author wu jingji
 */
public class MainViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragmentList;

    private SparseArray<String> fragmentTagMap = new SparseArray<>();

    public MainViewPagerAdapter(FragmentManager fm, List<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        fragmentTagMap.put(position, createdFragment.getTag());
        return createdFragment;
    }

    public String getFragmentTag(int position) {
        return fragmentTagMap.get(position);
    }
}
