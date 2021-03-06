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
        /** Totale delle foto presenti nel viaggio */
        String COUNT_FOTO = "count_foto";
    }

    interface CittaColumns {
        /** id della citta */
        String ID_CITTA = "id_citta";
        /** ID della citta nella tabella Dati_Citta */
        String ID_DATI_CITTA = "ref_dati_citta";
        /** ID del viaggio nel quale e stata visitata la citta */
        String ID_VIAGGIO = "ref_viaggio";
        /** Totale dei posti inclusi nella citta */
        String COUNT_POSTI = "count_posti";
        /** Numero dei posti visitati */
        String POSTI_VISITATI = "count_posti_visitati";
        /** Totale delle foto scattate nella citta */
        String COUNT_FOTO = "count_foto";
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
        /** Totale delel foto scattate nel posto */
        String COUNT_FOTO = "count_foto";
    }

    interface DatiCittaColumns {
        /** id della citta */
        String ID = "id_dati_citta";
        /** Nome della citta */
        String NOME = "nome";
        /** Nazione della citta' */
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
        /** Numero di riferimenti ai dati del luogo */
        String COUNT = "count";
    }

    interface FotoColumns {
        /** id della foto */
        String ID = "id_foto";
        /** Percorso dove andare a recuperare la foto */
        String PATH = "path";
        /** Data in cui e stata scattata la foto, a partire dal 1 Gennaio 1970 */
        String DATA = "data";
        /** Latitudine dove e stata scattata la foto */
        String LATITUDINE = "latitudine";
        /** Longitudine dove e stata scattata la foto */
        String LONGITUDINE = "longitudine";
        /** ID del viaggio associato alla foto */
        String ID_VIAGGIO = "ref_viaggio";
        /** ID della citta in cui e stata scattata la foto */
        String ID_CITTA = "ref_citta";
        /** ID del luogo dove e stata scattata la foto */
        String ID_POSTO = "ref_posto";
        /** ID della foto nel MediaStore */
        String ID_MEDIA_STORE = "id_media_store";
        /** 1 se la foto e' stata scattata tramite l'app */
        String CAMERA = "camera";
        /** Modello della fotocamera */
        String MODEL = "model";
        /** Dati exif della foto */
        String EXIF = "exif";
        /** Larghezza dell'immagine */
        String WIDTH = "width";
        /** Altezza dell'immagine */
        String HEIGHT = "height";
        /** Tipologia di foto */
        String MIME_TYPE = "mime_type";
        /** Dimensione dell'immagine */
        String SIZE = "size";
        /** Indirizzo della foto */
        String INDIRIZZO = "indirizzo";
    }

    /** Nome dell'authority */
    public static final String CONTENT_AUTHORITY = "com.stefano.andrea.mapper.provider";
    /** Uri dell'authority */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /** Costanti della tabella Viaggio */
    public static final class Viaggio implements ViaggioColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperDatabase.Tables.VIAGGIO).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.viaggi";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.viaggio";

        public static final String[] PROJECTION_ALL = {ID_VIAGGIO, NOME, COUNT_CITTA, COUNT_POSTI, COUNT_FOTO};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = ID_VIAGGIO + " DESC";
    }

    /** Costanti della tabella Citta */
    public static final class Citta implements CittaColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperDatabase.Tables.CITTA).build();
        public static final Uri DETTAGLI_VIAGGIO_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperDatabase.Tables.CITTA).appendPath("viaggio").build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.citta";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.citta";

        public static final String [] PROJECTION_ALL = {ID_CITTA, ID_DATI_CITTA, ID_VIAGGIO, COUNT_POSTI, POSTI_VISITATI, COUNT_FOTO, DatiCitta.NOME, DatiCitta.NAZIONE, DatiCitta.LATITUDINE, DatiCitta.LONGITUDINE, DatiCitta.COUNT};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = ID_CITTA + " DESC";
    }

    /** Costanti della tabella Posto */
    public static final class Posto implements PostoColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperDatabase.Tables.POSTO).build();
        public static final Uri POSTI_IN_CITTA_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperDatabase.Tables.POSTO).appendPath("citta").build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.posto";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.posto";

        public static final String [] PROJECTION_ALL = {ID_POSTO, VISITATO, ID_CITTA, ID_LUOGO, COUNT_FOTO, Luogo.ID, Luogo.NOME, Luogo.LATITUDINE, Luogo.LONGITUDINE};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = ID_POSTO + " DESC";
    }

    /** Costanti della tabella Dati Citta */
    public static final class DatiCitta implements DatiCittaColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperDatabase.Tables.DATI_CITTA).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.daticitta";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.daticitta";

        public static final String[] PROJECTION_ALL = {ID, NOME, NAZIONE, LATITUDINE, LONGITUDINE, COUNT};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = DatiCittaColumns.NOME + " ASC";
    }

    /** Costanti della tabella Luogo */
    public static final class Luogo implements LuogoColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperDatabase.Tables.LUOGO).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.luoghi";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.luogo";

        public static final String[] PROJECTION_ALL = {ID, NOME, LATITUDINE, LONGITUDINE, COUNT};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = LuogoColumns.NOME + " ASC";
    }

    /** Costanti della tabella Foto */
    public static final class Foto implements FotoColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperDatabase.Tables.FOTO).build();
        public static final Uri FOTO_IN_VIAGGIO_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperDatabase.Tables.FOTO).appendPath("viaggio").build();
        public static final Uri FOTO_IN_CITTA_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperDatabase.Tables.FOTO).appendPath("citta").build();
        public static final Uri FOTO_IN_POSTO_URI = BASE_CONTENT_URI.buildUpon().appendPath(MapperDatabase.Tables.FOTO).appendPath("posto").build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.foto";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.foto";

        public static final String[] PROJECTION_ALL = {ID, PATH, DATA, LATITUDINE, LONGITUDINE, ID_VIAGGIO, ID_CITTA, ID_POSTO, ID_MEDIA_STORE, CAMERA, MODEL, EXIF, WIDTH, HEIGHT, MIME_TYPE, SIZE, INDIRIZZO};

        /** "ORDER BY" clauses. */
        public static final String DEFAULT_SORT = FotoColumns.DATA + " ASC";
    }

}
