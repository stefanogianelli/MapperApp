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
        String INCREMENTA_COUNT_FOTO = "incrementa_count_foto";
        String DECREMENTA_COUNT_FOTO = "decrementa_count_foto";
        String UPDATE_COUNT_FOTO = "update_count_foto";
        String AGGIORNA_POSTI_VISITATI = "aggiorna_posti_visitati";
        String ELIMINA_DATI_CITTA = "elimina_dati_citta";
        String ELIMINA_LUOGO = "elimina_luogo";
    }

    private static final String DATABASE_NAME = "mapper_db";
    private static final int DATABASE_VERSION = 2;

    //Create table VIAGGIO
    private static final String CREATE_VIAGGIO = "CREATE TABLE \"" + Tables.VIAGGIO + "\" (" +
            "`" + MapperContract.Viaggio.ID_VIAGGIO +"` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`" + MapperContract.Viaggio.NOME + "` TEXT NOT NULL," +
            "`" + MapperContract.Viaggio.COUNT_CITTA + "` INTEGER DEFAULT 0," +
            "`" + MapperContract.Viaggio.COUNT_POSTI + "` INTEGER DEFAULT 0," +
            "`" + MapperContract.Viaggio.COUNT_FOTO + "` INTEGER DEFAULT 0);";

    //Create table CITTA
    private static final String CREATE_CITTA = "CREATE TABLE \"" + Tables.CITTA + "\" (" +
            "`" + MapperContract.Citta.ID_CITTA + "` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`" + MapperContract.Citta.ID_DATI_CITTA + "` INTEGER NOT NULL," +
            "`" + MapperContract.Citta.ID_VIAGGIO + "` INTEGER NOT NULL," +
            "`" + MapperContract.Citta.COUNT_POSTI + "` INTEGER DEFAULT 0," +
            "`" + MapperContract.Citta.POSTI_VISITATI + "` INTEGER DEFAULT 0," +
            "`" + MapperContract.Citta.COUNT_FOTO + "` INTEGER DEFAULT 0," +
            " FOREIGN KEY(`" + MapperContract.Citta.ID_DATI_CITTA + "`) REFERENCES " + Tables.DATI_CITTA + " (`" + MapperContract.DatiCitta.ID + "`)," +
            " FOREIGN KEY(`" + MapperContract.Citta.ID_VIAGGIO +"`) REFERENCES " + Tables.VIAGGIO + " (`" + MapperContract.Viaggio.ID_VIAGGIO + "`));";

    //Create table POSTO
    private static final String CREATE_POSTO = "CREATE TABLE \"" + Tables.POSTO + "\" (" +
            "`" + MapperContract.Posto.ID_POSTO + "` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`" + MapperContract.Posto.VISITATO + "` INTEGER DEFAULT 0," +
            "`" + MapperContract.Posto.ID_CITTA + "` INTEGER NOT NULL," +
            "`" + MapperContract.Posto.ID_LUOGO + "` INTEGER NOT NULL," +
            "`" + MapperContract.Posto.COUNT_FOTO + "` INTEGER DEFAULT 0," +
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
            "`" + MapperContract.Luogo.COUNT + "` INTEGER DEFAULT 0);";

    //Create table FOTO
    private static final String CREATE_FOTO = "CREATE TABLE \"" + Tables.FOTO + "\" (" +
            "`" + MapperContract.Foto.ID + "` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`" + MapperContract.Foto.PATH + "` TEXT NOT NULL," +
            "`" + MapperContract.Foto.DATA + "` INTEGER," +
            "`" + MapperContract.Foto.LATITUDINE + "` REAL NOT NULL," +
            "`" + MapperContract.Foto.LONGITUDINE + "` REAL NOT NULL," +
            "`" + MapperContract.Foto.ID_VIAGGIO + "` INTEGER NOT NULL," +
            "`" + MapperContract.Foto.ID_CITTA + "` INTEGER NOT NULL," +
            "`" + MapperContract.Foto.ID_POSTO + "` INTEGER DEFAULT null," +
            "`" + MapperContract.Foto.ID_MEDIA_STORE + "` INTEGER NOT NULL," +
            "`" + MapperContract.Foto.CAMERA + "` INTEGER DEFAULT 0," +
            "`" + MapperContract.Foto.MODEL + "` TEXT," +
            "`" + MapperContract.Foto.EXIF + "` TEXT," +
            "`" + MapperContract.Foto.WIDTH + "` TEXT," +
            "`" + MapperContract.Foto.HEIGHT + "` TEXT," +
            "`" + MapperContract.Foto.MIME_TYPE + "` TEXT," +
            "`" + MapperContract.Foto.SIZE + "` TEXT," +
            "`" + MapperContract.Foto.INDIRIZZO + "` TEXT," +
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

    //Trigger che incrementa i contatori delle foto
    private static final String TRIGGER_INCREMENTA_COUNT_FOTO = "CREATE TRIGGER " + Triggers.INCREMENTA_COUNT_FOTO + " AFTER INSERT ON " + Tables.FOTO + " BEGIN " +
            "UPDATE " + Tables.VIAGGIO + " SET " + MapperContract.Viaggio.COUNT_FOTO + " = " + MapperContract.Viaggio.COUNT_FOTO + " + 1 WHERE " + Tables.VIAGGIO + "." + MapperContract.Viaggio.ID_VIAGGIO + " = new." + MapperContract.Foto.ID_VIAGGIO + "; " +
            "UPDATE " + Tables.POSTO + " SET " + MapperContract.Posto.COUNT_FOTO + " = " + MapperContract.Posto.COUNT_FOTO + " + 1 WHERE " + Tables.POSTO + "." + MapperContract.Posto.ID_POSTO + " = new." + MapperContract.Foto.ID_POSTO + "; " +
            "UPDATE " + Tables.CITTA + " SET " + MapperContract.Citta.COUNT_FOTO + " = " + MapperContract.Citta.COUNT_FOTO + " + 1 WHERE " + Tables.CITTA + "." + MapperContract.Citta.ID_CITTA + " = new." + MapperContract.Foto.ID_CITTA + "; END;";

    //Trigger che decrementa i contatori delle foto
    private static final String TRIGGER_DECREMENTA_COUNT_FOTO = "CREATE TRIGGER " + Triggers.DECREMENTA_COUNT_FOTO + " AFTER DELETE ON " + Tables.FOTO + " BEGIN " +
            "UPDATE " + Tables.VIAGGIO + " SET " + MapperContract.Viaggio.COUNT_FOTO + " = " + MapperContract.Viaggio.COUNT_FOTO + " - 1 WHERE " + Tables.VIAGGIO + "." + MapperContract.Viaggio.ID_VIAGGIO + " = old." + MapperContract.Foto.ID_VIAGGIO + "; " +
            "UPDATE " + Tables.POSTO + " SET " + MapperContract.Posto.COUNT_FOTO + " = " + MapperContract.Posto.COUNT_FOTO + " - 1 WHERE " + Tables.POSTO + "." + MapperContract.Posto.ID_POSTO + " = old." + MapperContract.Foto.ID_POSTO + "; " +
            "UPDATE " + Tables.CITTA + " SET " + MapperContract.Citta.COUNT_FOTO + " = " + MapperContract.Citta.COUNT_FOTO + " - 1 WHERE " + Tables.CITTA + "." + MapperContract.Citta.ID_CITTA + " = old." + MapperContract.Foto.ID_CITTA + "; END;";

    //Trigger che aggiorna il contatore delle foto allo spostamento della foto
    private static final String TRIGGER_UPDATE_COUNT_FOTO = "CREATE TRIGGER " + Triggers.UPDATE_COUNT_FOTO + " AFTER UPDATE ON " + Tables.FOTO + " BEGIN " +
            "UPDATE " + Tables.VIAGGIO + " SET " + MapperContract.Viaggio.COUNT_FOTO + " = " + MapperContract.Viaggio.COUNT_FOTO + " - 1 WHERE " + Tables.VIAGGIO + "." + MapperContract.Viaggio.ID_VIAGGIO + " = old." + MapperContract.Foto.ID_VIAGGIO + "; " +
            "UPDATE " + Tables.VIAGGIO + " SET " + MapperContract.Viaggio.COUNT_FOTO + " = " + MapperContract.Viaggio.COUNT_FOTO + " + 1 WHERE " + Tables.VIAGGIO + "." + MapperContract.Viaggio.ID_VIAGGIO + " = new." + MapperContract.Foto.ID_VIAGGIO + "; " +
            "UPDATE " + Tables.POSTO + " SET " + MapperContract.Posto.COUNT_FOTO + " = " + MapperContract.Posto.COUNT_FOTO + " - 1 WHERE " + Tables.POSTO + "." + MapperContract.Posto.ID_POSTO + " = old." + MapperContract.Foto.ID_POSTO + "; " +
            "UPDATE " + Tables.POSTO + " SET " + MapperContract.Posto.COUNT_FOTO + " = " + MapperContract.Posto.COUNT_FOTO + " + 1 WHERE " + Tables.POSTO + "." + MapperContract.Posto.ID_POSTO + " = new." + MapperContract.Foto.ID_POSTO + "; " +
            "UPDATE " + Tables.CITTA + " SET " + MapperContract.Citta.COUNT_FOTO + " = " + MapperContract.Citta.COUNT_FOTO + " - 1 WHERE " + Tables.CITTA + "." + MapperContract.Citta.ID_CITTA + " = old." + MapperContract.Foto.ID_CITTA + "; " +
            "UPDATE " + Tables.CITTA + " SET " + MapperContract.Citta.COUNT_FOTO + " = " + MapperContract.Citta.COUNT_FOTO + " + 1 WHERE " + Tables.CITTA + "." + MapperContract.Citta.ID_CITTA + " = new." + MapperContract.Foto.ID_CITTA + "; END;";

    //Trigger che aggiorna il contatore dei posti visitati
    private static final String TRIGGER_AGGIORNA_POSTI_VISITATI = "CREATE TRIGGER " + Triggers.AGGIORNA_POSTI_VISITATI + " AFTER UPDATE OF " + MapperContract.Posto.VISITATO + " ON " + Tables.POSTO + " BEGIN " +
            "UPDATE " + Tables.CITTA + " SET " + MapperContract.Citta.POSTI_VISITATI + " = " + MapperContract.Citta.POSTI_VISITATI + " + 1 WHERE " + Tables.CITTA + "." + MapperContract.Citta.ID_CITTA + " = new." + MapperContract.Posto.ID_CITTA + " AND new." + MapperContract.Posto.VISITATO + " = 1; " +
            "UPDATE " + Tables.CITTA + " SET " + MapperContract.Citta.POSTI_VISITATI + " = " + MapperContract.Citta.POSTI_VISITATI + " - 1 WHERE " + Tables.CITTA + "." + MapperContract.Citta.ID_CITTA + " = new." + MapperContract.Posto.ID_CITTA + " AND new." + MapperContract.Posto.VISITATO + " = 0; END;";

    //Trigger che elimina i dati di una citta una volta che non sono piu' referenziati da nessuna citta
    private static final String TRIGGER_ELIMINA_DATI_CITTA = "CREATE TRIGGER " + Triggers.ELIMINA_DATI_CITTA +
            " AFTER UPDATE OF " + MapperContract.DatiCitta.COUNT + " ON " + Tables.DATI_CITTA +
            " FOR EACH ROW WHEN new." + MapperContract.DatiCitta.COUNT + " = 0 BEGIN " +
            "DELETE FROM " + Tables.DATI_CITTA + " WHERE " + MapperContract.DatiCitta.ID + " = new." + MapperContract.DatiCitta.ID + "; END;";

    //Trigger che elimina i dati di un luogo una volta che non sono piu' referenziati da nessun posto
    private static final String TRIGGER_ELIMINA_LUOGO = "CREATE TRIGGER " + Triggers.ELIMINA_LUOGO + "  AFTER UPDATE OF " + MapperContract.Luogo.COUNT + " ON " + Tables.LUOGO +
            " FOR EACH ROW WHEN new." + MapperContract.Luogo.COUNT + " = 0 BEGIN " +
            "DELETE FROM " + Tables.LUOGO + " WHERE " + MapperContract.Luogo.ID + " = new." + MapperContract.Luogo.ID + "; END;";

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
        db.execSQL(TRIGGER_INCREMENTA_COUNT_FOTO);
        db.execSQL(TRIGGER_DECREMENTA_COUNT_FOTO);
        db.execSQL(TRIGGER_UPDATE_COUNT_FOTO);
        db.execSQL(TRIGGER_AGGIORNA_POSTI_VISITATI);
        db.execSQL(TRIGGER_ELIMINA_DATI_CITTA);
        db.execSQL(TRIGGER_ELIMINA_LUOGO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2)
            upgradeFrom1to2(db);
        else
            recreateDatabase(db);
    }

    private void upgradeFrom1to2 (SQLiteDatabase db) {
        //rinomino tabella dei luoghi
        db.execSQL("ALTER TABLE " + Tables.LUOGO + " RENAME TO " + Tables.LUOGO + "_old;");
        //creo nuova tabella dei luoghi
        db.execSQL(CREATE_LUOGO);
        //copio valori
        db.execSQL("INSERT INTO " + Tables.LUOGO + " SELECT " + MapperContract.Luogo.ID + ", " +
                MapperContract.Luogo.NOME + ", " +
                MapperContract.Luogo.LATITUDINE + ", " +
                MapperContract.Luogo.LONGITUDINE + ", " +
                MapperContract.Luogo.COUNT + " FROM " + Tables.LUOGO + "_old ;");
        //elimino la vecchia tabella
        db.execSQL("DROP TABLE " + Tables.LUOGO + "_old;");
    }

    private void recreateDatabase (SQLiteDatabase db) {
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
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.INCREMENTA_COUNT_FOTO);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.DECREMENTA_COUNT_FOTO);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.UPDATE_COUNT_FOTO);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.AGGIORNA_POSTI_VISITATI);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.ELIMINA_DATI_CITTA);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.ELIMINA_LUOGO);
        onCreate(db);
    }
}
