package com.stefano.andrea.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.stefano.andrea.adapters.CittaAdapter;
import com.stefano.andrea.adapters.FotoAdapter;
import com.stefano.andrea.fragments.DettagliViaggioFragment;
import com.stefano.andrea.fragments.ElencoFotoFragment;
import com.stefano.andrea.loaders.FotoLoader;
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.utils.ScrollableTabActivity;
import com.stefano.andrea.utils.ScrollableTabAdapter;
import com.stefano.andrea.utils.SlidingTabLayout;

public class DettagliViaggioActivity extends ScrollableTabActivity implements CittaAdapter.CittaOnClickListener, FotoAdapter.FotoOnClickListener {

    public static final String EXTRA_ID_VIAGGIO = "com.stefano.andrea.mapper.DettagliViaggioActivity.idViaggio";
    public static final String EXTRA_ID_CITTA = "com.stefano.andrea.mapper.DettagliViaggioActivity.idCitta";
    public static final String EXTRA_NOME_CITTA = "com.stefano.andrea.mapper.DettagliViaggioActivity.nomeCitta";

    private CharSequence [] mTitles = {"Dettagli", "Foto"};
    private int mNumbOfTabs = 2;
    private long mIdViaggio;
    private String mNomeViaggio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_viaggio);
        //salvo i parametri ricevuti dall'intent
        if (getIntent().getExtras() != null) {
            mNomeViaggio = getIntent().getExtras().getString(MainActivity.EXTRA_NOME_VIAGGIO);
            mIdViaggio = getIntent().getExtras().getLong(MainActivity.EXTRA_ID_VIAGGIO);
        }
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
        TabDettagliViaggioAdapter adapter =  new TabDettagliViaggioAdapter(getSupportFragmentManager(), mTitles, mNumbOfTabs);
        //assegno al pager l'adapter
        pager.setAdapter(adapter);
        //assegno i parametri alla superclasse per lo scrolling
        setParameters(adapter, pager, toolbarView, headerView);
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
        getMenuInflater().inflate(R.menu.menu_activity_dettagli_viaggio, menu);
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

    /**
     * Avvia l'activity con i dettagli della citta
     * @param citta La citta selezionata
     */
    @Override
    public void selezionataCitta(Citta citta) {
        Intent intent = new Intent(this, DettagliCittaActivity.class);
        intent.putExtra(EXTRA_ID_VIAGGIO, mIdViaggio);
        intent.putExtra(EXTRA_ID_CITTA, citta.getId());
        intent.putExtra(EXTRA_NOME_CITTA, citta.getNome());
        startActivity(intent);
    }

    @Override
    public void selezionataFoto(Foto foto) {
        //TODO: completare
    }

    private class TabDettagliViaggioAdapter extends ScrollableTabAdapter {

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
