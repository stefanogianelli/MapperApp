package com.stefano.andrea.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.tasks.DeleteTask;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.tasks.UpdateTask;
import com.stefano.andrea.utils.SelectableAdapter;
import com.stefano.andrea.utils.SelectableHolder;

import java.util.List;

/**
 * ViaggiAdapter
 */
public class ViaggiAdapter extends SelectableAdapter<ViaggiAdapter.ViaggiHolder> implements DeleteTask.DeleteAdapter<Viaggio>, InsertTask.InsertAdapter<Viaggio>, UpdateTask.UpdateAdapter {

    private List<Viaggio> mListaViaggi;
    private ViaggioOnClickListener mListener;
    private Context mContext;
    private Activity mActivity;
    private ImageLoader mImageLoader;

    public interface ViaggioOnClickListener {
        void selezionatoViaggio (Viaggio viaggio);
        void rimuoviViaggio (Viaggio viaggio);
        void rinominaViaggio (int position, Viaggio viaggio);
    }

    public ViaggiAdapter(ViaggioOnClickListener listener, Activity activity, ActionMode.Callback callback) {
        super(activity, callback);
        mContext = activity.getApplicationContext();
        mListener = listener;
        mActivity = activity;
        mImageLoader = ImageLoader.getInstance();
    }

    public void setListaViaggi (List<Viaggio> lista) {
        mListaViaggi = lista;
        notifyDataSetChanged();
    }

    @Override
    public ViaggiHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viaggio_item, parent, false);
        return new ViaggiHolder(view);
    }

    @Override
    public void onBindViewHolder(ViaggiHolder holder, int position) {
        LinearLayout check = (LinearLayout) holder.itemView.findViewById(R.id.image_checked);
        if (isSelected(position)) {
            check.setVisibility(View.VISIBLE);
        } else {
            check.setVisibility(View.GONE);
        }
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

    @Override
    public void insertItem(Viaggio item) {
        mListaViaggi.add(0, item);
        notifyItemInserted(0);
    }

    @Override
    public void UpdateItem(int position, String nome) {
        mListaViaggi.get(position).setNome(nome);
        notifyItemChanged(position);
    }

    @Override
    public void cancellaItem(Viaggio item) {
        mListaViaggi.remove(item);
    }

    @Override
    public void notificaChange() {
        notifyDataSetChanged();
    }

    protected class ViaggiHolder extends SelectableHolder {

        private TextView nomeViaggio;
        private TextView viaggioLabel;
        private ImageButton button1;
        private ImageView copertina;

        public ViaggiHolder(View itemView) {
            super(itemView);
            nomeViaggio = (TextView) itemView.findViewById(R.id.viaggio_item_label);
            viaggioLabel = (TextView) itemView.findViewById(R.id.viaggio_item_label_subtitle);
            button1 = (ImageButton) itemView.findViewById(R.id.button_popup_item_viaggio);
            copertina = (ImageView) itemView.findViewById(R.id.copertina_viaggio);
        }

        public void bindViaggio (final Viaggio viaggio) {
            this.itemView.setTag(viaggio);
            nomeViaggio.setText(viaggio.getNome());
            viaggioLabel.setText(mContext.getResources().getQuantityString(R.plurals.statistiche_viaggio, viaggio.getCountPosti(), viaggio.getCountCitta(), viaggio.getCountPosti(), viaggio.getCountFoto()));
            mImageLoader.displayImage(viaggio.getPathFoto(), copertina);
            button1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //Creating the instance of PopupMenu
                    PopupMenu popup = new PopupMenu(mActivity, button1);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.popup_one, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            int id = item.getItemId();
                            switch (id) {
                                case R.id.menu_remove:
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
                                    dialog.setMessage(R.string.conferma_cancellazione_viaggio);
                                    dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mListener.rimuoviViaggio(viaggio);
                                            dialog.dismiss();
                                        }
                                    });
                                    dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    dialog.create().show();
                                    break;
                                case R.id.menu_rename:
                                    mListener.rinominaViaggio(getLayoutPosition(), viaggio);
                                    break;
                            }
                            return true;
                        }
                    });

                    popup.show();
                }
            });

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
