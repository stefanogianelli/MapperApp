package com.stefano.andrea.providers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * MapperDatabase
 */
public class MapperDatabase extends SQLiteOpenHelper {

    protected interface Tables {
        String VIAGGIO = "viaggio";
        String CITTA = "citta";
        String POSTO = "posto";
        String DATI_CITTA = "dati_citta";
        String LUOGO = "luogo";
        String FOTO = "foto";

        String CITTA_JOIN_DATI_CITTA = CITTA + " JOIN " + DATI_CITTA + " ON " + CITTA + "." + MapperContract.Citta.ID_DATI_CITTA +  "=" + DATI_CITTA + "." + MapperContract.DatiCitta.ID;
        String POSTO_JOIN_DATI_POSTO = POSTO + " JOIN " + LUOGO + " ON " + POSTO + "." + MapperContract.Posto.ID_LUOGO + "=" + LUOGO + "." + MapperContract.Luogo.ID;
    }

    private interface Triggers {
        String ELIMINA_CITTA_IN_VIAGGIO = "citta_in_viaggio_delete";
        String ELIMINA_POSTI_IN_CITTA = "posti_in_citta_delete";
        String ELIMINA_FOTO_IN_CITTA = "foto_in_citta_delete";
        String ELIMINA_FOTO_IN_POSTO = "fot_in_posto_delete";
        String INCREMENTA_COUNT_CITTA = "incrementa_count_citta";
        String DECREMENTA_COUNT_CITTA = "decrementa_count_citta";
        String INCREMENTA_COUNT_POSTI = "incrementa_count_posti";
        String DECREMENTA_COUNT_POSTI = "decrementa_count_posti";
        String ELIMINA_DATI_CITTA = "elimina_dati_citta";
        String ELIMINA_LUOGO = "elimina_luogo";
    }

    private static final String DATABASE_NAME = "mapper_db";
    private static final int DATABASE_VERSION = 1;

    //Create table VIAGGIO
    private static final String CREATE_VIAGGIO = "CREATE TABLE \"" + Tables.VIAGGIO + "\" (" +
            "`" + MapperContract.Viaggio.ID_VIAGGIO +"` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`" + MapperContract.Viaggio.NOME + "` TEXT NOT NULL," +
            "`" + MapperContract.Viaggio.COUNT_CITTA + "` INTEGER DEFAULT 0," +
            "`" + MapperContract.Viaggio.COUNT_POSTI + "` INTEGER DEFAULT 0," +
            "`" + MapperContract.Viaggio.PATH_FOTO + "` TEXT DEFAULT null);";

    //Create table CITTA
    private static final String CREATE_CITTA = "CREATE TABLE \"" + Tables.CITTA + "\" (" +
            "`" + MapperContract.Citta.ID_CITTA + "` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`" + MapperContract.Citta.ID_DATI_CITTA + "` INTEGER NOT NULL," +
            "`" + MapperContract.Citta.ID_VIAGGIO + "` INTEGER NOT NULL," +
            "`" + MapperContract.Citta.PERCENTUALE + "` REAL DEFAULT 0," +
            "`" + MapperContract.Citta.COUNT_POSTI + "` INTEGER DEFAULT 0," +
            "`" + MapperContract.Citta.POSTI_VISITATI + "` INTEGER DEFAULT 0," +
            " FOREIGN KEY(`" + MapperContract.Citta.ID_DATI_CITTA + "`) REFERENCES " + Tables.DATI_CITTA + " (`" + MapperContract.DatiCitta.ID + "`)," +
            " FOREIGN KEY(`" + MapperContract.Citta.ID_VIAGGIO +"`) REFERENCES " + Tables.VIAGGIO + " (`" + MapperContract.Viaggio.ID_VIAGGIO + "`));";

    //Create table POSTO
    private static final String CREATE_POSTO = "CREATE TABLE \"" + Tables.POSTO + "\" (" +
            "`" + MapperContract.Posto.ID_POSTO + "` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`" + MapperContract.Posto.VISITATO + "` INTEGER DEFAULT 0," +
            "`" + MapperContract.Posto.ID_CITTA + "` INTEGER NOT NULL," +
            "`" + MapperContract.Posto.ID_LUOGO + "` INTEGER NOT NULL," +
            "FOREIGN KEY(`" + MapperContract.Posto.ID_CITTA + "`) REFERENCES " + Tables.CITTA + " (`" + MapperContract.Citta.ID_CITTA + "`)," +
            "FOREIGN KEY(`" + MapperContract.Posto.ID_LUOGO + "`) REFERENCES " + Tables.LUOGO + " (`" + MapperContract.Luogo.ID + "`));";

    //Create table DATI_CITTA
    private static final String CREATE_DATI_CITTA = "CREATE TABLE \"" + Tables.DATI_CITTA + "\" (" +
            "`" + MapperContract.DatiCitta.ID + "` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`" + MapperContract.DatiCitta.NOME + "` TEXT NOT NULL," +
            "`" + MapperContract.DatiCitta.NAZIONE + "` TEXT NOT NULL," +
            "`" + MapperContract.DatiCitta.LATITUDINE + "` REAL NOT NULL," +
            "`" + MapperContract.DatiCitta.LONGITUDINE + "` REAL NOT NULL," +
            "`" + MapperContract.DatiCitta.COUNT + "` INTEGER DEFAULT 0);";

    //Create table LUOGO
    private static final String CREATE_LUOGO = "CREATE TABLE \"" + Tables.LUOGO + "\" (" +
            "`" + MapperContract.Luogo.ID + "` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`" + MapperContract.Luogo.NOME + "` TEXT NOT NULL," +
            "`" + MapperContract.Luogo.LATITUDINE + "` REAL NOT NULL," +
            "`" + MapperContract.Luogo.LONGITUDINE + "` REAL NOT NULL," +
            "`" + MapperContract.Luogo.ID_CITTA + "` INTEGER NOT NULL," +
            "`" + MapperContract.Luogo.COUNT + "` INTEGER DEFAULT 0," +
            "FOREIGN KEY(`" + MapperContract.Luogo.ID_CITTA + "`) REFERENCES " + Tables.DATI_CITTA + " (`" + MapperContract.DatiCitta.ID + "`));";

    //Create table FOTO
    private static final String CREATE_FOTO = "CREATE TABLE \"" + Tables.FOTO + "\" (" +
            "`" + MapperContract.Foto.ID + "` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`" + MapperContract.Foto.PATH + "` TEXT NOT NULL," +
            "`" + MapperContract.Foto.DATA + "` INTEGER," +
            "`" + MapperContract.Foto.LATITUDINE + "` REAL NOT NULL," +
            "`" + MapperContract.Foto.LONGITUDINE + "` REAL NOT NULL," +
            "`" + MapperContract.Foto.ID_VIAGGIO + "` INTEGER NOT NULL," +
            "`" + MapperContract.Foto.ID_CITTA + "` INTEGER NOT NULL," +
            "`" + MapperContract.Foto.ID_POSTO + "` INTEGER DEFAULT -1," +
            "FOREIGN KEY(`" + MapperContract.Foto.ID_VIAGGIO + "`) REFERENCES " + Tables.VIAGGIO + " (`" + MapperContract.Viaggio.ID_VIAGGIO + "`)," +
            "FOREIGN KEY(`" + MapperContract.Foto.ID_CITTA + "`) REFERENCES " + Tables.CITTA + " (`" + MapperContract.Citta.ID_CITTA + "`)," +
            "FOREIGN KEY(`" + MapperContract.Foto.ID_POSTO + "`) REFERENCES " + Tables.POSTO + " (`" + MapperContract.Posto.ID_POSTO + "`));";

    //Trigger che rimuove le citta contenute nel viaggio da eliminare
    private static final String TRIGGER_ELIMINA_CITTA_IN_VIAGGIO = "CREATE TRIGGER " + Triggers.ELIMINA_CITTA_IN_VIAGGIO + " BEFORE DELETE ON " +
            Tables.VIAGGIO + " BEGIN DELETE FROM " + Tables.CITTA + " WHERE " + MapperContract.Citta.ID_VIAGGIO + " = old." +
            MapperContract.Viaggio.ID_VIAGGIO + "; END;";

    //Trigger che rimuove i posti contenuti nella citta da rimuovere
    private static final String TRIGGER_ELIMINA_POSTI_IN_CITTA = "CREATE TRIGGER " + Triggers.ELIMINA_POSTI_IN_CITTA + " BEFORE DELETE ON " + Tables.CITTA + " BEGIN " +
            "DELETE FROM " + Tables.POSTO + " WHERE " + Tables.POSTO + "." + MapperContract.Posto.ID_CITTA + " = old." + MapperContract.Citta.ID_CITTA + "; END;";

    //Trigger che rimuove le foto associate alla citta eliminata
    private static final String TRIGGER_ELIMINA_FOTO_IN_CITTA = "CREATE TRIGGER " + Triggers.ELIMINA_FOTO_IN_CITTA + " BEFORE DELETE ON " + Tables.CITTA + " BEGIN " +
            "DELETE FROM " + Tables.FOTO + " WHERE " + Tables.FOTO + "." + MapperContract.Foto.ID_CITTA + " = old." + MapperContract.Citta.ID_CITTA + "; END;";

    //Trigger che rimuove le foto associate al posto eliminato
    private static final String TRIGGER_ELIMINA_FOTO_IN_POSTO = "CREATE TRIGGER " + Triggers.ELIMINA_FOTO_IN_POSTO + " BEFORE DELETE ON " + Tables.POSTO + " BEGIN " +
            "DELETE FROM " + Tables.FOTO + " WHERE " + Tables.FOTO + "." + MapperContract.Foto.ID_POSTO + " = old." + MapperContract.Posto.ID_POSTO + "; END;";

    //Trigger che incrementa i contatori delle citta
    private static final String TRIGGER_INCREMENTA_COUNT_CITTA = "CREATE TRIGGER " + Triggers.INCREMENTA_COUNT_CITTA +
            " AFTER INSERT ON " + Tables.CITTA + " BEGIN UPDATE " + Tables.VIAGGIO + " SET " + MapperContract.Viaggio.COUNT_CITTA +
            " = " + MapperContract.Viaggio.COUNT_CITTA + " + 1 WHERE " + Tables.VIAGGIO + "." + MapperContract.Viaggio.ID_VIAGGIO + " = new." + MapperContract.Citta.ID_VIAGGIO + ";" +
            "UPDATE " + Tables.DATI_CITTA + " SET " + MapperContract.DatiCitta.COUNT + " = " + MapperContract.DatiCitta.COUNT + " + 1 WHERE " +
            Tables.DATI_CITTA + "." + MapperContract.DatiCitta.ID + " = new." + MapperContract.Citta.ID_DATI_CITTA + "; END;";

    //Trigger che decrementa i contatori delle citta
    private static final String TRIGGER_DECREMENTA_COUNT_CITTA = "CREATE TRIGGER " + Triggers.DECREMENTA_COUNT_CITTA +
            " AFTER DELETE ON " + Tables.CITTA + " BEGIN UPDATE " + Tables.VIAGGIO + " SET " + MapperContract.Viaggio.COUNT_CITTA +
            " = " + MapperContract.Viaggio.COUNT_CITTA + " - 1 WHERE " + Tables.VIAGGIO + "." + MapperContract.Viaggio.ID_VIAGGIO + " = old." + MapperContract.Citta.ID_VIAGGIO + ";" +
            "UPDATE " + Tables.DATI_CITTA + " SET " + MapperContract.DatiCitta.COUNT + " = " + MapperContract.DatiCitta.COUNT + " - 1 WHERE " +
            Tables.DATI_CITTA + "." + MapperContract.DatiCitta.ID + " = old." + MapperContract.Citta.ID_DATI_CITTA + "; END;";

    //Trigger che incrementa i contatori dei posti visitati
    private static final String TRIGGER_INCREMENTA_COUNT_POSTI = "CREATE TRIGGER " + Triggers.INCREMENTA_COUNT_POSTI + " AFTER INSERT ON " + Tables.POSTO + " BEGIN " +
            "UPDATE " + Tables.CITTA + " SET " + MapperContract.Citta.COUNT_POSTI + " = " + MapperContract.Citta.COUNT_POSTI + " + 1 WHERE " + Tables.CITTA + "." + MapperContract.Citta.ID_CITTA + " = new." + MapperContract.Posto.ID_CITTA + "; " +
            "UPDATE " + Tables.LUOGO + " SET " + MapperContract.Luogo.COUNT + " = " + MapperContract.Luogo.COUNT + " + 1 WHERE " + Tables.LUOGO + "." + MapperContract.Luogo.ID + " = new." + MapperContract.Posto.ID_LUOGO + "; " +
            "UPDATE " + Tables.VIAGGIO + " SET " + MapperContract.Viaggio.COUNT_POSTI + " = " + MapperContract.Viaggio.COUNT_POSTI + " + 1 WHERE " + Tables.VIAGGIO + "." + MapperContract.Viaggio.ID_VIAGGIO + " IN (" +
            "SELECT " + MapperContract.Citta.ID_VIAGGIO + " FROM " + Tables.CITTA + " WHERE " + Tables.CITTA + "." + MapperContract.Citta.ID_CITTA + " = new." + MapperContract.Posto.ID_CITTA + "); END;";

    //Trigger che decrementa i contatori dei posti visitati
    private static final String TRIGGER_DECREMENTA_COUNT_POSTI = "CREATE TRIGGER " + Triggers.DECREMENTA_COUNT_POSTI + " AFTER DELETE ON " + Tables.POSTO + " BEGIN " +
            "UPDATE " + Tables.CITTA + " SET " + MapperContract.Citta.COUNT_POSTI + " = " + MapperContract.Citta.COUNT_POSTI + " - 1 WHERE " + Tables.CITTA + "." + MapperContract.Citta.ID_CITTA + " = old." + MapperContract.Posto.ID_CITTA + "; " +
            "UPDATE " + Tables.LUOGO + " SET " + MapperContract.Luogo.COUNT + " = " + MapperContract.Luogo.COUNT + " - 1 WHERE " + Tables.LUOGO + "." + MapperContract.Luogo.ID + " = old." + MapperContract.Posto.ID_LUOGO + "; " +
            "UPDATE " + Tables.VIAGGIO + " SET " + MapperContract.Viaggio.COUNT_POSTI + " = " + MapperContract.Viaggio.COUNT_POSTI + " - 1 WHERE " + Tables.VIAGGIO + "." + MapperContract.Viaggio.ID_VIAGGIO + " IN (" +
            "SELECT " + MapperContract.Citta.ID_VIAGGIO + " FROM " + Tables.CITTA + " WHERE " + Tables.CITTA + "." + MapperContract.Citta.ID_CITTA + " = old." + MapperContract.Posto.ID_CITTA + "); END;";

    //Trigger che elimina i dati di una citta una volta che non sono piu' referenziati da nessuna citta
    private static final String TRIGGER_ELIMINA_DATI_CITTA = "CREATE TRIGGER " + Triggers.ELIMINA_DATI_CITTA +
            " AFTER UPDATE OF " + MapperContract.DatiCitta.COUNT + " ON " + Tables.DATI_CITTA +
            " FOR EACH ROW WHEN new." + MapperContract.DatiCitta.COUNT + " = 0 BEGIN " +
            "DELETE FROM " + Tables.DATI_CITTA + " WHERE " + MapperContract.DatiCitta.ID + " = new." + MapperContract.DatiCitta.ID + "; END;";

    //Trigger che elimina i dati di un luogo una volta che non sono piu' referenziati da nessun posto
    private static final String TRIGGER_ELIMINA_LUOGO = "CREATE TRIGGER " + Triggers.ELIMINA_LUOGO + "  AFTER UPDATE OF " + MapperContract.Luogo.COUNT + " ON " + Tables.LUOGO +
            " FOR EACH ROW WHEN new." + MapperContract.Luogo.COUNT + " = 0 BEGIN " +
            "DELETE FROM " + Tables.LUOGO + " WHERE " + MapperContract.Luogo.ID + " = new." + MapperContract.Luogo.ID + "; END;";

    //Trigger che aggiorna la percentuale di completamento della citta
    //TODO: completare
    private static final String TRIGGER_UPDATE_PERCENTUALE = "";

    public MapperDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen (SQLiteDatabase db) {
        super.onOpen(db);
        //abilita foreign keys
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_VIAGGIO);
        db.execSQL(CREATE_CITTA);
        db.execSQL(CREATE_POSTO);
        db.execSQL(CREATE_DATI_CITTA);
        db.execSQL(CREATE_LUOGO);
        db.execSQL(CREATE_FOTO);
        db.execSQL(TRIGGER_ELIMINA_CITTA_IN_VIAGGIO);
        db.execSQL(TRIGGER_ELIMINA_POSTI_IN_CITTA);
        db.execSQL(TRIGGER_ELIMINA_FOTO_IN_CITTA);
        db.execSQL(TRIGGER_ELIMINA_FOTO_IN_POSTO);
        db.execSQL(TRIGGER_INCREMENTA_COUNT_CITTA);
        db.execSQL(TRIGGER_DECREMENTA_COUNT_CITTA);
        db.execSQL(TRIGGER_INCREMENTA_COUNT_POSTI);
        db.execSQL(TRIGGER_DECREMENTA_COUNT_POSTI);
        db.execSQL(TRIGGER_ELIMINA_DATI_CITTA);
        db.execSQL(TRIGGER_ELIMINA_LUOGO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.VIAGGIO);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.CITTA);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.POSTO);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.DATI_CITTA);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.LUOGO);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.FOTO);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.ELIMINA_CITTA_IN_VIAGGIO);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.ELIMINA_POSTI_IN_CITTA);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.ELIMINA_FOTO_IN_CITTA);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.ELIMINA_FOTO_IN_POSTO);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.INCREMENTA_COUNT_CITTA);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.DECREMENTA_COUNT_CITTA);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.INCREMENTA_COUNT_POSTI);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.DECREMENTA_COUNT_POSTI);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.ELIMINA_DATI_CITTA);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.ELIMINA_LUOGO);
        onCreate(db);
    }
}
