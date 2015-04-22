package com.stefano.andrea.adapters;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.utils.SelectableAdapter;

import java.util.List;

/**
 * ViaggiAdapter
 */
public class ViaggiAdapter extends SelectableAdapter<ViaggiAdapter.ViaggiHolder> {

    private List<Viaggio> mListaViaggi = null;
    private ContentResolver mResolver;
    private ViaggioOnClickListener mListener;

    public interface ViaggioOnClickListener {
        void OnClickItem (int position);
        boolean OnLongClickItem (int position);
        void selezionatoViaggio (Viaggio viaggio);
    }

    public ViaggiAdapter(ContentResolver resolver, ViaggioOnClickListener listener) {
        super();
        mResolver = resolver;
        mListener = listener;
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
        holder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
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
        /*int count = 0;
        for (int i = mListaViaggi.size(); i >= 0; i--) {
            if (mMultiSelector.isSelected(i, 0)) {
                count += cancellaViaggio(mListaViaggi.get(i));
            }
        }
        return count;*/
        return 0;
    }

    public class ViaggiHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView vNome;
        private View selectedOverlay;

        public ViaggiHolder(View itemView) {
            super(itemView);
            vNome = (TextView) itemView.findViewById(R.id.viaggio_item_label);
            selectedOverlay = itemView.findViewById(R.id.selected_overlay);
            itemView.setOnClickListener(this);
            itemView.setLongClickable(true);
            itemView.setOnLongClickListener(this);
        }

        public void bindViaggio (Viaggio viaggio) {
            this.itemView.setTag(viaggio);
            vNome.setText(viaggio.getNome());
        }

        @Override
        public void onClick(View v) {
            mListener.OnClickItem(getPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return mListener.OnLongClickItem(getPosition());
        }
    }
}
