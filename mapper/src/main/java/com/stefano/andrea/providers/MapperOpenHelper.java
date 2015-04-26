package com.stefano.andrea.providers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * MapperOpenHelper
 */
public class MapperOpenHelper extends SQLiteOpenHelper {

    protected interface Tables {
        String VIAGGIO = "viaggio";
        String CITTA = "citta";
        String POSTO = "posto";
        String DATI_CITTA = "dati_citta";
        String LUOGO = "luogo";
        String FOTO = "foto";

        String CITTA_JOIN_DATI_CITTA = CITTA + " JOIN " + DATI_CITTA + " ON " + CITTA + "." + MapperContract.Citta.ID_DATI_CITTA +  "=" + DATI_CITTA + "." + MapperContract.DatiCitta.ID;
    }

    protected interface Triggers {
        String ELIMINA_CITTA_IN_VIAGGIO = "citta_in_viaggio_delete";
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
            "`" + MapperContract.Foto.ID_CITTA + "` INTEGER NOT NULL," +
            "`" + MapperContract.Foto.ID_LUOGO + "` INTEGER DEFAULT null," +
            "FOREIGN KEY(`" + MapperContract.Foto.ID_CITTA + "`) REFERENCES " + Tables.CITTA + " (`" + MapperContract.Citta.ID_CITTA + "`)," +
            "FOREIGN KEY(`" + MapperContract.Foto.ID_LUOGO + "`) REFERENCES " + Tables.POSTO + " (`" + MapperContract.Posto.ID_POSTO + "`));";

    //Trigger elimina le citta contenute nel viaggio da eliminare
    private static final String TRIGGER_ELIMINA_CITTA_IN_VIAGGIO = "CREATE TRIGGER " + Triggers.ELIMINA_CITTA_IN_VIAGGIO + " BEFORE DELETE ON " +
            Tables.VIAGGIO + " BEGIN DELETE FROM " + Tables.CITTA + " WHERE " + MapperContract.Citta.ID_VIAGGIO + " = old." +
            MapperContract.Viaggio.ID_VIAGGIO + "; END;";

    public MapperOpenHelper(Context context) {
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
        onCreate(db);
    }
}
