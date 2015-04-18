package com.stefano.andrea.adapters;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.utils.CursorRecyclerAdapter;

/**
 * ViaggiAdapter
 */
public class ViaggiAdapter extends CursorRecyclerAdapter<ViaggiHolder> {

    private ViaggiHolder.ViaggiHolderListener mListener;

    public ViaggiAdapter(Cursor cursor, ViaggiHolder.ViaggiHolderListener listener) {
        super(cursor);
        mListener = listener;
    }

    @Override
    public void onBindViewHolderCursor(ViaggiHolder holder, Cursor cursor) {
        holder.setNomeViaggio(cursor.getString(cursor.getColumnIndex(MapperContract.Viaggio.NOME)));
        holder.setId(cursor.getLong(cursor.getColumnIndex(MapperContract.Viaggio._ID)));
    }

    @Override
    public ViaggiHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view =  LayoutInflater.from(viewGroup.getContext())
        .inflate(R.layout.viaggio_item, viewGroup, false);
        return new ViaggiHolder(view, mListener);
    }

}
