package com.stefano.andrea.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

import com.google.android.gms.maps.model.LatLng;
import com.stefano.andrea.activities.DettagliPostoActivity;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.adapters.PostiAdapter;
import com.stefano.andrea.dialogs.AddPostoDialog;
import com.stefano.andrea.loaders.PostiLoader;
import com.stefano.andrea.models.Posto;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.tasks.DeleteTask;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.utils.CustomFAB;
import com.stefano.andrea.utils.DialogHelper;
import com.stefano.andrea.utils.MapperContext;

import java.util.ArrayList;
import java.util.List;

/**
 * DettagliCittaFragment
 */
public class DettagliCittaFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Posto>>, PostiAdapter.PostoOnClickListener, AddPostoDialog.AggiungiPostoCallback {

    private static final String ID_VIAGGIO = "com.stefano.andrea.fragments.DettagliCittaFragment.idViaggio";
    private static final String ID_CITTA = "com.stefano.andrea.fragments.DettagliCittaFragment.idCitta";
    private static final int POSTI_LOADER = 2;

    private long mIdViaggio;
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
        mContext = MapperContext.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView =  inflater.inflate(R.layout.fragment_dettagli_citta, container, false);
        //acquisisco riferimenti
        RecyclerView mRecyclerView = (RecyclerView) mView.findViewById(R.id.recyclerview_scroll);
        mFab = (CustomFAB) mView.findViewById(R.id.fab_aggiunta_posto);
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
                    openDialogAddPosto(v);
                } else {
                    DialogHelper.showAlertDialog(mParentActivity, R.string.no_internet_title_dialog, R.string.no_internet_message_dialog);
                }
            }
        });
        return mView;
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
        new InsertTask<>(mParentActivity, mAdapter, posto).execute(InsertTask.INSERISCI_POSTO);
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
        new DeleteTask<>(mParentActivity, mAdapter, elencoPosti, indici).execute(DeleteTask.CANCELLA_POSTO);
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
        new DeleteTask<>(mParentActivity, mAdapter, mElencoPosti, mAdapter.getSelectedItems()).execute(DeleteTask.CANCELLA_POSTO);
    }

    private void openDialogAddPosto(View view) {
        FragmentManager fragmentManager = getFragmentManager();
        AddPostoDialog dialog = AddPostoDialog.newInstance();
        dialog.setCallback(this);
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
                .setDuration(900)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        slideToBottom(view);
                    }
                });

    }

}
