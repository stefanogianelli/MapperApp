package com.stefano.andrea.adapters;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stefano.andrea.activities.BuildConfig;
import com.stefano.andrea.helpers.CittaHelper;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.utils.CursorRecyclerViewAdapter;

import java.util.List;

/**
 * CittaAdapter
 */
public class CittaAdapter extends CursorRecyclerViewAdapter<CittaAdapter.CittaHolder> {

    private ContentResolver mResolver;
    private CittaOnClickListener mListener;
    private CittaHelper mHelper;

    public interface CittaOnClickListener {
        void selezionataCitta (long id);
    }

    public CittaAdapter(Cursor cursor, ContentResolver resolver, CittaOnClickListener listener) {
        super(cursor);
        mResolver = resolver;
        mListener = listener;
        mHelper = new CittaHelper(mResolver);
    }

    @Override
    public void onBindViewHolderCursor(CittaHolder holder, Cursor cursor) {
        String nome = cursor.getString(cursor.getColumnIndex(MapperContract.DatiCitta.NOME));
        long id = cursor.getLong(cursor.getColumnIndex(MapperContract.Citta.ID_CITTA));
        holder.vNome.setText(nome);
        holder.itemView.setTag(id);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.selezionataCitta((Long) v.getTag());
            }
        });
    }

    @Override
    public CittaHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //TODO: modificare id del layout
        /*View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.id_del_layout, parent, false);
        return new CittaHolder(view);*/
        return null;
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
        return mResolver.insert(MapperContract.Citta.CONTENT_URI, values);
    }

    public class CittaHolder extends RecyclerView.ViewHolder {

        public TextView vNome;

        public CittaHolder(View itemView) {
            super(itemView);
            //TODO: vNome = (TextView) v.findViewById(R.id.id_della_label);
        }
    }

}
