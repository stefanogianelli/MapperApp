package com.stefano.andrea.fragments;


import android.app.Activity;
import android.content.Intent;
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
import android.widget.Toast;

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
import com.stefano.andrea.utils.MapperContext;
import com.stefano.andrea.utils.MultiDrawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MappaFragment
 */
public class MappaFragment extends SupportMapFragment implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<List<GeoInfo>>, GoogleMap.OnMarkerClickListener, ClusterManager.OnClusterClickListener {

    public static final String EXTRA_TIPO_MAPPA = "com.stefano.andrea.fragments.MappaFragment.tipoMappa";
    public static final int MAPPA_CITTA = 0;
    public static final int MAPPA_POSTI = 1;

    private static final int MAP_COORD_LOADER = 4;
    private static final int MAP_PADDING = 150;
    private static final int TIMEOUT = 100;
    private static final double EARTHRADIUS = 6366198;
    private static final int DISTANCE_CITTA = 5000;
    private static final int DISTANCE_LUOGO = 500;

    private GoogleMap mMap;
    private List<GeoInfo> mMarkerData;
    private Map<Marker, GeoInfo> mMarkerDetail;
    private Activity mParentActivity;
    private MapperContext mContext;
    private long mId;
    private int mType;
    private IconGenerator mIconGenerator;
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
        mIconGenerator = new IconGenerator(mParentActivity);
        mIconGenerator.setStyle(IconGenerator.STYLE_ORANGE);
        mMarkerDetail = new HashMap<>();
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

        mClusterManager = new ClusterManager<GeoInfo>(mParentActivity, mMap);
        mClusterManager.setRenderer(new MarkerRenderer());
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        /*mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);*/

        mMap.setOnMarkerClickListener(this);
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
                //Marker marker = mMap.addMarker(createMarker(item));
                mClusterManager.addItem(item);
                //mMarkerDetail.put(marker, item);
                //builder.include(marker.getPosition());
            }
            /*if (mMarkerData.size() == 1) {
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
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, MAP_PADDING);*/
            CameraUpdate cu = CameraUpdateFactory.newLatLng(new LatLng(51.503186, -0.126446));
            mMap.moveCamera(cu);
        }
    }

    /*private MarkerOptions createMarker (GeoInfo item) {
        String title = mParentActivity.getResources().getString(R.string.map_bubble_title, item.getNome(), item.getCountFoto());
        return new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(mIconGenerator.makeIcon(title)))
                        .position(new LatLng(item.getLatitudine(), item.getLongitudine()));
    }*/

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
    }

    @Override
    public boolean onClusterClick(Cluster cluster) {
        String firstName = cluster.getItems().iterator().next().toString();
        Toast.makeText(mParentActivity, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();
        return true;
    }


    private class MarkerRenderer extends DefaultClusterRenderer<GeoInfo> {
        private final IconGenerator mIconGenerator = new IconGenerator(mParentActivity.getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(mParentActivity.getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public MarkerRenderer() {
            super(mParentActivity.getApplicationContext(), mMap, mClusterManager);

            View multiProfile = mParentActivity.getLayoutInflater().inflate(R.layout.multi_marker, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image_marker);

            mImageView = new ImageView(mParentActivity.getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_marker_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_marker_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(GeoInfo geoInfo, MarkerOptions markerOptions) {

            mImageView.setImageBitmap(geoInfo.getMiniatura());
            //mImageView.setImageResource((R.drawable.ic_location_city_grey600_48dp));
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(geoInfo.getNome());
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<GeoInfo> cluster, MarkerOptions markerOptions) {

            List<Drawable> clusterPhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (GeoInfo p : cluster.getItems()) {
                // Draw 4 at most.
                if (clusterPhotos.size() == 4) break;
                Drawable drawable = new BitmapDrawable(getResources(), p.getMiniatura());
                //Drawable drawable =  getResources().getDrawable(R.drawable.ic_location_city_grey600_48dp);
                drawable.setBounds(0, 0, width, height);
                clusterPhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(clusterPhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }






}
