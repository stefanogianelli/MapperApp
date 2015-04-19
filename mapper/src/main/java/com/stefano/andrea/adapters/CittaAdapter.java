package com.stefano.andrea.adapters;

import android.content.ContentResolver;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.models.Citta;

import java.util.List;

/**
 * CittaAdapter
 */
public class CittaAdapter extends RecyclerView.Adapter<CittaAdapter.CittaHolder> {

    private List<Citta> mListaCitta;
    private ContentResolver mResolver;
    private CittaOnClickListener mListener;

    public interface CittaOnClickListener {
        void selezionataCitta (long id);
    }

    public CittaAdapter (List<Citta> listaCitta, ContentResolver resolver, CittaOnClickListener listener) {
        mListaCitta = listaCitta;
        mResolver = resolver;
        mListener = listener;
    }

    @Override
    public CittaHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //TODO: modificare id del layout
        /*View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.id_del_layout, parent, false);
        return new CittaHolder(view);*/
        return null;
    }

    @Override
    public void onBindViewHolder(CittaHolder holder, int position) {
        Citta citta = mListaCitta.get(position);
        holder.vNome.setText(citta.getNome());
        holder.itemView.setTag(citta.getId());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.selezionataCitta((Long) v.getTag());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListaCitta.size();
    }

    public class CittaHolder extends RecyclerView.ViewHolder {

        public TextView vNome;

        public CittaHolder(View itemView) {
            super(itemView);
            //TODO: vNome = (TextView) v.findViewById(R.id.id_della_label);
        }
    }

}
