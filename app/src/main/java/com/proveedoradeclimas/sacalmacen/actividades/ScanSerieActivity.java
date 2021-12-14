package com.proveedoradeclimas.sacalmacen.actividades;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.device.ScanManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.proveedoradeclimas.sacalmacen.R;
import com.proveedoradeclimas.sacalmacen.clases.AlmacenSql;
import com.proveedoradeclimas.sacalmacen.clases.ScannerManager;
import com.proveedoradeclimas.sacalmacen.clases.zMisSonidos;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.proveedoradeclimas.sacalmacen.clases.zMisSonidos.Scan;
import static com.proveedoradeclimas.sacalmacen.clases.zMisSonidos.Wrong;

public class ScanSerieActivity extends AppCompatActivity {

    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action

    private ScannerManager miScannerMT02; //scanner viejito, el de siempre :D
    private ScanManager miScannerWepoy; //scanner el nuevo :/
    private int tipoScanner;
    private ArrayList<Equipo> items;
    private GridViewAdapter adapter;
    private AlmacenSql helper;
    private String nombreTabla;

    private boolean isScannerLocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_serie);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.arrow_left);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ScanSerieActivity.this)
                        .setTitle("SAC Android")
                        .setMessage("¿Ha terminado de Escanear?")
                        .setCancelable(false)
                        .setNegativeButton("No", null)
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface arg0, int arg1) {
                                finish();
                            }
                        }).create().show();
            }
        });

        setScanners();

        /***/
        Bundle extra = getIntent().getExtras();
        nombreTabla = extra.getString("NombreTabla");
        /***/

        EditText edtxtSerie = (EditText) findViewById(R.id.edtxt_scanserie_serie);

        edtxtSerie.setInputType(InputType.TYPE_NULL);

        items = new ArrayList<Equipo>();
        adapter = new GridViewAdapter(this);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        ListView gridview = (ListView) findViewById(R.id.lista_scanserie_equipo);
        gridview.setAdapter(adapter);
        gridview.setLongClickable(true);

        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                final int serie_seleccionada = position;

                if(tipoScanner != 0) bloquearScanner();

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                Log.d("TAG", "Entrada: " + serie_seleccionada);

                                SQLiteDatabase db = helper.getWritableDatabase();

                                db.execSQL("DELETE FROM "+ nombreTabla + " WHERE Serie='" + items.get(serie_seleccionada).Serie + "'");
                                db.close();

                                items.remove(serie_seleccionada);
                                adapter.notifyDataSetChanged();

                                if(tipoScanner != 0) desbloquearScanner();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                if(tipoScanner != 0) desbloquearScanner();
                                break;
                        }
                    }
                };

                builder.setMessage(
                        "¿Desea Borrar la Serie: " + items.get(position).Serie
                                + "?")
                        .setTitle("SAC Android")
                        .setPositiveButton("Sí", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

                return false;
            }
        });

        checaInformacionAnterior();
    }

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            try {
                if(tipoScanner == 1) {
                    byte[] barcode = intent.getByteArrayExtra("barcode");
                    int barcodelen = intent.getIntExtra("length", 0);
                    byte temp = intent.getByteExtra("barcodeType", (byte) 0);

                    String barcodeStr = new String(barcode, 0, barcodelen);

                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Calendar cal = Calendar.getInstance();
                    String fecha = dateFormat.format(cal.getTime()).toString();

                    zMisSonidos.playSound(getApplicationContext(), Scan);
                    procesaCadena(barcodeStr, fecha);
                }
                else if(tipoScanner == 2 && !isScannerLocked) {
                    byte[] barcode = intent.getByteArrayExtra("barcode");
                    int barcodelen = intent.getIntExtra("length", 0);
                    byte temp = intent.getByteExtra("barcodeType", (byte) 0);

                    String barcodeStr = new String(barcode, 0, barcodelen);

                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Calendar cal = Calendar.getInstance();
                    String fecha = dateFormat.format(cal.getTime()).toString();

                    zMisSonidos.playSound(getApplicationContext(), Scan);
                    procesaCadena(barcodeStr, fecha);
                }
                else if(tipoScanner == 2 && isScannerLocked) {
                    zMisSonidos.playSound(getApplicationContext(), Wrong);
                }
                else {
                    Log.i("TAG", "No es Scanner nuevo");
                }
            } catch(Exception e) {
                e.printStackTrace();
                Toast.makeText(ScanSerieActivity.this, e.getMessage() + ". - " + e.getCause(), Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (tipoScanner != 0) unregisterReceiver(mScanReceiver);
        } catch (Exception e) {
            Log.i("TAG", "aaa veda!!1");
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        setScanners();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String linea = "";

        if (tipoScanner == 0 && (keyCode == 115 || keyCode == 135 || keyCode == 80)) {
            linea = miScannerMT02.ScanneLine();
        }

        if (linea.length() > 0)
        {
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            String fecha = dateFormat.format(cal.getTime()).toString();

            procesaCadena(linea, fecha);
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("SAC Android")
                .setMessage("¿Ha terminado de Escanear?")
                .setCancelable(false)
                .setNegativeButton("No", null)
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        if(tipoScanner == 1) {
                            miScannerWepoy.lockTrigger();
                            miScannerWepoy.closeScanner();
                        }
                        ScanSerieActivity.super.onBackPressed();
                    }
                }).create().show();
    }

    private void setScanners() {

        if (Build.MANUFACTURER.equals("UBX") && (Build.MODEL.equals("SQ42") || Build.MODEL.equals("S95") || Build.MODEL.equals("S95H"))) {

            miScannerWepoy = new ScanManager();
            miScannerWepoy.openScanner();

            if(Build.PRODUCT.equals("i6200S")) {
                tipoScanner = 2;
                desbloquearScanner();
            }
            else {
                tipoScanner = 1;
                miScannerWepoy.unlockTrigger();
            }

            miScannerWepoy.switchOutputMode(0);

            IntentFilter filter = new IntentFilter();
            filter.addAction(SCAN_ACTION);
            registerReceiver(mScanReceiver, filter);
        }
        else if (Build.MANUFACTURER.equals("geofanci") && Build.MODEL.equals("PDA")) {

            tipoScanner = 0;

            miScannerMT02 = new ScannerManager(getBaseContext());
        }
    }

    private void bloquearScanner() {
        if(tipoScanner == 1) {
            //bueno
            miScannerWepoy.lockTrigger();
        }
        else if(tipoScanner == 2) {
            //malo
            isScannerLocked = true;
        }
        //0 no cuenta
    }

    private void desbloquearScanner() {
        if(tipoScanner == 1) {
            miScannerWepoy.unlockTrigger();
        }
        else if(tipoScanner == 2) {
            isScannerLocked = false;
        }
        //0 no cuenta
    }

    private void checaInformacionAnterior() {

        helper = new AlmacenSql(getApplicationContext(), nombreTabla);
        final SQLiteDatabase mDB = helper.getWritableDatabase();
        final Cursor cursor = mDB.rawQuery("SELECT Serie FROM " + nombreTabla, null);

        if(cursor.moveToFirst())
        {
            int count = cursor.getCount();

            if(count > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);

                if(tipoScanner != 0) bloquearScanner();

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Equipo miEquipo;

                                while(!cursor.isAfterLast()) {
                                    miEquipo = new Equipo();
                                    miEquipo.Serie = cursor.getString(0);
                                    items.add(miEquipo);
                                    cursor.moveToNext();
                                }

                                adapter.notifyDataSetChanged();
                                if(tipoScanner != 0) desbloquearScanner();

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                mDB.execSQL("DELETE FROM " + nombreTabla);
                                if(tipoScanner != 0) desbloquearScanner();
                                break;
                        }

                        cursor.close();
                        mDB.close();
                    }
                };

                builder.setMessage(
                        "Se encontraron " + Integer.toString(count) + " Series escaneadas, ¿Desea continuar usandolas?")
                        .setTitle("SAC Android")
                        .setPositiveButton("Sí", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
            else {
                cursor.close();
                mDB.close();
            }
        }
        else
        {
            cursor.close();
            mDB.close();
        }
    }

    private void procesaCadena(String line, String fecha) {

        String ls_opcion = line.substring(0, 1);
        EditText edtxtSerie = (EditText) findViewById(R.id.edtxt_scanserie_serie);

        if (ls_opcion.equals("1")) {

            if(line.substring(0, 2).equals("1P")) {

                errorEntrada("No se escaneo Serie.");
                return;
            }

            if(!validaSerie(line)){return;}
        }
        else {

            if(ls_opcion.equals("S")) {

                if(!validaSerie(line.substring(1))){return;}
            }
            else if(!validaSerie(line)){return;}
        }

        Equipo miEquipo = new Equipo();

        miEquipo.Serie = edtxtSerie.getText().toString();
        items.add(0, miEquipo);

        SQLiteDatabase db = helper.getWritableDatabase();

        db.execSQL("INSERT INTO " + nombreTabla + " (Serie) VALUES ('" + miEquipo.Serie + "')");
        db.close();

        adapter.notifyDataSetChanged();

        edtxtSerie.setText("");
    }

    private void errorEntrada(String texto)
    {
        try {
            EditText edtxtSerie = (EditText) findViewById(R.id.edtxt_scanserie_serie);

            zMisSonidos.playSound(getApplicationContext(), zMisSonidos.Wrong);
            zMisSonidos.playSound(getApplicationContext(), zMisSonidos.Wrong);
            Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
            edtxtSerie.setText("");
        } catch (Exception e)
        {
            Log.i("TAG", e.getMessage());
        }
    }

    private boolean validaSerie(String temp_serie)
    {
        for(int i = 0; i < items.size(); i++) {

            if(items.get(i).Serie.equals(temp_serie))
            {
                errorEntrada("La serie " + temp_serie + " ya fue escaneada.");
                return false;
            }
        }

        EditText edtxtSerie = (EditText) findViewById(R.id.edtxt_scanserie_serie);
        edtxtSerie.setText(temp_serie);

        return true;
    }

    private class Equipo
    {
        public String Serie;
    }
    static class misDatos
    {
        public TextView _Serie;
    }
    public class GridViewAdapter extends BaseAdapter {
        @SuppressWarnings("unused")
        private Context mContext;
        private LayoutInflater mInflater;

        public GridViewAdapter(Context c) {
            mContext = c;
            mInflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            misDatos v = null;

            v = new misDatos();
            convertView = mInflater.inflate(R.layout.layout_serie_row_list, parent, false);
            v._Serie = (TextView)convertView.findViewById(R.id.txt_layout_serie_serie);

            if(position == 0)
            {
                convertView.setTag(v);
            }

            if (position <= items.size())
            {
                v._Serie.setText(items.get(position).Serie);
            }

            TextView txtcount = (TextView) findViewById(R.id.txt_scanserie_contador);

            txtcount.setText(String.valueOf(items.size()));

            return convertView;
        }
    }
}
