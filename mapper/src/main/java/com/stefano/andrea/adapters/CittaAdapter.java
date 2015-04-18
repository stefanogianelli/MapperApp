package com.stefano.andrea.adapters;

import android.database.Cursor;
import android.view.ViewGroup;

import com.stefano.andrea.utils.CursorRecyclerAdapter;

/**
 * CittaAdapter
 */
public class CittaAdapter extends CursorRecyclerAdapter<CittaHolder> {

    private CittaHolder.CittaHolderListener mListener;

    public CittaAdapter(Cursor cursor, CittaHolder.CittaHolderListener listener) {
        super(cursor);
        mListener = listener;
    }

    @Override
    public void onBindViewHolderCursor(CittaHolder holder, Cursor cursor) {
        //TODO: completare
    }

    @Override
    public CittaHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //TODO: completare
        return null;
    }
}
