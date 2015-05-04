package com.stefano.andrea.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.stefano.andrea.activities.R;

/**
 * DialogsHelper
 */
public class DialogHelper {

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
    public interface AggiungiViaggioCallback {
        void creaViaggio(String nomeViaggio);
    }

    /**
     * Mostra il dialog per l'aggiunta di un nuovo viaggio
     * @param activity L'acitivity corrente
     * @param callback L'implementazione della callback di creazione del viaggio
     */
    public static void showDialogAggiungiViaggio(Activity activity, final AggiungiViaggioCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.fragment_add_viaggio, null))
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog d = (Dialog) dialog;
                        EditText nomeViaggio = (EditText) d.findViewById(R.id.text_add_viaggio);
                        callback.creaViaggio(nomeViaggio.getText().toString());
                        d.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel,  null);
        builder.create().show();
    }

    /**
     * Callback invocata dal dialog aggiungi citta
     */
    public interface AggiungiCittaCallback {
        void creaNuovaCitta (String nomeCitta, String nomeNazione);
    }

    /**
     * Mostra il dialog per l'aggiunta di una nuova citta
     * @param activity L'activity corrente
     * @param callback L'implementazione della callback per la creazione della citta
     */
    public static void showDialogAggiungiCitta (Activity activity, final AggiungiCittaCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.fragment_add_citta, null))
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog d = (Dialog) dialog;
                        EditText nomeNazione = (EditText) d.findViewById(R.id.text_add_citta_nn);
                        EditText nomeCitta = (EditText) d.findViewById(R.id.text_add_citta);
                        callback.creaNuovaCitta(nomeCitta.getText().toString(), nomeNazione.getText().toString());
                        d.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    /**
     * Callback invocata dal dialog aggiungi posto
     */
    public interface AggiungiPostoCallback {
        void creaNuovoPosto (String nomePosto);
    }

    /**
     * Mostra il dialog per l'aggiunta di un posto
     * @param activity L'activity corrente
     * @param callback L'implementazione della callback per l'aggiunta di un posto
     */
    public static void showDialogAggiungiPosto(Activity activity, final AggiungiPostoCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.fragment_add_posto, null))
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog d = (Dialog) dialog;
                        EditText nomePosto = (EditText) d.findViewById(R.id.text_add_posto);
                        callback.creaNuovoPosto(nomePosto.getText().toString());
                        d.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        builder.create().show();
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

}
