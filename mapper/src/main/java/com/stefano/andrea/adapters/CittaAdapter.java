package com.stefano.andrea.adapters;

import android.app.Activity;
import android.content.Context;
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
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.tasks.DeleteTask;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.utils.SelectableAdapter;
import com.stefano.andrea.utils.SelectableHolder;

import java.util.List;

/**
 * CittaAdapter
 */
public class CittaAdapter extends SelectableAdapter<CittaAdapter.CittaHolder> implements DeleteTask.DeleteAdapter<Citta>, InsertTask.InsertAdapter<Citta> {

    private List<Citta> mElencoCitta;
    private CittaOnClickListener mListener;
    private Context mContext;
    private Activity mActivity;

    public interface CittaOnClickListener {
        void selezionataCitta (Citta citta);
    }

    public CittaAdapter (Activity activity, ActionMode.Callback callback, CittaOnClickListener listener) {
        super(activity, callback);
        mContext = activity.getApplicationContext();
        mListener = listener;
        mActivity = activity;
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

    @Override
    public void insertItem(Citta item) {
        mElencoCitta.add(0, item);
        notifyItemInserted(0);
    }

    @Override
    public void cancellaItem(Citta item) {
        int pos = mElencoCitta.indexOf(item);
        mElencoCitta.remove(pos);
        notifyItemRemoved(pos);
    }

    public class CittaHolder extends SelectableHolder {

        private TextView nomeCitta;
        private TextView statCitta;
        private ImageButton button1;

        public CittaHolder(View itemView) {
            super(itemView);
            nomeCitta = (TextView) itemView.findViewById(R.id.citta_item_label);
            statCitta = (TextView) itemView.findViewById(R.id.citta_item_label_subtitle);
            button1 = (ImageButton) itemView.findViewById(R.id.button_popup_item_citta);
        }

        public void bindCitta (final Citta citta) {
            this.itemView.setTag(citta);
            nomeCitta.setText(citta.getNome());
            statCitta.setText(mContext.getResources().getQuantityString(R.plurals.statistiche_citta, citta.getCountPosti(), citta.getCountPosti()));

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
                                    Toast.makeText(mActivity, "Vuoi eliminare : " + citta.getNome(), Toast.LENGTH_SHORT).show();
                                    break;
                                case R.id.menu_rename:
                                    Toast.makeText(mActivity, "Vuoi rinominare : " + citta.getNome(), Toast.LENGTH_SHORT).show();
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
                mListener.selezionataCitta((Citta) v.getTag());
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
