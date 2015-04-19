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

import java.util.List;

/**
 * ViaggiAdapter
 */
public class ViaggiAdapter extends RecyclerView.Adapter<ViaggiAdapter.ViaggiHolder> {

    private List<Viaggio> mListaViaggi;
    private ContentResolver mResolver;
    private ViaggioOnClickListener mListener;

    public interface ViaggioOnClickListener {
        void selezionatoViaggio (long id);
    }

    public ViaggiAdapter (List<Viaggio> listaViaggi, ContentResolver resolver, ViaggioOnClickListener listener) {
        mListaViaggi = listaViaggi;
        mResolver = resolver;
        mListener = listener;
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
        holder.vNome.setText(viaggio.getNome());
        holder.itemView.setTag(viaggio.getId());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.selezionatoViaggio((Long) v.getTag());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListaViaggi.size();
    }

    public Uri creaNuovoViaggio (String nome) {
        ContentValues values = new ContentValues();
        values.put(MapperContract.Viaggio.NOME, nome);
        return mResolver.insert(MapperContract.Viaggio.CONTENT_URI, values);
    }

    public int deleteViaggio(long id) {
        Uri uri = ContentUris.withAppendedId(MapperContract.Viaggio.CONTENT_URI, id);
        return mResolver.delete(uri, null, null);
    }

    public int deleteViaggi (List<Integer> ids) {
        int count = 0;
        for(int id : ids) {
            count += deleteViaggio(id);
        }
        return count;
    }

    public class ViaggiHolder extends RecyclerView.ViewHolder {

        public TextView vNome;

        public ViaggiHolder(View v) {
            super(v);
            vNome = (TextView) v.findViewById(R.id.viaggio_item_label);
        }
    }
}
