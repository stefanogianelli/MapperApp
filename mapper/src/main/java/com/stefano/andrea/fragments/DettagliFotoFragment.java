package com.stefano.andrea.fragments;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stefano.andrea.activities.R;


public class DettagliFotoFragment extends Fragment {
    public View onCreateView() {
        //LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (inflater.inflate(R.layout.fragment_dettagli_foto, null));
    }

}