package com.stefano.andrea.adapters;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.utils.CursorRecyclerViewAdapter;

import java.util.List;

/**
 * ViaggiAdapter
 */
public class ViaggiAdapter extends CursorRecyclerViewAdapter<ViaggiAdapter.ViaggiHolder> {

    private ContentResolver mResolver;
    private ViaggioOnClickListener mListener;

    public interface ViaggioOnClickListener {
        void selezionatoViaggio (long id);
    }

    public ViaggiAdapter(Cursor cursor, ContentResolver resolver, ViaggioOnClickListener listener) {
        super(cursor);
        mResolver = resolver;
        mListener = listener;
    }

    @Override
    public void onBindViewHolderCursor(ViaggiHolder holder, Cursor cursor) {
        String nome = cursor.getString(cursor.getColumnIndex(MapperContract.Viaggio.NOME));
        long id = cursor.getLong(cursor.getColumnIndex(MapperContract.Viaggio.ID_VIAGGIO));;
        holder.vNome.setText(nome);
        holder.itemView.setTag(id);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.selezionatoViaggio((Long) v.getTag());
            }
        });
    }

    @Override
    public ViaggiHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viaggio_item, parent, false);
        return new ViaggiHolder(view);
    }

    public Uri creaNuovoViaggio (String nome) {
        ContentValues values = new ContentValues();
        values.put(MapperContract.Viaggio.NOME, nome);
        return mResolver.insert(MapperContract.Viaggio.CONTENT_URI, values);
    }

    public int cancellaViaggio(long id) {
        Uri uri = ContentUris.withAppendedId(MapperContract.Viaggio.CONTENT_URI, id);
        return mResolver.delete(uri, null, null);
    }

    public int cancellaViaggi (List<Integer> ids) {
        int count = 0;
        for(int id : ids) {
            count += cancellaViaggio(id);
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
