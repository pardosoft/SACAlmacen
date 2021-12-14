package com.proveedoradeclimas.sacalmacen.clases;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by Administrador on 14/09/2016.
 */
public class zMensajes {

    public static void MensajeOK(Activity App, String miTitulo, String miTexto)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(App);

        builder.setMessage(miTexto)
                .setTitle(miTitulo).setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { //dismiss the dialog
                    }
                }).show();
    }
}
