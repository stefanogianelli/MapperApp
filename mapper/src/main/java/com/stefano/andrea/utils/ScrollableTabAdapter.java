package com.stefano.andrea.utils;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * ScrollableTabAdapter
 */
public abstract class ScrollableTabAdapter extends FragmentStatePagerAdapter {

    private int mScrollY;

    public ScrollableTabAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setScrollY(int scrollY) {
        mScrollY = scrollY;
    }
}
