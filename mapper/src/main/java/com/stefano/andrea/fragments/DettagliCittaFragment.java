package com.stefano.andrea.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import com.stefano.andrea.activities.DettagliPostoActivity;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.adapters.PostiAdapter;
import com.stefano.andrea.dialogs.AddPostoDialog;
import com.stefano.andrea.dialogs.DialogHelper;
import com.stefano.andrea.loaders.PostiLoader;
import com.stefano.andrea.models.Posto;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.tasks.DeleteTask;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.utils.CustomFAB;
import com.stefano.andrea.utils.MapperContext;
import com.stefano.andrea.utils.SparseBooleanArrayParcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * DettagliCittaFragment
 */
public class DettagliCittaFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Posto>>, PostiAdapter.PostoOnClickListener, AddPostoDialog.AggiungiPostoCallback {

    private static final int DETTAGLI_CITTA_CALLBACK = 1010;
    private static final String BUNDLE_ACTION_MODE = "com.stefano.andrea.fragments.DettagliCittaFragment.actionMode";

    private static final String ID_CITTA = "com.stefano.andrea.fragments.DettagliCittaFragment.idCitta";
    private static final int POSTI_LOADER = 2;

    private long mIdCitta;
    private Activity mParentActivity;
    private ContentResolver mResolver;
    private PostiAdapter mAdapter;
    private CustomFAB mFab;
    private List<Posto> mElencoPosti;
    private MapperContext mContext;
    private View mView;

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

    public DettagliCittaFragment () { }

    public static DettagliCittaFragment newInstance(long idCitta) {
        DettagliCittaFragment fragment = new DettagliCittaFragment();
        Bundle args = new Bundle();
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
            mIdCitta = getArguments().getLong(ID_CITTA);
        }
        //acquisisco il content resolver
        mResolver = mParentActivity.getContentResolver();
        //avvio il loader dei posto
        getLoaderManager().initLoader(POSTI_LOADER, null, this);
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
        mView =  inflater.inflate(R.layout.fragment_dettagli_citta, container, false);
        //acquisisco riferimenti
        final RecyclerView recyclerView = (RecyclerView) mView.findViewById(R.id.recyclerview_scroll);
        mFab = (CustomFAB) mView.findViewById(R.id.fab_aggiunta_posto);
        final TextView nessunPostoInfo = (TextView) mView.findViewById(R.id.no_posti);
        //configuro recyclerview
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mParentActivity));
        mAdapter = new PostiAdapter(this, mParentActivity, mCallback);
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
                    openDialogAddPosto(v);
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
                    nessunPostoInfo.setVisibility(View.VISIBLE);
                else
                    nessunPostoInfo.setVisibility(View.GONE);
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
     * Avvio l'activity con l'elenco delle foto relative al posto selezionato
     * @param posto Il posto desiderato
     */
    @Override
    public void selezionatoPosto(Posto posto) {
        mContext.setIdPosto(posto.getId());
        mContext.setNomePosto(posto.getNome());
        Intent intent = new Intent(mParentActivity, DettagliPostoActivity.class);
        startActivity(intent);
    }

    /**
     * Aggiunge un nuovo posto all'interno di una citta'
     * @param nomePosto Il nome del posto
     * @param coordinates Le coordinate del posto
     */
    @Override
    public void creaNuovoPosto(String nomePosto, LatLng coordinates) {
        Posto posto = new Posto();
        posto.setNome(nomePosto);
        posto.setIdCitta(mIdCitta);
        posto.setLatitudine(coordinates.latitude);
        posto.setLongitudine(coordinates.longitude);
        new InsertTask<>(mParentActivity, mAdapter, posto, mListener).execute(InsertTask.INSERISCI_POSTO);
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
        new DeleteTask<>(mParentActivity, mAdapter, elencoPosti, indici, mListener).execute(DeleteTask.CANCELLA_POSTO);
    }

    /**
     * Aggiorna il parametro visitato di un posto
     * @param posto Il posto da modificare
     */
    @Override
    public void visitatoPosto(Posto posto) {
        Uri uri = ContentUris.withAppendedId(MapperContract.Posto.CONTENT_URI, posto.getId());
        ContentValues values = new ContentValues();
        values.put(MapperContract.Posto.VISITATO, posto.getVisitato());
        mResolver.update(uri, values, null, null);
    }

    /**
     * Cancella i posti selezionati dall'utente
     */
    private void cancellaElencoPosti() {
        new DeleteTask<>(mParentActivity, mAdapter, mElencoPosti, mAdapter.getSelectedItems(), mListener).execute(DeleteTask.CANCELLA_POSTO);
    }

    private void openDialogAddPosto(View view) {
        LinearLayout sugg = (LinearLayout) mView.findViewById(R.id.suggerimento_crea_posto);
        if (sugg.getVisibility()==View.VISIBLE){slideToBottom(sugg);}
        FragmentManager fragmentManager = getFragmentManager();
        AddPostoDialog dialog = AddPostoDialog.newInstance();
        dialog.setTargetFragment(this, DETTAGLI_CITTA_CALLBACK);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(android.R.id.content, dialog).addToBackStack(null).commit();
    }

    @Override
    public Loader<List<Posto>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case POSTI_LOADER:
                return new PostiLoader(mParentActivity, mIdCitta);
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
        if (mAdapter.getItemCount() == 0){
            final LinearLayout sugg = (LinearLayout) mView.findViewById(R.id.suggerimento_crea_posto);
            animateSugg(sugg);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Posto>> loader) { }

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
