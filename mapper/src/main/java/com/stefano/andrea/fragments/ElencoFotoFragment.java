package com.stefano.andrea.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.adapters.FotoAdapter;
import com.stefano.andrea.loaders.FotoLoader;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.tasks.DeleteTask;

import java.util.List;

/**
 * ElencoFotoFragment
 */
public class ElencoFotoFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Foto>>, FotoAdapter.FotoOnClickListener {

    private static final String EXTRA_ID = "id_elenco_foto";
    private static final String EXTRA_TIPO_ELENCO = "tipo_elenco";

    private static final int FOTO_LOADER = 0;

    private int mTipoElenco;
    private long mId;
    private Activity mParentActivity;
    private ContentResolver mResolver;
    private FotoAdapter mAdapter;
    private List<Foto> mElencoFoto;

    private ActionMode.Callback mCallback = new ActionMode.Callback () {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate (R.menu.menu_foto_selezionate, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_cancella_foto:
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mParentActivity);
                    dialog.setMessage(R.string.conferma_cancellazione_foto);
                    dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancellaElencoFoto();
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
        }
    };

    public ElencoFotoFragment () { }

    public static ElencoFotoFragment newInstance (long id, int tipoElenco) {
        ElencoFotoFragment fragment = new ElencoFotoFragment();
        Bundle args = new Bundle();
        args.putLong(EXTRA_ID, id);
        args.putInt(EXTRA_TIPO_ELENCO, tipoElenco);
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
            mTipoElenco = getArguments().getInt(EXTRA_TIPO_ELENCO);
            mId = getArguments().getLong(EXTRA_ID);
        }
        mResolver = mParentActivity.getContentResolver();
        mAdapter = new FotoAdapter(mParentActivity, mCallback, this);
        getLoaderManager().initLoader(FOTO_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_elenco_foto,container,false);
        //acquisisco riferimenti
        ObservableRecyclerView mRecyclerView = (ObservableRecyclerView) v.findViewById(R.id.gridViewFotoViaggio);
        //configuro recyclerview
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void selezionataFoto(Foto foto) {
        //TODO: completare
    }

    /**
     * Cancella le foto selezionate dall'utente
     */
    private void cancellaElencoFoto () {
        new DeleteTask<>(mParentActivity, mResolver, mAdapter, mElencoFoto, mAdapter.getSelectedItems()).execute(DeleteTask.CANCELLA_FOTO);
    }

    @Override
    public Loader<List<Foto>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case FOTO_LOADER:
                return new FotoLoader(mParentActivity, mResolver, mId, mTipoElenco);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Foto>> loader, List<Foto> data) {
        int id = loader.getId();
        switch (id) {
            case FOTO_LOADER:
                mAdapter.setElencoFoto(data);
                mElencoFoto = data;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Foto>> loader) {
        //do nothing
    }

}