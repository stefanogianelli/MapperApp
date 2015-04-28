package com.stefano.andrea.adapters;

import android.app.Activity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    public interface PostoOnClickListener {
        void selezionatoPosto (Posto posto);
    }

    public PostiAdapter(PostoOnClickListener listener, Activity activity, ActionMode.Callback callback) {
        super(activity, callback);
        mListener = listener;
    }

    public void setElencoPosti (List<Posto> elencoPosti) {
        if (mElencoPosti == null) {
            mElencoPosti = elencoPosti;
            notifyDataSetChanged();
        }
    }

    @Override
    public void onBindViewHolder1(PostiHolder holder, int position) {
        Posto posto = mElencoPosti.get(position);
        holder.bindPosto(posto);
    }

    @Override
    public PostiHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.checkbox_item, parent, false);
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
        int pos = mElencoPosti.indexOf(item);
        mElencoPosti.remove(pos);
        notifyItemRemoved(pos);
    }

    public class PostiHolder extends SelectableHolder {

        private TextView nomePosto;

        public PostiHolder(View itemView) {
            super(itemView);
            nomePosto = (TextView) itemView.findViewById(R.id.nome_posto);
        }

        private void bindPosto (Posto posto) {
            this.itemView.setTag(posto);
            nomePosto.setText(posto.getNome());
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
