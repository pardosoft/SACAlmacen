package com.proveedoradeclimas.sacalmacen.clases;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Administrador on 13/09/2016.
 */
public class MyApplication extends Application {

    private String endPoint;
    private String secretWord;
    private String pw;
    private String numAlmacen;
    private static Context mContext;
    private String endPointSecundario;
    private String endPointPrimario;
    private String endPointImpresion;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        if(settingsExists())
        {
            refreshValues();
        }
    }

    //Obtener Valores
    public static Context getContext(){
        return mContext;
    }
    public String getSecretWord() {
        return secretWord;
    }
    public String getPW() {
        return pw;
    }
    public String getNumproy() {
        return "0001";
    }
    public String getEndPoint() {
        return endPoint;
    }
    public String getEndPointImpresion() {
        return endPointImpresion;
    }
    public String getNumAlmacen() {
        return numAlmacen;
    }

    //Cambiar endPoint
    public void changeEndPoint() {

        ConexionesSql helper = new ConexionesSql(getApplicationContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        String primario, secundario;

        if (endPoint.equals(endPointSecundario))
        {
            endPoint = endPointPrimario;
            primario = endPointPrimario;
            secundario = endPointSecundario;
        }
        else
        {
            endPoint = endPointSecundario;
            primario = endPointSecundario;
            secundario = endPointPrimario;
        }

        db.execSQL("UPDATE " + Constantes.TABLA_CONEXIONES + " SET EndPointPrincipal='" + primario + "',EndPointSecundario='" + secundario + "'");

        db.close();
        helper.close();
    }

    public boolean settingsExists() {

        ConexionesSql helper = new ConexionesSql(getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + Constantes.TABLA_CONEXIONES, null);

        cursor.moveToFirst();

        int valCount = cursor.getInt(0);

        cursor.close();
        db.close();
        helper.close();

        if(valCount > 0)
            { return true; }
        else
            { return false; }
    }

    public void refreshValues() {

        ConexionesSql helper = new ConexionesSql(getApplicationContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columnas = {"EndPointPrincipal","EndPointSecundario","EndPointImpresion","secretWord","Password","NumAlmacen"};
        Cursor cursor = db.query(Constantes.TABLA_CONEXIONES, columnas, null, null, null, null, null, null);

        cursor.moveToFirst();
        endPoint = cursor.getString(cursor.getColumnIndex("EndPointPrincipal"));
        endPointSecundario = cursor.getString(cursor.getColumnIndex("EndPointSecundario"));
        endPointImpresion = cursor.getString(cursor.getColumnIndex("EndPointImpresion"));
        secretWord = cursor.getString(cursor.getColumnIndex("SecretWord"));
        pw = cursor.getString(cursor.getColumnIndex("Password"));
        numAlmacen = cursor.getString(cursor.getColumnIndex("NumAlmacen"));
        endPointPrimario = endPoint;

        cursor.close();
        db.close();
        helper.close();
    }
}
