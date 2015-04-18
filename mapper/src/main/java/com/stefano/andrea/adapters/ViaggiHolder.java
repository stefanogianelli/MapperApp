package com.stefano.andrea.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.stefano.andrea.activities.R;

/**
 * ViaggiHolder
 */
public class ViaggiHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private long id;
    private TextView vNome;
    private ViaggiHolderListener mListener;

    public interface ViaggiHolderListener {
        void selectViaggio (long id);
        int deleteViaggio (long id);
    }

    public ViaggiHolder(View v, ViaggiHolderListener listener) {
        super(v);
        v.setOnClickListener(this);
        vNome = (TextView) v.findViewById(R.id.viaggio_item_label);
        mListener = listener;
    }

    public void setId (long id) {
        this.id = id;
    }

    public void setNomeViaggio (String nome) {
        vNome.setText(nome);
    }

    @Override
    public void onClick(View v) {
        mListener.selectViaggio(id);
    }
}
