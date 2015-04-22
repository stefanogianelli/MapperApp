package com.stefano.andrea.activities;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.stefano.andrea.adapters.GridViewFotoViaggioAdapter;
import com.stefano.andrea.utils.ImageItem;

import java.util.ArrayList;

/**
 * Created by hp1 on 21-01-2015.
 */
public class FotoViaggio extends Fragment {
    private GridView gridView;
    private GridViewFotoViaggioAdapter gridAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.fragment_foto_viaggio,container,false);

        // Grid view per galleria immagini
        gridView = (GridView) v.findViewById(R.id.gridViewFotoViaggio);
        gridAdapter = new GridViewFotoViaggioAdapter(getActivity(), R.layout.grid_item_foto_viaggio_layout, getData());
        gridView.setAdapter(gridAdapter);

        return v;
    }


    // FZIONE TEMPORANEA...per cricare le varie immagini
    private ArrayList<ImageItem> getData() {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        TypedArray imgs = getResources().obtainTypedArray(R.array.image_ids);
        for (int i = 0; i < imgs.length(); i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgs.getResourceId(i, -1));
            imageItems.add(new ImageItem(bitmap, "Image#" + i));
        }
        return imageItems;
    }

}