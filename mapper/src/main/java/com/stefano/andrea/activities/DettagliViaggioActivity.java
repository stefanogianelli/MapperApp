package com.stefano.andrea.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.stefano.andrea.fragments.DettagliViaggioFragment;
import com.stefano.andrea.fragments.ElencoFotoFragment;
import com.stefano.andrea.loaders.FotoLoader;
import com.stefano.andrea.utils.MapperContext;
import com.stefano.andrea.utils.PhotoUtils;
import com.stefano.andrea.utils.SlidingTabLayout;

import java.io.IOException;

public class DettagliViaggioActivity extends ActionBarActivity {

    private long mIdViaggio;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_viaggio);
        //recupero i parametri dal contesto
        MapperContext context = MapperContext.getInstance();
        String mNomeViaggio = context.getNomeViaggio();
        mIdViaggio = context.getIdViaggio();
        //acquisito riferimenti
        View toolbarView = findViewById(R.id.dettagli_viaggio_toolbar);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        //attivo action bar
        setSupportActionBar((Toolbar) toolbarView);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //aggiungo il titolo alla action bar
        this.setTitle(mNomeViaggio);
        //Creo l'adapter per le tab
        TabDettagliViaggioAdapter mAdapter = new TabDettagliViaggioAdapter(getSupportFragmentManager());
        //assegno al pager l'adapter
        pager.setAdapter(mAdapter);
        //configuro le tab
        tabs.setDistributeEvenly(true);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        tabs.setViewPager(pager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dettagli_viaggio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_aggiungi_foto_dettagli_viaggio) {
            try {
                mImageUri = PhotoUtils.getOutputMediaFileUri();
            } catch (IOException e) {
                Toast.makeText(this, "Errore durante l'accesso alla memoria", Toast.LENGTH_SHORT).show();
            }
            PhotoUtils.mostraDialog(this, mImageUri);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PhotoUtils.startIntent(this, requestCode, resultCode, data, mImageUri, mIdViaggio, -1);
    }

    private class TabDettagliViaggioAdapter extends FragmentStatePagerAdapter {

        private CharSequence [] mTitles = {"Dettagli", "Foto"};
        private int mNumbOfTabs = 2;

        public TabDettagliViaggioAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0) {
                return DettagliViaggioFragment.newInstance(mIdViaggio);
            } else {
                return ElencoFotoFragment.newInstance(mIdViaggio, FotoLoader.FOTO_VIAGGIO);
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
}