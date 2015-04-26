package com.stefano.andrea.providers;

import android.net.Uri;

/**
 * MapperContract
 */
public final class MapperContract {

    interface ViaggioColumns {
        /** id del viaggio */
        String ID_VIAGGIO = "id_viaggio";
        /** Nome del viaggio */
        String NOME = "nome";
        /** Totale delle citta presenti nel viaggio */
        String COUNT_CITTA = "count_citta";
        /** Totale dei posti presenti nel viaggio */
        String COUNT_POSTI = "count_posti";
        /** Percorso della foto di copertina del viaggio */
        String PATH_FOTO = "path_foto";
    }

    interface CittaColumns {
        /** id della citta */
        String ID_CITTA = "id_citta";
        /** ID della citta nella tabella Dati_Citta */
        String ID_DATI_CITTA = "ref_dati_citta";
        /** ID del viaggio nel quale e stata visitata la citta */
        String ID_VIAGGIO = "ref_viaggio";
        /** Percentuale di completamento della visita */
        String PERCENTUALE = "percentuale";
        /** Totale dei posti inclusi nella citta */
        String COUNT_POSTI = "count_posti";
        /** Numero dei posti visitati */
        String POSTI_VISITATI = "count_posti_visitati";
    }

    interface PostoColumns {
        /** id del posto */
        String ID_POSTO = "id_posto";
        /** Booleano che indicata se il posto e stato visitato */
        String VISITATO = "visitato";
        /** ID della citta nella tabella Dati_Citta */
        String ID_CITTA = "ref_citta";
        /** ID del luogo collegato con la taballa Luogo */
        String ID_LUOGO = "ref_luogo";
    }

    interface DatiCittaColumns {
        /** id della citta */
        String ID = "id_dati_citta";
        /** Nome della citta */
        String NOME = "nome";
        /** Nome della nazione */
        String NAZIONE = "nazione";
        /** Latitudine della citta */
        String LATITUDINE = "latitudine";
        /** Longitudine della citta */
        String LONGITUDINE = "longitudine";
        /** Numero di riferimenti ai dati della citta */
        String COUNT = "count";
    }

    interface LuogoColumns {
        /** id del luogo */
        String ID = "id_luogo";
        /** Nome del luogo */
        String NOME = "nome";
        /** Latitudine del luogo */
        String LATITUDINE = "latitudine";
        /** Longitudine del luogo */
        String LONGITUDINE = "longitudine";
        /** ID della citta collegato con la tabella Dati_Citta */
        String ID_CITTA = "ref_citta";
        /** Numero di riferimenti ai dati del luogo */
        String COUNT = "count";
    }

    interface FotoColumns {
        /** id della foto */
        String ID = "id_foto";
        /** Percorso dove andare a recuperare la foto */
        String PATH = "path";
        /** Data in cui e stata scattata la foto */
        String DATA = "data";
        /** Latitudine dove e stata scattata la foto */
        String LATITUDINE = "latitudine";
        /** Longitudine dove e stata scattata la foto */
        String LONGITUDINE = "longitudine";
        /** ID della citta in cui e stata scattata la foto */
        String ID_CITTA = "ref_citta";
        /** ID del luogo dove e stata scattata la foto */
        String ID_LUOGO = "ref_luogo";
    }

    /** Nome dell'authority */
    public static final String CONTENT_AUTHORITY = "com.stefano.andrea.mapper.provider";
    /** Uri dell'authority */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /** Costanti della tabella Viaggio */
    public static final class Viaggio implements ViaggioColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperOpenHelper.Tables.VIAGGIO).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.viaggi";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.viaggio";

        public static final String[] PROJECTION_ALL = {ID_VIAGGIO, NOME, COUNT_CITTA, COUNT_POSTI, PATH_FOTO};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = ID_VIAGGIO + " DESC";
    }

    /** Costanti della tabella Citta */
    public static final class Citta implements CittaColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperOpenHelper.Tables.CITTA).build();
        public static final Uri DETTAGLI_VIAGGIO_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperOpenHelper.Tables.CITTA).appendPath("viaggio").build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.citta";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.citta";

        public static final String [] PROJECTION_ALL = {ID_CITTA, ID_DATI_CITTA, ID_VIAGGIO, PERCENTUALE, COUNT_POSTI, POSTI_VISITATI};
        public static final String [] PROJECTION_JOIN = {ID_CITTA, ID_DATI_CITTA, ID_VIAGGIO, PERCENTUALE, COUNT_POSTI, POSTI_VISITATI, DatiCitta.NOME, DatiCitta.NAZIONE, DatiCitta.LATITUDINE, DatiCitta.LONGITUDINE, DatiCitta.COUNT};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = ID_CITTA + " DESC";
    }

    /** Costanti della tabella Posto */
    public static final class Posto implements PostoColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperOpenHelper.Tables.POSTO).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.posto";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.posto";

        public static final String[] PROJECTION_ALL = {ID_LUOGO, VISITATO, ID_CITTA, ID_LUOGO};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = ID_POSTO + " DESC";
    }

    /** Costanti della tabella Dati Citta */
    public static final class DatiCitta implements DatiCittaColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperOpenHelper.Tables.DATI_CITTA).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.daticitta";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.daticitta";

        public static final String[] PROJECTION_ALL = {ID, NOME, NAZIONE, LATITUDINE, LONGITUDINE, COUNT};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = DatiCittaColumns.NOME + " ASC";
    }

    /** Costanti della tabella Luogo */
    public static final class Luogo implements LuogoColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperOpenHelper.Tables.LUOGO).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.luoghi";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.luogo";

        public static final String[] PROJECTION_ALL = {ID, NOME, LATITUDINE, LONGITUDINE, ID_CITTA, COUNT};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = LuogoColumns.NOME + " ASC";
    }

    /** Costanti della tabella Foto */
    public static final class Foto implements FotoColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperOpenHelper.Tables.FOTO).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.foto";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.foto";

        public static final String[] PROJECTION_ALL = {ID, PATH, DATA, LATITUDINE, LONGITUDINE, ID_CITTA, ID_LUOGO};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = FotoColumns.DATA + " ASC";
    }

}
