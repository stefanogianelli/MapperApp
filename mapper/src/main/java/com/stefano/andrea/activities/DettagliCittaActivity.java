package com.stefano.andrea.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.stefano.andrea.adapters.TabAdapter;
import com.stefano.andrea.fragments.DettagliCittaFragment;
import com.stefano.andrea.fragments.ElencoFotoFragment;
import com.stefano.andrea.fragments.MappaFragment;
import com.stefano.andrea.loaders.FotoLoader;
import com.stefano.andrea.utils.MapperContext;
import com.stefano.andrea.utils.PhotoUtils;
import com.stefano.andrea.utils.SlidingTabLayout;

import java.io.IOException;

public class DettagliCittaActivity extends ActionBarActivity {

    private static final int DETTAGLI_FRAGMENT = 0;
    private static final int FOTO_FRAGMENT = 1;
    private static final int MAPPA_FRAGMENT = 2;

    private long mIdViaggio;
    private long mIdCitta;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_citta);
        //recupero i parametri dal contesto
        MapperContext mContext = MapperContext.getInstance();
        mIdViaggio = mContext.getIdViaggio();
        mIdCitta = mContext.getIdCitta();
        String nomeCitta = mContext.getNomeCitta();
        //acquisito riferimenti
        View toolbarView = findViewById(R.id.dettagli_citta_toolbar);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        //attivo action bar
        setSupportActionBar((Toolbar) toolbarView);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //aggiungo il titolo alla action bar
        this.setTitle(nomeCitta);
        //creo l'adapter per le tab
        TabAdapter adapter =  new TabAdapter(getSupportFragmentManager());
        //assegno l'adapter al pager
        pager.setAdapter(adapter);
        //configuro tab
        adapter.addTab(getString(R.string.title_tab_dettagli), DettagliCittaFragment.newInstance(mIdViaggio, mIdCitta), DETTAGLI_FRAGMENT);
        adapter.addTab(getString(R.string.title_tab_foto), ElencoFotoFragment.newInstance(mIdCitta, FotoLoader.FOTO_CITTA), FOTO_FRAGMENT);
        adapter.addTab(getString(R.string.title_tab_mappa), MappaFragment.newInstance(MappaFragment.MAPPA_POSTI), MAPPA_FRAGMENT);
        tabs.setDistributeEvenly(true);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        tabs.setViewPager(pager);
        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dettagli_citta, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_aggiungi_foto_dettagli_citta) {
            try {
                mImageUri = PhotoUtils.getOutputMediaFileUri();
            } catch (IOException e) {
                Toast.makeText(this, "Errore durante l'accesso alla memoria", Toast.LENGTH_SHORT).show();
            }
            if (mImageUri != null)
                PhotoUtils.mostraDialog(this, mImageUri);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PhotoUtils.startIntent(this, requestCode, resultCode, data, mImageUri, mIdViaggio, mIdCitta);
    }

    /*public class TabDettagliCittaAdapter extends FragmentStatePagerAdapter {

        private CharSequence [] mTitles = {"Posti","Foto"};
        private int mNumbOfTabs = 2;

        public TabDettagliCittaAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0) {
                return DettagliCittaFragment.newInstance(mIdViaggio, mIdCitta);
            } else {
                return ElencoFotoFragment.newInstance(mIdCitta, FotoLoader.FOTO_CITTA);
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
    }*/

}
