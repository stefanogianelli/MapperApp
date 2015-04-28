package com.stefano.andrea.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.stefano.andrea.adapters.PostiAdapter;
import com.stefano.andrea.fragments.DettagliCittaFragment;
import com.stefano.andrea.fragments.FotoViaggioFragment;
import com.stefano.andrea.models.Posto;
import com.stefano.andrea.utils.ScrollableTabActivity;
import com.stefano.andrea.utils.ScrollableTabAdapter;
import com.stefano.andrea.utils.SlidingTabLayout;

public class DettagliCittaActivity extends ScrollableTabActivity implements PostiAdapter.PostoOnClickListener {

    private CharSequence [] mTitles = {"Posti","Foto"};
    private int mNumbOfTabs = 2;
    private long mIdViaggio;
    private long mIdCitta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_citta);
        //salvo i parametri ricevuti dall'intent
        String nomeCitta = "";
        if (getIntent() != null) {
            mIdViaggio = getIntent().getExtras().getLong(DettagliViaggioActivity.EXTRA_ID_VIAGGIO);
            mIdCitta = getIntent().getExtras().getLong(DettagliViaggioActivity.EXTRA_ID_CITTA);
            nomeCitta = getIntent().getExtras().getString(DettagliViaggioActivity.EXTRA_NOME_CITTA);
        }
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
        TabDettagliCittaAdapter adapter =  new TabDettagliCittaAdapter(getSupportFragmentManager(), mTitles, mNumbOfTabs);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void selezionatoPosto(Posto posto) {
        //TODO: completare
    }

    public class TabDettagliCittaAdapter extends ScrollableTabAdapter {

        private CharSequence [] mTitles;
        private int mNumbOfTabs;

        public TabDettagliCittaAdapter(FragmentManager fm, CharSequence [] titles, int numbOfTabSum) {
            super(fm);
            mTitles = titles;
            mNumbOfTabs = numbOfTabSum;
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0) {
                return DettagliCittaFragment.newInstance(mIdViaggio, mIdCitta);
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

}
