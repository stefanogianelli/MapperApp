package com.stefano.andrea.activities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.stefano.andrea.dialogs.AddCittaDialog;
import com.stefano.andrea.dialogs.AddPostoDialog;
import com.stefano.andrea.intents.MapperIntent;
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.models.ImageDetails;
import com.stefano.andrea.models.Posto;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.utils.DialogHelper;
import com.stefano.andrea.utils.FetchAddressIntentService;
import com.stefano.andrea.utils.PhotoUtils;

import java.util.ArrayList;
import java.util.List;

public class ModInfoFotoActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public final static String EXTRA_ID_VIAGGIO = "com.stefano.andrea.mapper.ModInfoFotoActivity.idViaggio";
    public final static String EXTRA_ID_CITTA = "com.stefano.andrea.mapper.ModInfoFotoActivity.idCitta";
    public final static String EXTRA_ID_POSTO = "com.stefano.andrea.mapper.ModInfoFotoActivity.idPosto";
    public final static String EXTRA_FOTO = "com.stefano.andrea.mapper.ModInfoFotoActivity.Foto";
    public final static String EXTRA_TIPO_FOTO = "com.stefano.andrea.mapper.ModInfoFotoActivity.tipoFoto";

    private static final String TAG = "ModInfoFotoActivity";

    private static final int CLEAR_CITTA = 1;
    private static final int CLEAR_POSTO = 2;
    private static final int ID_INSERT_CITY = -42;

    //Localization variables
    private static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    private static final String LOCATION_ADDRESS_KEY = "location-address";
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Location mFotoLocation;
    private boolean mAddressRequested;
    private AddressResultReceiver mResultReceiver;
    private Citta mCittaLocalizzata;

    private ContentResolver mResolver;
    private boolean mFotoSalvata = false;
    private boolean mFotoCancellata = false;
    private ImageView mImageView;
    private TextView mCountImages;
    private TextView mViaggioText;
    private TextView mCittaText;
    private ImageView mAddCittaButton;
    private TextView mPostoText;
    private Toolbar mInfoToolbar;
    private TextView mInfoText;
    private ProgressBar mInfoProgress;
    private Button mGeolocalizzaButton;
    private ImageView mAddPostoButton;
    //elenco dei percorsi delle imamgini
    private ArrayList<String> mImagePath;
    //indica se la foto e' stata scattata dalla fotocamera o acquisita dalla galleria
    private int mTipoFoto;
    //viaggio al quale associare la/e foto
    private Viaggio mViaggioSelezionato;
    //citta alla quale associare la/e foto
    private Citta mCittaSelezionata;
    /** posto al quale associare la/e foto */
    private Posto mPostoSelezionato;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_info_foto);
        //setup della toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_foto_activity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mResolver = getContentResolver();
        //acquisisco parametri dall'intent e inizializzo a -1 quelli senza valore
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_FOTO))
            mImagePath = intent.getStringArrayListExtra(EXTRA_FOTO);
        mTipoFoto = intent.getIntExtra(EXTRA_TIPO_FOTO, -1);
        long idViaggio = intent.getLongExtra(EXTRA_ID_VIAGGIO, -1);
        long idCitta = intent.getLongExtra(EXTRA_ID_CITTA, -1);
        long idPosto = intent.getLongExtra(EXTRA_ID_POSTO, -1);
        //acquisisco riferimenti
        mImageView = (ImageView) findViewById(R.id.thumb_mod_info_foto);
        mCountImages = (TextView) findViewById(R.id.numero_immagini);
        mViaggioText = (TextView) findViewById(R.id.txt_edit_viaggio_foto);
        mCittaText = (TextView) findViewById(R.id.txt_edit_citta_foto);
        mAddCittaButton = (ImageButton) findViewById(R.id.mod_foto_add_citta);
        mPostoText = (TextView) findViewById(R.id.txt_edit_posto_foto);
        mAddPostoButton = (ImageButton) findViewById(R.id.mod_foto_add_posto);
        mInfoToolbar = (Toolbar) findViewById(R.id.info_foto_message_bar);
        mInfoText = (TextView) findViewById(R.id.info_foto_message_text);
        mInfoProgress = (ProgressBar) findViewById(R.id.info_foto_progress_bar);
        mGeolocalizzaButton = (Button) findViewById(R.id.action_geolocalizza);
        mGeolocalizzaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mAddressRequested) {
                    setInfoToolbar(R.string.recupero_info, R.color.white);
                    fetchAddressButtonHandler();
                }
            }
        });
        //setup dei pulsanti
        setupAddViaggioButton();
        setupAddCittaButton();
        setupAddPostoButton();
        //inizializzo immagine
        inizializzaFoto();
        //carico elenchi
        updateViaggi();
        //inizializzo viaggio
        inizializzaViaggio(idViaggio);
        //inizializzo citta
        inizializzaCitta(idCitta);
        //inizializzo posto
        inizializzaPosto(idPosto);
        //configuro handler per la localizzazione
        mResultReceiver = new AddressResultReceiver(new Handler());
        mAddressRequested = false;
        updateValuesFromBundle(savedInstanceState);

        updateUIWidgets();
        buildGoogleApiClient();
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

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_salva_foto) {
            if (mViaggioSelezionato != null && mViaggioSelezionato.getId() != -1)
                if (mCittaSelezionata != null && mCittaSelezionata.getId() != -1 && mCittaSelezionata.getId() != ID_INSERT_CITY) {
                    addFoto();
                    finish();
                } else if (mCittaLocalizzata != null) {
                    addNewCity();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.citta_non_selezionata), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getResources().getString(R.string.viaggio_non_selezionato), Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.action_annula_foto) {
            if (mTipoFoto == PhotoUtils.CAMERA_REQUEST) {
                mFotoCancellata = cancellaFoto();
                if (mFotoCancellata)
                    Log.d(TAG, "Foto cancellata con successo");
                else
                    Log.d(TAG, "Errore durante l'eliminazione della foto");
            }
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addNewCity () {
        mCittaLocalizzata.setIdViaggio(mViaggioSelezionato.getId());
        InsertTask.InsertAdapter<Citta> adapter = new InsertTask.InsertAdapter<Citta>() {
            @Override
            public void insertItem(Citta item) {
                mCittaSelezionata = item;
                addFoto();
                finish();
            }
        };
        new InsertTask<>(ModInfoFotoActivity.this, adapter, mCittaLocalizzata).execute(InsertTask.INSERISCI_CITTA);
    }

    private void addFoto () {
        List<Foto> elencoFoto = new ArrayList<>();
        for (int i = 0; i < mImagePath.size(); i++) {
            Foto foto = new Foto();
            foto.setPath(mImagePath.get(i));
            //acquisisco dettagli dell'immagine
            ImageDetails dettagli = getMediaStoreData(foto.getPath());
            foto.setIdMediaStore(dettagli.getIdMediaStore());
            foto.setData(dettagli.getData());
            double lat = dettagli.getLatitudine();
            double lon = dettagli.getLongitudine();
            if (lat == 0 && lon == 0) {
                if (mPostoSelezionato != null) {
                    foto.setLatitudine(mPostoSelezionato.getLatitudine());
                    foto.setLongitudine(mPostoSelezionato.getLongitudine());
                } else {
                    foto.setLatitudine(mCittaSelezionata.getLatitudine());
                    foto.setLongitudine(mCittaSelezionata.getLongitudine());
                }
            } else {
                foto.setLatitudine(lat);
                foto.setLongitudine(lon);
            }
            foto.setIdViaggio(mViaggioSelezionato.getId());
            foto.setIdCitta(mCittaSelezionata.getId());
            foto.setMimeType(dettagli.getMimeType());
            foto.setWidth(dettagli.getWidth());
            foto.setHeight(dettagli.getHeight());
            foto.setSize(dettagli.getSize());
            if (mPostoSelezionato != null && mPostoSelezionato.getId() != -1)
                foto.setIdPosto(mPostoSelezionato.getId());
            if (mTipoFoto == PhotoUtils.CAMERA_REQUEST)
                foto.setCamera(1);
            elencoFoto.add(foto);
        }
        new InsertTask<>(this, null, elencoFoto).execute(InsertTask.INSERISCI_FOTO);
        mFotoSalvata = true;
    }

    /**
     * Mostra la foto scelta nell'apposita view
     */
    private void inizializzaFoto() {
        if (mImagePath != null) {
            //mostro la prima immagine a campione
            ImageLoader.getInstance().displayImage(mImagePath.get(0), mImageView);
            if (mImagePath.size() > 1) {
                mCountImages.setText(mImagePath.size() + " immagini");
                mCountImages.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Seleziona il vaggio se l'id e' diverso da -1
     * @param idViaggio L'id del viaggio
     */
    private void inizializzaViaggio(long idViaggio) {
        //controllo se e' stato selezionato un viaggio
        if (idViaggio != -1) {
            //viaggio selezionato
            Uri viaggio = ContentUris.withAppendedId(MapperContract.Viaggio.CONTENT_URI, idViaggio);
            String [] projection = { MapperContract.Viaggio.NOME };
            Cursor curViaggio = mResolver.query(viaggio, projection, null, null, null);
            if (curViaggio.moveToFirst()) {
                String nomeViaggio = curViaggio.getString(curViaggio.getColumnIndex(projection[0]));
                mViaggioSelezionato = new Viaggio(idViaggio, nomeViaggio);
                mViaggioText.setText(nomeViaggio);
            }
            curViaggio.close();
            updateCitta();
        }
    }

    /**
     * Carica l'elenco dei viaggi disponibili
     */
    private void updateViaggi () {
        final List<Viaggio> elencoViaggi = new ArrayList<>();
        String [] projection = {MapperContract.Viaggio.ID_VIAGGIO, MapperContract.Viaggio.NOME};
        Cursor curViaggio = mResolver.query(MapperContract.Viaggio.CONTENT_URI, projection, null, null, MapperContract.Viaggio.DEFAULT_SORT);
        while (curViaggio.moveToNext()) {
            Viaggio viaggio = new Viaggio();
            viaggio.setId(curViaggio.getLong(curViaggio.getColumnIndex(projection[0])));
            viaggio.setNome(curViaggio.getString(curViaggio.getColumnIndex(projection[1])));
            elencoViaggi.add(viaggio);
        }
        curViaggio.close();
        if (elencoViaggi.size() > 0) {
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
                            clearSelection(CLEAR_CITTA);
                            if (mCittaLocalizzata != null) {
                                displayAddressOutput();
                            } else
                                updateCitta();
                        }
                    });
                }
            });
        }
    }

    /**
     * Imposta il pulsante per l'aggiunta del viaggio
     */
    private void setupAddViaggioButton () {
        ImageButton addViaggio = (ImageButton) findViewById(R.id.mod_foto_add_viaggio);
        addViaggio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.showViaggioDialog(ModInfoFotoActivity.this, -1, -1, null, new DialogHelper.ViaggioDialogCallback() {
                    @Override
                    public void viaggioActionButton(int position, long id, String nomeViaggio) {
                        Viaggio viaggio = new Viaggio();
                        viaggio.setNome(nomeViaggio);
                        InsertTask.InsertAdapter<Viaggio> adapter = new InsertTask.InsertAdapter<Viaggio>() {
                            @Override
                            public void insertItem(Viaggio item) {
                                clearSelection(CLEAR_CITTA);
                                inizializzaViaggio(item.getId());
                                updateViaggi();
                                sendBroadcast(new Intent(MapperIntent.UPDATE_VIAGGIO));
                            }
                        };
                        new InsertTask<>(ModInfoFotoActivity.this, adapter, viaggio).execute(InsertTask.INSERISCI_VIAGGIO);
                    }
                });
            }
        });
    }

    /**
     * Seleziona la citta' se l'id e' diverso da -1
     * @param idCitta L'id della citta'
     */
    private void inizializzaCitta (long idCitta) {
        if (idCitta != -1) {
            Uri query = ContentUris.withAppendedId(MapperContract.Citta.CONTENT_URI, idCitta);
            String [] projection = {MapperContract.DatiCitta.NOME};
            Cursor cCitta = mResolver.query(query, projection, null, null, null);
            if (cCitta.moveToFirst()) {
                String nomeCitta = cCitta.getString(cCitta.getColumnIndex(projection[0]));
                mCittaText.setText(nomeCitta);
                mCittaSelezionata = new Citta();
                mCittaSelezionata.setId(idCitta);
                mCittaSelezionata.setNome(nomeCitta);
            }
            cCitta.close();
            updatePosti();
        }
    }

    /**
     * Aggiorna l'elenco delle citta' disponibili
     */
    private void updateCitta () {
        final List<Citta> elencoCitta = new ArrayList<>();
        if (mViaggioSelezionato != null) {
            Uri query = ContentUris.withAppendedId(MapperContract.Citta.DETTAGLI_VIAGGIO_URI, mViaggioSelezionato.getId());
            String[] projection = {MapperContract.Citta.ID_CITTA, MapperContract.DatiCitta.NOME, MapperContract.DatiCitta.NAZIONE};
            Cursor curCitta = mResolver.query(query, projection, null, null, MapperContract.Citta.DEFAULT_SORT);
            while (curCitta.moveToNext()) {
                Citta citta = new Citta();
                citta.setId(curCitta.getLong(curCitta.getColumnIndex(projection[0])));
                citta.setNome(curCitta.getString(curCitta.getColumnIndex(projection[1])));
                citta.setNazione(curCitta.getString(curCitta.getColumnIndex(projection[2])));
                elencoCitta.add(citta);
            }
            curCitta.close();
            mAddCittaButton.setClickable(true);
        }
        if (mCittaLocalizzata != null) {
            if (!elencoCitta.contains(mCittaLocalizzata))
                elencoCitta.add(0, mCittaLocalizzata);
            else {
                int pos = elencoCitta.indexOf(mCittaLocalizzata);
                mCittaSelezionata = elencoCitta.get(pos);
                mCittaText.setText(mCittaLocalizzata.getNome());
            }
        }
        if (elencoCitta.size() > 0) {
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
                            clearSelection(CLEAR_POSTO);
                            updatePosti();
                        }
                    });
                }
            });
        }
    }

    /**
     * Imposta il pulsante per l'aggiunta della citta'
     */
    private void setupAddCittaButton () {
        mAddCittaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                AddCittaDialog dialog = AddCittaDialog.newInstance();
                dialog.setCallback(new AddCittaDialog.AggiungiCittaCallback() {
                    @Override
                    public void creaNuovaCitta(String nomeCitta, String nazione, LatLng coordinates) {
                        Citta citta = new Citta();
                        citta.setIdViaggio(mViaggioSelezionato.getId());
                        citta.setNome(nomeCitta);
                        citta.setNazione(nazione);
                        citta.setLatitudine(coordinates.latitude);
                        citta.setLongitudine(coordinates.longitude);
                        InsertTask.InsertAdapter<Citta> adapter = new InsertTask.InsertAdapter<Citta>() {
                            @Override
                            public void insertItem(Citta item) {
                                clearSelection(CLEAR_POSTO);
                                inizializzaCitta(item.getId());
                                updateCitta();
                                sendBroadcast(new Intent(MapperIntent.UPDATE_CITTA));
                            }
                        };
                        new InsertTask<>(ModInfoFotoActivity.this, adapter, citta).execute(InsertTask.INSERISCI_CITTA);
                    }
                });
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(android.R.id.content, dialog).addToBackStack(null).commit();
            }
        });
        mAddCittaButton.setClickable(false);
    }

    /**
     * Inizializza il posto se l'id e' diverso da -1
     * @param idPosto L'id del posto
     */
    private void inizializzaPosto (long idPosto) {
        if (idPosto != -1) {
            Uri query = ContentUris.withAppendedId(MapperContract.Posto.CONTENT_URI, idPosto);
            String [] projection = {MapperContract.Luogo.NOME};
            Cursor curPosto = mResolver.query(query, projection, null, null, null);
            if (curPosto.moveToFirst()) {
                String nomePosto = curPosto.getString(curPosto.getColumnIndex(projection[0]));
                mPostoText.setText(nomePosto);
                mPostoSelezionato = new Posto();
                mPostoSelezionato.setId(idPosto);
                mPostoSelezionato.setNome(nomePosto);
            }
            curPosto.close();
        }
    }

    /**
     * Aggiorna l'elenco dei posti disponibili
     */
    private void updatePosti () {
        final List<Posto> elencoPosti = new ArrayList<>();
        if (mCittaSelezionata.getId() != ID_INSERT_CITY) {
            Uri query = ContentUris.withAppendedId(MapperContract.Posto.POSTI_IN_CITTA_URI, mCittaSelezionata.getId());
            String[] projection = {MapperContract.Posto.ID_POSTO, MapperContract.Luogo.NOME};
            Cursor curPosto = mResolver.query(query, projection, null, null, MapperContract.Luogo.DEFAULT_SORT);
            while (curPosto.moveToNext()) {
                Posto posto = new Posto();
                posto.setId(curPosto.getLong(curPosto.getColumnIndex(projection[0])));
                posto.setNome(curPosto.getString(curPosto.getColumnIndex(projection[1])));
                elencoPosti.add(posto);
            }
            curPosto.close();
            mAddPostoButton.setClickable(true);
            if (elencoPosti.size() > 0) {
                final PostoSpinnerAdapter postoAdapter = new PostoSpinnerAdapter(ModInfoFotoActivity.this, R.layout.spinner_row_item, elencoPosti);
                mPostoText.setClickable(true);
                mPostoText.setTextColor(getResources().getColor(R.color.black));
                mPostoText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogHelper.showListDialog(ModInfoFotoActivity.this, R.string.seleziona_posto, postoAdapter, new DialogHelper.ListDialogCallback() {
                            @Override
                            public void onItemClick(int position) {
                                Posto posto = elencoPosti.get(position);
                                mPostoSelezionato = posto;
                                mPostoText.setText(posto.getNome());
                            }
                        });
                    }
                });
            }
        }
    }

    /**
     * Imposta il pulsante per l'aggiunta dei posti
     */
    private void setupAddPostoButton () {
        mAddPostoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                AddPostoDialog dialog = AddPostoDialog.newInstance();
                dialog.setCallback(new AddPostoDialog.AggiungiPostoCallback() {
                    @Override
                    public void creaNuovoPosto(String nomePosto, LatLng coordinates) {
                        Posto posto = new Posto();
                        posto.setIdCitta(mCittaSelezionata.getId());
                        posto.setNome(nomePosto);
                        posto.setLatitudine(coordinates.latitude);
                        posto.setLongitudine(coordinates.longitude);
                        InsertTask.InsertAdapter<Posto> adapter = new InsertTask.InsertAdapter<Posto>() {
                            @Override
                            public void insertItem(Posto item) {
                                inizializzaPosto(item.getId());
                                updatePosti();
                                sendBroadcast(new Intent(MapperIntent.UPDATE_POSTO));
                            }
                        };
                        new InsertTask<>(ModInfoFotoActivity.this, adapter, posto).execute(InsertTask.INSERISCI_POSTO);
                    }
                });
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(android.R.id.content, dialog).addToBackStack(null).commit();
            }
        });
        mAddPostoButton.setClickable(false);
    }

    /**
     * Rimuove le selezioni effettuate
     * @param level CITTA se si vuole resettare sia la citta' che il posto, POSTO resetta solo il posto
     */
    private void clearSelection (int level) {
        switch (level) {
            case CLEAR_CITTA:
                mCittaSelezionata = null;
                mCittaText.setText(R.string.seleziona_citta);
                mCittaText.setTextColor(getResources().getColor(R.color.mod_subtitle));
                mCittaText.setClickable(false);
                mAddCittaButton.setClickable(false);
            case CLEAR_POSTO:
                mPostoSelezionato = null;
                mPostoText.setText(R.string.seleziona_posto);
                mPostoText.setTextColor(getResources().getColor(R.color.mod_subtitle));
                mPostoText.setClickable(false);
                mAddPostoButton.setClickable(false);
        }
    }

    /**
     * Acquisisce i dettagli di un'immagine
     * @param path Il percorso assoluto della foto
     * @return I dati dell'immagine
     */
    private ImageDetails getMediaStoreData (String path) {
        ImageDetails dettagli = new ImageDetails();
        String [] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE,
            MediaStore.MediaColumns.WIDTH, MediaStore.MediaColumns.HEIGHT, MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.SIZE};
        String selection = MediaStore.Images.Media.DATA + "=?";
        String [] selectionArgs = { path.substring(7) };
        Cursor cursor = mResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor.moveToFirst()) {
            dettagli.setIdMediaStore(cursor.getInt(cursor.getColumnIndex(projection[0])));
            dettagli.setData(cursor.getInt(cursor.getColumnIndex(projection[1])));
            dettagli.setLatitudine(cursor.getDouble(cursor.getColumnIndex(projection[2])));
            dettagli.setLongitudine(cursor.getDouble(cursor.getColumnIndex(projection[3])));
            dettagli.setWidth(cursor.getString(cursor.getColumnIndex(projection[4])));
            dettagli.setHeight(cursor.getString(cursor.getColumnIndex(projection[5])));
            dettagli.setMimeType(cursor.getString(cursor.getColumnIndex(projection[6])));
            dettagli.setSize(cursor.getInt(cursor.getColumnIndex(projection[7])));
        }
        cursor.close();
        return dettagli;
    }

    /**
     * Cancella la foto selezionata
     * Da richiamare solo se la foto e' stata scattata dalla fotocamera
     * @return true se il file e' stato eliminato, falso se si e' verificato un errore o il file non e' stato trovato
     */
    private boolean cancellaFoto () {
        String selection = MediaStore.Images.Media.DATA + "=?";
        String [] selectionArgs = {mImagePath.get(0).substring(7)};
        int count = mResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
        return count > 0;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        //TODO: trovare fix per il fatto che la funzione non venga sempre chiamata
        super.onDestroy();
        if (mTipoFoto == PhotoUtils.CAMERA_REQUEST && !mFotoSalvata && !mFotoCancellata) {
            boolean res = cancellaFoto();
            Log.v(TAG, "onDestroy: cancellata immagine? " + res);
        }
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

    /** Adapter per lo spinner dei posti' */
    private class PostoSpinnerAdapter extends ArrayAdapter<Posto> {

        public PostoSpinnerAdapter(Context context, int resource, List<Posto> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Posto posto = getItem(position);
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_row_item, parent, false);
            TextView nomePosto = (TextView) convertView.findViewById(R.id.spinner_nome_item);
            nomePosto.setText(posto.getNome());
            return convertView;
        }
    }

    //LOCATION METHODS

    /**
     * Updates fields based on data stored in the bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Check savedInstanceState to see if the address was previously requested.
            if (savedInstanceState.keySet().contains(ADDRESS_REQUESTED_KEY)) {
                mAddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUESTED_KEY);
            }
            // Check savedInstanceState to see if the location address string was previously found
            // and stored in the Bundle. If it was found, display the address string in the UI.
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mCittaLocalizzata = savedInstanceState.getParcelable(LOCATION_ADDRESS_KEY);
                displayAddressOutput();
            }
        }
    }

    /**
     * Builds a GoogleApiClient. Uses {@code #addApi} to request the LocationServices API.
     */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Runs when user clicks the Fetch Address button. Starts the service to fetch the address if
     * GoogleApiClient is connected.
     */
    private void fetchAddressButtonHandler() {
        mAddressRequested = true;
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startIntentService();
        }
        updateUIWidgets();
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    private void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(FetchAddressIntentService.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        if (mFotoLocation != null && !mAddressRequested) {
            Log.d(TAG, "Acquisco localita' dalla foto");
            intent.putExtra(FetchAddressIntentService.LOCATION_DATA_EXTRA, mFotoLocation);
        } else {
            Log.d(TAG, "Acquisco localita' corrente");
            intent.putExtra(FetchAddressIntentService.LOCATION_DATA_EXTRA, mLastLocation);
        }

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == FetchAddressIntentService.FAILURE_RESULT) {
                setInfoToolbar(resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY), R.color.red);
            } else if (resultCode == FetchAddressIntentService.SUCCESS_RESULT) {
                String nome = resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY);
                String nazione = resultData.getString(FetchAddressIntentService.RESULT_COUNTRY);
                String message = getResources().getString(R.string.photo_information_found, nome);
                setInfoToolbar(message, R.color.green);
                mCittaLocalizzata = new Citta();
                mCittaLocalizzata.setId(ID_INSERT_CITY);
                mCittaLocalizzata.setNome(nome);
                mCittaLocalizzata.setNazione(nazione);
                if (mAddressRequested) {
                    mCittaLocalizzata.setLatitudine(mLastLocation.getLatitude());
                    mCittaLocalizzata.setLongitudine(mLastLocation.getLongitude());
                } else {
                    mCittaLocalizzata.setLatitudine(mFotoLocation.getLatitude());
                    mCittaLocalizzata.setLongitudine(mFotoLocation.getLongitude());
                }
                //update UI
                displayAddressOutput();
            }

            // Reset. Enable the Fetch Address button and stop showing the progress bar.
            mAddressRequested = false;
            updateUIWidgets();
        }
    }

    /**
     * Toggles the visibility of the progress bar. Enables or disables the Fetch Address button.
     */
    private void updateUIWidgets() {
        if (mAddressRequested) {
            mInfoProgress.setVisibility(ProgressBar.VISIBLE);
            mGeolocalizzaButton.setEnabled(false);
        } else {
            mInfoProgress.setVisibility(ProgressBar.GONE);
            mGeolocalizzaButton.setEnabled(true);
        }
    }

    /**
     * Updates the address in the UI.
     */
    private void displayAddressOutput() {
        mCittaText.setText(mCittaLocalizzata.getNome());
        updateCitta();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save whether the address has been requested.
        savedInstanceState.putBoolean(ADDRESS_REQUESTED_KEY, mAddressRequested);

        // Save the address string.
        if (mCittaLocalizzata != null)
            savedInstanceState.putParcelable(LOCATION_ADDRESS_KEY, mCittaLocalizzata);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                setInfoToolbar(R.string.no_geocoder_available, R.color.red);
                return;
            }
            // It is possible that the user presses the button to get the address before the
            // GoogleApiClient object successfully connects. In such a case, mAddressRequested
            // is set to true, but no attempt is made to fetch the address (see
            // fetchAddressButtonHandler()) . Instead, we start the intent service here if the
            // user has requested an address, since we now have a connection to GoogleApiClient.
            if (mAddressRequested) {
                startIntentService();
            }
        }

        //Acquisisco coordinate della foto
        Log.d(TAG, "Acquisisco coordinate della foto");
        ImageDetails dettagli = getMediaStoreData(mImagePath.get(0));
        double latitudine = dettagli.getLatitudine();
        double longitudine = dettagli.getLongitudine();
        if (latitudine != 0 && longitudine != 0) {
            Log.d(TAG, "Coordinate trovate!");
            mFotoLocation = new Location("Mapper");
            mFotoLocation.setLatitude(latitudine);
            mFotoLocation.setLongitude(longitudine);
            startIntentService();
        } else {
            setInfoToolbar(R.string.no_foto_coordinate_available, R.color.red);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
        setInfoToolbar(R.string.connection_error, R.color.red);
    }

    private void setInfoToolbar (int stringId, int colorId) {
        mInfoToolbar.setBackgroundColor(getResources().getColor(colorId));
        mInfoText.setText(stringId);
    }

    private void setInfoToolbar (String message, int colorId) {
        mInfoToolbar.setBackgroundColor(getResources().getColor(colorId));
        mInfoText.setText(message);
    }

}
