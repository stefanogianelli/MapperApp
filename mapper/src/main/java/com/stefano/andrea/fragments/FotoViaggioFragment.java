package com.stefano.andrea.fragments;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.adapters.RecyclerViewFotoViaggioAdapter;
import com.stefano.andrea.models.ImageItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hp1 on 21-01-2015.
 */
public class FotoViaggioFragment extends Fragment {
    //private GridView gridView;
    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.fragment_foto_viaggio,container,false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.gridViewFotoViaggio);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(new RecyclerViewFotoViaggioAdapter(getData()));
        // Grid view per galleria immagini
        /*gridView = (GridView) v.findViewById(R.id.gridViewFotoViaggio);
        gridAdapter = new GridViewFotoViaggioAdapter(getActivity(), R.layout.grid_item_foto_viaggio_layout, getData());
        gridView.setAdapter(gridAdapter);/

        /*
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DettagliFotoFragment mFragment = new DettagliFotoFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack
                transaction.replace(R.id.pager, mFragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
                // TODO: Finire fragment full foto
            }
        }); */

            return v;
    }


    // FZIONE TEMPORANEA...per caricare le varie immagini
    private List<ImageItem> getData() {
        final List<ImageItem> imageItems = new ArrayList<>();
        TypedArray imgs = getResources().obtainTypedArray(R.array.image_ids);
        //for (int i = 0; i < imgs.length(); i++) {
        for (int i = 0; i < 2; i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgs.getResourceId(i, -1));
            imageItems.add(new ImageItem(bitmap, "Image#" + i));
        }
        return imageItems;
    }



}