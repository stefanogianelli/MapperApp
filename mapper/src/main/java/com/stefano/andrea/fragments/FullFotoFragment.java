package com.stefano.andrea.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
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
import com.stefano.andrea.activities.ModInfoFotoActivity;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.dialogs.DialogHelper;
import com.stefano.andrea.intents.MapperIntent;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.tasks.DeleteTask;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

/**
 * FullFotoFragment
 */
public class FullFotoFragment extends Fragment {

    private static final String EXTRA_LISTA_FOTO = "com.stefano.andrea.fragments.FullFotoFragment.listaFoto";
    private static final String EXTRA_IMAGE_POSITION = "com.stefano.andrea.fragments.FullFotoFragment.imagePosition";

    private final static int IMMERSIVE_MODE_TIMEOUT = 500;

    private Activity mParentActivity;
    private List<Foto> mElencoFoto;
    private int mPosition;
    private ViewPager mPager;
    private Toolbar mToolbar;

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
        mPager = (ViewPager) rootView.findViewById(R.id.pager);
        mToolbar = (Toolbar) rootView.findViewById(R.id.full_foto_toolbar);
        Bundle args = getArguments();
        if (args != null) {
            mElencoFoto = args.getParcelableArrayList(EXTRA_LISTA_FOTO);
            mPosition = args.getInt(EXTRA_IMAGE_POSITION);
        }

        ((AppCompatActivity) mParentActivity).setSupportActionBar(mToolbar);
        ((AppCompatActivity) mParentActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) mParentActivity).getSupportActionBar().setTitle(R.string.full_foto_fragment_title);
        mToolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        int uiOptions = mParentActivity.getWindow().getDecorView().getSystemUiVisibility();
        boolean isImmersiveModeEnabled =  ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if(isImmersiveModeEnabled){
            ((AppCompatActivity) mParentActivity).getSupportActionBar().hide();
        }else{
            ((AppCompatActivity) mParentActivity).getSupportActionBar().show();
        }
        uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        rootView.setSystemUiVisibility(uiOptions);

        mPager.setAdapter(new ImageAdapter(mParentActivity));
        mPager.setCurrentItem(mPosition);
        setHasOptionsMenu(true);
        setImmersiveTimer();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        int uiOptions = mParentActivity.getWindow().getDecorView().getSystemUiVisibility();
        boolean isImmersiveModeEnabled =  ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled){ toggleUiFlags(); }
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
        final Foto currentFoto = mElencoFoto.get(mPager.getCurrentItem());
        if (id == android.R.id.home) {
            getFragmentManager().popBackStack();
            return true;
        } else if (id == R.id.action_dettagli) {
            DialogHelper.showDettagliFotoDialog(mParentActivity, mElencoFoto.get(mPager.getCurrentItem()));
            return true;
        } else if (id == R.id.action_modifica) {
            Intent intent = new Intent(mParentActivity, ModInfoFotoActivity.class);
            ArrayList<Integer> fotoId = new ArrayList<>();
            ArrayList<Uri> fotoUris = new ArrayList<>();
            fotoId.add((int) currentFoto.getId());
            fotoUris.add(Uri.parse(currentFoto.getPath()));
            intent.putIntegerArrayListExtra(ModInfoFotoActivity.EXTRA_LISTA_FOTO, fotoId);
            intent.putParcelableArrayListExtra(ModInfoFotoActivity.EXTRA_FOTO, fotoUris);
            intent.putExtra(ModInfoFotoActivity.EXTRA_ID_POSTO, currentFoto.getIdPosto());
            intent.putExtra(ModInfoFotoActivity.EXTRA_ID_CITTA, currentFoto.getIdCitta());
            intent.putExtra(ModInfoFotoActivity.EXTRA_ID_VIAGGIO, currentFoto.getIdViaggio());
            intent.setAction(MapperIntent.MAPPER_MODIFICA_FOTO);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_mostra_su_mappa) {
            Uri maps = Uri.parse("geo:0,0?q=" + currentFoto.getLatitudine() + "," + currentFoto.getLongitudine() + "(Foto)");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(maps);
            if (intent.resolveActivity(mParentActivity.getPackageManager()) != null) {
                startActivity(intent);
            }
            return true;
        }else if(id == R.id.action_condividi_foto){
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType(currentFoto.getMimeType());
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse(currentFoto.getPath()));
            startActivity(Intent.createChooser(share, getString(R.string.condividi_foto)));
            return true;
        } else if (id == R.id.action_elimina_foto) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mParentActivity);
            dialog.setMessage(R.string.conferma_cancellazione_foto);
            dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {
                    List<Foto> elencoFoto = new ArrayList<>();
                    elencoFoto.add(currentFoto);
                    List<Integer> indici = new ArrayList<>();
                    indici.add(0);
                    DeleteTask.DeleteAdapter adapter = new DeleteTask.DeleteAdapter() {
                        @Override
                        public void cancellaItems(List<Integer> items) {
                            dialog.dismiss();
                            int position = mPager.getCurrentItem();
                            mElencoFoto.remove(position);
                            mPager.getAdapter().notifyDataSetChanged();
                            int lenght = mPager.getAdapter().getCount();
                            if (lenght == 0) {
                                getFragmentManager().popBackStack();
                            } else  {
                                if (position != 0) {
                                    mPager.setCurrentItem(position - 1);
                                } else {
                                    mPager.setCurrentItem(position);
                                }
                            }
                        }
                    };
                    new DeleteTask<>(mParentActivity, adapter, elencoFoto, indici).execute(DeleteTask.CANCELLA_FOTO);
                }
            });
            dialog.setNegativeButton(R.string.cancel, null);
            dialog.create().show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int paddingRight = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
                paddingRight = getNavigationBarHeight();
            mToolbar.setPadding(0, getStatusBarHeight(), paddingRight, 0);
        }
    }

    private class ImageAdapter extends PagerAdapter {

        private LayoutInflater inflater;

        public ImageAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);
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
            imageLayout.setTag(mElencoFoto.get(position).getPath());
            final ImageViewTouch imageView = (ImageViewTouch) imageLayout.findViewById(R.id.image);
            //imageView.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
            imageView.setSingleTapListener(new ImageViewTouch.OnImageViewTouchSingleTapListener() {
                @Override
                public void onSingleTapConfirmed() {
                    toggleUiFlags();
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
                    imageView.setImageBitmap(loadedImage, null, -1, 8f);
                }
            });

            view.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public int getItemPosition(Object object) {
            String path = (String) ((View) object).getTag();
            boolean exists = false;
            for (int i = 0; i < mElencoFoto.size(); i++) {
                if (mElencoFoto.get(i).getPath().equals(path)) {
                    exists = true;
                    break;
                }
            }
            if (exists)
                return PagerAdapter.POSITION_UNCHANGED;
            else
                return PagerAdapter.POSITION_NONE;
        }
    }


    /**
     * Detects and toggles immersive mode (also known as "hidey bar" mode).
     */
    private void toggleUiFlags() {

        View decorView = getActivity().getWindow().getDecorView();
        int uiOptions = decorView.getSystemUiVisibility();
        int newUiOptions = uiOptions;

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        decorView.setSystemUiVisibility(newUiOptions);

        // Nascondo la toolbar
        boolean isImmersiveModeEnabled =  ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if(isImmersiveModeEnabled){
            ((AppCompatActivity) mParentActivity).getSupportActionBar().show();
        }else{
            ((AppCompatActivity) mParentActivity).getSupportActionBar().hide();
        }

    }

    private void setImmersiveTimer(){
        new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(IMMERSIVE_MODE_TIMEOUT);
                        mParentActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toggleUiFlags();
                            }
                        });

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private int getNavigationBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result ;
    }

}



