package com.stefano.andrea.utils;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

/**
 * CursorRecyclerViewAdapter
 */
public abstract class CursorRecyclerViewAdapter<VH extends android.support.v7.widget.RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private Cursor mCursor;

    public CursorRecyclerViewAdapter (Cursor cursor) {
        mCursor = cursor;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        mCursor.moveToPosition(position);
        onBindViewHolderCursor(holder, mCursor);
    }

    public abstract void onBindViewHolderCursor(VH holder, Cursor cursor);

    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    public void swapCursor (Cursor cursor) {
        if (mCursor != cursor) {
            if (mCursor != null)
                mCursor.close();
            mCursor = cursor;
            notifyDataSetChanged();
        }
    }


}
