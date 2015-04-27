package com.stefano.andrea.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.stefano.andrea.activities.DettagliCittaActivity;
import com.stefano.andrea.activities.MainActivity;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.adapters.CittaAdapter;
import com.stefano.andrea.helpers.CommonAlertDialog;
import com.stefano.andrea.loaders.DettagliViaggioLoader;
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.tasks.DeleteTask;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.utils.CustomFAB;

import java.util.List;

/**
 * DettagliViaggioFragment
 */
public class DettagliViaggioFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Citta>>, CittaAdapter.CittaOnClickListener {

    private final static int CITTA_LOADER = 0;

    private RecyclerView mRecyclerView;
    private CittaAdapter mAdapter;
    private ContentResolver mResolver;
    private long mIdViaggio;
    private List<Citta> mElencoCitta;
    private CustomFAB mFab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //recupero id del viaggio
        mIdViaggio = getActivity().getIntent().getExtras().getLong(MainActivity.EXTRA_ID_VIAGGIO);
        mResolver = getActivity().getContentResolver();
        getLoaderManager().initLoader(CITTA_LOADER, null, this);
        mAdapter = new CittaAdapter(getActivity(), new ActionModeCallback(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dettagli_viaggio,container,false);
        mFab = (CustomFAB) v.findViewById(R.id.fab_aggiunta_citta);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_elenco_citta);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        mFab.attachToRecyclerView(mRecyclerView);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    openDialogAddCitta(v);
                } else {
                    new CommonAlertDialog(getActivity(), R.string.no_internet_title_dialog, R.string.no_internet_message_dialog);
                }
            }
        });
        return v;
    }

    /**
     * Avvia l'activity con i dettagli della citta
     * @param id parametro da definire
     */
    @Override
    public void selezionataCitta(long id) {
        Intent intent = new Intent(getActivity(), DettagliCittaActivity.class);
        startActivity(intent);
    }

    /**
     * Aggiunge una nuova citta al viaggio
     * @param nomeCitta Il nome della citta
     * @param nomeNazione Il nome della nazione
     */
    public void creaNuovaCitta (String nomeCitta, String nomeNazione) {
        Citta citta = new Citta();
        citta.setIdViaggio(mIdViaggio);
        citta.setNome(nomeCitta);
        citta.setNazione(nomeNazione);
        new InsertTask<>(getActivity(), mResolver, mAdapter, citta).execute(InsertTask.INSERISCI_CITTA);
    }

    /**
     * Cancella le citta selezionate dall'utente
     */
    public void cancellaElencoCitta () {
        new DeleteTask<>(getActivity(), mResolver, mAdapter, mElencoCitta, mAdapter.getSelectedItems()).execute(DeleteTask.CANCELLA_CITTA);
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
                        creaNuovaCitta(nomeCitta.getText().toString(), nomeNazione.getText().toString());
                        d.dismiss();
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
            mFab.hide();
            mFab.setForceHide(true);
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
            mAdapter.stopActionMode();
            mFab.setForceHide(false);
            mFab.show();
        }
    }
}