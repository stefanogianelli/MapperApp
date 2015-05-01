package com.stefano.andrea.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.stefano.andrea.adapters.FotoAdapter;
import com.stefano.andrea.adapters.PostiAdapter;
import com.stefano.andrea.fragments.DettagliCittaFragment;
import com.stefano.andrea.fragments.ElencoFotoFragment;
import com.stefano.andrea.loaders.FotoLoader;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.models.Posto;
import com.stefano.andrea.utils.DialogChooseFotoMode;
import com.stefano.andrea.utils.ScrollableTabActivity;
import com.stefano.andrea.utils.ScrollableTabAdapter;
import com.stefano.andrea.utils.SlidingTabLayout;

public class DettagliCittaActivity extends ScrollableTabActivity implements PostiAdapter.PostoOnClickListener, FotoAdapter.FotoOnClickListener {

    private static final String TAG = "DettagliCittaActivity";

    private CharSequence [] mTitles = {"Posti","Foto"};
    private int mNumbOfTabs = 2;
    private long mIdViaggio;
    private long mIdCitta;
    private Uri mImageUri;

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

        if (id == R.id.action_aggiungi_foto_dettagli_citta) {
            mImageUri = DialogChooseFotoMode.getImageUri();
            DialogChooseFotoMode.mostraDialog(this, mImageUri);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void selezionatoPosto(Posto posto) {
        //TODO: completare
    }

    @Override
    public void selezionataFoto(Foto foto) {
        //TODO: completare
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DialogChooseFotoMode.GALLERY_PICTURE && resultCode == RESULT_OK && data != null) {
            Log.v(TAG, data.getData().toString());
        } else if (requestCode == DialogChooseFotoMode.CAMERA_REQUEST && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, ModInfoFotoActivity.class);
            intent.putExtra(ModInfoFotoActivity.EXTRA_FOTO, mImageUri.toString());
            intent.putExtra(ModInfoFotoActivity.EXTRA_ID_VIAGGIO, mIdViaggio);
            intent.putExtra(ModInfoFotoActivity.EXTRA_ID_CITTA, mIdCitta);
            startActivity(intent);
        }
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
