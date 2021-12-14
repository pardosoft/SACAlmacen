package com.proveedoradeclimas.sacalmacen.actividades;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Triggering;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.proveedoradeclimas.sacalmacen.R;
import com.proveedoradeclimas.sacalmacen.clases.ScannerManager;
import com.proveedoradeclimas.sacalmacen.clases.zMisSonidos;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TestActivity extends AppCompatActivity {

    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action

    private ScanManager mScanManager; //scanner el nuevo
    private ScannerManager scannerManager; //scanner geofanci speedata mt02
    private int tipoScanner;

    EditText edtxtSerie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        edtxtSerie = (EditText)findViewById(R.id.edtxt_scanlinea_serie);
        edtxtSerie.setInputType(InputType.TYPE_NULL);

        if (Build.MANUFACTURER.equals("UBX") && Build.MODEL.equals("SQ42")) {

            tipoScanner = 1;

            mScanManager = new ScanManager();
            mScanManager.openScanner();

            mScanManager.unlockTrigger();

            mScanManager.switchOutputMode(0);
            mScanManager.setTriggerMode(Triggering.HOST);

            IntentFilter filter = new IntentFilter();
            filter.addAction(SCAN_ACTION);
            registerReceiver(mScanReceiver, filter);
        }
        else if (Build.MANUFACTURER.equals("geofanci") && Build.MODEL.equals("PDA")) {

            tipoScanner = 0;

            scannerManager = new ScannerManager(getBaseContext());
        }
    }

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            try {
                byte[] barcode = intent.getByteArrayExtra("barcode");
                int barcodelen = intent.getIntExtra("length", 0);
                byte temp = intent.getByteExtra("barcodeType", (byte) 0);

                String barcodeStr = new String(barcode, 0, barcodelen);

                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                String fecha = dateFormat.format(cal.getTime()).toString();

                zMisSonidos.playSound(getApplicationContext(), zMisSonidos.Scan);
                //procesaCadena(barcodeStr, fecha);
                edtxtSerie.setText("");
                edtxtSerie.setText(barcodeStr);

            } catch(Exception e) {
                e.printStackTrace();
                Toast.makeText(TestActivity.this, e.getMessage() + ". - " + e.getCause(), Toast.LENGTH_LONG).show();
            }
        }

    };

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        initScan();
        edtxtSerie.setText("");
        IntentFilter filter = new IntentFilter();
        int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
        String[] value_buf = mScanManager.getParameterString(idbuf);
        if(value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
            filter.addAction(value_buf[0]);
        } else {
            filter.addAction(SCAN_ACTION);
        }

        registerReceiver(mScanReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if(mScanManager != null) {
            mScanManager.stopDecode();
            //isScaning = false;
        }
        unregisterReceiver(mScanReceiver);
    }

    private void initScan() {
        // TODO Auto-generated method stub
        mScanManager = new ScanManager();
        mScanManager.openScanner();

        mScanManager.switchOutputMode(0);
    }
}
