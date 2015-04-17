package com.stefano.andrea.providers;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * MapperContract
 */
public final class MapperContract {

    /**
     * Nome della tabella Viaggio
     */
    public static final String TABLE_VIAGGIO = "viaggio";
    /**
     * Nome della tabella Citta
     */
    public static final String TABLE_CITTA = "citta";
    /**
     * Nome della tabella Posto
     */
    public static final String TABLE_POSTO = "posto";
    /**
     * Nome della tabella Dati Citta
     */
    public static final String TABLE_DATI_CITTA = "dati_citta";
    /**
     * Nome della tabella Luogo
     */
    public static final String TABLE_LUOGO = "luogo";
    /**
     * Nome della tabella Foto
     */
    public static final String TABLE_FOTO = "foto";

    /**
     * Nome dell'authority
     */
    public static final String AUTHORITY = "com.stefano.andrea.mapper.provider";
    /**
     * Uri dell'authority
     */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * Costanti della tabella Viaggio
     */
    public static final class Viaggio implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(MapperContract.AUTHORITY_URI, "viaggio");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/viaggi";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/viaggio";
        public static final String NOME = "nome";
        public static final String[] PROJECTION_ALL = {_ID, NOME};
    }

    /**
     * Costanti della tabella Citta
     */
    public static final class Citta implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(MapperContract.AUTHORITY_URI, "citta");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/citta";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/citta";
        public static final String ID_CITTA = "id_citta";
        public static final String ID_VIAGGIO = "id_viaggio";
        public static final String PERCENTUALE = "percentuale";
        public static final String[] PROJECTION_ALL = {_ID, ID_CITTA, ID_VIAGGIO, PERCENTUALE};
    }

    /**
     * Costanti della tabella Posto
     */
    public static final class Posto implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(MapperContract.AUTHORITY_URI, "posto");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/posto";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/posto";
        public static final String VISITATO = "visitato";
        public static final String ID_CITTA = "id_citta";
        public static final String ID_LUOGO = "luogo";
        public static final String[] PROJECTION_ALL = {_ID, VISITATO, ID_CITTA, ID_LUOGO};
    }

    /**
     * Costanti della tabella Dati Citta
     */
    public static final class DatiCitta implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(MapperContract.AUTHORITY_URI, "daticitta");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/daticitta";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/daticitta";
        public static final String NOME = "nome";
        public static final String NAZIONE = "nazione";
        public static final String LATITUDINE = "latitudine";
        public static final String LONGITUDINE = "longitudine";
        public static final String[] PROJECTION_ALL = {_ID, NOME, NAZIONE, LATITUDINE, LONGITUDINE};
    }

    /**
     * Costanti della tabella Luogo
     */
    public static final class Luogo implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(MapperContract.AUTHORITY_URI, "luogo");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/luoghi";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/luogo";
        public static final String NOME = "nome";
        public static final String LATITUDINE = "latitudine";
        public static final String LONGITUDINE = "longitudine";
        public static final String ID_CITTA = "id_citta";
        public static final String[] PROJECTION_ALL = {_ID, NOME, LATITUDINE, LONGITUDINE, ID_CITTA};
    }

    /**
     * Costanti della tabella Foto
     */
    public static final class Foto implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(MapperContract.AUTHORITY_URI, "foto");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/foto";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/foto";
        public static final String PATH = "path";
        public static final String DATA = "data";
        public static final String LATITUDINE = "latitudine";
        public static final String LONGITUDINE = "longitudine";
        public static final String ID_CITTA = "id_citta";
        public static final String ID_LUOGO = "luogo";
        public static final String[] PROJECTION_ALL = {_ID, PATH, DATA, LATITUDINE, LONGITUDINE, ID_CITTA, ID_LUOGO};
    }

}
