package com.stefano.andrea.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.stefano.andrea.adapters.ViaggiAdapter;
import com.stefano.andrea.loaders.ViaggiLoader;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.tasks.DeleteTask;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.utils.CustomFAB;
import com.stefano.andrea.utils.MapperContext;
import com.stefano.andrea.utils.PhotoUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<List<Viaggio>>, ViaggiAdapter.ViaggioOnClickListener {

    private final static String TAG = "MainActivity";
    private final static int VIAGGI_LOADER = 0;

    private RecyclerView mRecyclerView;
    private ViaggiAdapter mAdapter;
    private ContentResolver mResolver;
    private List<Viaggio> mListaViaggi;
    private CustomFAB mFab;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //acquisisco riferimento al content provider
        mResolver = getContentResolver();
        //inizializzo recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.elenco_viaggi);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //inizializzo l'adapter
        mAdapter = new ViaggiAdapter(this, this, new ActionModeCallback());
        mRecyclerView.setAdapter(mAdapter);
        //inizializzo il caricamento dei dati dei viaggi
        getLoaderManager().initLoader(VIAGGI_LOADER, null, this);
        //acquisisco riferimento al fab
        mFab = (CustomFAB) findViewById(R.id.fab_aggiunta_viaggio);
        mFab.attachToRecyclerView(mRecyclerView);
        //Inizializzo imageloader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_aggiungi_foto_main) {
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
     * Crea un nuovo viaggio nel database
     * @param nome Il nome del viaggio
     */
    public void creaViaggio (String nome) {
        Viaggio viaggio = new Viaggio(nome);
        new InsertTask<>(this, mResolver, mAdapter, viaggio).execute(InsertTask.INSERISCI_VIAGGIO);
    }

    /**
     * Cancella i viaggi selezionati dall'utente
     */
    public void cancellaViaggi () {
        new DeleteTask<>(this, mResolver, mAdapter, mListaViaggi, mAdapter.getSelectedItems()).execute(DeleteTask.CANCELLA_VIAGGIO);
    }

    /**
     * Mostra la finestra di dialogo per creare un nuovo viaggio
     * @param view View
     */
    public void openDialogAddViaggio(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.fragment_add_viaggio, null))
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog d = (Dialog) dialog;
                        EditText nomeViaggio = (EditText) d.findViewById(R.id.text_add_viaggio);
                        creaViaggio(nomeViaggio.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    @Override
    public Loader<List<Viaggio>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case VIAGGI_LOADER:
                return new ViaggiLoader(this, mResolver);
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
    }

    @Override
    public void onLoaderReset(Loader<List<Viaggio>> loader) {
        //do nothing
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent = null;
        ArrayList<String> fotoUris = new ArrayList<>();
        if (requestCode == PhotoUtils.GALLERY_PICTURE && resultCode == RESULT_OK) {
            //singola immagine
            if (data.getData() != null) {
                intent = new Intent(this, ModInfoFotoActivity.class);
                fotoUris.add(data.getData().toString());
                intent.putExtra(ModInfoFotoActivity.EXTRA_TIPO_FOTO, PhotoUtils.GALLERY_PICTURE);
            } else if (data.getClipData() != null) {
                intent = new Intent(this, ModInfoFotoActivity.class);
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    fotoUris.add(item.getUri().toString());
                }
                intent.putExtra(ModInfoFotoActivity.EXTRA_TIPO_FOTO, PhotoUtils.GALLERY_PICTURE);
            }
        } else if (requestCode == PhotoUtils.CAMERA_REQUEST && resultCode == RESULT_OK) {
            intent = new Intent(this, ModInfoFotoActivity.class);
            fotoUris.add(mImageUri.toString());
            intent.putExtra(ModInfoFotoActivity.EXTRA_TIPO_FOTO, PhotoUtils.CAMERA_REQUEST);
        }
        if (intent != null) {
            intent.putStringArrayListExtra(ModInfoFotoActivity.EXTRA_FOTO, fotoUris);
            startActivity(intent);
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {

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
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_cancella_viaggio:
                    cancellaViaggi();
                    mode.finish();
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
    }
}
