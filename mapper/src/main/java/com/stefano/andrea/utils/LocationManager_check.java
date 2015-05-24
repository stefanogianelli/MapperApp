package com.stefano.andrea.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

/**
 * LocationManager_check
 */
public class LocationManager_check {

    private Boolean locationServiceBoolean = false;
    private static AlertDialog alert;

    public LocationManager_check(Context context) {
        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        boolean gpsIsEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkIsEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (networkIsEnabled && gpsIsEnabled) {
            locationServiceBoolean = true;

        } else if (!networkIsEnabled && gpsIsEnabled) {
            locationServiceBoolean = true;

        } else if (networkIsEnabled && !gpsIsEnabled) {
            locationServiceBoolean = true;
        }

    }

    public Boolean isLocationServiceAvailable() {
        return locationServiceBoolean;
    }

    public void createLocationServiceError(final Activity activityObj) {

        // show alert dialog if Internet is not connected
        AlertDialog.Builder builder = new AlertDialog.Builder(activityObj);

        //TODO: portare nelle stringhe
        builder.setMessage(
                "Devi abilitare la localizzazione per usare questa funzione. Attivala dalle impostazioni.")
                .setTitle("Usare la posizione?")
                .setCancelable(false)
                .setPositiveButton("Impostazioni",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                activityObj.startActivity(intent);
                                alert.dismiss();
                            }
                        })
                .setNegativeButton("Annulla",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                alert.dismiss();
                            }
                        });
        alert = builder.create();
        alert.show();
    }

}