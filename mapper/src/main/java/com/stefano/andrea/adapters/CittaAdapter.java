package com.stefano.andrea.adapters;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stefano.andrea.activities.BuildConfig;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.helpers.CittaHelper;
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.providers.MapperContract;

import java.util.List;

/**
 * CittaAdapter
 */
public class CittaAdapter extends RecyclerView.Adapter<CittaAdapter.CittaHolder> {

    private List<Citta> mElencoCitta;
    private ContentResolver mResolver;
    private CittaOnClickListener mListener;
    private CittaHelper mHelper;

    public interface CittaOnClickListener {
        void selezionataCitta (long id);
    }

    public CittaAdapter (Context context, ContentResolver resolver, CittaOnClickListener listener) {
        mResolver = resolver;
        mListener = listener;
        mHelper = new CittaHelper(context, mResolver);
    }

    public void setElencoCitta (List<Citta> elencoCitta) {
        if (mElencoCitta == null) {
            mElencoCitta = elencoCitta;
            notifyDataSetChanged();
        }
    }

    @Override
    public CittaAdapter.CittaHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.citta_item, parent, false);
        return new CittaHolder(view);
    }

    @Override
    public void onBindViewHolder(CittaAdapter.CittaHolder holder, int position) {
        Citta citta = mElencoCitta.get(position);
        holder.bindCitta(citta);
    }

    @Override
    public int getItemCount() {
        if (mElencoCitta != null) {
            return mElencoCitta.size();
        } else {
            return 0;
        }
    }

    public Uri creaNuovaCitta (long idViaggio, String nome, String nazione) {
        long idCitta = mHelper.getDatiCitta(nome, nazione);
        if (idCitta == -1)
            idCitta = mHelper.creaCitta(nome, nazione);
        if (BuildConfig.DEBUG && idCitta == -1)
            throw new AssertionError();
        ContentValues values = new ContentValues();
        values.put(MapperContract.Citta.ID_VIAGGIO, idViaggio);
        values.put(MapperContract.Citta.ID_DATI_CITTA, idCitta);
        values.put(MapperContract.Citta.PERCENTUALE, 0);
        Uri uri = mResolver.insert(MapperContract.Citta.CONTENT_URI, values);
        Uri query = ContentUris.withAppendedId(MapperContract.DatiCitta.CONTENT_URI, idCitta);
        String [] projetion = {MapperContract.DatiCitta.LATITUDINE, MapperContract.DatiCitta.LONGITUDINE};
        Cursor c = mResolver.query(query, projetion, null, null, MapperContract.DatiCitta.DEFAULT_SORT);
        if (c != null) {
            c.moveToNext();
            double lon = c.getDouble(c.getColumnIndex(MapperContract.DatiCitta.LONGITUDINE));
            double lat = c.getDouble(c.getColumnIndex(MapperContract.DatiCitta.LATITUDINE));
            mElencoCitta.add(0, new Citta(Long.parseLong(uri.getLastPathSegment()), nome, nazione, lat, lon));
            c.close();
            notifyItemInserted(0);
            return uri;
        }
        return null;
    }

    public class CittaHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView vNome;

        public CittaHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            vNome = (TextView) itemView.findViewById(R.id.citta_item_label);
        }

        public void bindCitta (Citta citta) {
            this.itemView.setId((int)citta.getId());
            vNome.setText(citta.getNome());
        }

        @Override
        public void onClick(View v) {
            mListener.selezionataCitta(v.getId());
        }
    }

}
