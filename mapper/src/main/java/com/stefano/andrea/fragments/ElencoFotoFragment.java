package com.stefano.andrea.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stefano.andrea.activities.ModInfoFotoActivity;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.adapters.FotoAdapter;
import com.stefano.andrea.intents.MapperIntent;
import com.stefano.andrea.loaders.FotoLoader;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.tasks.DeleteTask;
import com.stefano.andrea.utils.SparseBooleanArrayParcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * ElencoFotoFragment
 */
public class ElencoFotoFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Foto>>, FotoAdapter.FotoOnClickListener {

    private static final String TAG = "ElencoFotoFragments";
    private static final String BUNDLE_ACTION_MODE = "com.stefano.andrea.fragments.DettagliFotoFragment.actionMode";
    private static final int FOTO_LOADER = 3;

    private static final String EXTRA_ID = "com.stefano.andrea.fragments.ElencoFotoFragment.id";
    private static final String EXTRA_TIPO_ELENCO = "com.stefano.andrea.fragments.ElencoFotoFragment.tipoElenco";

    private static final int COLUMNS_PORTRAIT = 3;
    private static final int COLUMNS_LANDSCAPE = 5;

    private int mTipoElenco;
    private long mId;
    private Activity mParentActivity;
    private FotoAdapter mAdapter;
    private ArrayList<Foto> mElencoFoto;
    private RecyclerView mRecyclerView;
    private LinearLayout mProgressBar;

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
                case R.id.menu_modifica_foto:
                    Intent intent = new Intent(mParentActivity, ModInfoFotoActivity.class);
                    ArrayList<Integer> fotoId = new ArrayList<>();
                    ArrayList<Uri> fotoUris = new ArrayList<>();
                    for (int i = 0; i < mElencoFoto.size(); i++) {
                        if (mAdapter.getSelectedItems().contains(i)) {
                            fotoId.add((int) mElencoFoto.get(i).getId());
                            fotoUris.add(Uri.parse(mElencoFoto.get(i).getPath()));
                        }
                    }
                    intent.putIntegerArrayListExtra(ModInfoFotoActivity.EXTRA_LISTA_FOTO, fotoId);
                    intent.putParcelableArrayListExtra(ModInfoFotoActivity.EXTRA_FOTO, fotoUris);
                    intent.setAction(MapperIntent.MAPPER_MODIFICA_FOTO);
                    switch (mTipoElenco) {
                        case FotoLoader.FOTO_POSTO:
                            intent.putExtra(ModInfoFotoActivity.EXTRA_ID_POSTO, mElencoFoto.get(0).getIdPosto());
                        case FotoLoader.FOTO_CITTA:
                            intent.putExtra(ModInfoFotoActivity.EXTRA_ID_CITTA, mElencoFoto.get(0).getIdCitta());
                        case FotoLoader.FOTO_VIAGGIO:
                            intent.putExtra(ModInfoFotoActivity.EXTRA_ID_VIAGGIO, mElencoFoto.get(0).getIdViaggio());
                    }
                    startActivity(intent);
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
        mAdapter = new FotoAdapter(mParentActivity, mCallback, this);
        getLoaderManager().initLoader(FOTO_LOADER, null, this);
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
        View v = inflater.inflate(R.layout.fragment_elenco_foto,container,false);
        //acquisisco riferimenti
        mRecyclerView = (RecyclerView) v.findViewById(R.id.gridViewFotoViaggio);
        mProgressBar = (LinearLayout) v.findViewById(R.id.loading_layout);
        final TextView nessunaFotoInfo = (TextView) v.findViewById(R.id.no_foto);
        //configuro recyclerview
        mRecyclerView.setHasFixedSize(true);
        int columns;
        if (mParentActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            columns = COLUMNS_PORTRAIT;
        else
            columns = COLUMNS_LANDSCAPE;
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columns));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
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
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkVisibility();
            }

            private void checkVisibility () {
                if (mAdapter.getItemCount() == 0)
                    nessunaFotoInfo.setVisibility(View.VISIBLE);
                else
                    nessunaFotoInfo.setVisibility(View.GONE);
            }
        });
        return v;
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int columns;
        if (mParentActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            columns = COLUMNS_PORTRAIT;
        else
            columns = COLUMNS_LANDSCAPE;
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columns));
    }

    @Override
    public void selezionataFoto(int posizione) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FullFotoFragment fragment = FullFotoFragment.newInstance(mElencoFoto, posizione);
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.addToBackStack(TAG);
        fragmentTransaction.commit();
    }

    /**
     * Cancella le foto selezionate dall'utente
     */
    private void cancellaElencoFoto () {
        new DeleteTask<>(mParentActivity, mAdapter, mElencoFoto, mAdapter.getSelectedItems()).execute(DeleteTask.CANCELLA_FOTO);
    }

    @Override
    public Loader<List<Foto>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case FOTO_LOADER:
                return new FotoLoader(mParentActivity, mId, mTipoElenco);
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
                mElencoFoto = (ArrayList<Foto>) data;
                mProgressBar.setVisibility(View.GONE);
                LinearLayout sugg = (LinearLayout) mParentActivity.findViewById(R.id.suggerimento_crea_foto);
                if(sugg!=null){
                    if(mAdapter.getItemCount()==0) {
                        animateSugg(sugg);
                    }else{
                        sugg.setVisibility(View.GONE);
                    }
                }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Foto>> loader) { }

    public void animateSugg(final View view){
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0.0f);
        view.animate()
                .translationY(view.getHeight())
                .alpha(1.0f)
                .setDuration(900);

    }

}