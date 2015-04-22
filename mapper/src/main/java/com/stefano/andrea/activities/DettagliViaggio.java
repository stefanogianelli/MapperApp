package com.stefano.andrea.activities;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.stefano.andrea.adapters.CittaAdapter;
import com.stefano.andrea.loaders.DettagliViaggioLoader;
import com.stefano.andrea.models.Citta;

import java.util.List;

/**
 * DettagliViaggio
 */
public class DettagliViaggio extends Fragment implements LoaderManager.LoaderCallbacks<List<Citta>>, CittaAdapter.CittaOnClickListener {

    private final static int CITTA_LOADER = 0;

    private RecyclerView mRecyclerView;
    private CittaAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ContentResolver mResolver;
    private long mIdViaggio;
    private String mNomeViaggio;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //recupero id e nome del viaggio
        mIdViaggio = getActivity().getIntent().getExtras().getLong(MainActivity.EXTRA_ID_VIAGGIO);
        mNomeViaggio = getActivity().getIntent().getExtras().getString(MainActivity.EXTRA_NOME_VIAGGIO);
        mResolver = getActivity().getContentResolver();
        getLoaderManager().initLoader(CITTA_LOADER, null, this);
        getActivity().setTitle(mNomeViaggio);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dettagli_viaggio,container,false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_elenco_citta);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CittaAdapter(mResolver, this);
        mRecyclerView.setAdapter(mAdapter);
        v.findViewById(R.id.fab_add_citta).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogAddCitta(v);
            }
        });
        return v;
    }

    @Override
    public void selezionataCitta(long id) {
        //TODO: intent per aprire l'activity dettagli della citta
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
                        mAdapter.creaNuovaCitta(mIdViaggio, nomeCitta.getText().toString(), nomeNazione.getText().toString());
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
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Citta>> loader) {
        //do nothing
    }
}