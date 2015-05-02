package com.stefano.andrea.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.stefano.andrea.fragments.DettagliCittaFragment;
import com.stefano.andrea.fragments.ElencoFotoFragment;
import com.stefano.andrea.loaders.FotoLoader;
import com.stefano.andrea.utils.MapperContext;
import com.stefano.andrea.utils.PhotoUtils;
import com.stefano.andrea.utils.ScrollableTabActivity;
import com.stefano.andrea.utils.ScrollableTabAdapter;
import com.stefano.andrea.utils.SlidingTabLayout;

import java.io.IOException;

public class DettagliCittaActivity extends ScrollableTabActivity {

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
        View headerView = findViewById(R.id.dettagli_citta_header);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        //attivo action bar
        setSupportActionBar((Toolbar) toolbarView);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //aggiungo il titolo alla action bar
        this.setTitle(nomeCitta);
        //creo l'adapter per le tab
        TabDettagliCittaAdapter adapter =  new TabDettagliCittaAdapter(getSupportFragmentManager());
        //assegno l'adapter al pager
        pager.setAdapter(adapter);
        //assegno i parametri alla superclasse per lo scrolling
        setParameters(adapter, pager, toolbarView, headerView);
        //configuro tab
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
                mImageUri = PhotoUtils.getImageUri();
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

    public class TabDettagliCittaAdapter extends ScrollableTabAdapter {

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
    }

}
