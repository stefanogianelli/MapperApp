package com.stefano.andrea.utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * SelectableHolder
 */
public abstract class SelectableHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public SelectableHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        itemView.setLongClickable(true);
        itemView.setOnLongClickListener(this);
    }
}