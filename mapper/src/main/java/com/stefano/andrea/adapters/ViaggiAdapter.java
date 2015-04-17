package com.stefano.andrea.adapters;

import android.content.ContentResolver;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.utils.CursorRecyclerAdapter;

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
        View view =  LayoutInflater.from(viewGroup.getContext())
        .inflate(R.layout.viaggio_item, viewGroup, false);
        return new ViaggiHolder(view);
    }

    public class ViaggiHolder extends RecyclerView.ViewHolder {

        private TextView vNome;

        public ViaggiHolder(View v) {
            super(v);
            vNome = (TextView) v.findViewById(R.id.viaggio_item_label);
        }
    }
}
