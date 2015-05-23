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
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.EventListener;
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
import com.stefano.andrea.utils.SparseBooleanArrayParcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * DettagliViaggioFragment
 */
public class DettagliViaggioFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Citta>>, CittaAdapter.CittaOnClickListener, AddCittaDialog.AggiungiCittaCallback {

    private static final int DETTAGLI_VIAGGIO_CALLBACK = 1000;
    private static final String BUNDLE_ACTION_MODE = "com.stefano.andrea.fragments.DettagliViaggioFragment.actionMode";
    private static final int CITTA_LOADER = 1;
    private static final String ID_VIAGGIO = "com.stefano.andrea.fragments.DettagliViaggioFragment.idViaggio";

    private CittaAdapter mAdapter;
    private long mIdViaggio;
    private List<Citta> mElencoCitta;
    private CustomFAB mFab;
    private Activity mParentActivity;
    private MapperContext mContext;
    private View mView;

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
        //verifico ripristino della action mode
        if (savedInstanceState != null) {
            SparseBooleanArrayParcelable mItemsSelected = savedInstanceState.getParcelable(BUNDLE_ACTION_MODE);
            if (mItemsSelected != null) {
                mAdapter.restoreActionMode(mItemsSelected);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_dettagli_viaggio, container, false);
        //acquisisco riferimenti
        mFab = (CustomFAB) mView.findViewById(R.id.fab_aggiunta_citta);
        final RecyclerView recyclerView = (RecyclerView) mView.findViewById(R.id.recyclerview_scroll);
        final TextView nessunaCittaInfo = (TextView) mView.findViewById(R.id.no_citta);
        //configuro recyclerview
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mParentActivity));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //configuro fab
        mFab.attachToRecyclerView(recyclerView);
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
                recyclerView.smoothScrollToPosition(0);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkVisibility();
            }

            private void checkVisibility () {
                if (mAdapter.getItemCount() == 0)
                    nessunaCittaInfo.setVisibility(View.VISIBLE);
                else
                    nessunaCittaInfo.setVisibility(View.GONE);
            }
        });
        return mView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAdapter != null && mAdapter.isEnabledSelectionMode()) {
            outState.putParcelable(BUNDLE_ACTION_MODE, mAdapter.saveActionmode());
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible()) {
            if (!isVisibleToUser) {
                mAdapter.finishActionMode();
            }
        }
    }

    /**
     * Aggiunge una nuova citta al viaggio
     * @param nomeCitta Il nome della citta
     * @param nazione La nazione della citta'
     * @param coordinates Le coordinate della citta'
     */
    @Override
    public void creaNuovaCitta(String nomeCitta, String nazione, LatLng coordinates) {
        Citta citta = new Citta();
        citta.setIdViaggio(mIdViaggio);
        citta.setNome(nomeCitta);
        citta.setNazione(nazione);
        citta.setLatitudine(coordinates.latitude);
        citta.setLongitudine(coordinates.longitude);
        new InsertTask<>(mParentActivity, mAdapter, citta, mListener).execute(InsertTask.INSERISCI_CITTA);
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
        new DeleteTask<>(mParentActivity, mAdapter, elencoCitta, indici, mListener).execute(DeleteTask.CANCELLA_CITTA);
    }

    /**
     * Cancella le citta selezionate dall'utente
     */
    private void cancellaElencoCitta() {
        new DeleteTask<>(mParentActivity, mAdapter, mElencoCitta, mAdapter.getSelectedItems(), mListener).execute(DeleteTask.CANCELLA_CITTA);
    }

    private void openDialogAddCitta(View view) {
        LinearLayout sugg = (LinearLayout) mView.findViewById(R.id.suggerimento_crea_citta);
        if (sugg.getVisibility()==View.VISIBLE){slideToBottom(sugg);}
        FragmentManager fragmentManager = getFragmentManager();
        AddCittaDialog dialog = AddCittaDialog.newInstance();
        dialog.setTargetFragment(this, DETTAGLI_VIAGGIO_CALLBACK);
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
        if (mAdapter.getItemCount() == 0){
            final LinearLayout sugg = (LinearLayout) mView.findViewById(R.id.suggerimento_crea_citta);
            animateSugg(sugg);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Citta>> loader) { }

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

}