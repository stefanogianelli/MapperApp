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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.utils.DialogHelper;
import com.stefano.andrea.utils.LocationHelper;
import com.stefano.andrea.utils.PhotoUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModInfoFotoActivity extends ActionBarActivity {

    public final static String EXTRA_ID_VIAGGIO = "com.stefano.andrea.mapper.ModInfoFotoActivity.idViaggio";
    public final static String EXTRA_ID_CITTA = "com.stefano.andrea.mapper.ModInfoFotoActivity.idCitta";
    public final static String EXTRA_FOTO = "com.stefano.andrea.mapper.ModInfoFotoActivity.Foto";
    public final static String EXTRA_TIPO_FOTO = "com.stefano.andrea.mapper.ModInfoFotoActivity.tipoFoto";

    private static final String TAG = "ModInfoFotoActivity";

    private static final int CLEAR_CITTA = 1;
    private static final int CLEAR_POSTO = 2;

    private ContentResolver mResolver;
    private boolean mFotoSalvata = false;
    private ImageView mImageView;
    private TextView mViaggioText;
    private TextView mCittaText;
    //elenco dei percorsi delle imamgini
    private ArrayList<String> mImagePath;
    //indica se la foto e' stata scattata dalla fotocamera o acquisita dalla galleria
    private int mTipoFoto;
    //viaggio al quale associare la/e foto
    private Viaggio mViaggioSelezionato;
    //citta alla quale associare la/e foto
    private Citta mCittaSelezionata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_info_foto);
        mResolver = getContentResolver();
        //acquisisco parametri dall'intent e inizializzo a -1 quelli senza valore
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_FOTO))
            mImagePath = intent.getStringArrayListExtra(EXTRA_FOTO);
        mTipoFoto = intent.getIntExtra(EXTRA_TIPO_FOTO, -1);
        long idViaggio = intent.getLongExtra(EXTRA_ID_VIAGGIO, -1);
        long idCitta = intent.getLongExtra(EXTRA_ID_CITTA, -1);
        //acquisisco riferimenti
        mImageView = (ImageView) findViewById(R.id.thumb_mod_info_foto);
        mViaggioText = (TextView) findViewById(R.id.txt_edit_viaggio_foto);
        mCittaText = (TextView) findViewById(R.id.txt_edit_citta_foto);
        //inizializzo immagine
        inizializzaFoto();
        //carico elenchi
        updateViaggi();
        //inizializzo viaggio
        inizializzaViaggio(idViaggio);
        //inizializzo citta
        inizializzaCitta(idCitta);
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
            if (mViaggioSelezionato != null && mViaggioSelezionato.getId() != -1)
                if (mCittaSelezionata != null && mCittaSelezionata.getId() != -1) {
                    List<Foto> elencoFoto = new ArrayList<>();
                    for (int i = 0; i < mImagePath.size(); i++) {
                        Foto foto = new Foto();
                        foto.setPath(mImagePath.get(i));
                        if (mTipoFoto == PhotoUtils.CAMERA_REQUEST) {
                            //acquisisco lat/lon dai dati Exif
                            try {
                                LatLng coord = LocationHelper.getCoordinatesFromExif(foto.getPath());
                                foto.setLongitudine(coord.longitude);
                                foto.setLatitudine(coord.latitude);
                                Log.v(TAG, "Dati EXIF: lat = " + coord.latitude + ", lon = " + coord.longitude);
                            } catch (IOException e) {
                                //utilizzo i dati della citta
                                Log.i(TAG, "Impossibile ottenere dati EXIF");
                                foto.setLatitudine(mCittaSelezionata.getLatitudine());
                                foto.setLongitudine(mCittaSelezionata.getLongitudine());
                            }
                        } else {
                            foto.setLatitudine(mCittaSelezionata.getLatitudine());
                            foto.setLongitudine(mCittaSelezionata.getLongitudine());
                        }
                        foto.setIdViaggio(mViaggioSelezionato.getId());
                        foto.setIdCitta(mCittaSelezionata.getId());
                        elencoFoto.add(foto);
                    }
                    new InsertTask<>(this, mResolver, null, elencoFoto).execute(InsertTask.INSERISCI_FOTO);
                    mFotoSalvata = true;
                    finish();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.citta_non_selezionata), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getResources().getString(R.string.viaggio_non_selezionato), Toast.LENGTH_SHORT).show();
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
            //mostro la prima immagine a campione
            ImageLoader.getInstance().displayImage(mImagePath.get(0), mImageView);
        }
    }

    /**
     * Carica i viaggi nei quali e' possibile salvare la foto
     */
    private void inizializzaViaggio(long idViaggio) {
        //controllo se e' stato selezionato un viaggio
        if (idViaggio != -1) {
            //viaggio selezionato
            Uri viaggio = ContentUris.withAppendedId(MapperContract.Viaggio.CONTENT_URI, idViaggio);
            String [] projection = { MapperContract.Viaggio.NOME };
            Cursor curViaggio = mResolver.query(viaggio, projection, null, null, null);
            if (curViaggio != null && curViaggio.getCount() > 0) {
                curViaggio.moveToFirst();
                String nomeViaggio = curViaggio.getString(curViaggio.getColumnIndex(projection[0]));
                mViaggioSelezionato = new Viaggio(idViaggio, nomeViaggio);
                mViaggioText.setText(nomeViaggio);
                curViaggio.close();
            }
            updateCitta();
        }
    }

    private void updateViaggi () {
        final List<Viaggio> elencoViaggi = new ArrayList<>();
        String [] projection = {MapperContract.Viaggio.ID_VIAGGIO, MapperContract.Viaggio.NOME};
        Cursor curViaggio = mResolver.query(MapperContract.Viaggio.CONTENT_URI, projection, null, null, MapperContract.Viaggio.DEFAULT_SORT);
        if (curViaggio != null && curViaggio.getCount() > 0) {
            while (curViaggio.moveToNext()) {
                Viaggio viaggio = new Viaggio();
                viaggio.setId(curViaggio.getLong(curViaggio.getColumnIndex(projection[0])));
                viaggio.setNome(curViaggio.getString(curViaggio.getColumnIndex(projection[1])));
                elencoViaggi.add(viaggio);
            }
            curViaggio.close();
        }
        final ViaggiSpinnerAdapter viaggiAdapter = new ViaggiSpinnerAdapter(ModInfoFotoActivity.this, R.layout.spinner_row_item, elencoViaggi);
        mViaggioText.setClickable(true);
        mViaggioText.setTextColor(getResources().getColor(R.color.black));
        mViaggioText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.showListDialog(ModInfoFotoActivity.this, R.string.spinner_elenco_viaggio_titolo, viaggiAdapter, new DialogHelper.ListDialogCallback() {
                    @Override
                    public void onItemClick(int position) {
                        Viaggio viaggio = elencoViaggi.get(position);
                        mViaggioSelezionato = viaggio;
                        mViaggioText.setText(viaggio.getNome());
                        //ricarico l'elenco delle citta'
                        updateCitta();
                    }
                });
            }
        });
    }

    /**
     * Carica le citta nelle quali e' possibile salvare la foto
     */
    private void inizializzaCitta (long idCitta) {
        if (idCitta != -1) {
            Uri query = ContentUris.withAppendedId(MapperContract.Citta.CONTENT_URI, idCitta);
            String [] projection = {MapperContract.DatiCitta.NOME};
            Cursor cCitta = mResolver.query(query, projection, null, null, null);
            if (cCitta != null && cCitta.getCount() > 0) {
                cCitta.moveToFirst();
                String nomeCitta = cCitta.getString(cCitta.getColumnIndex(projection[0]));
                mCittaText.setText(nomeCitta);
                mCittaSelezionata = new Citta();
                mCittaSelezionata.setId(idCitta);
                mCittaSelezionata.setNome(nomeCitta);
                cCitta.close();
            }
        }
    }

    private void updateCitta () {
        clearSelection(CLEAR_CITTA);
        final List<Citta> elencoCitta = new ArrayList<>();
        Uri query = ContentUris.withAppendedId(MapperContract.Citta.DETTAGLI_VIAGGIO_URI, mViaggioSelezionato.getId());
        String [] projection = {MapperContract.Citta.ID_CITTA, MapperContract.DatiCitta.NOME};
        Cursor curCitta = mResolver.query(query, projection, null, null, MapperContract.Citta.DEFAULT_SORT);
        if (curCitta != null && curCitta.getCount() > 0) {
            while (curCitta.moveToNext()) {
                Citta citta = new Citta();
                citta.setId(curCitta.getLong(curCitta.getColumnIndex(projection[0])));
                citta.setNome(curCitta.getString(curCitta.getColumnIndex(projection[1])));
                elencoCitta.add(citta);
            }
            curCitta.close();
        }
        final CittaSpinnerAdapter cittaAdapter = new CittaSpinnerAdapter(ModInfoFotoActivity.this, R.layout.spinner_row_item, elencoCitta);
        mCittaText.setClickable(true);
        mCittaText.setTextColor(getResources().getColor(R.color.black));
        mCittaText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.showListDialog(ModInfoFotoActivity.this, R.string.seleziona_citta, cittaAdapter, new DialogHelper.ListDialogCallback() {
                    @Override
                    public void onItemClick(int position) {
                        Citta citta = elencoCitta.get(position);
                        mCittaSelezionata = citta;
                        mCittaText.setText(citta.getNome());
                    }
                });
            }
        });
    }

    private void clearSelection (int level) {
        switch (level) {
            case CLEAR_CITTA:
                mCittaSelezionata = null;
                mCittaText.setText(R.string.seleziona_citta);
                mCittaText.setTextColor(getResources().getColor(R.color.mod_subtitle));
            case CLEAR_POSTO:
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
        public View getView(int position, View convertView, ViewGroup parent) {
            Viaggio viaggio = getItem(position);
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_row_item, parent, false);
            TextView nomeViaggio = (TextView) convertView.findViewById(R.id.spinner_nome_item);
            nomeViaggio.setText(viaggio.getNome());
            return convertView;
        }
    }

    /** Adapter per lo spinner delle citta' */
    private class CittaSpinnerAdapter extends ArrayAdapter<Citta> {

        public CittaSpinnerAdapter(Context context, int resource, List<Citta> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Citta citta = getItem(position);
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_row_item, parent, false);
            TextView nomeCitta = (TextView) convertView.findViewById(R.id.spinner_nome_item);
            nomeCitta.setText(citta.getNome());
            return convertView;
        }
    }
}
