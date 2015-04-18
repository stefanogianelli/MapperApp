package com.stefano.andrea.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * CittaHolder
 */
public class CittaHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private long mId;
    private long mIdCitta;
    private TextView vNome;
    private CittaHolderListener mListener;

    public interface CittaHolderListener {
        void selectedCitta (long id, long idCitta);
    }

    public CittaHolder(View itemView, CittaHolderListener listener) {
        super(itemView);
        itemView.setOnClickListener(this);
        mListener = listener;
        //TODO: vNome = (TextView) v.findViewById(R.id.id_della_label);
    }

    public void setId(long id) {
        mId = id;
    }

    public void setId_citta(long idCitta) {
        mIdCitta = idCitta;
    }

    public void setNomeCitta (String nome) {
        vNome.setText(nome);
    }

    @Override
    public void onClick(View v) {
        mListener.selectedCitta(mId, mIdCitta);
    }
}
