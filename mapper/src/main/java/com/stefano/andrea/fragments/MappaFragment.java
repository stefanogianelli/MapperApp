package com.stefano.andrea.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.stefano.andrea.activities.DettagliCittaActivity;
import com.stefano.andrea.activities.DettagliPostoActivity;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.loaders.CoordinateLoader;
import com.stefano.andrea.models.GeoInfo;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.utils.MapperContext;
import com.stefano.andrea.utils.MultiDrawable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * MappaFragment
 */
public class MappaFragment extends SupportMapFragment implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<List<GeoInfo>>, ClusterManager.OnClusterClickListener<GeoInfo>, ClusterManager.OnClusterItemClickListener<GeoInfo>, ClusterManager.OnClusterItemInfoWindowClickListener<GeoInfo> {

    private static final String TAG = "MappaFragment";
    private static final long ID_CITTA = -42;

    public static final String EXTRA_TIPO_MAPPA = "com.stefano.andrea.fragments.MappaFragment.tipoMappa";
    public static final int MAPPA_CITTA = 0;
    public static final int MAPPA_POSTI = 1;

    private static final int MAP_COORD_LOADER = 4;
    private static final int TIMEOUT = 100;
    private static final int MAP_PADDING = 250;
    private static final int NUMERO_MINIATURE = 4;
    private static final double EARTHRADIUS = 6366198;
    private static final int DISTANCE_CITTA = 5000;
    private static final int DISTANCE_LUOGO = 500;

    private GoogleMap mMap;
    private List<GeoInfo> mMarkerData;
    private Activity mParentActivity;
    private MapperContext mContext;
    private long mId;
    private int mType;
    private ClusterManager<GeoInfo> mClusterManager;
    private List<GeoInfo> mClickedCluster;
    private GeoInfo mClickedItem;
    private Random mRandom;

    public static MappaFragment newInstance(int tipoMappa) {
        MappaFragment fragment = new MappaFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_TIPO_MAPPA, tipoMappa);
        fragment.setArguments(args);
        return fragment;
    }

    public MappaFragment() { }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mParentActivity = activity;
        mContext = MapperContext.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        int tipoMappa = Integer.MIN_VALUE;
        if (args != null) {
            tipoMappa = args.getInt(EXTRA_TIPO_MAPPA);
        }
        switch (tipoMappa) {
            case MAPPA_CITTA:
                mId = mContext.getIdViaggio();
                mType = CoordinateLoader.ELENCO_CITTA;
                break;
            case MAPPA_POSTI:
                mId = mContext.getIdCitta();
                mType = CoordinateLoader.ELENCO_POSTI;
                break;
            default:
                mId = -1;
        }
        mClickedCluster = new ArrayList<>();
        mRandom = new Random();
        if (mId != -1)
            getLoaderManager().initLoader(MAP_COORD_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mapView = super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_mappa, container, false);
        FrameLayout layout = (FrameLayout) view.findViewById(R.id.container_mappa);
        layout.addView(mapView, 0);
        getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mClusterManager = new ClusterManager<>(mParentActivity, mMap);
        mClusterManager.setRenderer(new MarkerRenderer());
        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new SingleItemAdapter());
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);
    }

    @Override
    public Loader<List<GeoInfo>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MAP_COORD_LOADER:
                return new CoordinateLoader(mParentActivity, mId, mType);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<List<GeoInfo>> loader, List<GeoInfo> data) {
        int id = loader.getId();
        switch (id) {
            case MAP_COORD_LOADER:
                mMarkerData = data;
                if (mType == CoordinateLoader.ELENCO_POSTI) {
                    //carico anche i dettagli della citta
                    Cursor c = mParentActivity.getContentResolver().query(MapperContract.Citta.CONTENT_URI,
                            MapperContract.Citta.PROJECTION_ALL,
                            MapperContract.Citta.ID_CITTA + "=?",
                            new String [] {Long.toString(mContext.getIdCitta())},
                            null);
                    if (c.moveToFirst()) {
                        GeoInfo citta = new GeoInfo();
                        citta.setId(ID_CITTA);
                        citta.setNome(c.getString(c.getColumnIndex(MapperContract.DatiCitta.NOME)));
                        citta.setLatitudine(c.getDouble(c.getColumnIndex(MapperContract.DatiCitta.LATITUDINE)));
                        citta.setLongitudine(c.getDouble(c.getColumnIndex(MapperContract.DatiCitta.LONGITUDINE)));
                        citta.setCountFoto(c.getInt(c.getColumnIndex(MapperContract.Citta.COUNT_FOTO)));
                        Cursor foto = mParentActivity.getContentResolver().query(MapperContract.Foto.CONTENT_URI,
                                new String[]{MapperContract.Foto.ID_MEDIA_STORE},
                                MapperContract.Foto.ID_CITTA + "=?",
                                new String [] {Long.toString(c.getLong(c.getColumnIndex(MapperContract.Citta.ID_CITTA)))},
                                MapperContract.Foto.DEFAULT_SORT + " LIMIT 1");
                        List<Bitmap> miniature = new ArrayList<>();
                        if (foto.moveToFirst()) {
                            int idMediaStore = foto.getInt(foto.getColumnIndex(MapperContract.Foto.ID_MEDIA_STORE));
                            miniature.add(MediaStore.Images.Thumbnails.getThumbnail(null, idMediaStore, MediaStore.Images.Thumbnails.MICRO_KIND, null));
                        }
                        citta.setMiniature(miniature);
                        foto.close();
                        mMarkerData.add(citta);
                    }
                    c.close();
                }
                //attendo che la mappa sia stata caricata
                final Handler handler = new Handler(mParentActivity.getMainLooper());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (mMap == null) {
                            try {
                                Thread.sleep(TIMEOUT);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                setMarkers();
                                mClusterManager.cluster();
                            }
                        });
                    }
                }).start();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<GeoInfo>> loader) { }

    private void setMarkers () {
        mMap.clear();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if (mMarkerData.size() > 0) {
            for (int i = 0; i < mMarkerData.size(); i++) {
                GeoInfo item = mMarkerData.get(i);
                mClusterManager.addItem(item);
                builder.include(item.getPosition());
            }
            if (mMarkerData.size() == 1) {
                LatLngBounds tmpBounds = builder.build();
                LatLng center = tmpBounds.getCenter();
                int dist;
                if (mType == CoordinateLoader.ELENCO_CITTA)
                    dist = DISTANCE_CITTA;
                else
                    dist = DISTANCE_LUOGO;
                LatLng northEast = move(center, dist, dist);
                LatLng southWest = move(center, -dist, -dist);
                builder.include(southWest);
                builder.include(northEast);
            }
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, MAP_PADDING);
            mMap.moveCamera(cu);
        }
    }

    private static LatLng move(LatLng startLL, double toNorth, double toEast) {
        double lonDiff = meterToLongitude(toEast, startLL.latitude);
        double latDiff = meterToLatitude(toNorth);
        return new LatLng(startLL.latitude + latDiff, startLL.longitude
                + lonDiff);
    }

    private static double meterToLongitude(double meterToEast, double latitude) {
        double latArc = Math.toRadians(latitude);
        double radius = Math.cos(latArc) * EARTHRADIUS;
        double rad = meterToEast / radius;
        return Math.toDegrees(rad);
    }

    private static double meterToLatitude(double meterToNorth) {
        double rad = meterToNorth / EARTHRADIUS;
        return Math.toDegrees(rad);
    }

    @Override
    public boolean onClusterClick(Cluster<GeoInfo> cluster) {
        mClickedCluster.clear();
        mClickedCluster.addAll(cluster.getItems());
        //mostro dialog con l'elenco degli item presenti nel cluster
        ClusterDialog.newInstance(mClickedCluster, mType, mContext).show(getFragmentManager(), "Dialog");
        return false;
    }

    @Override
    public boolean onClusterItemClick(GeoInfo geoInfo) {
        mClickedItem = geoInfo;
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(GeoInfo item) {
        switch (mType) {
            case CoordinateLoader.ELENCO_CITTA:
                mContext.setIdCitta(item.getId());
                mContext.setNomeCitta(item.getNome());
                startActivity(new Intent(mParentActivity, DettagliCittaActivity.class));
                break;
            case CoordinateLoader.ELENCO_POSTI:
                mContext.setIdPosto(item.getId());
                mContext.setNomePosto(item.getNome());
                startActivity(new Intent(mParentActivity, DettagliPostoActivity.class));
                break;
        }
    }

    private class MarkerRenderer extends DefaultClusterRenderer<GeoInfo> {
        private final IconGenerator mIconGenerator = new IconGenerator(mParentActivity);
        private final IconGenerator mClusterIconGenerator = new IconGenerator(mParentActivity);
        private final ImageView mImageView;
        private final TextView mSingleTextMarker;
        private final ImageView mClusterImageView;
        private final TextView mTextMarker;
        private final int mDimension;

        public MarkerRenderer() {
            super(mParentActivity, mMap, mClusterManager);

            View multiProfile = mParentActivity.getLayoutInflater().inflate(R.layout.multi_marker, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image_marker);
            mTextMarker = (TextView) multiProfile.findViewById(R.id.text_marker);

            View singleProfile = mParentActivity.getLayoutInflater().inflate(R.layout.single_item_marker, null);
            mIconGenerator.setContentView(singleProfile);
            mIconGenerator.setBackground(getResources().getDrawable(R.drawable.marker_custom_circle));
            mImageView = (ImageView) singleProfile.findViewById(R.id.single_image_marker);
            mSingleTextMarker = (TextView) singleProfile.findViewById(R.id.single_text_marker);

            mDimension = (int) getResources().getDimension(R.dimen.custom_marker_image);
        }

        @Override
        protected void onBeforeClusterItemRendered(GeoInfo geoInfo, MarkerOptions markerOptions) {
            if (geoInfo.getMiniature().size() > 0) {
                int randomPosition = mRandom.nextInt(geoInfo.getMiniature().size());
                mImageView.setImageBitmap(geoInfo.getMiniature().get(randomPosition));
                mSingleTextMarker.setText(Integer.toString(geoInfo.getCountFoto()));
                mSingleTextMarker.setVisibility(View.VISIBLE);
            } else {
                mImageView.setImageResource(R.drawable.ic_location_city_grey600_48dp);
                mSingleTextMarker.setVisibility(View.GONE);
            }
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<GeoInfo> cluster, MarkerOptions markerOptions) {
            List<Drawable> clusterPhotos = new ArrayList<>(Math.min(NUMERO_MINIATURE, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;
            int count = 0;

            for (GeoInfo p : cluster.getItems()) {
                count += p.getMiniature().size();
                for (Bitmap b : p.getMiniature()) {
                    /** Draw {@link NUMERO_MINIATURE} at most. */
                    if (clusterPhotos.size() == NUMERO_MINIATURE) break;
                    Drawable drawable = new BitmapDrawable(getResources(), b);
                    drawable.setBounds(0, 0, width, height);
                    clusterPhotos.add(drawable);
                }
            }
            MultiDrawable multiDrawable = new MultiDrawable(clusterPhotos);
            multiDrawable.setBounds(0, 0, width, height);

            if (count > 0) {
                mClusterImageView.setImageDrawable(multiDrawable);
                mSingleTextMarker.setVisibility(View.VISIBLE);
            } else {
                mClusterImageView.setImageResource(R.drawable.ic_location_city_grey600_48dp);
                mSingleTextMarker.setVisibility(View.GONE);
            }
            mTextMarker.setText(String.valueOf(cluster.getSize()));
            Bitmap icon = mClusterIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            return cluster.getSize() > 1;
        }
    }

    private class SingleItemAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            View view = mParentActivity.getLayoutInflater().inflate(R.layout.dialog_marker_single, null);
            TextView nome = (TextView) view.findViewById(R.id.dm_nome);
            TextView count = (TextView) view.findViewById(R.id.dm_count_foto);
            ImageView sfondo = (ImageView) view.findViewById(R.id.dm_foto);
            nome.setText(mClickedItem.getNome());
            count.setText(getResources().getString(R.string.map_count_foto, mClickedItem.getCountFoto()));
            if (mClickedItem.getMiniature().size() > 0) {
                int randomPosition = mRandom.nextInt(mClickedItem.getMiniature().size());
                sfondo.setImageBitmap(mClickedItem.getMiniature().get(randomPosition));
            } else {
                sfondo.setImageResource(R.drawable.noimg_small);
            }
            return view;
        }
    }

    public static class ClusterDialog extends DialogFragment {

        private List<GeoInfo> items;
        private int mType;
        private MapperContext mContext;
        private Random mRandom;

        public static ClusterDialog newInstance (List<GeoInfo> elenco, int type, MapperContext context) {
            ClusterDialog dialog = new ClusterDialog();
            dialog.items = elenco;
            dialog.mType = type;
            dialog.mContext = context;
            dialog.mRandom = new Random();
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ClusterSpinnerAdapter adapter = new ClusterSpinnerAdapter(getActivity(), R.layout.item_dialog_marker_multiple, items);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) { }
            });
            return builder.create();
        }

        /** Adapter per lo spinner dei cluster */
        private class ClusterSpinnerAdapter extends ArrayAdapter<GeoInfo> {

            private int mResource;

            public ClusterSpinnerAdapter(Context context, int resource, List<GeoInfo> objects) {
                super(context, resource, objects);
                mResource = resource;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final GeoInfo item = getItem(position);
                if (convertView == null)
                    convertView = LayoutInflater.from(getContext()).inflate(mResource, parent, false);
                ImageView foto = (ImageView) convertView.findViewById(R.id.dmm_immagine);
                TextView nome = (TextView) convertView.findViewById(R.id.dmm_titolo);
                nome.setText(item.getNome());
                if (item.getMiniature().size() > 0) {
                    int randomPosition = mRandom.nextInt(item.getMiniature().size());
                    foto.setImageBitmap(item.getMiniature().get(randomPosition));
                } else {
                    foto.setImageResource(R.drawable.ic_location_city_grey600_48dp);
                }
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        switch (mType) {
                            case CoordinateLoader.ELENCO_CITTA:
                                mContext.setIdCitta(item.getId());
                                mContext.setNomeCitta(item.getNome());
                                startActivity(new Intent(getActivity(), DettagliCittaActivity.class));
                                break;
                            case CoordinateLoader.ELENCO_POSTI:
                                mContext.setIdPosto(item.getId());
                                mContext.setNomePosto(item.getNome());
                                startActivity(new Intent(getActivity(), DettagliPostoActivity.class));
                                break;
                        }
                    }
                });
                return convertView;
            }
        }

    }

}
