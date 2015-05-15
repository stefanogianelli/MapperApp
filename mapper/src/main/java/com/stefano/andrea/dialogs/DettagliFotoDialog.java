package com.stefano.andrea.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.utils.FetchAddressIntentService;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DettagliFotoDialog
 */
public class DettagliFotoDialog extends DialogFragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "DettagliFotoDialog";

    private static final String TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm";
    private static final String EXTRA_FOTO = "com.stefano.andrea.dialogs.DettagliFotoDialog.foto";

    private Activity mParentActivity;
    private GoogleApiClient mGoogleApiClient;
    private Foto mFoto;
    private AddressResultReceiver mResultReceiver;
    private Location mLocation;
    private TextView mIndirizzoView;

    public static DettagliFotoDialog newInstance (Foto foto) {
        DettagliFotoDialog dialog = new DettagliFotoDialog();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_FOTO, foto);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mParentActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null)
            mFoto = args.getParcelable(EXTRA_FOTO);
        mLocation = new Location("Mapper");
        mLocation.setLatitude(mFoto.getLatitudine());
        mLocation.setLongitude(mFoto.getLongitudine());
        mGoogleApiClient = new GoogleApiClient.Builder(mParentActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mResultReceiver = new AddressResultReceiver(new Handler());
    }

    @Nullable
    @Override
    @SuppressLint("SimpleDateFormat")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_dettagli_foto, container, false);
        view.setOnClickListener(null);
        LinearLayout fotoCamera = (LinearLayout) view.findViewById(R.id.df_container_fotocamera);
        LinearLayout fotoExif = (LinearLayout) view.findViewById(R.id.df_container_exif);
        TextView percorso = (TextView) view.findViewById(R.id.df_testo_percorso);
        TextView formato = (TextView) view.findViewById(R.id.df_testo_formato);
        TextView dimensione = (TextView) view.findViewById(R.id.df_testo_dimensione);
        TextView risoluzione = (TextView) view.findViewById(R.id.df_testo_risoluzione);
        TextView fotocamera = (TextView) view.findViewById(R.id.df_testo_fotocamera);
        TextView exif = (TextView) view.findViewById(R.id.df_testo_exif);
        TextView data = (TextView) view.findViewById(R.id.df_testo_data);
        mIndirizzoView = (TextView) view.findViewById(R.id.df_testo_indirizzo);
        TextView btnClose = (TextView) view.findViewById(R.id.btn_closeDettagliFoto);

        percorso.setText(mFoto.getPath().substring(7));
        formato.setText(mFoto.getMimeType());
        String dimensioneSI = formatByte(mFoto.getSize(), true);
        dimensione.setText(dimensioneSI);
        risoluzione.setText(mFoto.getWidth() + "x" + mFoto.getHeight());
        if (mFoto.getModel() == null || mFoto.getModel().isEmpty()) {
            fotoCamera.setVisibility(View.GONE);
        } else {
            fotocamera.setText(mFoto.getModel());
        }
        if (mFoto.getExif().contains("null")) {
            fotoExif.setVisibility(View.GONE);
        } else {
            exif.setText(mFoto.getExif());
        }
        data.setText(new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date(mFoto.getData())));
        mIndirizzoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri maps = Uri.parse("geo:0,0?q=" + mFoto.getLatitudine() + "," + mFoto.getLongitudine() + "(Posto)");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(maps);
                if (intent.resolveActivity(mParentActivity.getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
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

    public String formatByte(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Intent intent = new Intent(mParentActivity, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.RECEIVER, mResultReceiver);
        intent.putExtra(FetchAddressIntentService.LOCATION_DATA_EXTRA, mLocation);
        mParentActivity.startService(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            String indirizzo = resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY);
            if (resultData.containsKey(FetchAddressIntentService.RESULT_COUNTRY)) {
                indirizzo += ", " + resultData.getString(FetchAddressIntentService.RESULT_COUNTRY);
            }
            mIndirizzoView.setText(indirizzo);
        }
    }

}
