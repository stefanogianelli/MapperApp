package com.stefano.andrea.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.stefano.andrea.activities.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Asynchronously handles an intent using a worker thread. Receives a ResultReceiver object and a
 * location through an intent. Tries to fetch the address for the location using a Geocoder, and
 * sends the result to the ResultReceiver.
 */
public class FetchAddressIntentService extends IntentService {

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.stefano.andrea.mapper";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY  = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String RESULT_COUNTRY = PACKAGE_NAME + ".RESULT_COUNTRY";
    public static final String RESULT_COORDINATES = PACKAGE_NAME + ".RESULT_COORDINATES";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    private static final int TIMEOUT = 30000;

    private static final String TAG = "FetchAddressIntent";

    /**
     * The receiver where results are forwarded from this service.
     */
    protected ResultReceiver mReceiver;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public FetchAddressIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    /**
     * Tries to get the location address using a Geocoder. If successful, sends an address to a
     * result receiver. If unsuccessful, sends an error message instead.
     * Note: We define a {@link android.os.ResultReceiver} in * MainActivity to process content
     * sent from this service.
     *
     * This service calls this method from the default worker thread with the intent that started
     * the service. When this method returns, the service automatically stops.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";

        mReceiver = intent.getParcelableExtra(RECEIVER);

        // Check if receiver was properly registered.
        if (mReceiver == null) {
            Log.wtf(TAG, "No receiver received. There is nowhere to send the results.");
            return;
        }

        //set timeout
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                deliverResultToReceiver(getString(R.string.service_not_available));
                stopSelf();
            }
        }, TIMEOUT);

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(LOCATION_DATA_EXTRA);

        // Make sure that the location data was really sent over through an extra. If it wasn't,
        // send an error error message and return.
        if (location == null) {
            errorMessage = getString(R.string.no_location_data_provided);
            Log.wtf(TAG, errorMessage);
            deliverResultToReceiver(errorMessage);
            return;
        }

        // Errors could still arise from using the Geocoder (for example, if there is no
        // connectivity, or if the Geocoder is given illegal location data). Or, the Geocoder may
        // simply not have an address for a location. In all these cases, we communicate with the
        // receiver using a resultCode indicating failure. If an address is found, we use a
        // resultCode indicating success.

        // The Geocoder used in this sample. The Geocoder's responses are localized for the given
        // Locale, which represents a specific geographical or linguistic region. Locales are used
        // to alter the presentation of information such as numbers or dates to suit the conventions
        // in the region they describe.
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Address found using the Geocoder.
        List<Address> addresses = null;

        try {
            // Using getFromLocation() returns an array of Addresses for the area immediately
            // surrounding the given latitude and longitude. The results are a best guess and are
            // not guaranteed to be accurate.
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, we get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " + location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(errorMessage);
        } else {
            Address address = addresses.get(0);
            // Fetch the address lines using {@code getAddressLine},
            // join them, and send them to the thread. The {@link android.location.address}
            // class provides other options for fetching address details that you may prefer
            // to use. Here are some examples:
            // getLocality() ("Mountain View", for example)
            // getAdminArea() ("CA", for example)
            // getPostalCode() ("94043", for example)
            // getCountryCode() ("US", for example)
            // getCountryName() ("United States", for example)
            String locality = address.getLocality();
            String country = address.getCountryName();
            try {
                List<Address> citiesInfo = geocoder.getFromLocationName(address.getLocality() + "," + address.getCountryName(), 1);
                if (citiesInfo.size() > 0) {
                    Address cityInfo = citiesInfo.get(0);
                    LatLng coordinates = new LatLng(cityInfo.getLatitude(), cityInfo.getLongitude());
                    if (locality != null && country != null) {
                        deliverResultToReceiver(address.getLocality(), address.getCountryName(), coordinates);
                        return;
                    } else {
                        errorMessage = getString(R.string.no_address_found);
                    }
                } else
                    errorMessage = getString(R.string.no_address_found);
            } catch (IOException e) {
                errorMessage = getString(R.string.service_not_available);
                Log.e(TAG, errorMessage, e);
            }
            if (!errorMessage.isEmpty())
                deliverResultToReceiver(errorMessage);
        }
    }

    /**
     * Sends a resultCode and message to the receiver.
     */
    private void deliverResultToReceiver(String message) {
        Bundle bundle = new Bundle();
        bundle.putString(RESULT_DATA_KEY, message);
        mReceiver.send(FetchAddressIntentService.FAILURE_RESULT, bundle);
    }

    /**
     * Sends a resultCode and two messages to the receiver.
     */
    private void deliverResultToReceiver(String city, String country, LatLng coordinates) {
        Bundle bundle = new Bundle();
        bundle.putString(RESULT_DATA_KEY, city);
        bundle.putString(RESULT_COUNTRY, country);
        bundle.putParcelable(RESULT_COORDINATES, coordinates);
        mReceiver.send(FetchAddressIntentService.SUCCESS_RESULT, bundle);
    }

}