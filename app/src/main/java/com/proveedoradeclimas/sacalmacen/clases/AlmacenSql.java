package com.proveedoradeclimas.sacalmacen.clases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrador on 15/09/2016.
 */
public class AlmacenSql extends SQLiteOpenHelper {

    private static String miTabla = "";
    public static final String COLUMN_ID = "_id";

    private final String TAG = "MyInventarySQL";
    private static final String DATABASE_NAME = "SAC_Inv.db";
    private static final int DATABASE_VERSION = 1;

    public AlmacenSql(Context context, String _Tabla) {
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
        miTabla = _Tabla;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // TODO Auto-generated method stub
        String DATABASE_CREATE_ENTRADA = "CREATE TABLE "
                + Constantes.TABLA_ENTRADA + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + " Modelo TEXT NOT NULL, "
                + " Serie TEXT NOT NULL );";
        database.execSQL(DATABASE_CREATE_ENTRADA);

        String DATABASE_CREATE_SALIDA = "CREATE TABLE "
                + Constantes.TABLA_SALIDA + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                /***+ " Modelo text not null, "***/
                + " Serie TEXT NOT NULL, "
                + " Fecha TEXT "
                + ");";
        database.execSQL(DATABASE_CREATE_SALIDA);

        String DATABASE_CREATE_SALIDA_GUIA = "CREATE TABLE "
                + Constantes.TABLA_SALIDA_GUIA + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                /***+ " Modelo text not null, "***/
                + " Serie TEXT NOT NULL, "
                + " Fecha TEXT, "
                + " Guia TEXT "
                + ");";
        database.execSQL(DATABASE_CREATE_SALIDA_GUIA);

        String DATABASE_CREATE_INVENTARIO = "CREATE TABLE "
                + Constantes.TABLA_INVENTARIO + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                /***+ " Modelo text not null, "***/
                + " Serie TEXT NOT NULL, "
                + " Fecha TEXT NOT NULL, "
                + " idZona TEXT NOT NULL, "
                + " descZona TEXT NOT NULL );";
        database.execSQL(DATABASE_CREATE_INVENTARIO);

        String DATABASE_CREATE_DEVOLUCION = "CREATE TABLE "
                + Constantes.TABLA_DEVOLUCION + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                /***+ " Modelo text not null, "***/
                + " Serie TEXT NOT NULL, "
                + " Fecha TEXT );";
        database.execSQL(DATABASE_CREATE_DEVOLUCION);

        String DATABASE_CREATE_TRANSFERENCIA = "CREATE TABLE "
                + Constantes.TABLA_TRANSFERENCIA + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                /***+ " Modelo text not null, "***/
                + " Serie TEXT NOT NULL, "
                + " Fecha TEXT );";
        database.execSQL(DATABASE_CREATE_TRANSFERENCIA);

        String DATABASE_CREATE_AJUSTES = "CREATE TABLE "
                + Constantes.TABLA_AJUSTE + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + " Modelo TEXT NOT NULL, "
                + " Serie TEXT NOT NULL, "
                + " idAjuste INT NOT NULL, "
                + " descAjuste TEXT NOT NULL, "
                + " Observaciones TEXT, "
                + " Fecha TEXT "
                + ");";
        database.execSQL(DATABASE_CREATE_AJUSTES);

        String DATABASE_CREATE_APARTADO = "CREATE TABLE "
                + Constantes.TABLA_APARTADO + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "Serie TEXT NOT NULL, "
                + "Fecha TEXT "
                + ");";
        database.execSQL(DATABASE_CREATE_APARTADO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
