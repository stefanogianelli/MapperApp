package com.stefano.andrea.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.adapters.PlaceAutocompleteAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * AddCittaDialog
 */
public class AddCittaDialog extends DialogFragment implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "AddCittaDialog";

    /**
     * Callback invocata dal dialog aggiungi citta
     */
    public interface AggiungiCittaCallback {
        void creaNuovaCitta (String nomeCitta, String indirizzo, LatLng coordinates);
    }

    private AggiungiCittaCallback mCallback;
    private GoogleApiClient mGoogleApiClient;
    private Activity mParentActivity;
    private PlaceAutocompleteAdapter mAdapter;
    private String mPlaceName;
    private String mPlaceAddress;
    private LatLng mPlaceCoordinates;

    private static final LatLngBounds PLACES_BOUND = new LatLngBounds(
            new LatLng(36.164943, -8.353179), new LatLng(71.437853, 37.086275));

    public static AddCittaDialog newInstance () {
        return new AddCittaDialog();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mParentActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient = new GoogleApiClient.Builder(mParentActivity)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivity);
        LayoutInflater inflater = mParentActivity.getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_add_citta, null);
        builder.setView(v);

        final EditText nomeCitta = (EditText) v.findViewById(R.id.text_add_citta);
        AutoCompleteTextView autocompleteView = (AutoCompleteTextView) v.findViewById(R.id.autocomplete_citta);
        // Register a listener that receives callbacks when a suggestion has been selected
        autocompleteView.setOnItemClickListener(mAutocompleteClickListener);
        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
        List<Integer> placesTypes = new ArrayList<>();
        placesTypes.add(Place.TYPE_GEOCODE);
        AutocompleteFilter filter = AutocompleteFilter.create(placesTypes);
        mAdapter = new PlaceAutocompleteAdapter(mParentActivity, R.layout.item_autocomplete,
                mGoogleApiClient, PLACES_BOUND, filter);
        autocompleteView.setAdapter(mAdapter);

        // Add action buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Dialog d = (Dialog) dialog;
                EditText nomeNazione = (EditText) d.findViewById(R.id.text_add_citta_nn);
                //mCallback.creaNuovaCitta(nomeCitta.getText().toString(), nomeNazione.getText().toString());
                mCallback.creaNuovaCitta(mPlaceName, mPlaceAddress, mPlaceCoordinates);
                dialog.dismiss();
            }
        })
                .setNegativeButton(R.string.cancel, null);

        final AlertDialog dialog = builder.create();

        nomeCitta.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence c, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence c, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 0) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
        //dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(mParentActivity,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    public void setCallback (AggiungiCittaCallback callback) {
        mCallback = callback;
    }

    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
     * String...)
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
             read the place ID.
              */
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(TAG, "Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Log.i(TAG, "Called getPlaceById to get Place details for " + item.placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);
            mPlaceName = place.getName().toString();
            mPlaceAddress = place.getAddress().toString();
            mPlaceCoordinates = place.getLatLng();

            Log.i(TAG, "Place details received: " + place.getName());

            places.release();
        }
    };
}
