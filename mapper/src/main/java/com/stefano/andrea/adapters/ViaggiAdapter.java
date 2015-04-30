package com.stefano.andrea.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.tasks.DeleteTask;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.utils.SelectableAdapter;
import com.stefano.andrea.utils.SelectableHolder;

import java.util.List;

/**
 * ViaggiAdapter
 */
public class ViaggiAdapter extends SelectableAdapter<ViaggiAdapter.ViaggiHolder> implements DeleteTask.DeleteAdapter<Viaggio>, InsertTask.InsertAdapter<Viaggio> {

    private List<Viaggio> mListaViaggi;
    private ViaggioOnClickListener mListener;
    private Context mContext;
    private Activity mActivity;

    public interface ViaggioOnClickListener {
        void selezionatoViaggio (Viaggio viaggio);
    }

    public ViaggiAdapter(ViaggioOnClickListener listener, ActionBarActivity activity, ActionMode.Callback callback) {
        super(activity, callback);
        mContext = activity.getApplicationContext();
        mListener = listener;
        mActivity = activity;
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

    @Override
    public void insertItem(Viaggio item) {
        mListaViaggi.add(0, item);
        notifyItemInserted(0);
    }

    @Override
    public void cancellaItem(Viaggio item) {
        int pos = mListaViaggi.indexOf(item);
        mListaViaggi.remove(pos);
        notifyItemRemoved(pos);
    }

    public class ViaggiHolder extends SelectableHolder {

        private TextView nomeViaggio;
        private TextView viaggioLabel;
        private ImageButton button1;

        public ViaggiHolder(View itemView) {
            super(itemView);
            nomeViaggio = (TextView) itemView.findViewById(R.id.viaggio_item_label);
            viaggioLabel = (TextView) itemView.findViewById(R.id.viaggio_item_label_subtitle);
            button1 = (ImageButton) itemView.findViewById(R.id.button_popup);
        }

        public void bindViaggio (final Viaggio viaggio) {
            this.itemView.setTag(viaggio);
            nomeViaggio.setText(viaggio.getNome());
            viaggioLabel.setText(mContext.getResources().getQuantityString(R.plurals.statistiche_viaggio, viaggio.getCountPosti(), viaggio.getCountCitta(), viaggio.getCountPosti()));

            button1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //Creating the instance of PopupMenu
                    PopupMenu popup = new PopupMenu(mActivity, button1);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            int id = item.getItemId();
                            switch (id) {
                                case R.id.menu_remove:
                                     Toast.makeText(mActivity, "Vuoi eliminare : " + viaggio.getNome(), Toast.LENGTH_SHORT).show();
                                    break;
                                case R.id.menu_rename:
                                       Toast.makeText(mActivity, "Vuoi rinominare : " + viaggio.getNome(), Toast.LENGTH_SHORT).show();
                                    break;
                            }
                            return true;
                        }
                    });

                    popup.show();//showing popup menu
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
