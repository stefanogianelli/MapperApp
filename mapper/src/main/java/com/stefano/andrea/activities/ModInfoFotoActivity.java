package com.stefano.andrea.activities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.utils.DialogHelper;
import com.stefano.andrea.utils.PhotoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModInfoFotoActivity extends ActionBarActivity {

    public final static String EXTRA_ID_VIAGGIO = "com.stefano.andrea.mapper.ModInfoFotoActivity.idViaggio";
    public final static String EXTRA_ID_CITTA = "com.stefano.andrea.mapper.ModInfoFotoActivity.idCitta";
    public final static String EXTRA_FOTO = "com.stefano.andrea.mapper.ModInfoFotoActivity.Foto";
    public final static String EXTRA_TIPO_FOTO = "com.stefano.andrea.mapper.ModInfoFotoActivity.tipoFoto";

    private static final String TAG = "ModInfoFotoActivity";

    private static final int ADD_TAG = -42;

    private ArrayList<String> mImagePath;
    private ContentResolver mResolver;
    private long mIdViaggio;
    private long mIdCitta;
    private boolean mFotoSalvata = false;
    private int mTipoFoto;
    private ImageView mImageView;
    private Spinner mViaggioSpinner;
    private TextView mNomeCittaView;

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
        mImageView = (ImageView) findViewById(R.id.thumb_mod_info_foto);
        mViaggioSpinner = (Spinner) findViewById(R.id.txt_edit_viaggio_foto);
        mNomeCittaView = (TextView) findViewById(R.id.txt_edit_citta_foto);
        //configuro immagine
        inizializzaFoto();
        //configuro viaggio
        inizializzaViaggio();
        //configuro citta
        inizializzaCitta();
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
                    Toast.makeText(this, getApplicationContext().getResources().getString(R.string.citta_non_selezionata), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getApplicationContext().getResources().getString(R.string.viaggio_non_selezionato), Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.action_annula_foto) {
            boolean res = cancellaFoto();
            if (mTipoFoto == PhotoUtils.CAMERA_REQUEST) {
                if (res)
                    Log.d(TAG, "Foto cancellata con successo");
                else
                    Log.d(TAG, "Errore durante l'eliminazione della foto");
            }
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Mostra la foto scelta nell'apposita view
     */
    private void inizializzaFoto() {
        if (mImagePath != null) {
            //TODO: mostrare indicatore del numero delle imamgini selezionate
            ImageLoader.getInstance().displayImage(mImagePath.get(0), mImageView);
        }
    }

    /**
     * Carica i viaggi nei quali e' possibile salvare la foto
     */
    private void inizializzaViaggio() {
        List<Viaggio> elencoViaggi = new ArrayList<>();
        //controllo se e' stato selezionato un viaggio
        if (mIdViaggio != -1) {
            //viaggio selezionato
            Uri viaggio = ContentUris.withAppendedId(MapperContract.Viaggio.CONTENT_URI, mIdViaggio);
            String [] projection = {MapperContract.Viaggio.NOME};
            Cursor cViaggio = mResolver.query(viaggio, projection, null, null, null);
            if (cViaggio != null && cViaggio.getCount() > 0) {
                cViaggio.moveToFirst();
                String nomeViaggio = cViaggio.getString(cViaggio.getColumnIndex(projection[0]));
                elencoViaggi.add(new Viaggio(mIdViaggio, nomeViaggio));
                cViaggio.close();
            }
        } else {
            //viaggio non selezionato
            String [] projection = {MapperContract.Viaggio.ID_VIAGGIO, MapperContract.Viaggio.NOME};
            Cursor cViaggio = mResolver.query(MapperContract.Viaggio.CONTENT_URI, projection, null, null, null);
            if (cViaggio != null && cViaggio.getCount() > 0) {
                while (cViaggio.moveToNext()) {
                    Viaggio viaggio = new Viaggio();
                    viaggio.setId(cViaggio.getLong(cViaggio.getColumnIndex(projection[0])));
                    viaggio.setNome(cViaggio.getString(cViaggio.getColumnIndex(projection[1])));
                    elencoViaggi.add(viaggio);
                }
                cViaggio.close();
            }
        }
        elencoViaggi.add(new Viaggio(ADD_TAG, "Crea nuovo"));
        ViaggiSpinnerAdapter viaggiAdapter = new ViaggiSpinnerAdapter(this, R.layout.spinner_viaggio_item, elencoViaggi);
        mViaggioSpinner.setAdapter(viaggiAdapter);
        mViaggioSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Viaggio viaggio = (Viaggio) parent.getItemAtPosition(position);
                if (viaggio.getId() == ADD_TAG) {
                    DialogHelper.showDialogAggiungiViaggio(ModInfoFotoActivity.this, new DialogHelper.AggiungiViaggioCallback() {
                        @Override
                        public void creaViaggio(String nomeViaggio) {
                            Viaggio viaggio = new Viaggio(nomeViaggio);
                            InsertTask.InsertAdapter<Viaggio> adapter = new InsertTask.InsertAdapter<Viaggio>() {
                                @Override
                                public void insertItem(Viaggio item) {
                                    mIdViaggio = item.getId();
                                    Log.v(TAG, "Id nuovo viaggio: " + mIdViaggio);
                                }
                            };
                            new InsertTask<>(ModInfoFotoActivity.this, mResolver, adapter, viaggio).execute(InsertTask.INSERISCI_VIAGGIO);
                        }
                    });
                } else {
                    mIdViaggio = viaggio.getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    /**
     * Carica le citta nelle quali e' possibile salvare la foto
     */
    private void inizializzaCitta () {
        if (mIdCitta != -1) {
            Uri citta = ContentUris.withAppendedId(MapperContract.Citta.CONTENT_URI, mIdCitta);
            String [] projection = {MapperContract.DatiCitta.NOME};
            Cursor cCitta = mResolver.query(citta, projection, null, null, null);
            if (cCitta != null && cCitta.getCount() > 0) {
                cCitta.moveToFirst();
                String nomeCitta = cCitta.getString(cCitta.getColumnIndex(projection[0]));
                mNomeCittaView.setText(nomeCitta);
                cCitta.close();
            }
        }
    }

    /**
     * Cancella la foto selezionata
     * Funziona solo se la foto e' stata scattata dalla fotocamera
     * @return true se il file e' stato eliminato, falso se si e' verificato un errore o il file era
     * stato aggiunto dalla galleria
     */
    private boolean cancellaFoto () {
        if (mTipoFoto == PhotoUtils.CAMERA_REQUEST) {
            File file = new File(mImagePath.get(0).substring(7));
            return file.delete();
        } else
            return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mFotoSalvata)
            cancellaFoto();
    }

    /** Adapter per lo spinner dei viaggi */
    private class ViaggiSpinnerAdapter extends ArrayAdapter<Viaggio> {

        public ViaggiSpinnerAdapter(Context context, int resource, List<Viaggio> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView (int position, View convertView, ViewGroup parent) {
            Viaggio viaggio = getItem(position);
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_viaggio_item, parent, false);
            TextView nomeViaggio = (TextView) convertView.findViewById(R.id.spinner_nome_viaggio);
            nomeViaggio.setText(viaggio.getNome());
            return convertView;
        }
    }
}
