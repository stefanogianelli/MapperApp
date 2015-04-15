package com.stefano.andrea.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * MapperOpenHelper
 */
public class MapperOpenHelper extends SQLiteOpenHelper {

    //Create table VIAGGIO
    private static final String CREATE_VIAGGIO = "CREATE TABLE \"" + MapperContract.TABLE_VIAGGIO + "\" (" +
            "`" + MapperContract.Viaggio._ID +"` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`" + MapperContract.Viaggio.NOME + "` TEXT NOT NULL);";
    //Create table CITTA
    private static final String CREATE_CITTA = "CREATE TABLE \"" + MapperContract.TABLE_CITTA + "\" (" +
            "`" + MapperContract.Citta._ID + "` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`" + MapperContract.Citta.ID_CITTA + "` INTEGER NOT NULL," +
            "`" + MapperContract.Citta.ID_VIAGGIO + "` INTEGER NOT NULL," +
            "`" + MapperContract.Citta.PERCENTUALE + "` REAL DEFAULT -1," +
            " FOREIGN KEY(`" + MapperContract.Citta.ID_CITTA + "`) REFERENCES " + MapperContract.TABLE_DATI_CITTA + " (`" + MapperContract.DatiCitta._ID + "`)," +
            " FOREIGN KEY(`" + MapperContract.Citta.ID_VIAGGIO +"`) REFERENCES " + MapperContract.TABLE_VIAGGIO + " (`" + MapperContract.Viaggio._ID + "`));";
    //Create table POSTO
    private static final String CREATE_POSTO = "CREATE TABLE \"" + MapperContract.TABLE_POSTO + "\" (" +
            "`" + MapperContract.Posto._ID + "` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`" + MapperContract.Posto.VISITATO + "` INTEGER DEFAULT 0," +
            "`" + MapperContract.Posto.ID_CITTA + "` INTEGER NOT NULL," +
            "`" + MapperContract.Posto.ID_LUOGO + "` INTEGER NOT NULL," +
            "FOREIGN KEY(`" + MapperContract.Posto.ID_CITTA + "`) REFERENCES " + MapperContract.TABLE_CITTA + " (`" + MapperContract.Citta._ID + "`)," +
            "FOREIGN KEY(`" + MapperContract.Posto.ID_LUOGO + "`) REFERENCES " + MapperContract.TABLE_LUOGO + " (`" + MapperContract.Luogo._ID + "`));";
    //Create table DATI_CITTA
    private static final String CREATE_DATI_CITTA = "CREATE TABLE \"" + MapperContract.TABLE_DATI_CITTA + "\" (" +
            "`" + MapperContract.DatiCitta._ID + "` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`" + MapperContract.DatiCitta.NOME + "` TEXT NOT NULL," +
            "`" + MapperContract.DatiCitta.NAZIONE + "` TEXT NOT NULL," +
            "`" + MapperContract.DatiCitta.LATITUDINE + "` REAL NOT NULL," +
            "`" + MapperContract.DatiCitta.LONGITUDINE + "` REAL NOT NULL);";
    //Create table LUOGO
    private static final String CREATE_LUOGO = "CREATE TABLE \"" + MapperContract.TABLE_LUOGO + "\" (" +
            "`" + MapperContract.Luogo._ID + "` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`" + MapperContract.Luogo.NOME + "` TEXT NOT NULL," +
            "`" + MapperContract.Luogo.LATITUDINE + "` REAL NOT NULL," +
            "`" + MapperContract.Luogo.LONGITUDINE + "` REAL NOT NULL," +
            "`" + MapperContract.Luogo.ID_CITTA + "` INTEGER NOT NULL," +
            "FOREIGN KEY(`" + MapperContract.Luogo.ID_CITTA + "`) REFERENCES " + MapperContract.TABLE_DATI_CITTA + " (`" + MapperContract.DatiCitta._ID + "`));";
    //Create table FOTO
    private static final String CREATE_FOTO = "CREATE TABLE \"" + MapperContract.TABLE_FOTO + "\" (" +
            "`" + MapperContract.Foto._ID + "` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`" + MapperContract.Foto.PATH + "` TEXT NOT NULL," +
            "`" + MapperContract.Foto.DATA + "` INTEGER," +
            "`" + MapperContract.Foto.LATITUDINE + "` REAL NOT NULL," +
            "`" + MapperContract.Foto.LONGITUDINE + "` REAL NOT NULL," +
            "`" + MapperContract.Foto.ID_CITTA + "` INTEGER NOT NULL," +
            "`" + MapperContract.Foto.ID_LUOGO + "` INTEGER DEFAULT null," +
            "FOREIGN KEY(`" + MapperContract.Foto.ID_CITTA + "`) REFERENCES " + MapperContract.TABLE_CITTA + " (`" + MapperContract.Citta._ID + "`)," +
            "FOREIGN KEY(`" + MapperContract.Foto.ID_LUOGO + "`) REFERENCES " + MapperContract.TABLE_POSTO + " (`" + MapperContract.Posto._ID + "`));";

    public MapperOpenHelper(Context context) {
        super(context, MapperContract.DATABASE_NAME, null, MapperContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_VIAGGIO);
        db.execSQL(CREATE_CITTA);
        db.execSQL(CREATE_POSTO);
        db.execSQL(CREATE_DATI_CITTA);
        db.execSQL(CREATE_LUOGO);
        db.execSQL(CREATE_FOTO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MapperContract.TABLE_VIAGGIO);
        db.execSQL("DROP TABLE IF EXISTS " + MapperContract.TABLE_CITTA);
        db.execSQL("DROP TABLE IF EXISTS " + MapperContract.TABLE_POSTO);
        db.execSQL("DROP TABLE IF EXISTS " + MapperContract.TABLE_DATI_CITTA);
        db.execSQL("DROP TABLE IF EXISTS " + MapperContract.TABLE_LUOGO);
        db.execSQL("DROP TABLE IF EXISTS " + MapperContract.TABLE_FOTO);
        onCreate(db);
    }
}
