package com.stefano.andrea.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.adapters.CittaAdapter;
import com.stefano.andrea.helpers.CommonAlertDialog;
import com.stefano.andrea.loaders.CittaLoader;
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.tasks.DeleteTask;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.utils.CustomFAB;

import java.util.List;

/**
 * DettagliViaggioFragment
 */
public class DettagliViaggioFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Citta>> {

    private static final int CITTA_LOADER = 0;
    private static final String ID_VIAGGIO = "id_viaggio";

    public static final String ARG_INITIAL_POSITION = "ARG_INITIAL_POSITION";

    private ObservableRecyclerView mRecyclerView;
    private CittaAdapter mAdapter;
    private ContentResolver mResolver;
    private long mIdViaggio;
    private List<Citta> mElencoCitta;
    private CustomFAB mFab;
    private Activity mParentActivity;

    public DettagliViaggioFragment () { }

    public static DettagliViaggioFragment newInstance(long idViaggio) {
        DettagliViaggioFragment fragment = new DettagliViaggioFragment();
        Bundle args = new Bundle();
        args.putLong(ID_VIAGGIO, idViaggio);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //recupero id del viaggio
        if (getArguments() != null) {
            mIdViaggio = getArguments().getLong(ID_VIAGGIO);
        }
        //acquisisco riferimento all'activity
        mParentActivity = getActivity();
        //acquisisco content resolver
        mResolver = mParentActivity.getContentResolver();
        //creo l'adapter
        mAdapter = new CittaAdapter(mParentActivity, new ActionModeCallback(), (CittaAdapter.CittaOnClickListener) mParentActivity);
        //avvio il loader delle citta
        getLoaderManager().initLoader(CITTA_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dettagli_viaggio, container, false);
        //acquisisco riferimenti
        mFab = (CustomFAB) v.findViewById(R.id.fab_aggiunta_citta);
        mRecyclerView = (ObservableRecyclerView) v.findViewById(R.id.recyclerview_scroll);
        //configuro recyclerview
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mParentActivity));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        if (mParentActivity instanceof ObservableScrollViewCallbacks) {
            // Scroll to the specified offset after layout
            Bundle args = getArguments();
            if (args != null && args.containsKey(ARG_INITIAL_POSITION)) {
                final int initialPosition = args.getInt(ARG_INITIAL_POSITION, 0);
                ScrollUtils.addOnGlobalLayoutListener(mRecyclerView, new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.scrollVerticallyToPosition(initialPosition);
                    }
                });
            }
            mRecyclerView.setScrollViewCallbacks((ObservableScrollViewCallbacks) mParentActivity);
        }
        //configuro fab
        mFab.attachToRecyclerView(mRecyclerView);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connMgr = (ConnectivityManager) mParentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    openDialogAddCitta(v);
                } else {
                    new CommonAlertDialog(mParentActivity, R.string.no_internet_title_dialog, R.string.no_internet_message_dialog);
                }
            }
        });
        return v;
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
        new InsertTask<>(mParentActivity, mResolver, mAdapter, citta).execute(InsertTask.INSERISCI_CITTA);
    }

    /**
     * Cancella le citta selezionate dall'utente
     */
    public void cancellaElencoCitta () {
        new DeleteTask<>(mParentActivity, mResolver, mAdapter, mElencoCitta, mAdapter.getSelectedItems()).execute(DeleteTask.CANCELLA_CITTA);
    }

    public void openDialogAddCitta(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivity);
        LayoutInflater inflater = mParentActivity.getLayoutInflater();
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
                return new CittaLoader(mParentActivity, mResolver, mIdViaggio);
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
            mode.getMenuInflater().inflate (R.menu.menu_citta_selezionate, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_cancella_citta:
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