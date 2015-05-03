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
import com.stefano.andrea.activities.R;
import com.stefano.andrea.adapters.PostiAdapter;
import com.stefano.andrea.loaders.PostiLoader;
import com.stefano.andrea.models.Posto;
import com.stefano.andrea.tasks.DeleteTask;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.utils.CustomFAB;
import com.stefano.andrea.utils.DialogHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * DettagliCittaFragment
 */
public class DettagliCittaFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Posto>>, PostiAdapter.PostoOnClickListener {

    private static final String ID_VIAGGIO = "id_viaggio";
    private static final String ID_CITTA = "id_citta";
    private static final int POSTI_LOADER = 0;

    private long mIdViaggio;
    private long mIdCitta;
    private Activity mParentActivity;
    private ContentResolver mResolver;
    private PostiAdapter mAdapter;
    private ObservableRecyclerView mRecyclerView;
    private CustomFAB mFab;
    private List<Posto> mElencoPosti;

    private ActionMode.Callback mCallback = new ActionMode.Callback () {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mFab.hide();
            mFab.setForceHide(true);
            mode.getMenuInflater().inflate (R.menu.menu_posti_selezionati, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_cancella_posto:
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mParentActivity);
                    dialog.setMessage(R.string.conferma_cancellazione_posti);
                    dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancellaElencoPosti();
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

    public DettagliCittaFragment () { }

    public static DettagliCittaFragment newInstance(long idViaggio, long idCitta) {
        DettagliCittaFragment fragment = new DettagliCittaFragment();
        Bundle args = new Bundle();
        args.putLong(ID_VIAGGIO, idViaggio);
        args.putLong(ID_CITTA, idCitta);
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
        if (getArguments() != null) {
            mIdViaggio = getArguments().getLong(ID_VIAGGIO);
            mIdCitta = getArguments().getLong(ID_CITTA);
        }
        //acquisisco il content resolver
        mResolver = mParentActivity.getContentResolver();
        //creo l'adapter
        mAdapter = new PostiAdapter(this, mParentActivity, mCallback);
        //avvio il loader dei posto
        getLoaderManager().initLoader(POSTI_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_dettagli_citta, container, false);
        //acquisisco riferimenti
        mRecyclerView = (ObservableRecyclerView) view.findViewById(R.id.recyclerview_scroll);
        mFab = (CustomFAB) view.findViewById(R.id.fab_aggiunta_posto);
        //configuro recyclerview
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mParentActivity));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        if (mParentActivity instanceof ObservableScrollViewCallbacks) {
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
                    openDialogAddPosto(v);
                } else {
                    DialogHelper.showAlertDialog(mParentActivity, R.string.no_internet_title_dialog, R.string.no_internet_message_dialog);
                }
            }
        });
        return view;
    }

    /**
     * Avvio l'activity con l'elenco delle foto relative al posto selezionato
     * @param posto Il posto desiderato
     */
    @Override
    public void selezionatoPosto(Posto posto) {
        //TODO: completare
    }

    /**
     * Aggiunge un nuovo posto all'interno di una citta'
     * @param nomePosto Il nome del posto
     */
    private void creaNuovoPosto(String nomePosto) {
        Posto posto = new Posto();
        posto.setNome(nomePosto);
        posto.setIdCitta(mIdCitta);
        new InsertTask<>(mParentActivity, mResolver, mAdapter, posto).execute(InsertTask.INSERISCI_POSTO);
    }

    /**
     * Cancella un posto
     * @param posto Il posto da eliminare
     */
    @Override
    public void cancellaPosto(Posto posto) {
        List<Posto> elencoPosti = new ArrayList<>();
        elencoPosti.add(posto);
        List<Integer> indici = new ArrayList<>();
        indici.add(0);
        new DeleteTask<>(mParentActivity, mResolver, mAdapter, elencoPosti, indici).execute(DeleteTask.CANCELLA_POSTO);
    }

    /**
     * Cancella i posti selezionati dall'utente
     */
    private void cancellaElencoPosti() {
        new DeleteTask<>(mParentActivity, mResolver, mAdapter, mElencoPosti, mAdapter.getSelectedItems()).execute(DeleteTask.CANCELLA_POSTO);
    }

    private void openDialogAddPosto(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivity);
        LayoutInflater inflater = mParentActivity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.fragment_add_posto, null))
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog d = (Dialog) dialog;
                        EditText nomePosto = (EditText) d.findViewById(R.id.text_add_posto);
                        creaNuovoPosto(nomePosto.getText().toString());
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
    public Loader<List<Posto>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case POSTI_LOADER:
                return new PostiLoader(mParentActivity, mResolver, mIdCitta);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Posto>> loader, List<Posto> data) {
        int id = loader.getId();
        switch (id) {
            case POSTI_LOADER:
                mAdapter.setElencoPosti(data);
                mElencoPosti = data;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Posto>> loader) {
        //do nothing
    }

}
