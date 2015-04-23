package com.stefano.andrea.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.models.ImageItem;

import java.util.ArrayList;

/**
 * Created by Andre on 22/04/2015.
 */
public class RecyclerViewFotoViaggioAdapter extends RecyclerView.Adapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList data = new ArrayList();

    public RecyclerViewFotoViaggioAdapter(Context context, int layoutResourceId, ArrayList data) {
        //super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}