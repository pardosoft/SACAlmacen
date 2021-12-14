package com.proveedoradeclimas.sacalmacen.clases;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Administrador on 14/09/2016.
 */
public class WsMethods {

    /*************************************** ENTRADA **********************************************/
    public static String buscarOcEntrada(MyApplication App, String ps_oc) {
        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "Valida_OC",
                "http://www.proveedoradeclimas.com/Valida_OC",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_sucursal", App.getNumproy());
        client.agregarParametro("as_ordencompra", ps_oc);
        client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            App.changeEndPoint();

            client = new CallWs(
                    App.getEndPoint(),
                    "http://www.proveedoradeclimas.com/",
                    "Valida_OC",
                    "http://www.proveedoradeclimas.com/Valida_OC",
                    true);

            client.agregarHeader("Bismarck", "St0ckageD3Bienz");
            client.agregarParametro("as_sucursal", App.getNumproy());
            client.agregarParametro("as_ordencompra", ps_oc);
            client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

            if(client.llamarWs() == true) {
                return client.obtenResultado();
            }
        }

        return client.obtenError();
    }

    public static String guardarEntrada(MyApplication App, String ps_entrada) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "wEntradaAlmacen",
                "http://www.proveedoradeclimas.com/wEntradaAlmacen",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_xml", ps_entrada);
        client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            App.changeEndPoint();

            client = new CallWs(
                    App.getEndPoint(),
                    "http://www.proveedoradeclimas.com/",
                    "wEntradaAlmacen",
                    "http://www.proveedoradeclimas.com/wEntradaAlmacen",
                    true);

            client.agregarHeader("Bismarck", "St0ckageD3Bienz");
            client.agregarParametro("as_xml", ps_entrada);
            client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

            if(client.llamarWs() == true) {
                return client.obtenResultado();
            }
        }

        return client.obtenError();
    }

    public static String imprimirEntrada(MyApplication App, String ps_entrada) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPointImpresion(),
                "http://www.proveedoradeclimas.com/",
                "Imprime_Nota_Entrada",
                "http://www.proveedoradeclimas.com/Imprime_Nota_Entrada",
                false);

        client.agregarParametro("as_xml", ps_entrada);

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            return client.obtenError();
        }
    }

    public static String getXmlNotaEntrada(MyApplication App, String ps_remision, int pi_camion) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "XML_Nota_Entrada",
                "http://www.proveedoradeclimas.com/XML_Nota_Entrada",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_numproy", App.getNumproy());
        client.agregarParametro("as_remision", ps_remision);
        client.agregarParametro("ai_camion", Integer.toString(pi_camion));

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            return client.obtenError();
        }
    }

    /**************************************** SALIDA **********************************************/
    public static String buscarRemisionSalida(MyApplication App, String ps_remision) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "Valida_Remision",
                "http://www.proveedoradeclimas.com/Valida_Remision",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_sucursal", App.getNumproy());
        client.agregarParametro("as_remision", ps_remision);
        client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            App.changeEndPoint();

            client = new CallWs(
                    App.getEndPoint(),
                    "http://www.proveedoradeclimas.com/",
                    "Valida_Remision",
                    "http://www.proveedoradeclimas.com/Valida_Remision",
                    true);

            client.agregarHeader("Bismarck", "St0ckageD3Bienz");
            client.agregarParametro("as_sucursal", App.getNumproy());
            client.agregarParametro("as_remision", ps_remision);
            client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

            if(client.llamarWs() == true) {
                return client.obtenResultado();
            }
        }

        return client.obtenError();
    }

    public static String guardarSalida(MyApplication App, String ps_salida) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "wSalidaAlmacen",
                "http://www.proveedoradeclimas.com/wSalidaAlmacen",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_xml", ps_salida);
        client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            App.changeEndPoint();

            client = new CallWs(
                    App.getEndPoint(),
                    "http://www.proveedoradeclimas.com/",
                    "wSalidaAlmacen",
                    "http://www.proveedoradeclimas.com/wSalidaAlmacen",
                    true);

            client.agregarHeader("Bismarck", "St0ckageD3Bienz");
            client.agregarParametro("as_xml", ps_salida);
            client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

            if(client.llamarWs() == true) {
                return client.obtenResultado();
            }
        }

        return client.obtenError();
    }

    public static String imprimirSalida(MyApplication App, String ps_salida) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPointImpresion(),
                "http://www.proveedoradeclimas.com/",
                "Imprime_Nota_Salida",
                "http://www.proveedoradeclimas.com/Imprime_Nota_Salida",
                false);

        client.agregarParametro("as_xml", ps_salida);

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            return client.obtenError();
        }
    }

    public static String getXmlNotaSalida(MyApplication App, String ps_remision) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "XML_Nota_Salida",
                "http://www.proveedoradeclimas.com/XML_Nota_Salida",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_numproy", App.getNumproy());
        client.agregarParametro("as_remision", ps_remision);

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            return client.obtenError();
        }
    }

    /**************************************** INVENTARIO ******************************************/
    public static String guardarInventario (MyApplication App, String ps_inventario) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "wInventarioAlmacen",
                "http://www.proveedoradeclimas.com/wInventarioAlmacen",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_xml", ps_inventario);
        client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            App.changeEndPoint();

            client = new CallWs(
                    App.getEndPoint(),
                    "http://www.proveedoradeclimas.com/",
                    "wInventarioAlmacen",
                    "http://www.proveedoradeclimas.com/wInventarioAlmacen",
                    true);

            client.agregarHeader("Bismarck", "St0ckageD3Bienz");
            client.agregarParametro("as_xml", ps_inventario);
            client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

            if(client.llamarWs() == true) {
                return client.obtenResultado();
            }
        }

        return client.obtenError();
    }

    public static String isInventarioAbierto(MyApplication App) {

        CallWs client;

        if(!isNetworkAvailable(App)) {
            return "9No hay Conexion.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "Valida_Inventario_Abierto",
                "http://www.proveedoradeclimas.com/Valida_Inventario_Abierto",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            App.changeEndPoint();

            client = new CallWs(
                    App.getEndPoint(),
                    "http://www.proveedoradeclimas.com/",
                    "Valida_Inventario_Abierto",
                    "http://www.proveedoradeclimas.com/Valida_Inventario_Abierto",
                    true);

            client.agregarHeader("Bismarck", "St0ckageD3Bienz");
            client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

            if(client.llamarWs() == true) {
                return client.obtenResultado();
            }
        }

        return client.obtenError();
    }

    public static String getModelo(MyApplication app, String ps_serie) {
        CallWs client;

        if (!isNetworkAvailable(app)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                app.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "ObtenModelo",
                "http://www.proveedoradeclimas.com/ObtenModelo",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_serie", ps_serie);

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }

        return client.obtenError();
    }

    public static String firmaInventario(MyApplication app, String ps_xml) {
        CallWs client;

        if(!isNetworkAvailable(app)) {
            return "9No hay conexión";
        }

        client = new CallWs(
                app.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "wFirma_Inventario",
                "http://www.proveedoradeclimas.com/wFirma_Inventario",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_xml", ps_xml);

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }

        return client.obtenError();
    }

    public static String guardarSerieInventario(MyApplication app, String ps_serie, String ps_numdescarga, String ps_ubicacion, String ps_usuario) {
        //public string wInventario2(int ai_numdescarga, string as_serie, int ai_ubicacion, string as_usuario)
        CallWs client;

        if(!isNetworkAvailable(app)) {
            return "9No hay conexión";
        }

        client = new CallWs(
                app.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "wInventario2",
                "http://www.proveedoradeclimas.com/wInventario2",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("ai_numdescarga", ps_numdescarga);
        client.agregarParametro("as_serie", ps_serie);
        client.agregarParametro("ai_ubicacion", ps_ubicacion);
        client.agregarParametro("as_usuario", ps_usuario);

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }

        return client.obtenError();
    }

    /************************************** TRANSFERENCIA *****************************************/
    public static String buscarRemisionTransferencia(MyApplication App, String ps_remision) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "Valida_Transferencia",
                "http://www.proveedoradeclimas.com/Valida_Transferencia",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_sucursal", App.getNumproy());
        client.agregarParametro("as_remision", ps_remision);
        client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            App.changeEndPoint();

            client = new CallWs(
                    App.getEndPoint(),
                    "http://www.proveedoradeclimas.com/",
                    "Valida_Transferencia",
                    "http://www.proveedoradeclimas.com/Valida_Transferencia",
                    true);

            client.agregarHeader("Bismarck", "St0ckageD3Bienz");
            client.agregarParametro("as_sucursal", App.getNumproy());
            client.agregarParametro("as_remision", ps_remision);
            client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

            if(client.llamarWs() == true) {
                return client.obtenResultado();
            }
        }

        return client.obtenError();
    }

    public static String guardarTransferencia(MyApplication App, String ps_transferencia) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "wTransferenciaAlmacen",
                "http://www.proveedoradeclimas.com/wTransferenciaAlmacen",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_xml", ps_transferencia);
        client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            App.changeEndPoint();

            client = new CallWs(
                    App.getEndPoint(),
                    "http://www.proveedoradeclimas.com/",
                    "wTransferenciaAlmacen",
                    "http://www.proveedoradeclimas.com/wTransferenciaAlmacen",
                    true);

            client.agregarHeader("Bismarck", "St0ckageD3Bienz");
            client.agregarParametro("as_xml", ps_transferencia);
            client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

            if(client.llamarWs() == true) {
                return client.obtenResultado();
            }
        }

        return client.obtenError();
    }

    public static String imprimirTransferencia(MyApplication App, String ps_transferencia) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPointImpresion(),
                "http://www.proveedoradeclimas.com/",
                "Imprime_Nota_Transferencia",
                "http://www.proveedoradeclimas.com/Imprime_Nota_Transferencia",
                false);

        client.agregarParametro("as_xml", ps_transferencia);

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            return client.obtenError();
        }
    }

    public static String getXmlNotaTransferencia(MyApplication App, String ps_remision) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "XML_Nota_Transferencia",
                "http://www.proveedoradeclimas.com/XML_Nota_Transferencia",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_numproy", App.getNumproy());
        client.agregarParametro("as_remision", ps_remision);

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            return client.obtenError();
        }
    }

    /*************************************** DEVOLUCION *******************************************/
    public static String buscarRemisionDevolucion(MyApplication App, String ps_remision) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "Valida_Devolucion",
                "http://www.proveedoradeclimas.com/Valida_Devolucion",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_sucursal", App.getNumproy());
        client.agregarParametro("as_remision", ps_remision);

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            App.changeEndPoint();

            client = new CallWs(
                    App.getEndPoint(),
                    "http://www.proveedoradeclimas.com/",
                    "Valida_Devolucion",
                    "http://www.proveedoradeclimas.com/Valida_Devolucion",
                    true);

            client.agregarHeader("Bismarck", "St0ckageD3Bienz");
            client.agregarParametro("as_sucursal", App.getNumproy());
            client.agregarParametro("as_remision", ps_remision);

            if(client.llamarWs() == true) {
                return client.obtenResultado();
            }
        }

        return client.obtenError();
    }

    public static String guardarDevolucion(MyApplication App, String ps_devolucion) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "wDevolucionAlmacen",
                "http://www.proveedoradeclimas.com/wDevolucionAlmacen",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_xml", ps_devolucion);
        client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            App.changeEndPoint();

            client = new CallWs(
                    App.getEndPoint(),
                    "http://www.proveedoradeclimas.com/",
                    "wDevolucionAlmacen",
                    "http://www.proveedoradeclimas.com/wDevolucionAlmacen",
                    true);

            client.agregarHeader("Bismarck", "St0ckageD3Bienz");
            client.agregarParametro("as_xml", ps_devolucion);
            client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

            if(client.llamarWs() == true) {
                return client.obtenResultado();
            }
        }

        return client.obtenError();
    }

    public static String imprimirDevolucion(MyApplication App, String ps_devolucion) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPointImpresion(),
                "http://www.proveedoradeclimas.com/",
                "Imprime_Nota_Devolucion",
                "http://www.proveedoradeclimas.com/Imprime_Nota_Devolucion",
                false);

        client.agregarParametro("as_xml", ps_devolucion);

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            return client.obtenError();
        }
    }

    /***************************************** AJUSTE *********************************************/
    public static String guardarAjuste(MyApplication App, String ps_ajuste) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "wAjustesAlmacen",
                "http://www.proveedoradeclimas.com/wAjustesAlmacen",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_xml", ps_ajuste);
        client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            App.changeEndPoint();

            client = new CallWs(
                    App.getEndPoint(),
                    "http://www.proveedoradeclimas.com/",
                    "wAjustesAlmacen",
                    "http://www.proveedoradeclimas.com/wAjustesAlmacen",
                    true);

            client.agregarHeader("Bismarck", "St0ckageD3Bienz");
            client.agregarParametro("as_xml", ps_ajuste);
            client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

            if(client.llamarWs() == true) {
                return client.obtenResultado();
            }
        }

        return client.obtenError();
    }

    /**************************************** APARTADO ********************************************/
    public static String buscarRemisionApartado(MyApplication App, String ps_remision) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "Valida_Apartado",
                "http://www.proveedoradeclimas.com/Valida_Apartado",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_sucursal", App.getNumproy());
        client.agregarParametro("as_remision", ps_remision);
        client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            App.changeEndPoint();

            client = new CallWs(
                    App.getEndPoint(),
                    "http://www.proveedoradeclimas.com/",
                    "Valida_Apartado",
                    "http://www.proveedoradeclimas.com/Valida_Apartado",
                    true);

            client.agregarHeader("Bismarck", "St0ckageD3Bienz");
            client.agregarParametro("as_sucursal", App.getNumproy());
            client.agregarParametro("as_remision", ps_remision);
            client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

            if(client.llamarWs() == true) {
                return client.obtenResultado();
            }
        }

        return client.obtenError();
    }

    public static String guardarApartado(MyApplication App, String ps_apartado) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "wApartadoAlmacen",
                "http://www.proveedoradeclimas.com/wApartadoAlmacen",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_xml", ps_apartado);
        client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            App.changeEndPoint();

            client = new CallWs(
                    App.getEndPoint(),
                    "http://www.proveedoradeclimas.com/",
                    "wApartadoAlmacen",
                    "http://www.proveedoradeclimas.com/wApartadoAlmacen",
                    true);

            client.agregarHeader("Bismarck", "St0ckageD3Bienz");
            client.agregarParametro("as_xml", ps_apartado);
            client.agregarParametro("ai_num_almacen", App.getNumAlmacen());

            if(client.llamarWs() == true) {
                return client.obtenResultado();
            }
        }

        return client.obtenError();
    }

    /**************************************** GENERAL *********************************************/
    public static String getXmlAlmacenes(MyApplication App) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "XML_Almacen",
                "http://www.proveedoradeclimas.com/XML_Almacenes",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_numproy", App.getNumproy());

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            App.changeEndPoint();

            client = new CallWs(
                    App.getEndPoint(),
                    "http://www.proveedoradeclimas.com/",
                    "XML_Almacen",
                    "http://www.proveedoradeclimas.com/XML_Almacenes",
                    true);

            client.agregarHeader("Bismarck", "St0ckageD3Bienz");
            client.agregarParametro("as_numproy", App.getNumproy());

            if(client.llamarWs() == true) {
                return client.obtenResultado();
            }
        }

        return client.obtenError();
    }

    public static String autorizarUsuario(MyApplication App, String ps_credenciales) {

        CallWs client;

        if (!isNetworkAvailable(App)) {
            return "9No hay conexión.";
        }

        client = new CallWs(
                App.getEndPoint(),
                "http://www.proveedoradeclimas.com/",
                "Valida_Login",
                "http://www.proveedoradeclimas.com/Valida_Login",
                true);

        client.agregarHeader("Bismarck", "St0ckageD3Bienz");
        client.agregarParametro("as_xml", ps_credenciales);

        if(client.llamarWs() == true) {
            return client.obtenResultado();
        }
        else {
            App.changeEndPoint();

            client = new CallWs(
                    App.getEndPoint(),
                    "http://www.proveedoradeclimas.com/",
                    "Valida_Login",
                    "http://www.proveedoradeclimas.com/Valida_Login",
                    true);

            client.agregarHeader("Bismarck", "St0ckageD3Bienz");
            client.agregarParametro("as_xml", ps_credenciales);

            if(client.llamarWs() == true) {
                return client.obtenResultado();
            }
        }

        return client.obtenError();
    }

    private static boolean isNetworkAvailable(MyApplication App) {

        ConnectivityManager connectivityManager = (ConnectivityManager) App.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
}
