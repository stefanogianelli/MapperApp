package com.stefano.andrea.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.stefano.andrea.activities.MainActivity;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.adapters.CittaAdapter;
import com.stefano.andrea.helpers.CittaHelper;
import com.stefano.andrea.loaders.DettagliViaggioLoader;
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.providers.MapperContract;

import java.util.List;

/**
 * DettagliViaggioFragment
 */
public class DettagliViaggioFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Citta>>, CittaAdapter.CittaOnClickListener {

    private final static int CITTA_LOADER = 0;

    private RecyclerView mRecyclerView;
    private CittaAdapter mAdapter;
    private ContentResolver mResolver;
    private CittaHelper mHelper;
    private long mIdViaggio;
    private String mNomeViaggio;
    private List<Citta> mElencoCitta;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //recupero id e nome del viaggio
        mIdViaggio = getActivity().getIntent().getExtras().getLong(MainActivity.EXTRA_ID_VIAGGIO);
        mNomeViaggio = getActivity().getIntent().getExtras().getString(MainActivity.EXTRA_NOME_VIAGGIO);
        mResolver = getActivity().getContentResolver();
        mHelper = new CittaHelper(getActivity(), mResolver);
        getLoaderManager().initLoader(CITTA_LOADER, null, this);
        getActivity().setTitle(mNomeViaggio);
        mAdapter = new CittaAdapter(getActivity(), new ActionModeCallback(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dettagli_viaggio,container,false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_elenco_citta);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        v.findViewById(R.id.fab_add_citta).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    openDialogAddCitta(v);
                } else {
                    //TODO: mostrare errore connessione internet non disponibile
                }
            }
        });
        return v;
    }

    @Override
    public void selezionataCitta(long id) {
        //TODO: intent per aprire l'activity dettagli della citta
    }

    /**
     * Aggiunge una nuova citta al viaggio
     * @param nome Il nome della citta
     * @param nazione La nazione
     * @return L'uri della citta aggiunta, null altrimenti
     */
    public Uri aggiungiCitta (String nome, String nazione) {
        long idCitta = mHelper.getDatiCitta(nome, nazione);
        if (idCitta == -1)
            idCitta = mHelper.creaCitta(nome, nazione);
        ContentValues values = new ContentValues();
        values.put(MapperContract.Citta.ID_VIAGGIO, mIdViaggio);
        values.put(MapperContract.Citta.ID_DATI_CITTA, idCitta);
        values.put(MapperContract.Citta.PERCENTUALE, 0);
        Uri uri = mResolver.insert(MapperContract.Citta.CONTENT_URI, values);
        long id = Long.parseLong(uri.getLastPathSegment());
        if (uri.getLastPathSegment().equals("-1")) {
            //recupero informazioni sulla citta
            Uri query = ContentUris.withAppendedId(MapperContract.DatiCitta.CONTENT_URI, idCitta);
            String[] projetion = {MapperContract.DatiCitta.LATITUDINE, MapperContract.DatiCitta.LONGITUDINE};
            Cursor c = mResolver.query(query, projetion, null, null, MapperContract.DatiCitta.DEFAULT_SORT);
            if (c != null) {
                c.moveToNext();
                double lon = c.getDouble(c.getColumnIndex(MapperContract.DatiCitta.LONGITUDINE));
                double lat = c.getDouble(c.getColumnIndex(MapperContract.DatiCitta.LATITUDINE));
                mAdapter.creaNuovaCitta(id, nome, nazione, lat, lon);
                c.close();
                return uri;
            }
        }
        return null;
    }

    /**
     * Cancella una citta
     * @param citta La citta da cancellare
     * @return 1 se l'operazione e' termanata correttamente, 0 altrimenti
     */
    public int cancellaCitta (Citta citta) {
        Uri uri = ContentUris.withAppendedId(MapperContract.Citta.CONTENT_URI, citta.getId());
        int count = mResolver.delete(uri, null, null);
        if (count != 0)
            mAdapter.cancellaCitta(citta);
        return count;
    }

    public int cancellaElencoCitta () {
        int count = 0;
        for (int i = mElencoCitta.size(); i >= 0; i--) {
            if (mAdapter.isSelected(i)) {
                count += cancellaCitta(mElencoCitta.get(i));
            }
        }
        return count;
    }

    public void openDialogAddCitta(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.fragment_add_citta, null))
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog d = (Dialog) dialog;
                        EditText nomeNazione = (EditText) d.findViewById(R.id.text_add_citta_nn);
                        EditText nomeCitta = (EditText) d.findViewById(R.id.text_add_citta);
                        aggiungiCitta(nomeCitta.getText().toString(), nomeNazione.getText().toString());
                        //TODO: mostrare messaggio in caso di errore
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
    public Loader<List<Citta>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case CITTA_LOADER:
                return new DettagliViaggioLoader(getActivity(), getActivity().getContentResolver(), mIdViaggio);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Citta>> loader, List<Citta> data) {
        int id = loader.getId();
        switch (id) {
            case CITTA_LOADER:
                mAdapter.setElencoCitta(data);
                mElencoCitta = data;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Citta>> loader) {
        //do nothing
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
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
                    cancellaElencoCitta();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelection();
        }
    }
}