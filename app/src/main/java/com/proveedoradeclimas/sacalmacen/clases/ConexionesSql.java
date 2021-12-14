package com.proveedoradeclimas.sacalmacen.clases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Administrador on 13/09/2016.
 */
public class ConexionesSql extends SQLiteOpenHelper {

    private static String miTabla = "";
    public static final String COLUMN_ID = "_id";

    private static final String DATABASE_NAME = "Conexiones.db";
    private static final int DATABASE_VERSION = 1;

    public ConexionesSql(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String DATABASE_CREATE = "CREATE TABLE " + Constantes.TABLA_CONEXIONES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + " EndPointPrincipal TEXT NOT NULL, "
                + " EndPointSecundario TEXT NOT NULL, "
                + " EndPointImpresion TEXT NOT NULL, "
                + " SecretWord TEXT NOT NULL, "
                + " Password TEXT NOT NULL, "
                + " NumAlmacen TEXT NOT NULL);";
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
