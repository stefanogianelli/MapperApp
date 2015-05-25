package com.stefano.andrea.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.stefano.andrea.activities.R;

/**
 * LocationManagerCheck
 */
public class LocationManagerCheck {

    private LocationManager mLocationManager;
    private Activity mActivity;

    public LocationManagerCheck(Activity activity) {
        mActivity = activity;
        mLocationManager = (LocationManager) activity.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }

    public Boolean isLocationServiceAvailable() {
        boolean locationServiceBoolean = false;
        boolean gpsIsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkIsEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (networkIsEnabled && gpsIsEnabled) {
            locationServiceBoolean = true;
        } else if (!networkIsEnabled && gpsIsEnabled) {
            locationServiceBoolean = true;
        } else if (networkIsEnabled && !gpsIsEnabled) {
            locationServiceBoolean = true;
        }
        return locationServiceBoolean;
    }

    public void createLocationServiceError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setMessage(
                mActivity.getString(R.string.no_localizzazione_messaggio))
                .setTitle(mActivity.getString(R.string.no_localizzazione_titolo))
                .setCancelable(false)
                .setPositiveButton(mActivity.getString(R.string.action_settings),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                mActivity.startActivity(intent);
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(mActivity.getString(R.string.annulla), null);
        builder.create().show();
    }

}