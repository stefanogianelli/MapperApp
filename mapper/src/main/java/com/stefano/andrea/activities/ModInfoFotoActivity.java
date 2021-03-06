package com.stefano.andrea.activities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.DocumentsContract;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.stefano.andrea.dialogs.AddCittaDialog;
import com.stefano.andrea.dialogs.AddPostoDialog;
import com.stefano.andrea.dialogs.DialogHelper;
import com.stefano.andrea.intents.MapperIntent;
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.models.Posto;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.services.FetchAddressIntentService;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.tasks.UpdateTask;
import com.stefano.andrea.utils.LocationManagerCheck;
import com.stefano.andrea.utils.PhotoUtils;

import java.util.ArrayList;
import java.util.List;

public class ModInfoFotoActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, AddCittaDialog.AggiungiCittaCallback, AddPostoDialog.AggiungiPostoCallback, LocationListener {

    private static final String CLASS_NAME = "com.stefano.andrea.activities.ModInfoFotoAcitivity";

    public final static String EXTRA_ID_VIAGGIO = CLASS_NAME + ".idViaggio";
    public final static String EXTRA_ID_CITTA = CLASS_NAME + ".idCitta";
    public final static String EXTRA_ID_POSTO = CLASS_NAME + ".idPosto";
    public final static String EXTRA_FOTO = CLASS_NAME + ".foto";
    public final static String EXTRA_LISTA_FOTO = CLASS_NAME + ".listaFoto";
    public final static String EXTRA_TIPO_FOTO = CLASS_NAME + ".tipoFoto";

    private static final String TAG = "ModInfoFotoActivity";

    private static final int CLEAR_CITTA = 1;
    private static final int CLEAR_POSTO = 2;
    private static final int ID_INSERT_CITY = -42;
    private static final int EMPTY_ID = -1;

    private static final String ADDRESS_REQUESTED_KEY = CLASS_NAME + ".address_request_pending";
    private static final String LOCATION_ADDRESS_KEY = CLASS_NAME + ".location_address";
    private static final String FOTO_ADDRESS_KEY = CLASS_NAME + ".foto_address";
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = CLASS_NAME + ".requesting_location_updates_key";

    //Localization variables
    private GoogleApiClient mGoogleApiClient;
    /** Represents a geographical location. */
    private Location mLastLocation;
    private Location mFotoLocation;
    private boolean mAddressRequested;
    private boolean mFotoRequested;
    private AddressResultReceiver mResultReceiver;
    private Citta mCittaLocalizzata;
    private LocationManagerCheck mLocationManagerCheck;
    /** Stores parameters for requests to the FusedLocationProviderApi. */
    private LocationRequest mLocationRequest;
    /** Tracks the status of the location updates request. Value changes when the user presses the Start Updates and Stop Updates buttons. */
    private Boolean mRequestingLocationUpdates;
    /** The desired interval for location updates. Inexact. Updates may be more or less frequent. */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    /** The fastest rate for active location updates. Exact. Updates will never be more frequent than this value. */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private ContentResolver mResolver;
    private boolean mFotoSalvata = false;
    private boolean mFotoCancellata = false;
    private boolean mStartFromIntent = false;
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
    private ArrayList<Uri> mFotoUris;
    //elenco degli ID delle immagini da modificare
    private ArrayList<Integer> mFotoIDs;
    //indica se la foto e' stata scattata dalla fotocamera o acquisita dalla galleria
    private int mTipoFoto;
    //viaggio al quale associare la/e foto
    private Viaggio mViaggioSelezionato;
    //citta alla quale associare la/e foto
    private Citta mCittaSelezionata;
    /** posto al quale associare la/e foto */
    private Posto mPostoSelezionato;
    /** ID del viaggio passato tramite intent */
    private long mIdViaggio;
    /** ID della citta' passato tramite intent */
    private long mIdCitta;
    /** ID del posto passato tramite intent */
    private long mIdPosto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_info_foto);
        //setup della toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_foto_activity);
        setSupportActionBar(toolbar);
        mResolver = getContentResolver();
        //acquisisco parametri dall'intent e inizializzo a EMPTY_ID quelli senza valore
        Intent intent = getIntent();
        String action = intent.getAction();
        if (MapperIntent.MAPPER_FOTO.equals(action) || MapperIntent.MAPPER_MODIFICA_FOTO.equals(action)) {
            //foto dalla app
            mFotoUris = intent.getParcelableArrayListExtra(EXTRA_FOTO);
        } else if (Intent.ACTION_SEND.equals(action)) {
            //ricevuta una foto via intent
            mFotoUris = new ArrayList<>();
            mFotoUris.add((Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
            mStartFromIntent = true;
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            //ricevute più foto via intent
            mFotoUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            mStartFromIntent = true;
        }
        //verifico se abilitare la frecca indietro della action bar
        if (!mStartFromIntent && getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //acquisisco path dei file quando effettuo acquisizioni dalla galleria
        mTipoFoto = intent.getIntExtra(EXTRA_TIPO_FOTO, EMPTY_ID);
        if (!MapperIntent.MAPPER_MODIFICA_FOTO.equals(action) && mTipoFoto != PhotoUtils.CAMERA_REQUEST) {
            List<Uri> correctedUris = new ArrayList<>();
            for (int i = 0; i < mFotoUris.size(); i++) {
                Log.d(TAG, "Original uri: " + mFotoUris.get(i).toString());
                correctedUris.add(getGalleryPhotoUri(mFotoUris.get(i)));
                Log.d(TAG, "New uri: " + correctedUris.get(i).toString());
            }
            mFotoUris.clear();
            mFotoUris.addAll(correctedUris);
        }
        if (intent.hasExtra(EXTRA_LISTA_FOTO))
            mFotoIDs = intent.getIntegerArrayListExtra(EXTRA_LISTA_FOTO);
        mIdViaggio = intent.getLongExtra(EXTRA_ID_VIAGGIO, EMPTY_ID);
        mIdCitta = intent.getLongExtra(EXTRA_ID_CITTA, EMPTY_ID);
        mIdPosto = intent.getLongExtra(EXTRA_ID_POSTO, EMPTY_ID);
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
                    fetchAddressButtonHandler();
                }
            }
        });
        //setup dei pulsanti
        setupAddViaggioButton();
        setupAddCittaButton();
        setupAddPostoButton();
        //inizializzo immagine
        setupImageLoader();
        inizializzaFoto();
        //carico elenchi
        updateViaggi();
        //inizializzo viaggio
        inizializzaViaggio(mIdViaggio);
        //inizializzo citta
        inizializzaCitta(mIdCitta);
        //inizializzo posto
        inizializzaPosto(mIdPosto);
        //configuro handler per la localizzazione
        mLocationManagerCheck = new LocationManagerCheck(this);
        mResultReceiver = new AddressResultReceiver(new Handler());
        mAddressRequested = false;
        mFotoRequested = false;
        mRequestingLocationUpdates = true;
        updateValuesFromBundle(savedInstanceState);

        buildGoogleApiClient();
        updateUIWidgets();
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
            //verifico se sono in modalita' modifica o creazione
            if (mFotoIDs != null) {
                //modalita' modifica
                if (mViaggioSelezionato != null && mViaggioSelezionato.getId() != EMPTY_ID) {
                    if (mCittaSelezionata != null && mCittaSelezionata.getId() != EMPTY_ID && mCittaSelezionata.getId() != ID_INSERT_CITY) {
                        updateFoto();
                    } else if (mCittaLocalizzata != null) {
                        addNewCity();
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.citta_non_selezionata), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getResources().getString(R.string.viaggio_non_selezionato), Toast.LENGTH_SHORT).show();
                }
            } else {
                //modalità creazione
                if (mViaggioSelezionato != null && mViaggioSelezionato.getId() != EMPTY_ID)
                    if (mCittaSelezionata != null && mCittaSelezionata.getId() != EMPTY_ID && mCittaSelezionata.getId() != ID_INSERT_CITY) {
                        addFoto();
                    } else if (mCittaLocalizzata != null) {
                        addNewCity();
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.citta_non_selezionata), Toast.LENGTH_SHORT).show();
                    }
                else {
                    Toast.makeText(this, getResources().getString(R.string.viaggio_non_selezionato), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        } else if (id == R.id.action_annula_foto) {
            if (mTipoFoto == PhotoUtils.CAMERA_REQUEST) {
                mFotoCancellata = cancellaFoto();
                if (mFotoCancellata)
                    Log.d(TAG, "Foto cancellata con successo");
                else
                    Log.d(TAG, "Errore durante l'eliminazione della foto");
            }
            endActivity();
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
                if (mFotoIDs == null) {
                    addFoto();
                } else {
                    updateFoto();
                }
            }
        };
        new InsertTask<>(ModInfoFotoActivity.this, adapter, mCittaLocalizzata).execute(InsertTask.INSERISCI_CITTA);
    }

    private void updateFoto () {
        boolean updated = false;
        ContentValues values = new ContentValues();
        if (mIdViaggio != mViaggioSelezionato.getId()) {
            //modifico viaggio
            values.put(MapperContract.Foto.ID_VIAGGIO, mViaggioSelezionato.getId());
        }
        if (mIdCitta != mCittaSelezionata.getId()) {
            //modifico citta'
            values.put(MapperContract.Foto.ID_CITTA, mCittaSelezionata.getId());
            //resetto il posto
            values.putNull(MapperContract.Foto.ID_POSTO);
            updated = true;
        }
        if (mPostoSelezionato != null && mIdPosto != mPostoSelezionato.getId()) {
            //modifico posto
            if (mPostoSelezionato.getId() == EMPTY_ID)
                values.putNull(MapperContract.Foto.ID_POSTO);
            else
                values.put(MapperContract.Foto.ID_POSTO, mPostoSelezionato.getId());
            updated = true;
        }
        if (updated) {
            String indirizzo = "";
            if (mPostoSelezionato != null && mPostoSelezionato.getId() != EMPTY_ID)
                indirizzo += mPostoSelezionato.getNome() + ", ";
            indirizzo += mCittaSelezionata.getNome() + ", " + mCittaSelezionata.getNazione();
            values.put(MapperContract.Foto.INDIRIZZO, indirizzo);
        }
        if (values.size() > 0) {
            UpdateTask.UpdateAdapter adapter = new UpdateTask.UpdateAdapter() {
                @Override
                public void UpdateItem(int position, String nome) {
                    endActivity();
                }
            };
            new UpdateTask(this, 0, values, mFotoIDs, adapter).execute(UpdateTask.UPDATE_FOTO);
        } else {
            endActivity();
        }
    }

    private void addFoto () {
        List<Foto> elencoFoto = new ArrayList<>();
        for (int i = 0; i < mFotoUris.size(); i++) {
            Foto foto = new Foto();
            foto.setPath(mFotoUris.get(i).toString());
            //acquisisco dettagli dell'immagine
            getMediaStoreData(foto);
            //acquisisco dettagli dell'immagine
            if (foto.getLatitudine() == 0 && foto.getLongitudine() == 0) {
                if (mPostoSelezionato != null) {
                    foto.setLatitudine(mPostoSelezionato.getLatitudine());
                    foto.setLongitudine(mPostoSelezionato.getLongitudine());
                } else {
                    foto.setLatitudine(mCittaSelezionata.getLatitudine());
                    foto.setLongitudine(mCittaSelezionata.getLongitudine());
                }
            }
            foto.setIdViaggio(mViaggioSelezionato.getId());
            foto.setIdCitta(mCittaSelezionata.getId());
            String indirizzo = "";
            if (mPostoSelezionato != null && mPostoSelezionato.getId() != EMPTY_ID) {
                foto.setIdPosto(mPostoSelezionato.getId());
                indirizzo += mPostoSelezionato.getNome() + ", ";
            }
            indirizzo += mCittaSelezionata.getNome() + ", " + mCittaSelezionata.getNazione();
            foto.setIndirizzo(indirizzo);
            if (mTipoFoto == PhotoUtils.CAMERA_REQUEST)
                foto.setCamera(1);
            elencoFoto.add(foto);
        }
        InsertTask.InsertAdapter adapter = new InsertTask.InsertAdapter() {
            @Override
            public void insertItem(Object item) {
                endActivity();
            }
        };
        new InsertTask<>(this, adapter, elencoFoto).execute(InsertTask.INSERISCI_FOTO);
        mFotoSalvata = true;
    }

    /**
     * Mostra la foto scelta nell'apposita view
     */
    private void inizializzaFoto() {
        if (mFotoUris != null) {
            //mostro la prima immagine a campione
            ImageLoader.getInstance().displayImage(mFotoUris.get(0).toString(), mImageView);
            if (mFotoUris.size() > 1) {
                mCountImages.setText(mFotoUris.size() + " immagini");
                mCountImages.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Mostra il nome del viaggio corrente e aggiorna l'elenco delle citta'
     */
    private void displayViaggio () {
        mViaggioText.setText(mViaggioSelezionato.getNome());
        if (mCittaLocalizzata != null) {
            clearSelection(CLEAR_CITTA);
            displayAddressOutput();
        } else
            updateCitta();
    }

    /**
     * Seleziona il vaggio se l'id e' diverso da EMPTY_ID
     * @param idViaggio L'id del viaggio
     */
    private void inizializzaViaggio(long idViaggio) {
        //controllo se e' stato selezionato un viaggio
        if (idViaggio != EMPTY_ID) {
            //viaggio selezionato
            Uri viaggio = ContentUris.withAppendedId(MapperContract.Viaggio.CONTENT_URI, idViaggio);
            String [] projection = { MapperContract.Viaggio.NOME };
            Cursor curViaggio = mResolver.query(viaggio, projection, null, null, null);
            if (curViaggio.moveToFirst()) {
                String nomeViaggio = curViaggio.getString(curViaggio.getColumnIndex(projection[0]));
                mViaggioSelezionato = new Viaggio(idViaggio, nomeViaggio);
                displayViaggio();
            }
            curViaggio.close();
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
                            mViaggioSelezionato = elencoViaggi.get(position);
                            displayViaggio();

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
     * Mostra la citta' selezionata e aggiorna l'elenco dei posti
     */
    private void displayCitta () {
        mCittaText.setText(mCittaSelezionata.getNome());
        clearSelection(CLEAR_POSTO);
        updatePosti();
    }

    /**
     * Seleziona la citta' se l'id e' diverso da EMPTY_ID
     * @param idCitta L'id della citta'
     */
    private void inizializzaCitta (long idCitta) {
        if (idCitta != EMPTY_ID) {
            Uri query = ContentUris.withAppendedId(MapperContract.Citta.CONTENT_URI, idCitta);
            String [] projection = {MapperContract.DatiCitta.NOME, MapperContract.DatiCitta.NAZIONE, MapperContract.DatiCitta.LATITUDINE, MapperContract.DatiCitta.LONGITUDINE};
            Cursor cCitta = mResolver.query(query, projection, null, null, null);
            if (cCitta.moveToFirst()) {
                mCittaSelezionata = new Citta();
                mCittaSelezionata.setId(idCitta);
                mCittaSelezionata.setNome(cCitta.getString(cCitta.getColumnIndex(projection[0])));
                mCittaSelezionata.setNazione(cCitta.getString(cCitta.getColumnIndex(projection[1])));
                mCittaSelezionata.setLatitudine(cCitta.getLong(cCitta.getColumnIndex(projection[2])));
                mCittaSelezionata.setLongitudine(cCitta.getLong(cCitta.getColumnIndex(projection[3])));
                displayCitta();
            }
            cCitta.close();
        }
    }

    /**
     * Aggiorna l'elenco delle citta' disponibili
     */
    private void updateCitta () {
        final List<Citta> elencoCitta = new ArrayList<>();
        if (mViaggioSelezionato != null) {
            Uri query = ContentUris.withAppendedId(MapperContract.Citta.DETTAGLI_VIAGGIO_URI, mViaggioSelezionato.getId());
            String[] projection = {MapperContract.Citta.ID_CITTA, MapperContract.DatiCitta.NOME, MapperContract.DatiCitta.NAZIONE,
                MapperContract.DatiCitta.LATITUDINE, MapperContract.DatiCitta.LONGITUDINE};
            Cursor curCitta = mResolver.query(query, projection, null, null, MapperContract.Citta.DEFAULT_SORT);
            while (curCitta.moveToNext()) {
                Citta citta = new Citta();
                citta.setId(curCitta.getLong(curCitta.getColumnIndex(projection[0])));
                citta.setNome(curCitta.getString(curCitta.getColumnIndex(projection[1])));
                citta.setNazione(curCitta.getString(curCitta.getColumnIndex(projection[2])));
                citta.setLatitudine(curCitta.getDouble(curCitta.getColumnIndex(projection[3])));
                citta.setLongitudine(curCitta.getDouble(curCitta.getColumnIndex(projection[4])));
                elencoCitta.add(citta);
            }
            curCitta.close();
            mAddCittaButton.setClickable(true);
        }
        if (mCittaLocalizzata != null) {
            if (!elencoCitta.contains(mCittaLocalizzata)) {
                elencoCitta.add(0, mCittaLocalizzata);
                mCittaSelezionata = mCittaLocalizzata;
            } else {
                int pos = elencoCitta.indexOf(mCittaLocalizzata);
                mCittaSelezionata = elencoCitta.get(pos);
                mCittaText.setText(mCittaLocalizzata.getNome());
                updatePosti();
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
                            mCittaSelezionata = elencoCitta.get(position);
                            displayCitta();
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
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(android.R.id.content, dialog).addToBackStack(null).commit();
            }
        });
        mAddCittaButton.setClickable(false);
    }

    /**
     * Inizializza il posto se l'id e' diverso da EMPTY_ID
     * @param idPosto L'id del posto
     */
    private void inizializzaPosto (long idPosto) {
        if (idPosto != EMPTY_ID) {
            Uri query = ContentUris.withAppendedId(MapperContract.Posto.CONTENT_URI, idPosto);
            String [] projection = {MapperContract.Luogo.NOME, MapperContract.Luogo.LATITUDINE, MapperContract.Luogo.LONGITUDINE};
            Cursor curPosto = mResolver.query(query, projection, null, null, null);
            if (curPosto.moveToFirst()) {
                String nomePosto = curPosto.getString(curPosto.getColumnIndex(projection[0]));
                mPostoText.setText(nomePosto);
                mPostoSelezionato = new Posto();
                mPostoSelezionato.setId(idPosto);
                mPostoSelezionato.setNome(curPosto.getString(curPosto.getColumnIndex(projection[0])));
                mPostoSelezionato.setLatitudine(curPosto.getDouble(curPosto.getColumnIndex(projection[1])));
                mPostoSelezionato.setLongitudine(curPosto.getDouble(curPosto.getColumnIndex(projection[2])));
            }
            curPosto.close();
        }
    }

    /**
     * Aggiorna l'elenco dei posti disponibili
     */
    private void updatePosti () {
        final List<Posto> elencoPosti = new ArrayList<>();
        elencoPosti.add(new Posto(EMPTY_ID, "Nessun Posto"));
        if (mCittaSelezionata.getId() != ID_INSERT_CITY) {
            Uri query = ContentUris.withAppendedId(MapperContract.Posto.POSTI_IN_CITTA_URI, mCittaSelezionata.getId());
            String[] projection = {MapperContract.Posto.ID_POSTO, MapperContract.Luogo.NOME, MapperContract.Luogo.LATITUDINE, MapperContract.Luogo.LONGITUDINE};
            Cursor curPosto = mResolver.query(query, projection, null, null, MapperContract.Luogo.DEFAULT_SORT);
            while (curPosto.moveToNext()) {
                Posto posto = new Posto();
                posto.setId(curPosto.getLong(curPosto.getColumnIndex(projection[0])));
                posto.setNome(curPosto.getString(curPosto.getColumnIndex(projection[1])));
                posto.setLatitudine(curPosto.getDouble(curPosto.getColumnIndex(projection[2])));
                posto.setLongitudine(curPosto.getDouble(curPosto.getColumnIndex(projection[3])));
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
     * @param foto La foto che si vuole cercare
     */
    private void getMediaStoreData (Foto foto) {
        String [] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE,
                MediaStore.MediaColumns.WIDTH, MediaStore.MediaColumns.HEIGHT, MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.SIZE};
        String selection = MediaStore.Images.Media.DATA + "=?";
        String [] selectionArgs = { foto.getPath().substring(7) };
        Cursor cursor = mResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor.moveToFirst()) {
            foto.setIdMediaStore(cursor.getInt(cursor.getColumnIndex(projection[0])));
            foto.setData(cursor.getInt(cursor.getColumnIndex(projection[1])));
            foto.setLatitudine(cursor.getDouble(cursor.getColumnIndex(projection[2])));
            foto.setLongitudine(cursor.getDouble(cursor.getColumnIndex(projection[3])));
            foto.setWidth(cursor.getString(cursor.getColumnIndex(projection[4])));
            foto.setHeight(cursor.getString(cursor.getColumnIndex(projection[5])));
            foto.setMimeType(cursor.getString(cursor.getColumnIndex(projection[6])));
            foto.setSize(cursor.getInt(cursor.getColumnIndex(projection[7])));
        }
        cursor.close();
    }

    /**
     * Restituisce le coordinate di una foto
     * @param path Il percorso della foto
     * @return Le coordinate della foto
     */
    private LatLng getCoordinates (String path) {
        String [] projection = { MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE };
        String selection = MediaStore.Images.Media.DATA + "=?";
        String [] selectionArgs = { path.substring(7) };
        Cursor cursor = mResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
        double lat = 0;
        double lon = 0;
        if (cursor.moveToFirst()) {
            lat = cursor.getDouble(cursor.getColumnIndex(projection[0]));
            lon = cursor.getDouble(cursor.getColumnIndex(projection[1]));
        }
        cursor.close();
        return new LatLng(lat, lon);
    }

    /**
     * Cancella la foto selezionata
     * Da richiamare solo se la foto e' stata scattata dalla fotocamera
     * @return true se il file e' stato eliminato, falso se si e' verificato un errore o il file non e' stato trovato
     */
    private boolean cancellaFoto () {
        String selection = MediaStore.Images.Media.DATA + "=?";
        String [] selectionArgs = {mFotoUris.get(0).toString().substring(7)};
        int count = mResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
        return count > 0;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            if (mLocationManagerCheck.isLocationServiceAvailable()) {
                startLocationUpdates();
                updateUIWidgets();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            if (mLocationManagerCheck.isLocationServiceAvailable()) {
                stopLocationUpdates();
                updateUIWidgets();
            }
        }
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
        super.onDestroy();
        if (mTipoFoto == PhotoUtils.CAMERA_REQUEST && !mFotoSalvata && !mFotoCancellata) {
            boolean res = cancellaFoto();
            Log.v(TAG, "onDestroy: cancellata immagine? " + res);
        }
    }

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
            if (savedInstanceState.keySet().contains(FOTO_ADDRESS_KEY)) {
                mFotoRequested = savedInstanceState.getBoolean(FOTO_ADDRESS_KEY);
            }
            // Check savedInstanceState to see if the location address string was previously found
            // and stored in the Bundle. If it was found, display the address string in the UI.
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mCittaLocalizzata = savedInstanceState.getParcelable(LOCATION_ADDRESS_KEY);
                if (mCittaSelezionata == null)
                    displayAddressOutput();
            }
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
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
        createLocationRequest();
    }

    /**
     * Runs when user clicks the Fetch Address button. Starts the service to fetch the address if
     * GoogleApiClient is connected.
     */
    private void fetchAddressButtonHandler() {
        if(mLastLocation != null) {
            mAddressRequested = true;
            if (mGoogleApiClient.isConnected()) {
                startIntentService();
            }
            setInfoToolbar(R.string.recupero_info, R.color.white, R.color.black);
            updateUIWidgets();
        } else {
           if(!mLocationManagerCheck.isLocationServiceAvailable()){
               mLocationManagerCheck.createLocationServiceError();
           } else {
               setInfoToolbar(R.string.no_address_found, R.color.red, R.color.white);
           }
        }
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
                setInfoToolbar(resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY), R.color.red, R.color.white);
            } else if (resultCode == FetchAddressIntentService.SUCCESS_RESULT) {
                String nome = resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY);
                String nazione = resultData.getString(FetchAddressIntentService.RESULT_COUNTRY);
                LatLng coordinates = resultData.getParcelable(FetchAddressIntentService.RESULT_COORDINATES);
                String message = getResources().getString(R.string.photo_information_found, nome);
                setInfoToolbar(message, R.color.green, R.color.white);
                mCittaLocalizzata = new Citta();
                mCittaLocalizzata.setId(ID_INSERT_CITY);
                mCittaLocalizzata.setNome(nome);
                mCittaLocalizzata.setNazione(nazione);
                mCittaLocalizzata.setLatitudine(coordinates.latitude);
                mCittaLocalizzata.setLongitudine(coordinates.longitude);
                //update UI
                displayAddressOutput();
            }

            // Reset. Enable the Fetch Address button and stop showing the progress bar.
            mAddressRequested = false;
            mFotoRequested = false;
            updateUIWidgets();
        }
    }

    /**
     * Toggles the visibility of the progress bar. Enables or disables the Fetch Address button.
     */
    private void updateUIWidgets() {
        if (mAddressRequested || mFotoRequested) {
            mInfoProgress.setVisibility(ProgressBar.VISIBLE);
            mGeolocalizzaButton.setEnabled(false);
        } else {
            if (mInfoProgress.getVisibility() == ProgressBar.VISIBLE)
                mInfoProgress.setVisibility(ProgressBar.GONE);
            if (mLocationManagerCheck.isLocationServiceAvailable() && mRequestingLocationUpdates) {
                //attendo acquisizione posizione
                mGeolocalizzaButton.setText(R.string.location_search_in_progress);
                mGeolocalizzaButton.setEnabled(false);
            } else {
                mGeolocalizzaButton.setText(R.string.geolocalizza);
                mGeolocalizzaButton.setEnabled(true);
            }
        }
    }

    /**
     * Updates the address in the UI.
     */
    private void displayAddressOutput() {
        mCittaText.setText(mCittaLocalizzata.getNome());
        clearSelection(CLEAR_POSTO);
        updateCitta();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save whether the address has been requested.
        savedInstanceState.putBoolean(ADDRESS_REQUESTED_KEY, mAddressRequested);
        savedInstanceState.putBoolean(FOTO_ADDRESS_KEY, mFotoRequested);
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
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
                setInfoToolbar(R.string.no_geocoder_available, R.color.red, R.color.white);
                return;
            }
            //disable location updates
            mRequestingLocationUpdates = false;
            stopLocationUpdates();
            updateUIWidgets();
            // It is possible that the user presses the button to get the address before the
            // GoogleApiClient object successfully connects. In such a case, mAddressRequested
            // is set to true, but no attempt is made to fetch the address (see
            // fetchAddressButtonHandler()) . Instead, we start the intent service here if the
            // user has requested an address, since we now have a connection to GoogleApiClient.
            if (mAddressRequested) {
                startIntentService();
            }
        }

        if (mRequestingLocationUpdates) {
            if (mLocationManagerCheck.isLocationServiceAvailable()) {
                startLocationUpdates();
                updateUIWidgets();
            }
        }

        if (mFotoIDs == null && mCittaLocalizzata == null && !mAddressRequested && mIdCitta == EMPTY_ID && mIdPosto == EMPTY_ID) {
            //Acquisisco coordinate della foto
            LatLng coord = getCoordinates(mFotoUris.get(0).toString());
            double latitudine = coord.latitude;
            double longitudine = coord.longitude;
            if (latitudine != 0 && longitudine != 0) {
                mFotoLocation = new Location("Mapper");
                mFotoLocation.setLatitude(latitudine);
                mFotoLocation.setLongitude(longitudine);
                mFotoRequested = true;
                startIntentService();
                setInfoToolbar(R.string.recupero_info_foto, R.color.white, R.color.black);
                updateUIWidgets();
            } else {
                setInfoToolbar(R.string.no_foto_coordinate_available, R.color.red, R.color.white);
            }
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
        setInfoToolbar(R.string.connection_error, R.color.red, R.color.white);
    }

    private void setInfoToolbar (String message, int colorId, int textColorId) {
        if (mInfoToolbar.getVisibility() == View.INVISIBLE)
            mInfoToolbar.setVisibility(View.VISIBLE);
        mInfoToolbar.setBackgroundColor(getResources().getColor(colorId));
        mInfoText.setText(message);
        mInfoText.setTextColor(getResources().getColor(textColorId));
    }

    private void setInfoToolbar (int stringId, int colorId, int textColorId) {
        setInfoToolbar(getString(stringId), colorId, textColorId);
    }

    /**
     * Restituisce l'uri corretta dell'immagine selezionata dalla galleria.
     * E' necessario l'utilizzo quando la versione di android e' superiore a kitkat
     * @param imageUri L'uri dell'immagine acquisita dal document provider
     * @return L'uri corretta dell'immagine
     */
    private Uri getGalleryPhotoUri (Uri imageUri) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                //documents uri
                String wholeID = DocumentsContract.getDocumentId(imageUri);
                String id = wholeID.split(":")[1];
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(id).build();
            } catch (IllegalArgumentException e) {
                //media provider uri
                uri = imageUri;
            }
        } else {
            //on version below kitkat no need to involve DocumentsProvider
            uri = imageUri;
        }
        String [] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = mResolver.query(uri, projection, null, null, null);
        String filePath = null;
        int columnIndex = cursor.getColumnIndex(projection[0]);
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return Uri.parse("file://" + filePath);
    }

    /**
     * Inizializza, se necessario, la libreria Universal Image Loader
     */
    private void setupImageLoader () {
        if (!ImageLoader.getInstance().isInited()) {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.noimg)
                    .showImageOnFail(R.drawable.noimg)
                    .cacheOnDisk(true)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .considerExifParams(true)
                    .build();
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                    .defaultDisplayImageOptions(options)
                    .build();
            ImageLoader.getInstance().init(config);
        }
    }

    /**
     * Termina l'activity e rimanda alla home se l'app è stata avviata tramite intent
     */
    private void endActivity () {
        if (mStartFromIntent) {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    private void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mRequestingLocationUpdates = false;
        stopLocationUpdates();
        updateUIWidgets();
    }

}