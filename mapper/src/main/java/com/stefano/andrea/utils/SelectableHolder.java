package com.stefano.andrea.utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.stefano.andrea.activities.R;

/**
 * SelectableHolder
 */
public abstract class SelectableHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public View mSelectedOverlay;

    public SelectableHolder(View itemView) {
        super(itemView);
        mSelectedOverlay = itemView.findViewById(R.id.selected_overlay);
        itemView.setOnClickListener(this);
        itemView.setLongClickable(true);
        itemView.setOnLongClickListener(this);
    }
}