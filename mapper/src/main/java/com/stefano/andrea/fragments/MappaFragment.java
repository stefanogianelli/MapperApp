package com.stefano.andrea.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

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
import com.google.maps.android.ui.IconGenerator;
import com.stefano.andrea.activities.DettagliCittaActivity;
import com.stefano.andrea.activities.DettagliPostoActivity;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.loaders.CoordinateLoader;
import com.stefano.andrea.models.GeoInfo;
import com.stefano.andrea.utils.MapperContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MappaFragment
 */
public class MappaFragment extends SupportMapFragment implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<List<GeoInfo>>, GoogleMap.OnMarkerClickListener {

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
                Marker marker = mMap.addMarker(createMarker(item));
                mMarkerDetail.put(marker, item);
                builder.include(marker.getPosition());
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

    private MarkerOptions createMarker (GeoInfo item) {
        String title = mParentActivity.getResources().getString(R.string.map_bubble_title, item.getNome(), item.getCountFoto());
        return new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(mIconGenerator.makeIcon(title)))
                        .position(new LatLng(item.getLatitudine(), item.getLongitudine()));
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
}
