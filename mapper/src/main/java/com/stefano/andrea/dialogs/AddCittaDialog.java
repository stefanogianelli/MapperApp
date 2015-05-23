package com.stefano.andrea.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.stefano.andrea.activities.ModInfoFotoActivity;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.adapters.PlaceAutocompleteAdapter;
import com.stefano.andrea.fragments.DettagliViaggioFragment;

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
        void creaNuovaCitta (String nomeCitta, String nazione, LatLng coordinates);
    }

    private AggiungiCittaCallback mCallback;
    private GoogleApiClient mGoogleApiClient;
    private Activity mParentActivity;
    private PlaceAutocompleteAdapter mAdapter;
    private ProgressBar mProgressBar;
    private ProgressDialog mAddCittaDialog;
    private ListView mSuggestions;

    private static final LatLngBounds PLACES_BOUND = new LatLngBounds(
            new LatLng(36.164943, -8.353179), new LatLng(71.437853, 37.086275));

    public static AddCittaDialog newInstance () {
        return new AddCittaDialog();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mParentActivity = activity;
        if (mParentActivity instanceof ModInfoFotoActivity)
            mCallback = (AggiungiCittaCallback) mParentActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient = new GoogleApiClient.Builder(mParentActivity)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }
        Fragment fragment = getTargetFragment();
        if (fragment instanceof DettagliViaggioFragment)
            mCallback = (AggiungiCittaCallback) fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_add_citta, container, false);
        view.setOnClickListener(null);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_add_citta_dialog);
        final EditText autocompleteView = (EditText) view.findViewById(R.id.autocomplete_citta);
        final ImageView clearButton = (ImageView) view.findViewById(R.id.clearable_button_clear);
        mSuggestions = (ListView) view.findViewById(R.id.autocomplete_suggestions);
        mProgressBar = (ProgressBar) view.findViewById(R.id.toolbar_progress_bar);
        mAddCittaDialog = new ProgressDialog(mParentActivity);
        mAddCittaDialog.setMessage("Aggiungo città");
        toolbar.setTitle(getString(R.string.aggiungi_citta));
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                dismiss();
            }
        });
        List<Integer> placesTypes = new ArrayList<>();
        placesTypes.add(Place.TYPE_GEOCODE);
        AutocompleteFilter filter = AutocompleteFilter.create(placesTypes);
        mAdapter = new PlaceAutocompleteAdapter(mParentActivity, R.layout.item_autocomplete_citta,
                mGoogleApiClient, PLACES_BOUND, filter, new PlaceAutocompleteAdapter.OnLoadComplete() {
            @Override
            public void onLoadComplete() {
                mProgressBar.setVisibility(View.GONE);
                TextView noRes = (TextView) view.findViewById(R.id.no_risultati);
                if (mAdapter.getCount() == 0) {
                    noRes.setVisibility(View.VISIBLE);
                } else {
                    noRes.setVisibility(View.GONE);
                }
            }
        });
        mSuggestions.setAdapter(mAdapter);
        mSuggestions.setOnItemClickListener(mAutocompleteClickListener);
        autocompleteView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                if (!query.isEmpty()) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mAdapter.getFilter().filter(query);
                    clearButton.setEnabled(true);
                } else {
                    mAdapter.clear();
                    clearButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        autocompleteView.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            hideKeyboard();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
        autocompleteView.requestFocus();
        InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autocompleteView.setText("");
            }
        });
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //TODO: tradurre e portare nelle strings
        Toast.makeText(mParentActivity,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
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
            mSuggestions.setFocusable(false);
            mSuggestions.setEnabled(false);
            mAddCittaDialog.show();
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
             read the place ID.
              */
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
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
            String placeName = place.getName().toString();
            String address = place.getAddress().toString().replace(" ", "");
            String [] addresses = address.split(",");
            String nazione = addresses[addresses.length - 1];
            LatLng placeCoordinates = place.getLatLng();

            mCallback.creaNuovaCitta(placeName, nazione, placeCoordinates);

            places.release();
            hideKeyboard();
            mAddCittaDialog.dismiss();
            dismiss();
        }
    };

    public void hideKeyboard () {
        InputMethodManager inputMethodManager = (InputMethodManager) mParentActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = mParentActivity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if(view == null) {
            view = new View(mParentActivity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
