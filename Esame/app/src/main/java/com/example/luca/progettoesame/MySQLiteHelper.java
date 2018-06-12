package com.example.luca.progettoesame;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MySQLiteHelper extends SQLiteOpenHelper {
    private static MySQLiteHelper sInstance;

    public static final String TAG = "Helper";

    private static final String DATABASE_NAME = "WorldCitiesDB_ok.db";///data/data/com.example.luca.progettoesame/assets/db/
    private static final int DATABASE_VERSION = 6;
    public static final String COUNTRY_CODE_CAPITALI = "";
    private Context context;
    private static final String DATABASE_TEMPLATE = "db/WorldCitiesDB.sql";

    @Override
    public void onCreate(SQLiteDatabase database) {
        StringBuilder dbString = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(DATABASE_TEMPLATE)));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                dbString.append(mLine);
                dbString.append('\n');
            }
        } catch (IOException e) {
            Log.e("File", "File non trovato 1");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("File", "File non trovato 2");
                }
            }
        }
        for (String row : dbString.toString().split(";\n")) {
            if (row != null && row.length() > 0) {
                database.execSQL(row + ";");
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        onCreate(db);
    }

    public ArrayList<Citta> elencoCitta(String codStato) {
        ArrayList<Citta> citta = new ArrayList<>();
        String qryCitta;
        if (codStato == COUNTRY_CODE_CAPITALI) {
            qryCitta = "SELECT * FROM tblCitta WHERE IsCapitale=1";
        } else {
            qryCitta = "SELECT * FROM tblCitta " +
                    "INNER JOIN tblStato ON " +
                    "tblCitta.IdStato = tblStato.IdStato " +
                    "WHERE '" + codStato + "' like CodiceDue OR '" + codStato + "' like CodiceTre";
            Log.d("SQL", qryCitta);
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(qryCitta, null);
            while (cursor.moveToNext()) {
                Citta c = new Citta(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getInt(4),
                        cursor.getDouble(5),
                        cursor.getDouble(6));
                citta.add(c);
                Log.d("SQLCitta", c.toString());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get a product from database");
        }
        if (cursor != null) {
            cursor.close();
        }
        return citta;
    }

    public String getNomeStatoByCitta(String citta) {
        String qry;
        qry = "SELECT tblStato.NomeStato " +
                "FROM tblStato INNER JOIN tblCitta ON tblStato.IdStato = tblCitta.IdStato " +
                "WHERE tblCitta.NomeCitta ='" + citta + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(qry, null);
            while (cursor.moveToNext()) {
                String nomeStato = cursor.getString(0);
                return nomeStato;
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get a product from database");
        }
        if (cursor != null) {
            cursor.close();
        }
        return "Stato non trovato";
    }

    public Stato getStatoByCountryCode(String countryCode) {
        String qry;
        qry = "SELECT tblStato.* " +
                "FROM tblStato INNER JOIN tblCitta ON tblStato.IdStato = tblCitta.IdStato " +
                "WHERE tblStato.CodiceTre ='" + countryCode + "' OR tblStato.CodiceDue='" + countryCode + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(qry, null);
            while (cursor.moveToNext()) {
                Stato stato = new Stato(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4));
                return stato;
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get a product from database");
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }


    public static synchronized MySQLiteHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new MySQLiteHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
}
