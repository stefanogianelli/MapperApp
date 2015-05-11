package com.stefano.andrea.adapters;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * TabAdapter
 */
public class TabAdapter extends FragmentPagerAdapter {

    private final HashMap<Integer, Fragment> mFragments;
    private final ArrayList<Integer> mTabNums;
    private final ArrayList<CharSequence> mTabTitles;

    @SuppressLint("UseSparseArrays")
    public TabAdapter(FragmentManager fm) {
        super(fm);
        mFragments = new HashMap<Integer, Fragment>(3);
        mTabNums = new ArrayList<Integer>(3);
        mTabTitles = new ArrayList<CharSequence>(2);
    }

    public void addTab(String tabTitle, Fragment newFragment, int tabId) {
        mTabTitles.add(tabTitle);
        mFragments.put(tabId, newFragment);
        mTabNums.add(tabId);
        notifyDataSetChanged();
    }

    public Fragment getTabFragment(int tabNum) {
        if (mFragments.containsKey(tabNum)) {
            return mFragments.get(tabNum);
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitles.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(mTabNums.get(position));
    }
}
