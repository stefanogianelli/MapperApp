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

import com.stefano.andrea.adapters.CittaAdapter;
import com.stefano.andrea.adapters.FotoAdapter;
import com.stefano.andrea.fragments.DettagliViaggioFragment;
import com.stefano.andrea.fragments.ElencoFotoFragment;
import com.stefano.andrea.loaders.FotoLoader;
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.utils.MapperContext;
import com.stefano.andrea.utils.PhotoUtils;
import com.stefano.andrea.utils.ScrollableTabActivity;
import com.stefano.andrea.utils.ScrollableTabAdapter;
import com.stefano.andrea.utils.SlidingTabLayout;

import java.io.IOException;

public class DettagliViaggioActivity extends ScrollableTabActivity implements CittaAdapter.CittaOnClickListener, FotoAdapter.FotoOnClickListener {

    private long mIdViaggio;
    private Uri mImageUri;
    private MapperContext mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_viaggio);
        //recupero i parametri dal contesto
        mContext = MapperContext.getInstance();
        String mNomeViaggio = mContext.getNomeViaggio();
        mIdViaggio = mContext.getIdViaggio();
        //acquisito riferimenti
        View toolbarView = findViewById(R.id.dettagli_viaggio_toolbar);
        View headerView = findViewById(R.id.dettagli_viaggio_header);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        //attivo action bar
        setSupportActionBar((Toolbar) toolbarView);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //aggiungo il titolo alla action bar
        this.setTitle(mNomeViaggio);
        //Creo l'adapter per le tab
        TabDettagliViaggioAdapter mAdapter = new TabDettagliViaggioAdapter(getSupportFragmentManager());
        //assegno al pager l'adapter
        pager.setAdapter(mAdapter);
        //assegno i parametri alla superclasse per lo scrolling
        setParameters(mAdapter, pager, toolbarView, headerView);
        //configuro le tab
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
                propagateToolbarState(toolbarIsShown());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        propagateToolbarState(toolbarIsShown());
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
                mImageUri = PhotoUtils.getImageUri();
            } catch (IOException e) {
                Toast.makeText(this, "Errore durante l'accesso alla memoria", Toast.LENGTH_SHORT).show();
            }
            PhotoUtils.mostraDialog(this, mImageUri);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Avvia l'activity con i dettagli della citta
     * @param citta La citta selezionata
     */
    @Override
    public void selezionataCitta(Citta citta) {
        mContext.setIdCitta(citta.getIdCitta());
        mContext.setNomeCitta(citta.getNome());
        Intent intent = new Intent(this, DettagliCittaActivity.class);
        startActivity(intent);
    }

    @Override
    public void selezionataFoto(Foto foto) {
        //TODO: completare
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PhotoUtils.startIntent(this, requestCode, resultCode, data, mImageUri, mIdViaggio, -1);
    }

    private class TabDettagliViaggioAdapter extends ScrollableTabAdapter {

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
