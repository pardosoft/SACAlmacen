package com.proveedoradeclimas.sacalmacen.clases;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrador on 12/09/2016.
 */
public class ScannerManager {

    private DeviceControl DevCtrl;
    private static final String TAG = "ScannerManger";
    private SerialPort mSerialPort;
    public int fd;
    final private Context _Contexto;
    private boolean Powered = false;
    private static Timer retrig_timer = new Timer();
    private boolean key_start = true;

    public ScannerManager(Context miContexto){
        _Contexto = miContexto;
        ActivaScanner();
    }

    private void ActivaScanner()
    {
        try {
            DevCtrl = new DeviceControl("/proc/driver/scan");
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "Driver no encontrado.", e);

            new AlertDialog.Builder(_Contexto).setTitle("SAC - ERROR").setMessage("NO SE ENCONTRÓ EL SCANER: " + e.getMessage()).setPositiveButton("CERRAR", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((Activity) _Contexto).finish();
                }
            }).show();

            return;
        }

        try {
            mSerialPort = new SerialPort("/dev/eser0", 9600);//35
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "Puerto Inaccesible");
            e.printStackTrace();

            new AlertDialog.Builder(_Contexto).setTitle("SAC - ERROR").setMessage("NO SE ENCONTRÓ EL PUERTO: " + e.getMessage()).setPositiveButton("CERRAR", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((Activity) _Contexto).finish();
                }
            }).show();

            try {
                DevCtrl.DeviceClose();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return;
        }

        fd = mSerialPort.getFd();

        //if(fd > 0){
        //	Log.d(TAG,"Abierto " + fd);
        //}
    }
    class RetrigTask extends TimerTask
    {
        @Override
        public void run() {
            try {
                DevCtrl.TriggerOffDevice();
            } catch (IOException e) {
                e.printStackTrace();
            }
            key_start = false;
            Powered = false;
        }
    }
    public String ScanneLine()
    {
        try {
            String l_buffer = "";

            if(Powered == false)
            {
                Powered = true;
                DevCtrl.PowerOnDevice();
            }

            retrig_timer = new Timer();
            retrig_timer.schedule(new RetrigTask(), 2000);	//start a timer, if the data is not received within a period of time, stop the scan.

            fd = mSerialPort.getFd();
            DevCtrl.TriggerOnDevice();
            key_start = true;
            //Log.d(TAG, "Lectura Inicia");

            while(key_start)
            {
                try {

                    l_buffer = mSerialPort.ReadSerial(fd, 1024);

                    if(l_buffer != null){
                        l_buffer = l_buffer.replace("*", "").trim();
                        retrig_timer.cancel();
                        DevCtrl.TriggerOffDevice();
                        key_start = false;
                        Powered = false;
                        Log.d(TAG, "Buffer: " + l_buffer);

                        if (checaCadena(l_buffer)){
                            zMisSonidos.playSound(_Contexto, zMisSonidos.Scan);
                            return l_buffer;
                        }
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "Error de Lectura: " + e.getMessage());
            e.printStackTrace();
        }

        //Log.d(TAG, "Lectura Termina");

        return "";
    }

    private boolean checaCadena(String ps_cadena)
    {
        if (ps_cadena.contains("$")) { return false; }
        if (ps_cadena.contains("%")) { return false; }
        if (ps_cadena.contains("&")) { return false; }
        if (ps_cadena.contains("'")) { return false; }

        return true;
    }
}
