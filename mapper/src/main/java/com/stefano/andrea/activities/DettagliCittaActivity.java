package com.stefano.andrea.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
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

public class DettagliCittaActivity extends AppCompatActivity {

    private static final String BUNDLE_ID_VIAGGIO = "com.stefano.andrea.activities.DettagliCittaActivity.idViaggio";
    private static final String BUNDLE_ID_CITTA = "com.stefano.andrea.activities.DettagliCittaActivity.idCitta";
    private static final String BUNDLE_NOME_CITTA = "com.stefano.andrea.activities.DettagliCittaActivity.nomeCitta";

    private static final int DETTAGLI_FRAGMENT = 0;
    private static final int FOTO_FRAGMENT = 1;
    private static final int MAPPA_FRAGMENT = 2;

    private long mIdViaggio;
    private long mIdCitta;
    private String mNomeCitta;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_citta);
        //recupero i parametri dal contesto
        MapperContext context = MapperContext.getInstance();
        if (savedInstanceState != null) {
            mIdViaggio = savedInstanceState.getLong(BUNDLE_ID_VIAGGIO);
            context.setIdViaggio(mIdViaggio);
            mIdCitta = savedInstanceState.getLong(BUNDLE_ID_CITTA);
            context.setIdCitta(mIdCitta);
            mNomeCitta = savedInstanceState.getString(BUNDLE_NOME_CITTA);
            context.setNomeCitta(mNomeCitta);
        } else {
            mIdViaggio = context.getIdViaggio();
            mIdCitta = context.getIdCitta();
            mNomeCitta = context.getNomeCitta();
        }
        //acquisito riferimenti
        Toolbar toolbar = (Toolbar) findViewById(R.id.dettagli_citta_toolbar);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        //attivo action bar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //aggiungo il titolo alla action bar
        this.setTitle(mNomeCitta);
        //creo l'adapter per le tab
        TabAdapter adapter =  new TabAdapter(getSupportFragmentManager());
        //assegno l'adapter al pager
        pager.setAdapter(adapter);
        //configuro tab
        adapter.addTab(getString(R.string.title_tab_dettagli), DettagliCittaFragment.newInstance(mIdCitta), DETTAGLI_FRAGMENT);
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
            if (mImageUri != null) {
                LinearLayout sugg = (LinearLayout) findViewById(R.id.suggerimento_crea_posto);
                if (sugg!=null && sugg.getVisibility()== View.VISIBLE){slideToBottom(sugg);}
                PhotoUtils.mostraDialog(this, mImageUri);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(BUNDLE_ID_VIAGGIO, mIdViaggio);
        outState.putLong(BUNDLE_ID_CITTA, mIdCitta);
        outState.putString(BUNDLE_NOME_CITTA, mNomeCitta);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PhotoUtils.startIntent(this, requestCode, resultCode, data, mImageUri, mIdViaggio, mIdCitta);
    }

    public void slideToBottom(View view){
        TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight());
        animate.setDuration(500);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

}
