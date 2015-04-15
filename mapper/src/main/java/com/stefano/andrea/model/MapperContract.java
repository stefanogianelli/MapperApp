package com.stefano.andrea.model;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.net.PortUnreachableException;

/**
 * MapperContract
 */
public final class MapperContract {

    protected static final String DATABASE_NAME = "mapperdb";
    protected static final int DATABASE_VERSION = 2;

    public static final String TABLE_VIAGGIO = "viaggio";
    public static final String TABLE_CITTA = "citta";
    public static final String TABLE_POSTO = "posto";
    public static final String TABLE_DATI_CITTA = "dati_citta";
    public static final String TABLE_LUOGO = "luogo";
    public static final String TABLE_FOTO = "foto";

    public static final String AUTHORITY = "com.stefano.andrea.mapper";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final class Viaggio implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(MapperContract.AUTHORITY_URI, "viaggio");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/viaggi";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/viaggio";
        public static final String NOME = "nome";
        public static final String[] PROJECTION_ALL = {_ID, NOME};
    }

    public static final class Citta implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(MapperContract.AUTHORITY_URI, "citta");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/citta";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/citta";
        public static final String ID_CITTA = "id_citta";
        public static final String ID_VIAGGIO = "id_viaggio";
        public static final String PERCENTUALE = "percentuale";
        public static final String[] PROJECTION_ALL = {_ID, ID_CITTA, ID_VIAGGIO, PERCENTUALE};
    }

    public static final class Posto implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(MapperContract.AUTHORITY_URI, "posto");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/posto";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/posto";
        public static final String VISITATO = "visitato";
        public static final String ID_CITTA = "id_citta";
        public static final String ID_LUOGO = "luogo";
        public static final String[] PROJECTION_ALL = {_ID, VISITATO, ID_CITTA, ID_LUOGO};
    }

    public static final class DatiCitta implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(MapperContract.AUTHORITY_URI, "daticitta");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/daticitta";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/daticitta";
        public static final String NOME = "nome";
        public static final String NAZIONE = "nazione";
        public static final String LATITUDINE = "latitudine";
        public static final String LONGITUDINE = "longitudine";
        public static final String[] PROJECTION_ALL = {_ID, NOME, NAZIONE, LATITUDINE, LONGITUDINE};
    }

    public static final class Luogo implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(MapperContract.AUTHORITY_URI, "luogo");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/luoghi";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/luogo";
        public static final String NOME = "nome";
        public static final String LATITUDINE = "latitudine";
        public static final String LONGITUDINE = "longitudine";
        public static final String ID_CITTA = "id_citta";
        public static final String[] PROJECTION_ALL = {_ID, NOME, LATITUDINE, LONGITUDINE, ID_CITTA};
    }

    public static final class Foto implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(MapperContract.AUTHORITY_URI, "foto");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/foto";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/foto";
        public static final String PATH = "path";
        public static final String DATA = "data";
        public static final String LATITUDINE = "latitudine";
        public static final String LONGITUDINE = "longitudine";
        public static final String ID_CITTA = "id_citta";
        public static final String ID_LUOGO = "luogo";
        public static final String[] PROJECTION_ALL = {_ID, PATH, DATA, LATITUDINE, LONGITUDINE, ID_CITTA, ID_LUOGO};
    }

}
