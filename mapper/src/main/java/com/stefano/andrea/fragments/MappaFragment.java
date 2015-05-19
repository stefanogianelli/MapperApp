package com.stefano.andrea.fragments;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.loaders.CoordinateLoader;
import com.stefano.andrea.models.GeoInfo;
import com.stefano.andrea.utils.MapperContext;
import com.stefano.andrea.utils.MultiDrawable;

import java.util.ArrayList;
import java.util.List;

/**
 * MappaFragment
 */
public class MappaFragment extends SupportMapFragment implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<List<GeoInfo>>, ClusterManager.OnClusterClickListener<GeoInfo>,
        ClusterManager.OnClusterInfoWindowClickListener<GeoInfo>, ClusterManager.OnClusterItemClickListener<GeoInfo>, ClusterManager.OnClusterItemInfoWindowClickListener<GeoInfo> {

    public static final String EXTRA_TIPO_MAPPA = "com.stefano.andrea.fragments.MappaFragment.tipoMappa";
    public static final int MAPPA_CITTA = 0;
    public static final int MAPPA_POSTI = 1;

    private static final int MAP_COORD_LOADER = 4;
    private static final int TIMEOUT = 100;

    private static final float MIN_ZOOM_POSTI = 13f;
    private static final float MIN_ZOOM_CITTA = 11f;
    private static final int NUMERO_MINIATURE = 4;

    private GoogleMap mMap;
    private List<GeoInfo> mMarkerData;
    private Activity mParentActivity;
    private MapperContext mContext;
    private long mId;
    private int mType;
    private ClusterManager<GeoInfo> mClusterManager;

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
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);
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
            LatLngBounds bounds = builder.build();
            float zoom;
            if (mType == CoordinateLoader.ELENCO_CITTA)
                zoom = MIN_ZOOM_CITTA;
            else
                zoom = MIN_ZOOM_POSTI;
            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), zoom);
            mMap.moveCamera(cu);
        }
    }

    /*@Override
    public boolean onMarkerClick(Marker marker) {
        GeoInfo item = mMarkerDetail.get(marker);
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
        return false;
    }*/

    @Override
    public boolean onClusterClick(Cluster<GeoInfo> cluster) {
        long id = cluster.getItems().iterator().next().getId();
        String nome = cluster.getItems().iterator().next().getNome();
        Toast.makeText(mParentActivity, nome + " (" + id + ")\nFoto: " + cluster.getSize(), Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<GeoInfo> cluster) {
        // Does nothing, but you could go to a list of the users.
        Toast.makeText(mParentActivity, "onClusterInfoWindowsClick", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onClusterItemClick(GeoInfo geoInfo) {
        // Does nothing, but you could go into the user's profile page, for example.
        Toast.makeText(mParentActivity, "onClusterItemClick", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(GeoInfo geoInfo) {
        // Does nothing, but you could go into the user's profile page, for example.
        Toast.makeText(mParentActivity, "onClusterItemInfoWindowClick", Toast.LENGTH_SHORT).show();
    }

    private class MarkerRenderer extends DefaultClusterRenderer<GeoInfo> {
        private final IconGenerator mIconGenerator = new IconGenerator(mParentActivity);
        private final IconGenerator mClusterIconGenerator = new IconGenerator(mParentActivity);
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final TextView mTextMarker;
        private final int mDimension;

        public MarkerRenderer() {
            super(mParentActivity, mMap, mClusterManager);

            View multiProfile = mParentActivity.getLayoutInflater().inflate(R.layout.multi_marker, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image_marker);
            mTextMarker = (TextView) multiProfile.findViewById(R.id.text_marker);

            mImageView = new ImageView(mParentActivity);
            mDimension = (int) getResources().getDimension(R.dimen.custom_marker_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_marker_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(GeoInfo geoInfo, MarkerOptions markerOptions) {
            mImageView.setImageBitmap(geoInfo.getMiniatura());
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(geoInfo.getNome());
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<GeoInfo> cluster, MarkerOptions markerOptions) {
            List<Drawable> clusterPhotos = new ArrayList<>(Math.min(NUMERO_MINIATURE, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (GeoInfo p : cluster.getItems()) {
                /** Draw {@link NUMERO_MINIATURE} at most. */
                if (clusterPhotos.size() == NUMERO_MINIATURE) break;
                Drawable drawable = new BitmapDrawable(getResources(), p.getMiniatura());
                drawable.setBounds(0, 0, width, height);
                clusterPhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(clusterPhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            mTextMarker.setText(String.valueOf(cluster.getSize()));
            Bitmap icon = mClusterIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }






}
