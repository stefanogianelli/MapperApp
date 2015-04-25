package com.stefano.andrea.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.stefano.andrea.fragments.DettagliCittaFragment;
import com.stefano.andrea.fragments.FotoViaggioFragment;

/**
 * Created by Andre on 25/04/2015.
 */
public class TabDettagliCittaAdapter  extends FragmentStatePagerAdapter{

    private CharSequence [] mTitles;
    private int mNumbOfTabs;

    // Build a Constructor and assign the passed Values to appropriate values in the class
    public TabDettagliCittaAdapter(FragmentManager fm, CharSequence [] titles, int numbOfTabSum) {
        super(fm);
        mTitles = titles;
        mNumbOfTabs = numbOfTabSum;
    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        if(position == 0) // if the position is 0 we are returning the First tab
        {
            DettagliCittaFragment tab1 = new DettagliCittaFragment();
            return tab1;
        }
        else             // As we are having 2 tabs if the position is now 0 it must be 1 so we are returning second tab
        {
            FotoViaggioFragment tab2 = new FotoViaggioFragment();
            return tab2;
        }
    }

    // This method return the titles for the Tabs in the Tab Strip
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    // This method return the Number of tabs for the tabs Strip
    @Override
    public int getCount() {
        return mNumbOfTabs;
    }
}