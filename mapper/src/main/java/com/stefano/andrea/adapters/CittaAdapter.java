package com.stefano.andrea.adapters;

import android.app.Activity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.utils.SelectableAdapter;
import com.stefano.andrea.utils.SelectableHolder;

import java.util.List;

/**
 * CittaAdapter
 */
public class CittaAdapter extends SelectableAdapter<CittaAdapter.CittaHolder> {

    private List<Citta> mElencoCitta;
    private CittaOnClickListener mListener;

    public interface CittaOnClickListener {
        void selezionataCitta (long id);
    }

    public CittaAdapter (Activity activity, ActionMode.Callback callback, CittaOnClickListener listener) {
        super(activity, callback);
        mListener = listener;
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
    public void onBindViewHolder1(CittaAdapter.CittaHolder holder, int position) {
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

    public void creaNuovaCitta (Citta citta) {
        mElencoCitta.add(0, citta);
        notifyItemInserted(0);
    }

    public void cancellaCitta (Citta citta) {
        int pos = mElencoCitta.indexOf(citta);
        mElencoCitta.remove(pos);
        notifyItemRemoved(pos);
    }

    public class CittaHolder extends SelectableHolder {

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
            if (isEnabledSelectionMode())
                toggleSelection(getLayoutPosition());
            else
                mListener.selezionataCitta(v.getId());
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
