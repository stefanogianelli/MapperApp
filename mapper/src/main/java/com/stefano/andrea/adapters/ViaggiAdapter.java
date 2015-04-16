package com.stefano.andrea.adapters;

import android.content.ContentResolver;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stefano.andrea.model.MapperContract;

/**
 * ViaggiAdapter
 */
public class ViaggiAdapter extends CursorRecyclerAdapter<ViaggiAdapter.ViaggiHolder> {

    private ContentResolver resolver;

    public ViaggiAdapter(Cursor cursor, ContentResolver resolver) {
        super(cursor);
        this.resolver = resolver;
    }

    @Override
    public void onBindViewHolderCursor(ViaggiHolder holder, Cursor cursor) {
        String nome = cursor.getString(cursor.getColumnIndex(MapperContract.Viaggio.NOME));
        holder.vNome.setText(nome);
    }

    @Override
    public ViaggiHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        //TODO: completare reference view
        //View view = null;
        //return new ViaggiHolder(view);
        return null;
    }

    public class ViaggiHolder extends RecyclerView.ViewHolder {

        private TextView vNome;

        public ViaggiHolder(View v) {
            super(v);
            //TODO: aggiungere reference all'elemento alla TextView
            //esempio: vNome = (TextView) v.findViewById(R.id.nome_viaggio);
        }
    }
}
