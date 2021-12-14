package com.proveedoradeclimas.sacalmacen.clases;

import android.os.Environment;

import java.io.File;

/**
 * Created by Administrador on 01/11/2016.
 */
public class Constantes {

    public static final int MODULO_ALMACEN = 7;
    public static final int TAREA_ENTRADA = 777;
    public static final int TAREA_SALIDA = 778;
    public static final int TAREA_INVENTARIO = 779;
    public static final int TAREA_DEVOLUCION = 780;
    public static final int TAREA_TRANSFERENCIA = 781;
    public static final int TAREA_AJUSTE = 782;
    public static final int TAREA_APARTADO = 783;

    public static final String TABLA_ENTRADA = "Entrada";
    public static final String TABLA_SALIDA = "Salida";
    public static final String TABLA_SALIDA_GUIA = "Salida_Guia";
    public static final String TABLA_INVENTARIO = "Inventario";
    public static final String TABLA_DEVOLUCION = "Devolucion";
    public static final String TABLA_TRANSFERENCIA = "Transferencia";
    public static final String TABLA_AJUSTE = "Ajuste";
    public static final String TABLA_APARTADO = "Apartado";
    public static final String TABLA_CONEXIONES = "Conexiones";

    /*
        1	Firma del Cliente a la entrega. (Salida de Almácen)
        2	Firma del Chofer que entrega. (Entrada de Almácen)
        3	Firma del Almacenista que recibe. (Entrada de Almácen)
        4	Firma de quien recibe. (Transferencia de Almácen)
        5	Firma Contador. (Inventario)
     */
    public static final int TIPO_FIRMA_CLIENTE = 1;
    public static final int TIPO_FIRMA_CHOFER = 2;
    public static final int TIPO_FIRMA_ALMACENISTA = 3;
    public static final int TIPO_FIRMA_RECEPTOR = 4;
    public static final int TIPO_FIRMA_CONTADOR = 5;

    public static final String APP_PREF = "Proveedora";
    public static final String PREF_KEY_EMPRESA = "empresa";
    public static final String FILE_APK_PROVEE = "SAC_Proveedora.apk";

    public static final String PATH_APP_PROVEE = Environment.getExternalStorageDirectory().toString()
            + File.separator
            + "SAC Almacen";
    public static final String PATH_APP_EUROMTY = Environment.getExternalStorageDirectory().toString()
            + File.separator
            + "SAC Almacen Euro MTY";
    public static final String PATH_APP_EUROSLP = Environment.getExternalStorageDirectory().toString()
            + File.separator
            + "SAC Almacen Euro SLP";

}
