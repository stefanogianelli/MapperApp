package com.stefano.andrea.adapters;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.utils.SelectableAdapter;
import com.stefano.andrea.utils.SelectableHolder;

import java.util.List;

/**
 * ViaggiAdapter
 */
public class ViaggiAdapter extends SelectableAdapter<ViaggiAdapter.ViaggiHolder> {

    private List<Viaggio> mListaViaggi;
    private ViaggioOnClickListener mListener;

    public interface ViaggioOnClickListener {
        void selezionatoViaggio (Viaggio viaggio);
    }

    public ViaggiAdapter(ViaggioOnClickListener listener, ActionBarActivity activity, ActionMode.Callback callback) {
        super(activity, callback);
        mListener = listener;
    }

    public void setListaViaggi (List<Viaggio> lista) {
        if (mListaViaggi == null) {
            mListaViaggi = lista;
            notifyDataSetChanged();
        }
    }

    @Override
    public ViaggiHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viaggio_item, parent, false);
        return new ViaggiHolder(view);
    }

    @Override
    public void onBindViewHolder1(ViaggiHolder holder, int position) {
        Viaggio viaggio = mListaViaggi.get(position);
        holder.bindViaggio(viaggio);
    }

    @Override
    public int getItemCount() {
        if (mListaViaggi != null)
            return mListaViaggi.size();
        else
            return 0;
    }

    public void creaNuovoViaggio (long id, String nome) {
        Viaggio viaggio = new Viaggio(id, nome);
        mListaViaggi.add(0, viaggio);
        notifyItemInserted(0);
    }

    public void cancellaViaggio (Viaggio viaggio) {
        int pos = mListaViaggi.indexOf(viaggio);
        mListaViaggi.remove(pos);
        notifyItemRemoved(pos);
    }

    public class ViaggiHolder extends SelectableHolder {

        private TextView vNome;

        public ViaggiHolder(View itemView) {
            super(itemView);
            vNome = (TextView) itemView.findViewById(R.id.viaggio_item_label);
        }

        public void bindViaggio (Viaggio viaggio) {
            this.itemView.setTag(viaggio);
            vNome.setText(viaggio.getNome());
        }

        @Override
        public void onClick(View v) {
            if (isEnabledSelectionMode())
                toggleSelection(getLayoutPosition());
            else
                mListener.selezionatoViaggio((Viaggio) v.getTag());
        }

        @Override
        public boolean onLongClick(View v) {
            if (!isEnabledSelectionMode())
                startActionMode();
            toggleSelection(getLayoutPosition());
            return true;
        }
    }
}
