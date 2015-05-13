package com.stefano.andrea.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.stefano.andrea.activities.DettagliCittaActivity;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.adapters.CittaAdapter;
import com.stefano.andrea.dialogs.AddCittaDialog;
import com.stefano.andrea.loaders.CittaLoader;
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.tasks.DeleteTask;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.utils.CustomFAB;
import com.stefano.andrea.utils.DialogHelper;
import com.stefano.andrea.utils.MapperContext;

import java.util.ArrayList;
import java.util.List;

/**
 * DettagliViaggioFragment
 */
public class DettagliViaggioFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Citta>>, CittaAdapter.CittaOnClickListener, AddCittaDialog.AggiungiCittaCallback {

    private static final int CITTA_LOADER = 1;
    private static final String ID_VIAGGIO = "com.stefano.andrea.fragments.DettagliViaggioFragment.idViaggio";

    private CittaAdapter mAdapter;
    private long mIdViaggio;
    private List<Citta> mElencoCitta;
    private CustomFAB mFab;
    private Activity mParentActivity;
    private MapperContext mContext;

    private ActionMode.Callback mCallback = new ActionMode.Callback () {

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
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_cancella_citta:
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mParentActivity);
                    dialog.setMessage(R.string.conferma_cancellazione_piu_citta);
                    dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancellaElencoCitta();
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
            mAdapter.stopActionMode();
            mFab.setForceHide(false);
            mFab.show();
        }
    };

    public DettagliViaggioFragment () { }

    public static DettagliViaggioFragment newInstance(long idViaggio) {
        DettagliViaggioFragment fragment = new DettagliViaggioFragment();
        Bundle args = new Bundle();
        args.putLong(ID_VIAGGIO, idViaggio);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mParentActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //recupero id del viaggio
        if (getArguments() != null) {
            mIdViaggio = getArguments().getLong(ID_VIAGGIO);
        }
        //creo l'adapter
        mAdapter = new CittaAdapter(mParentActivity, mCallback, this);
        //avvio il loader delle citta
        getLoaderManager().initLoader(CITTA_LOADER, null, this);
        //acquisisco contesto
        mContext = MapperContext.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dettagli_viaggio, container, false);
        //acquisisco riferimenti
        mFab = (CustomFAB) v.findViewById(R.id.fab_aggiunta_citta);
        RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerview_scroll);
        //configuro recyclerview
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mParentActivity));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
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
                    DialogHelper.showAlertDialog(mParentActivity, R.string.no_internet_title_dialog, R.string.no_internet_message_dialog);
                }
            }
        });
        return v;
    }

    /**
     * Aggiunge una nuova citta al viaggio
     * @param nomeCitta Il nome della citta
     * @param coordinates Le coordinate della citta'
     */
    @Override
    public void creaNuovaCitta(String nomeCitta, String indirizzo, LatLng coordinates) {
        Citta citta = new Citta();
        citta.setIdViaggio(mIdViaggio);
        citta.setNome(nomeCitta);
        citta.setNazione(indirizzo);
        citta.setLatitudine(coordinates.latitude);
        citta.setLongitudine(coordinates.longitude);
        new InsertTask<>(mParentActivity, mAdapter, citta).execute(InsertTask.INSERISCI_CITTA);
    }

    /**
     * Avvia l'activity con i dettagli della citta
     * @param citta La citta selezionata
     */
    @Override
    public void selezionataCitta(Citta citta) {
        mContext.setIdCitta(citta.getId());
        mContext.setNomeCitta(citta.getNome());
        Intent intent = new Intent(mParentActivity, DettagliCittaActivity.class);
        startActivity(intent);
    }

    /**
     * Cancella una citta
     * @param citta La citta da rimuovere
     */
    @Override
    public void cancellaCitta(Citta citta) {
        List<Citta> elencoCitta = new ArrayList<>();
        elencoCitta.add(citta);
        List<Integer> indici = new ArrayList<>();
        indici.add(0);
        new DeleteTask<>(mParentActivity, mAdapter, elencoCitta, indici).execute(DeleteTask.CANCELLA_CITTA);
    }

    /**
     * Cancella le citta selezionate dall'utente
     */
    private void cancellaElencoCitta() {
        new DeleteTask<>(mParentActivity, mAdapter, mElencoCitta, mAdapter.getSelectedItems()).execute(DeleteTask.CANCELLA_CITTA);
    }

    private void openDialogAddCitta(View view) {
        FragmentManager fragmentManager = getFragmentManager();
        AddCittaDialog dialog = AddCittaDialog.newInstance();
        dialog.setCallback(this);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(android.R.id.content, dialog).addToBackStack(null).commit();
    }

    @Override
    public Loader<List<Citta>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case CITTA_LOADER:
                return new CittaLoader(mParentActivity, mIdViaggio);
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
    public void onLoaderReset(Loader<List<Citta>> loader) { }
}