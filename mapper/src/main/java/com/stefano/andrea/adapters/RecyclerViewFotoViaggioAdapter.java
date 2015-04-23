package com.stefano.andrea.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.models.ImageItem;

import java.util.List;

/**
 * Created by Andre on 22/04/2015.
 */
public class RecyclerViewFotoViaggioAdapter extends RecyclerView.Adapter<RecyclerViewFotoViaggioAdapter.FotoHolder> {

    private List<ImageItem> mListaFoto;

    public RecyclerViewFotoViaggioAdapter (List<ImageItem> listaFoto) {
        mListaFoto = listaFoto;
    }

    @Override
    public FotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item_foto_viaggio_layout, parent, false);
        return new FotoHolder(view);
    }

    @Override
    public void onBindViewHolder(FotoHolder holder, int position) {
        ImageItem foto = mListaFoto.get(position);
        //holder.mNomeFoto.setText(foto.getTitle());
        holder.mFoto.setImageBitmap(foto.getImage());
    }

    @Override
    public int getItemCount() {
        if (mListaFoto != null)
            return mListaFoto.size();
        else
            return 0;
    }

    public class FotoHolder extends RecyclerView.ViewHolder {

        //private TextView mNomeFoto;
        private ImageView mFoto;

        public FotoHolder(View itemView) {
            super(itemView);
            //mNomeFoto = (TextView) itemView.findViewById(R.id.text_grid_item_foto_viaggio);
            mFoto = (ImageView) itemView.findViewById(R.id.image_grid_item_foto_viaggio);
        }
    }
}