package com.stefano.andrea.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.models.Foto;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DialogsHelper
 */
public class DialogHelper {

    private static final String TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm";

    /**
     * Mostra un alert dialog standard, con solo il pulsante OK
     * @param context Il contesto dell'applicazione
     * @param titleId L'id del titolo
     * @param messageId L'id del messaggio
     */
    public static void showAlertDialog (Context context, int titleId, int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleId);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * Callback invocata dal dialog della creazione del viaggio
     */
    public interface ViaggioDialogCallback {
        void viaggioActionButton(int position, long id, String nomeViaggio);
    }

    /**
     * Mostra il dialog per l'aggiunta di un nuovo viaggio
     * @param activity L'acitivity corrente
     * @param callback L'implementazione della callback di creazione del viaggio
     */
    public static void showViaggioDialog(Activity activity, final int position, final long idViaggio, final String nome, final ViaggioDialogCallback callback) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_add_viaggio, null);
        builder.setView(v);
        final EditText nomeViaggio = (EditText) v.findViewById(R.id.text_add_viaggio);
        if (nome != null && !nome.isEmpty())
            nomeViaggio.setText(nome);
        // Add action buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                onViaggioPositiveButtonClick(position, idViaggio, nome, nomeViaggio, callback);
                dialog.dismiss();
            }
        })
        .setNegativeButton(R.string.cancel, null);
        final AlertDialog dialog = builder.create();
        nomeViaggio.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence c, int i, int i2, int i3) {}
            @Override public void onTextChanged(CharSequence c, int i, int i2, int i3) {}
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 0){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        //  Setto il tasto INVIO (solo se il testo non è vuoto)
        nomeViaggio.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (nomeViaggio.getText().toString().length() != 0)) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            onViaggioPositiveButtonClick(position, idViaggio, nome, nomeViaggio, callback);
                            dialog.dismiss();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private static void onViaggioPositiveButtonClick (int position, long id, String nome, EditText nomeViaggio, ViaggioDialogCallback callback) {
        String input = nomeViaggio.getText().toString();
        if (nome != null && !nome.isEmpty()) {
            if (!nome.equals(input)) {
                callback.viaggioActionButton(position, id, input);
            }
        } else {
            callback.viaggioActionButton(position, id, input);
        }
    }

    /**
     * Callback invocata dal list dialog
     */
    public interface ListDialogCallback {
        void onItemClick (int position);
    }

    /**
     * Mostra un elenco in un dialog. Permette una singola scelta
     * @param activity L'activity corrente
     * @param titolo L'id del titolo
     * @param adapter L'adapter che genera la lista da mostrare
     * @param callback L'implementazione dell'azione associata al dialog
     */
    public static void showListDialog (Activity activity, int titolo, ArrayAdapter adapter, final ListDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(titolo);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onItemClick(which);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @SuppressLint("SimpleDateFormat")
    public static void showDettagliFotoDialog (Activity activity, Foto foto) {
       final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_dettagli_foto, null);
        TextView percorso = (TextView) v.findViewById(R.id.df_testo_percorso);
        TextView formato = (TextView) v.findViewById(R.id.df_testo_formato);
        TextView dimensione = (TextView) v.findViewById(R.id.df_testo_dimensione);
        TextView risoluzione = (TextView) v.findViewById(R.id.df_testo_risoluzione);
        TextView fotocamera = (TextView) v.findViewById(R.id.df_testo_fotocamera);
        TextView exif = (TextView) v.findViewById(R.id.df_testo_exif);
        TextView data = (TextView) v.findViewById(R.id.df_testo_data);
        TextView indirizzo = (TextView) v.findViewById(R.id.df_testo_indirizzo);
        TextView btnClose = (TextView) v.findViewById(R.id.btn_closeDettagliFoto);

        percorso.setText(foto.getPath().substring(7));
        formato.setText(foto.getMimeType());
        String dimensioneSI = formatByte(foto.getSize(), true);
        dimensione.setText(dimensioneSI);
        risoluzione.setText(foto.getWidth() + "x" + foto.getHeight());
        fotocamera.setText(foto.getModel());
        exif.setText(foto.getExif());
        data.setText(new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date(foto.getData())));
        indirizzo.setText("indirizzo");

        builder.setView(v);
        final AlertDialog dialog = builder.create();

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static String formatByte(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

}
