package com.stefano.andrea.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.stefano.andrea.adapters.TabDettagliViaggioAdapter;
import com.stefano.andrea.utils.SlidingTabLayout;

public class DettagliViaggioActivity extends ActionBarActivity {

    public final static String EXTRA_ID_CITTA = "com.stefano.andrea.DettagliViaggioActivity.idCitta";
    public final static String EXTRA_NOME_CITTA = "com.stefano.andrea.DettagliViaggioActivity.nomeCitta";
    private ViewPager mPager;
    private TabDettagliViaggioAdapter mAdapter;
    private SlidingTabLayout mTabs;
    private CharSequence [] mTitles = {"Dettagli","Foto"};
    private int mNumbOfTabs = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_viaggio);

        // Creating The TabDettagliViaggioAdapter and Passing Fragment Manager, mTitles fot the Tabs and Number Of Tabs.
        mAdapter =  new TabDettagliViaggioAdapter(getSupportFragmentManager(), mTitles, mNumbOfTabs);

        // Assigning ViewPager View and setting the mAdapter
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        // Assiging the Sliding Tab Layout View
        mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        mTabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the mTabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        mTabs.setViewPager(mPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
