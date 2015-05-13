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

    public static void showDettagliFotoDialog (Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_dettagli_foto, null);
        builder.setView(v);
        builder.create().show();
    }

}
