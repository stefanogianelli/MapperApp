package com.stefano.andrea.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.utils.DialogHelper;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

/**
 * FullFotoFragment
 */
public class FullFotoFragment extends Fragment {

    private static final String EXTRA_LISTA_FOTO = "com.stefano.andrea.fragments.FullFotoFragment.listaFoto";
    private static final String EXTRA_IMAGE_POSITION = "com.stefano.andrea.fragments.FullFotoFragment.imagePosition";

    private Activity mParentActivity;
    private List<Foto> mElencoFoto;
    private int mPosition;

    public FullFotoFragment () { }

    public static FullFotoFragment newInstance (ArrayList<Foto> elencoFoto, int posizione) {
        FullFotoFragment fragment = new FullFotoFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(EXTRA_LISTA_FOTO, elencoFoto);
        args.putInt(EXTRA_IMAGE_POSITION, posizione);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mParentActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_full_foto, container, false);
        ViewPager pager = (ViewPager) rootView.findViewById(R.id.pager);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.full_foto_toolbar);
        Bundle args = getArguments();
        if (args != null) {
            mElencoFoto = args.getParcelableArrayList(EXTRA_LISTA_FOTO);
            mPosition = args.getInt(EXTRA_IMAGE_POSITION);
        }
        ((ActionBarActivity) mParentActivity).setSupportActionBar(toolbar);
        ((ActionBarActivity) mParentActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ActionBarActivity) mParentActivity).getSupportActionBar().setTitle(R.string.full_foto_fragment_title);
        pager.setAdapter(new ImageAdapter(mParentActivity));
        pager.setCurrentItem(mPosition);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.removeItem(R.id.action_aggiungi_foto_main);
        menu.removeItem(R.id.action_aggiungi_foto_dettagli_viaggio);
        menu.removeItem(R.id.action_aggiungi_foto_dettagli_citta);
        menu.removeItem(R.id.action_aggiungi_foto_dettagli_posto);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_full_foto, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getFragmentManager().popBackStack();
            return true;
        } else if (id == R.id.action_dettagli) {
            DialogHelper.showDettagliFotoDialog(mParentActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ImageAdapter extends PagerAdapter {

        private LayoutInflater inflater;

        public ImageAdapter(Context context) {
            inflater = LayoutInflater.from(context);


        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mElencoFoto.size();
        }

        @Override
        public Object instantiateItem(final ViewGroup view, int position) {
            View imageLayout = inflater.inflate(R.layout.item_full_image, view, false);
            final ImageViewTouch imageView = (ImageViewTouch) imageLayout.findViewById(R.id.image);
            //imageView.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
            imageView.setSingleTapListener(new ImageViewTouch.OnImageViewTouchSingleTapListener() {
                @Override
                public void onSingleTapConfirmed() {
                    toggleUiFlags();
                    // Nascondo la toolbar
                    int uiOptions = getActivity().getWindow().getDecorView().getSystemUiVisibility();
                    boolean isImmersiveModeEnabled =  ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
                    if(isImmersiveModeEnabled){
                        ((ActionBarActivity) mParentActivity).getSupportActionBar().hide();
                    }else{
                        ((ActionBarActivity) mParentActivity).getSupportActionBar().show();
                    }
                }
            });
            final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);

            ImageLoader.getInstance().loadImage(mElencoFoto.get(position).getPath(), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    spinner.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    String message = null;
                    switch (failReason.getType()) {
                        case IO_ERROR:
                            message = "Input/Output error";
                            break;
                        case DECODING_ERROR:
                            message = "Image can't be decoded";
                            break;
                        case NETWORK_DENIED:
                            message = "Downloads are denied";
                            break;
                        case OUT_OF_MEMORY:
                            message = "Out Of Memory error";
                            break;
                        case UNKNOWN:
                            message = "Unknown error";
                            break;
                    }
                    Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();

                    spinner.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    spinner.setVisibility(View.GONE);
                    imageView.setImageBitmap(loadedImage, null, - 1, 8f);
                }
            });

            view.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }
    }


    /**
     * Detects and toggles immersive mode (also known as "hidey bar" mode).
     */
    public void toggleUiFlags() {

        View decorView = getActivity().getWindow().getDecorView();
        int uiOptions = decorView.getSystemUiVisibility();
        int newUiOptions = uiOptions;

            newUiOptions ^= View.SYSTEM_UI_FLAG_LOW_PROFILE;

            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;

            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE;

            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView.setSystemUiVisibility(newUiOptions);

    }



}



