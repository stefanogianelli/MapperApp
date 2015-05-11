package com.stefano.andrea.adapters;

import android.app.Activity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.tasks.DeleteTask;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.utils.SelectableAdapter;
import com.stefano.andrea.utils.SelectableHolder;

import java.util.List;

/**
 * FotoAdapter
 */
public class FotoAdapter extends SelectableAdapter<FotoAdapter.FotoHolder> implements DeleteTask.DeleteAdapter<Foto>, InsertTask.InsertAdapter<List<Foto>> {

    private FotoOnClickListener mListener;
    private List<Foto> mElencoFoto;
    private ImageLoader mImageLoader;

    public interface FotoOnClickListener {
        void selezionataFoto (int posizione);
    }

    public FotoAdapter(Activity activity, ActionMode.Callback callback, FotoOnClickListener listener) {
        super(activity, callback);
        mListener = listener;
        mImageLoader = ImageLoader.getInstance();
    }

    public void setElencoFoto (List<Foto> elencoFoto) {
        mElencoFoto = elencoFoto;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(FotoHolder holder, int position) {
        LinearLayout check = (LinearLayout) holder.itemView.findViewById(R.id.image_checked);
        if (isSelected(position)) {
            check.setVisibility(View.VISIBLE);
        } else {
            check.setVisibility(View.GONE);
        }
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

    @Override
    public void insertItem(List<Foto> item) {
        for (int i = 0; i < item.size(); i++) {
            Foto foto = item.get(i);
            mElencoFoto.add(0, foto);
            notifyItemInserted(0);
        }
    }


    @Override
    public void cancellaItem(Foto item) {
        mElencoFoto.remove(item);
    }

    @Override
    public void notificaChange() {
        notifyDataSetChanged();
    }

    protected class FotoHolder extends SelectableHolder {

        private ImageView fotoView;

        public FotoHolder(View itemView) {
            super(itemView);
            fotoView = (ImageView) itemView.findViewById(R.id.image_grid_item_foto_viaggio);
        }

        public void bindFoto (Foto foto) {
            mImageLoader.displayImage(foto.getPath(), fotoView);
        }

        @Override
        public void onClick(View v) {
            if (isEnabledSelectionMode())
                toggleSelection(getLayoutPosition());
            else
                mListener.selezionataFoto(getLayoutPosition());
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
