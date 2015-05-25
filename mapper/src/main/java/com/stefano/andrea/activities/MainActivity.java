package com.stefano.andrea.activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.snowdream.android.app.UpdateFormat;
import com.github.snowdream.android.app.UpdateManager;
import com.github.snowdream.android.app.UpdateOptions;
import com.github.snowdream.android.app.UpdatePeriod;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.EventListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.stefano.andrea.adapters.ViaggiAdapter;
import com.stefano.andrea.loaders.ViaggiLoader;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.tasks.DeleteTask;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.tasks.UpdateTask;
import com.stefano.andrea.utils.CustomFAB;
import com.stefano.andrea.utils.DialogHelper;
import com.stefano.andrea.utils.MapperContext;
import com.stefano.andrea.utils.PhotoUtils;
import com.stefano.andrea.utils.SparseBooleanArrayParcelable;
import com.stefano.andrea.utils.UpdateListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Viaggio>>, ViaggiAdapter.ViaggioOnClickListener, DialogHelper.ViaggioDialogCallback {

    private static final String BUNDLE_ACTION_MODE = "com.stefano.andrea.activities.MainActivity.actionMode";
    private static final int VIAGGI_LOADER = 0;
    private static final String TAG = "MainActivity";

    private static final String UPDATE_XML_URL = "https://github.com/stefanogianelli/MapperApp/tree/master/mapper/mapper_update.xml";

    private ViaggiAdapter mAdapter;
    private List<Viaggio> mListaViaggi;
    private CustomFAB mFab;
    private Uri mImageUri;

    private ActionMode.Callback mCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mFab.hide();
            mFab.setForceHide(true);
            mode.getMenuInflater().inflate (R.menu.menu_viaggi_selezionati, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_cancella_viaggio:
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setMessage(R.string.conferma_cancellazione_viaggi);
                    dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancellaViaggi();
                            dialog.dismiss();
                            mode.finish();
                        }
                    });
                    dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mode.finish();
                        }
                    });
                    dialog.create().show();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelection();
            mFab.setForceHide(false);
            mFab.show();
        }
    };

    private EventListener mListener = new EventListener() {
        @Override
        public void onShow(Snackbar snackbar) {
            mFab.moveUp(snackbar.getHeight());
        }

        @Override
        public void onShowByReplace(Snackbar snackbar) { }

        @Override
        public void onShown(Snackbar snackbar) { }

        @Override
        public void onDismiss(Snackbar snackbar) {
            mFab.moveDown(snackbar.getHeight());
        }

        @Override
        public void onDismissByReplace(Snackbar snackbar) { }

        @Override
        public void onDismissed(Snackbar snackbar) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //acquisisco i riferimenti
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_activity_toolbar);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.elenco_viaggi);
        mFab = (CustomFAB) findViewById(R.id.fab_aggiunta_viaggio);
        final TextView nessunViaggioInfo = (TextView) findViewById(R.id.no_viaggio);
        //attivo action bar
        toolbar.setLogo(R.drawable.logo_icon_24);
        setSupportActionBar(toolbar);
        //inizializzo recyclerview
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //inizializzo l'adapter
        mAdapter = new ViaggiAdapter(this, this, mCallback);
        recyclerView.setAdapter(mAdapter);
        //aggiungo observer all'adapter
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkVisibility();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkVisibility();
                if (((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition() >= 0) {
                    recyclerView.smoothScrollToPosition(0);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkVisibility();
            }

            private void checkVisibility () {
                if (mAdapter.getItemCount() == 0)
                    nessunViaggioInfo.setVisibility(View.VISIBLE);
                else
                    nessunViaggioInfo.setVisibility(View.GONE);
            }
        });
        //verifico ripristino della action mode
        if (savedInstanceState != null) {
            SparseBooleanArrayParcelable itemsSelected = savedInstanceState.getParcelable(BUNDLE_ACTION_MODE);
            if (itemsSelected != null) {
                mAdapter.restoreActionMode(itemsSelected);
            }
        }
        //inizializzo il caricamento dei dati dei viaggi
        getSupportLoaderManager().initLoader(VIAGGI_LOADER, null, this);
        //inizializzo Floating Action Button
        mFab.attachToRecyclerView(recyclerView);
        //Inizializzo imageloader
        setupImageLoader();
        //controllo presenza di aggiornamenti
        checkUpdates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            startActivity(new Intent(this,AboutActivity.class));
            return true;
        } else if (id == R.id.action_aggiungi_foto_main) {
            try {
                mImageUri = PhotoUtils.getOutputMediaFileUri();
            } catch (IOException e) {
                Toast.makeText(this, "Errore durante l'accesso alla memoria", Toast.LENGTH_SHORT).show();
            }
            if (mImageUri != null) {
                LinearLayout sugg = (LinearLayout) findViewById(R.id.suggerimento_crea_viaggio);
                if (sugg.getVisibility()==View.VISIBLE){slideToBottom(sugg);}
                PhotoUtils.mostraDialog(this, mImageUri);
            }
        } else if (id == R.id.action_log_reader) {
            startActivity(new Intent(this, LogActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAdapter != null && mAdapter.isEnabledSelectionMode()) {
            outState.putParcelable(BUNDLE_ACTION_MODE, mAdapter.saveActionmode());
        }
    }

    /**
     * Avvia la schermata contenente i dettagli del viaggio
     * @param viaggio Il viaggio selezionato
     */
    @Override
    public void selezionatoViaggio(Viaggio viaggio) {
        MapperContext context = MapperContext.getInstance();
        context.setIdViaggio(viaggio.getId());
        context.setNomeViaggio(viaggio.getNome());
        Intent intent = new Intent(this, DettagliViaggioActivity.class);
        startActivity(intent);
    }

    /**
     * Rimuove il viaggio selezionato
     * @param viaggio Il viaggio da rimuovere
     */
    @Override
    public void rimuoviViaggio(Viaggio viaggio) {
        List<Viaggio> elencoViaggi = new ArrayList<>();
        elencoViaggi.add(viaggio);
        List<Integer> indici = new ArrayList<>();
        indici.add(0);
        new DeleteTask<>(this, mAdapter, elencoViaggi, indici, mListener).execute(DeleteTask.CANCELLA_VIAGGIO);
    }

    /**
     * Rinomina il viaggio selezionato
     * @param position La posizione del viaggio nel recyclerview
     * @param viaggio Il viaggio da rinominare
     */
    @Override
    public void rinominaViaggio(int position, Viaggio viaggio) {
        DialogHelper.showViaggioDialog(this, position, viaggio.getId(), viaggio.getNome(), this);
    }

    /**
     * Crea o modifica un nuovo viaggio nel database
     * @param position La posizione del viaggio nel recyclerview
     * @param id L'id del viaggio, -1 se il viaggio e' da creare
     * @param nome Il nome del viaggio
     */
    @Override
    public void viaggioActionButton(int position, long id, String nome) {
        if (id == -1) {
            Viaggio viaggio = new Viaggio(nome);
            new InsertTask<>(this, mAdapter, viaggio, mListener).execute(InsertTask.INSERISCI_VIAGGIO);
        } else {
            List<Integer> elencoId = new ArrayList<>();
            elencoId.add((int) id);
            ContentValues values = new ContentValues();
            values.put(MapperContract.Viaggio.NOME, nome);
            new UpdateTask(this, position, values, elencoId, mAdapter, mListener).execute(UpdateTask.UPDATE_VIAGGIO);
        }
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
     * Cancella i viaggi selezionati dall'utente
     */
    private void cancellaViaggi() {
        new DeleteTask<>(this, mAdapter, mListaViaggi, mAdapter.getSelectedItems(), mListener).execute(DeleteTask.CANCELLA_VIAGGIO);
    }

    /**
     * Mostra la finestra di dialogo per creare un nuovo viaggio
     * @param view View
     */
    public void openDialogAddViaggio(View view) {
        LinearLayout sugg = (LinearLayout) findViewById(R.id.suggerimento_crea_viaggio);
        if (sugg.getVisibility()==View.VISIBLE){slideToBottom(sugg);}
        DialogHelper.showViaggioDialog(this, -1, -1, null, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PhotoUtils.startIntent(this, requestCode, resultCode, data, mImageUri, -1, -1);
    }

    @Override
    public Loader<List<Viaggio>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case VIAGGI_LOADER:
                return new ViaggiLoader(this);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Viaggio>> loader, List<Viaggio> data) {
        int id = loader.getId();
        switch (id) {
            case VIAGGI_LOADER:
                mAdapter.setListaViaggi(data);
                mListaViaggi = data;
        }
        if (mAdapter.getItemCount() == 0){
            final LinearLayout sugg = (LinearLayout) findViewById(R.id.suggerimento_crea_viaggio);
            animateSugg(sugg);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Viaggio>> loader) { }

    public void slideToBottom(View view){
        TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight());
        animate.setDuration(500);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    public void animateSugg(final View view){
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0.0f);
        view.animate()
                .translationY(view.getHeight())
                .alpha(1.0f)
                .setDuration(900);
    }

    private void checkUpdates () {
        UpdateManager manager = new UpdateManager(this);

        UpdateOptions options = new UpdateOptions.Builder(this)
                .checkUrl(UPDATE_XML_URL)
                .updateFormat(UpdateFormat.XML)
                .updatePeriod(new UpdatePeriod(UpdatePeriod.EACH_TIME))
                .checkPackageName(true)
                .build();
        manager.check(this, options, new UpdateListener());
    }

}
