package com.stefano.andrea.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
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

import com.melnykov.fab.FloatingActionButton;
import com.stefano.andrea.adapters.ViaggiAdapter;
import com.stefano.andrea.loaders.ViaggiLoader;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.tasks.DeleteTask;

import java.util.List;

public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<List<Viaggio>>, ViaggiAdapter.ViaggioOnClickListener {

    public final static String EXTRA_ID_VIAGGIO = "com.stefano.andrea.mainActivity.idViaggio";
    public final static String EXTRA_NOME_VIAGGIO = "com.stefano.andrea.mainActivitynomeViaggio";
    private final static int VIAGGI_LOADER = 0;

    private RecyclerView mRecyclerView;
    private ViaggiAdapter mAdapter;
    private ContentResolver mResolver;
    private List<Viaggio> mListaViaggi;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //acquisisco riferimento al content provider
        mResolver = getContentResolver();
        //inizializzo il caricamento dei dati dei viaggi
        getLoaderManager().initLoader(VIAGGI_LOADER, null, this);
        //inizializzo recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.elenco_viaggi);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //inizializzo l'adapter
        mAdapter = new ViaggiAdapter(this, this, new ActionModeCallback());
        mRecyclerView.setAdapter(mAdapter);
        //acquisisco riferimento al fab
        mFab = (FloatingActionButton) findViewById(R.id.fab_aggiunta_viaggio);
        mFab.attachToRecyclerView(mRecyclerView);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Avvia la schermata contenente i dettagli del viaggio
     * @param viaggio Il viaggio selezionato
     */
    @Override
    public void selezionatoViaggio(Viaggio viaggio) {
        Intent intent = new Intent(this, DettagliViaggioActivity.class);
        intent.putExtra(EXTRA_ID_VIAGGIO, viaggio.getId());
        intent.putExtra(EXTRA_NOME_VIAGGIO, viaggio.getNome());
        startActivity(intent);
    }

    /**
     * Crea un nuovo viaggio nel database
     * @param nome Il nome del viaggio
     * @return L'uri del viaggio inserito nel database, null se l'operazione fallisce
     */
    public Uri creaViaggio (String nome) {
        ContentValues values = new ContentValues();
        values.put(MapperContract.Viaggio.NOME, nome);
        Uri uri = mResolver.insert(MapperContract.Viaggio.CONTENT_URI, values);
        if (uri != null && uri.getLastPathSegment() != "-1") {
            long id = Long.parseLong(uri.getLastPathSegment());
            mAdapter.creaNuovoViaggio(id, nome);
            return uri;
        }
        return null;
    }

    /**
     * Cancella i viaggi selezionati dall'utente
     */
    public void cancellaViaggi () {
        new DeleteTask<>(this, mResolver, mAdapter, mListaViaggi, mAdapter.getSelectedItems()).execute(DeleteTask.CANCELLA_VIAGGIO);
    }

    /**
     * Mostra la finestra di dialogo per creare un nuovo viaggio
     * @param view
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

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mFab.hide();
            mode.getMenuInflater().inflate (R.menu.viaggi_list_on_long_click, menu);
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
            mFab.show();
        }
    }
}
