package com.stefano.andrea.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
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
import com.stefano.andrea.fragments.DettagliViaggioFragment;
import com.stefano.andrea.fragments.ElencoFotoFragment;
import com.stefano.andrea.fragments.MappaFragment;
import com.stefano.andrea.loaders.FotoLoader;
import com.stefano.andrea.utils.CustomFAB;
import com.stefano.andrea.utils.MapperContext;
import com.stefano.andrea.utils.PhotoUtils;
import com.stefano.andrea.utils.SlidingTabLayout;

import java.io.IOException;

public class DettagliViaggioActivity extends AppCompatActivity {

    private static final String BUNDLE_ID_VIAGGIO = "com.stefano.andrea.activities.DettagliViaggioActivity.idViaggio";
    private static final String BUNDLE_NOME_VIAGGIO = "com.stefano.andrea.activities.DettagliViaggioActivity.nomeViaggio";

    private static final int DETTAGLI_FRAGMENT = 0;
    private static final int FOTO_FRAGMENT = 1;
    private static final int MAPPA_FRAGMENT = 2;

    private long mIdViaggio;
    private String mNomeViaggio;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_viaggio);
        //recupero i parametri dal contesto
        MapperContext context = MapperContext.getInstance();
        if (savedInstanceState != null) {
            mIdViaggio = savedInstanceState.getLong(BUNDLE_ID_VIAGGIO);
            context.setIdViaggio(mIdViaggio);
            mNomeViaggio = savedInstanceState.getString(BUNDLE_NOME_VIAGGIO);
            context.setNomeViaggio(mNomeViaggio);
        } else {
            mNomeViaggio = context.getNomeViaggio();
            mIdViaggio = context.getIdViaggio();
        }
        //acquisito riferimenti
        Toolbar toolbarView = (Toolbar) findViewById(R.id.dettagli_viaggio_toolbar);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        //attivo action bar
        setSupportActionBar(toolbarView);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //aggiungo il titolo alla action bar
        this.setTitle(mNomeViaggio);
        //Creo l'adapter per le tab
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager());
        //assegno al pager l'adapter
        pager.setAdapter(adapter);
        //configuro le tab
        adapter.addTab(getString(R.string.title_tab_dettagli), DettagliViaggioFragment.newInstance(mIdViaggio), DETTAGLI_FRAGMENT);
        adapter.addTab(getString(R.string.title_tab_foto), ElencoFotoFragment.newInstance(mIdViaggio, FotoLoader.FOTO_VIAGGIO), FOTO_FRAGMENT);
        adapter.addTab(getString(R.string.title_tab_mappa), MappaFragment.newInstance(MappaFragment.MAPPA_CITTA), MAPPA_FRAGMENT);
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
        getMenuInflater().inflate(R.menu.menu_dettagli_viaggio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_aggiungi_foto_dettagli_viaggio) {
            try {
                mImageUri = PhotoUtils.getOutputMediaFileUri();
            } catch (IOException e) {
                Toast.makeText(this, "Errore durante l'accesso alla memoria", Toast.LENGTH_SHORT).show();
            }
            LinearLayout sugg = (LinearLayout) findViewById(R.id.suggerimento_crea_citta);
            if (sugg!=null && sugg.getVisibility()== View.VISIBLE){slideToBottom(sugg);}
            PhotoUtils.mostraDialog(this, mImageUri);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(BUNDLE_ID_VIAGGIO, mIdViaggio);
        outState.putString(BUNDLE_NOME_VIAGGIO, mNomeViaggio);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PhotoUtils.startIntent(this, requestCode, resultCode, data, mImageUri, mIdViaggio, -1);
    }

    public void slideToBottom(View view){
        TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight());
        animate.setDuration(500);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }
}