package com.stefano.andrea.adapters;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.providers.MapperContract;

import java.util.List;

/**
 * ViaggiAdapter
 */
public class ViaggiAdapter extends RecyclerView.Adapter <ViaggiAdapter.ViaggiHolder> {

    private List<Viaggio> mListaViaggi = null;
    private Activity mActivity;
    private ContentResolver mResolver;
    private ViaggioOnClickListener mListener;
    private MultiSelector mMultiSelector;
    private ModalMultiSelectorCallback mDeleteMode;

    public interface ViaggioOnClickListener {
        void selezionatoViaggio (Viaggio viaggio);
    }

    public ViaggiAdapter(final Activity activity, Cursor cursor, ContentResolver resolver, ViaggioOnClickListener listener) {
        mActivity = activity;
        mResolver = resolver;
        mListener = listener;
        mMultiSelector = new MultiSelector();
        mDeleteMode = new MultiSelection(mMultiSelector);
    }

    public void setListaViaggi (List<Viaggio> lista) {
        if (mListaViaggi == null) {
            mListaViaggi = lista;
            notifyDataSetChanged();
        }
    }

    @Override
    public ViaggiHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viaggio_item, parent, false);
        return new ViaggiHolder(view);
    }

    @Override
    public void onBindViewHolder(ViaggiHolder holder, int position) {
        Viaggio viaggio = mListaViaggi.get(position);
        holder.bindViaggio(viaggio);
    }

    @Override
    public int getItemCount() {
        if (mListaViaggi != null)
            return mListaViaggi.size();
        else
            return 0;
    }

    public Uri creaNuovoViaggio (String nome) {
        ContentValues values = new ContentValues();
        values.put(MapperContract.Viaggio.NOME, nome);
        Uri uri = mResolver.insert(MapperContract.Viaggio.CONTENT_URI, values);
        Viaggio viaggio = new Viaggio(Long.parseLong(uri.getLastPathSegment()), nome);
        mListaViaggi.add(0, viaggio);
        notifyItemInserted(0);
        return uri;
    }

    public int cancellaViaggio (Viaggio viaggio) {
        Uri uri = ContentUris.withAppendedId(MapperContract.Viaggio.CONTENT_URI, viaggio.getId());
        int count = mResolver.delete(uri, null, null);
        int pos = mListaViaggi.indexOf(viaggio);
        mListaViaggi.remove(pos);
        notifyItemRemoved(pos);
        return count;
    }

    public int cancellaViaggi () {
        int count = 0;
        for (int i = mListaViaggi.size(); i >= 0; i--) {
            if (mMultiSelector.isSelected(i, 0)) {
                count += cancellaViaggio(mListaViaggi.get(i));
            }
        }
        return count;
    }

    public class ViaggiHolder extends SwappingHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView vNome;

        public ViaggiHolder(View v) {
            super(v, mMultiSelector);
            vNome = (TextView) v.findViewById(R.id.viaggio_item_label);
            v.setOnClickListener(this);
            v.setLongClickable(true);
            v.setOnLongClickListener(this);
        }

        public void bindViaggio (Viaggio viaggio) {
            this.itemView.setTag(viaggio);
            vNome.setText(viaggio.getNome());
        }

        @Override
        public void onClick(View v) {
            if (mMultiSelector.tapSelection(this)) {
                mMultiSelector.setSelected(this, true);
            } else {
                mListener.selezionatoViaggio((Viaggio) v.getTag());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            ((ActionBarActivity) mActivity).startSupportActionMode(mDeleteMode);
            mMultiSelector.setSelected(this, true);
            return true;
        }
    }

    private class MultiSelection extends ModalMultiSelectorCallback {

        public MultiSelection(MultiSelector multiSelector) {
            super(multiSelector);
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            mActivity.getMenuInflater().inflate(R.menu.viaggi_list_on_long_click, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId()==  R.id.menu_cancella_viaggio){
                actionMode.finish();
                cancellaViaggi();
                mMultiSelector.clearSelections();
                return true;
            }
            return false;
        }
    }
}
