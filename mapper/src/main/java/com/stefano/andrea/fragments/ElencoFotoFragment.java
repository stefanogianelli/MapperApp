package com.stefano.andrea.fragments;

import android.app.Activity;
import android.content.ContentResolver;
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

import java.util.List;

/**
 * ElencoFotoFragment
 */
public class ElencoFotoFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Foto>> {

    public static final String EXTRA_ID = "id_elenco_foto";
    public static final String EXTRA_TIPO_ELENCO = "tipo_elenco";

    private static final int FOTO_LOADER = 0;

    private ObservableRecyclerView mRecyclerView;
    private int mTipoElenco;
    private long mId;
    private Activity mParentActivity;
    private ContentResolver mResolver;
    private FotoAdapter mAdapter;
    private FotoAdapter.FotoOnClickListener mListener;

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
        try {
            mListener = (FotoAdapter.FotoOnClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " deve implementare FotoOnClickListener");
        }
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
        mAdapter = new FotoAdapter(mParentActivity, new ActionModeCallback(), mListener);
        getLoaderManager().initLoader(FOTO_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_elenco_foto,container,false);
        //acquisisco riferimenti
        mRecyclerView = (ObservableRecyclerView) v.findViewById(R.id.gridViewFotoViaggio);
        //configuro recyclerview
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        return v;
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
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Foto>> loader) {
        //do nothing
    }

    private class ActionModeCallback implements ActionMode.Callback {

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
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_cancella_foto:
                    //TODO: cancellazione foto
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.stopActionMode();
        }
    }

}