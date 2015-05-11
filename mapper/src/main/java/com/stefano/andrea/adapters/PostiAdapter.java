package com.stefano.andrea.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.models.Posto;
import com.stefano.andrea.tasks.DeleteTask;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.utils.SelectableAdapter;
import com.stefano.andrea.utils.SelectableHolder;

import java.util.List;

/**
 * PostiAdapter
 */
public class PostiAdapter extends SelectableAdapter<PostiAdapter.PostiHolder> implements DeleteTask.DeleteAdapter<Posto>, InsertTask.InsertAdapter<Posto> {

    private List<Posto> mElencoPosti;
    private PostoOnClickListener mListener;
    private Activity mActivity;

    public interface PostoOnClickListener {
        void selezionatoPosto (Posto posto);
        void cancellaPosto (Posto posto);
        void visitatoPosto (Posto posto);
    }

    public PostiAdapter(PostoOnClickListener listener, Activity activity, ActionMode.Callback callback) {
        super(activity, callback);
        mListener = listener;
        mActivity = activity;
    }

    public void setElencoPosti (List<Posto> elencoPosti) {
        mElencoPosti = elencoPosti;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(PostiHolder holder, int position) {
        CardView card = (CardView) holder.itemView;
        if (isSelected(position)) {
            card.setCardBackgroundColor(mActivity.getResources().getColor(R.color.selected_overlay));
        } else {
            card.setCardBackgroundColor(Color.WHITE);
        }
        Posto posto = mElencoPosti.get(position);
        holder.bindPosto(posto);
    }

    @Override
    public PostiHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.posti_item, parent, false);
        return new PostiHolder(view);
    }

    @Override
    public int getItemCount() {
        if (mElencoPosti != null) {
            return mElencoPosti.size();
        } else {
            return 0;
        }
    }

    @Override
    public void insertItem(Posto item) {
        mElencoPosti.add(0, item);
        notifyItemInserted(0);
    }

    @Override
    public void cancellaItem(Posto item) {
        mElencoPosti.remove(item);
    }

    @Override
    public void notificaChange() {
        notifyDataSetChanged();
    }

    protected class PostiHolder extends SelectableHolder {

        private CheckBox checkBox;
        private TextView nomePosto;
        private ImageButton menuButton;

        public PostiHolder(View itemView) {
            super(itemView);
            nomePosto = (TextView) itemView.findViewById(R.id.nome_posto);
            menuButton = (ImageButton) itemView.findViewById(R.id.button_popup_item_posto);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox_posto);
        }

        private void bindPosto (final Posto posto) {
            this.itemView.setTag(posto);
            nomePosto.setText(posto.getNome());
            checkBox.setChecked(posto.isVisitato());
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    posto.setVisitato(isChecked);
                    mListener.visitatoPosto(posto);
                }
            });
            menuButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //Creating the instance of PopupMenu
                    PopupMenu popup = new PopupMenu(mActivity, menuButton);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            int id = item.getItemId();
                            switch (id) {
                                case R.id.menu_remove:
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
                                    dialog.setMessage(R.string.conferma_cancellazione_posto);
                                    dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mListener.cancellaPosto(posto);
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
                                    Toast.makeText(mActivity, "Vuoi rinominare : " + posto.getNome(), Toast.LENGTH_SHORT).show();
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
                mListener.selezionatoPosto((Posto) v.getTag());
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
