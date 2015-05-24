package com.stefano.andrea.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.stefano.andrea.fragments.ElencoFotoFragment;
import com.stefano.andrea.loaders.FotoLoader;
import com.stefano.andrea.utils.MapperContext;
import com.stefano.andrea.utils.PhotoUtils;

import java.io.IOException;

public class DettagliPostoActivity extends AppCompatActivity {

    private static final String BUNDLE_ID_VIAGGIO = "com.stefano.andrea.activities.DettagliPostoActivity.idViaggio";
    private static final String BUNDLE_ID_CITTA = "com.stefano.andrea.activities.DettagliPostoActivity.idCitta";
    private static final String BUNDLE_ID_POSTO = "com.stefano.andrea.activities.DettagliPostoActivity.idPosto";
    private static final String BUNDLE_NOME_POSTO = "com.stefano.andrea.activities.DettagliPostoActivity.nomePosto";

    private Uri mImageUri;
    private long mIdViaggio;
    private long mIdCitta;
    private long mIdPosto;
    private String mNomePosto;
    private LinearLayout sugg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_posto);
        //acquisisco riferimenti
        sugg = (LinearLayout) findViewById(R.id.suggerimento_crea_foto);
        MapperContext context = MapperContext.getInstance();
        if (savedInstanceState != null) {
            mIdViaggio = savedInstanceState.getLong(BUNDLE_ID_VIAGGIO);
            context.setIdViaggio(mIdViaggio);
            mIdCitta = savedInstanceState.getLong(BUNDLE_ID_CITTA);
            context.setIdCitta(mIdCitta);
            mIdPosto = savedInstanceState.getLong(BUNDLE_ID_POSTO);
            context.setIdPosto(mIdPosto);
            mNomePosto = savedInstanceState.getString(BUNDLE_NOME_POSTO);
            context.setNomePosto(mNomePosto);
        } else {
            mIdViaggio = context.getIdViaggio();
            mIdCitta = context.getIdCitta();
            mIdPosto = context.getIdPosto();
            mNomePosto = context.getNomePosto();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.dettagli_posto_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(mNomePosto);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ElencoFotoFragment fragment = ElencoFotoFragment.newInstance(mIdPosto, FotoLoader.FOTO_POSTO);
        fragmentTransaction.add(R.id.posti_container, fragment);
        fragmentTransaction.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dettagli_posto, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_aggiungi_foto_dettagli_posto) {
            try {
                mImageUri = PhotoUtils.getOutputMediaFileUri();
            } catch (IOException e) {
                Toast.makeText(this, "Errore durante l'accesso alla memoria", Toast.LENGTH_SHORT).show();
            }
            if (mImageUri != null) {
                if (sugg != null && sugg.getVisibility()== View.VISIBLE){slideToBottom(sugg);}
                PhotoUtils.mostraDialog(this, mImageUri);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(BUNDLE_ID_VIAGGIO, mIdViaggio);
        outState.putLong(BUNDLE_ID_CITTA, mIdCitta);
        outState.putLong(BUNDLE_ID_POSTO, mIdPosto);
        outState.putString(BUNDLE_NOME_POSTO, mNomePosto);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PhotoUtils.startIntent(this, requestCode, resultCode, data, mImageUri, mIdViaggio, mIdCitta, mIdPosto);
    }

    public void slideToBottom(View view){
        TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight());
        animate.setDuration(500);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

}
