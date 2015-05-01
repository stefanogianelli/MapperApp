package com.stefano.andrea.activities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.utils.DialogChooseFotoMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModInfoFotoActivity extends ActionBarActivity {

    public final static String EXTRA_ID_VIAGGIO = "com.stefano.andrea.mapper.ModInfoFotoActivity.idViaggio";
    public final static String EXTRA_ID_CITTA = "com.stefano.andrea.mapper.ModInfoFotoActivity.idCitta";
    public final static String EXTRA_FOTO = "com.stefano.andrea.mapper.ModInfoFotoActivity.Foto";
    public final static String EXTRA_TIPO_FOTO = "com.stefano.andrea.mapper.ModInfoFotoActivity.tipoFoto";

    private static final String TAG = "ModInfoFotoActivity";

    private ArrayList<String> mImagePath;
    private ContentResolver mResolver;
    private long mIdViaggio;
    private long mIdCitta;
    private boolean mFotoSalvata = false;
    private int mTipoFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_info_foto);
        mResolver = getContentResolver();
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_FOTO))
                mImagePath = intent.getStringArrayListExtra(EXTRA_FOTO);
            mIdViaggio = intent.getLongExtra(EXTRA_ID_VIAGGIO, -1);
            mIdCitta = intent.getLongExtra(EXTRA_ID_CITTA, -1);
            mTipoFoto = intent.getIntExtra(EXTRA_TIPO_FOTO, -1);
        }
        //acquisisco riferimenti
        ImageView imageView = (ImageView) findViewById(R.id.thumb_mod_info_foto);
        TextView nomeViaggioView = (TextView) findViewById(R.id.txt_edit_viaggio_foto);
        TextView nomeCittaView = (TextView) findViewById(R.id.txt_edit_citta_foto);
        //assegno i dati raccolti
        if (mImagePath != null) {
            //TODO: mostrare indicatore del numero delle imamgini selezionate
            ImageLoader.getInstance().displayImage(mImagePath.get(0), imageView);
        }
        if (mIdViaggio != -1) {
            Uri viaggio = ContentUris.withAppendedId(MapperContract.Viaggio.CONTENT_URI, mIdViaggio);
            String [] projection = {MapperContract.Viaggio.NOME};
            Cursor cViaggio = mResolver.query(viaggio, projection, null, null, null);
            if (cViaggio != null && cViaggio.getCount() > 0) {
                cViaggio.moveToFirst();
                String nomeViaggio = cViaggio.getString(cViaggio.getColumnIndex(projection[0]));
                nomeViaggioView.setText(nomeViaggio);
                cViaggio.close();
            }
        }
        if (mIdCitta != -1) {
            Uri citta = ContentUris.withAppendedId(MapperContract.Citta.CONTENT_URI, mIdCitta);
            String [] projection = {MapperContract.DatiCitta.NOME};
            Cursor cCitta = mResolver.query(citta, projection, null, null, null);
            if (cCitta != null && cCitta.getCount() > 0) {
                cCitta.moveToFirst();
                String nomeCitta = cCitta.getString(cCitta.getColumnIndex(projection[0]));
                nomeCittaView.setText(nomeCitta);
                cCitta.close();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mod_info_foto, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_salva_foto) {
            if (mIdViaggio != -1)
                if (mIdCitta != -1) {
                    List<Foto> elencoFoto = new ArrayList<>();
                    for (int i = 0; i < mImagePath.size(); i++) {
                        Foto foto = new Foto();
                        foto.setPath(mImagePath.get(i));
                        foto.setLatitudine(0);
                        foto.setLongitudine(0);
                        foto.setIdViaggio(mIdViaggio);
                        foto.setIdCitta(mIdCitta);
                        elencoFoto.add(foto);
                    }
                    new InsertTask<>(this, mResolver, null, elencoFoto).execute(InsertTask.INSERISCI_FOTO);
                    mFotoSalvata = true;
                    finish();
                } else {
                    Toast.makeText(this, "Selezionare una citta", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Selezionare un viaggio", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.action_annula_foto) {
            boolean res = cancellaFoto();
            if (BuildConfig.DEBUG && res)
                Log.v(TAG, "Foto cancellata con successo");
            else
                Log.v(TAG, "Errore durante l'eliminazione della foto");
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean cancellaFoto () {
        if (mTipoFoto == DialogChooseFotoMode.CAMERA_REQUEST) {
            File file = new File(mImagePath.get(0).substring(7));
            return file.delete();
        } else
            return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mFotoSalvata)
            cancellaFoto();
    }
}
