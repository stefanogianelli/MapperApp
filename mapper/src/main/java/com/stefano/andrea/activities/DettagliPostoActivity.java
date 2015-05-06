package com.stefano.andrea.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.stefano.andrea.fragments.ElencoFotoFragment;
import com.stefano.andrea.loaders.FotoLoader;
import com.stefano.andrea.utils.MapperContext;
import com.stefano.andrea.utils.PhotoUtils;

import java.io.IOException;

public class DettagliPostoActivity extends ActionBarActivity {

    private Uri mImageUri;
    private long mIdViaggio;
    private long mIdCitta;
    private long mIdPosto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_posto);
        //acquisisco riferimenti
        MapperContext context = MapperContext.getInstance();
        mIdViaggio = context.getIdViaggio();
        mIdCitta = context.getIdCitta();
        mIdPosto = context.getIdPosto();
        String nomePosto = context.getNomePosto();
        Toolbar toolbar = (Toolbar) findViewById(R.id.dettagli_posto_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(nomePosto);
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
                mImageUri = PhotoUtils.getImageUri();
            } catch (IOException e) {
                Toast.makeText(this, "Errore durante l'accesso alla memoria", Toast.LENGTH_SHORT).show();
            }
            if (mImageUri != null)
                PhotoUtils.mostraDialog(this, mImageUri);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PhotoUtils.startIntent(this, requestCode, resultCode, data, mImageUri, mIdViaggio, mIdCitta, mIdPosto);
    }

}
