package com.stefano.andrea.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.utils.SelectableAdapter;
import com.stefano.andrea.utils.SelectableHolder;

import java.util.List;

/**
 * FotoAdapter
 */
public class FotoAdapter extends SelectableAdapter<FotoAdapter.FotoHolder> {

    private Context mContext;
    private FotoOnClickListener mListener;
    private List<Foto> mElencoFoto;

    public interface FotoOnClickListener {
        void selezionataFoto (Foto foto);
    }

    public FotoAdapter(Activity activity, ActionMode.Callback callback, FotoOnClickListener listener) {
        super(activity, callback);
        mListener = listener;
        mContext = activity.getApplicationContext();
    }

    public void setElencoFoto (List<Foto> elencoFoto) {
        if (mElencoFoto == null) {
            mElencoFoto = elencoFoto;
            notifyDataSetChanged();
        }
    }

    @Override
    public void onBindViewHolder1(FotoHolder holder, int position) {
        Foto foto = mElencoFoto.get(position);
        holder.bindFoto(foto);
    }

    @Override
    public FotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_foto_viaggio_layout, parent, false);
        return new FotoHolder(view);
    }

    @Override
    public int getItemCount() {
        if (mElencoFoto != null)
            return mElencoFoto.size();
        else
            return 0;
    }

    protected class FotoHolder extends SelectableHolder {

        private ImageView fotoView;

        public FotoHolder(View itemView) {
            super(itemView);
            fotoView = (ImageView) itemView.findViewById(R.id.image_grid_item_foto_viaggio);
        }

        public void bindFoto (Foto foto) {
            Picasso.with(mContext).load(foto.getPath()).into(fotoView);
        }

        @Override
        public void onClick(View v) {

        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }

}
