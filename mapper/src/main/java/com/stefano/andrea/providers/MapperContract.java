package com.stefano.andrea.providers;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * MapperContract
 */
public final class MapperContract {

    interface ViaggioColumns {
        /** Nome del viaggio */
        String NOME = "nome";
    }

    interface CittaColumns {
        /** ID della citta nella tabella Dati_Citta */
        String ID_CITTA = "id_citta";
        /** ID del viaggio nel quale e stata visitata la citta */
        String ID_VIAGGIO = "id_viaggio";
        /* Percentuale di completamento della visita */
        String PERCENTUALE = "percentuale";
    }

    interface PostoColumns {
        /** Booleano che indicata se il posto e stato visitato */
        String VISITATO = "visitato";
        /** ID della citta nella tabella Dati_Citta */
        String ID_CITTA = "id_citta";
        /** ID del luogo collegato con la taballa Luogo */
        String ID_LUOGO = "luogo";
    }

    interface DatiCittaColumns {
        /** Nome della citta */
        String NOME = "nome";
        /** Nome della nazione */
        String NAZIONE = "nazione";
        /** Latitudine della citta */
        String LATITUDINE = "latitudine";
        /** Longitudine della citta */
        String LONGITUDINE = "longitudine";
    }

    interface LuogoColumns {
        /** Nome del luogo */
        String NOME = "nome";
        /** Latitudine del luogo */
        String LATITUDINE = "latitudine";
        /** Longitudine del luogo */
        String LONGITUDINE = "longitudine";
        /** ID della citta collegato con la tabella Dati_Citta */
        String ID_CITTA = "id_citta";
    }

    interface FotoColumns {
        /** Percorso dove andare a recuperare la foto */
        String PATH = "path";
        /** Data in cui e stata scattata la foto */
        String DATA = "data";
        /** Latitudine dove e stata scattata la foto */
        String LATITUDINE = "latitudine";
        /** Longitudine dove e stata scattata la foto */
        String LONGITUDINE = "longitudine";
        /** ID della citta in cui e stata scattata la foto */
        String ID_CITTA = "id_citta";
        /** ID del luogo dove e stata scattata la foto */
        String ID_LUOGO = "id_luogo";
    }

    /** Nome dell'authority */
    public static final String CONTENT_AUTHORITY = "com.stefano.andrea.mapper.provider";
    /** Uri dell'authority */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /** Costanti della tabella Viaggio */
    public static final class Viaggio implements BaseColumns, ViaggioColumns {
        public static final String TABLE_NAME = "viaggio";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.viaggi";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.viaggio";

        public static final String[] PROJECTION_ALL = {_ID, NOME};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = _ID + " ASC";
    }

    /** Costanti della tabella Citta */
    public static final class Citta implements BaseColumns, CittaColumns {
        public static final String TABLE_NAME = "citta";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.citta";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.citta";

        public static final String[] PROJECTION_ALL = {_ID, ID_CITTA, ID_VIAGGIO, PERCENTUALE};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = _ID + " ASC";
    }

    /** Costanti della tabella Posto */
    public static final class Posto implements BaseColumns, PostoColumns {
        public static final String TABLE_NAME = "posto";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.posto";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.posto";

        public static final String[] PROJECTION_ALL = {_ID, VISITATO, ID_CITTA, ID_LUOGO};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = _ID + " ASC";
    }

    /** Costanti della tabella Dati Citta */
    public static final class DatiCitta implements BaseColumns, DatiCittaColumns {
        public static final String TABLE_NAME = "daticitta";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.daticitta";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.daticitta";

        public static final String[] PROJECTION_ALL = {_ID, NOME, NAZIONE, LATITUDINE, LONGITUDINE};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = DatiCittaColumns.NOME + " ASC";
    }

    /** Costanti della tabella Luogo */
    public static final class Luogo implements BaseColumns, LuogoColumns {
        public static final String TABLE_NAME = "luogo";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.luoghi";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.luogo";

        public static final String[] PROJECTION_ALL = {_ID, NOME, LATITUDINE, LONGITUDINE, ID_CITTA};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = LuogoColumns.NOME + " ASC";
    }

    /** Costanti della tabella Foto */
    public static final class Foto implements BaseColumns, FotoColumns {
        public static final String TABLE_NAME = "foto";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.foto";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.foto";

        public static final String[] PROJECTION_ALL = {_ID, PATH, DATA, LATITUDINE, LONGITUDINE, ID_CITTA, ID_LUOGO};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = FotoColumns.DATA + " ASC";
    }

}
