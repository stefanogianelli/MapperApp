package com.stefano.andrea.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_add_viaggio, null);
        builder.setView(v);

        final EditText nomeViaggio = (EditText) v.findViewById(R.id.text_add_viaggio);
        // Add action buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                callback.creaViaggio(nomeViaggio.getText().toString());
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
                            callback.creaViaggio(nomeViaggio.getText().toString());
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_add_citta, null);
        builder.setView(v);

        final EditText nomeCitta = (EditText) v.findViewById(R.id.text_add_citta);

        // Add action buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog d = (Dialog) dialog;
                        EditText nomeNazione = (EditText) d.findViewById(R.id.text_add_citta_nn);
                        callback.creaNuovaCitta(nomeCitta.getText().toString(), nomeNazione.getText().toString());
                        dialog.dismiss();
                    }
                })
        .setNegativeButton(R.string.cancel, null);

        final AlertDialog dialog = builder.create();

        nomeCitta.addTextChangedListener(new TextWatcher() {
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
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_add_posto, null);
        builder.setView(v);

        final EditText nomePosto = (EditText) v.findViewById(R.id.text_add_posto);
            // Add action buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    Dialog d = (Dialog) dialog;
                    callback.creaNuovoPosto(nomePosto.getText().toString());
                    d.dismiss();
                }
                })
            .setNegativeButton(R.string.cancel, null);

        final AlertDialog dialog = builder.create();

        nomePosto.addTextChangedListener(new TextWatcher() {
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
        nomePosto.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (nomePosto.getText().toString().length()!=0))
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            callback.creaNuovoPosto(nomePosto.getText().toString());
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
