package com.stefano.andrea.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.stefano.andrea.fragments.DettagliViaggioFragment;
import com.stefano.andrea.fragments.FotoViaggioFragment;
import com.stefano.andrea.utils.ScrollableTabAdapter;


/**
 * TabDettagliViaggioAdapter
 */

public class TabDettagliViaggioAdapter  extends ScrollableTabAdapter {

    private CharSequence [] mTitles;
    private int mNumbOfTabs;

    public TabDettagliViaggioAdapter(FragmentManager fm, CharSequence [] titles, int numbOfTabSum) {
        super(fm);
        mTitles = titles;
        mNumbOfTabs = numbOfTabSum;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            return new DettagliViaggioFragment();
        } else {
            return new FotoViaggioFragment();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    @Override
    public int getCount() {
        return mNumbOfTabs;
    }
}