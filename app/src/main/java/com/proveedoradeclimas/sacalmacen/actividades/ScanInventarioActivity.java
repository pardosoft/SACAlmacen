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

public class ScanInventarioActivity extends AppCompatActivity {

    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action

    private ScannerManager miScannerMT02; //scanner viejito, el de siempre :D
    private ScanManager miScannerWepoy; //scanner el nuevo :/
    private int tipoScanner;
    private ArrayList<Equipo> items;
    private GridViewAdapter adapter;
    private AlmacenSql helper;
    private String nombreTabla;
    private String idZona = "";
    private String descZona = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_inventario);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.arrow_left);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ScanInventarioActivity.this)
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

        EditText edtxtSerie = (EditText) findViewById(R.id.edtxt_scaninventario_serie);

        edtxtSerie.setInputType(InputType.TYPE_NULL);

        items = new ArrayList<Equipo>();
        adapter = new GridViewAdapter(this);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        ListView gridview = (ListView) findViewById(R.id.lista_scaninventario_equipo);
        gridview.setAdapter(adapter);
        gridview.setLongClickable(true);

        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                final int serie_seleccionada = position;

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
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
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
                byte[] barcode = intent.getByteArrayExtra("barcode");
                int barcodelen = intent.getIntExtra("length", 0);
                byte temp = intent.getByteExtra("barcodeType", (byte) 0);

                String barcodeStr = new String(barcode, 0, barcodelen);

                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                String fecha = dateFormat.format(cal.getTime()).toString();

                zMisSonidos.playSound(getApplicationContext(), zMisSonidos.Scan);
                procesaCadena(barcodeStr, fecha);

            } catch(Exception e) {
                e.printStackTrace();
                Toast.makeText(ScanInventarioActivity.this, e.getMessage() + ". - " + e.getCause(), Toast.LENGTH_LONG).show();
            }
        }

    };

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (tipoScanner == 1) unregisterReceiver(mScanReceiver);
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
                        ScanInventarioActivity.super.onBackPressed();
                    }
                }).create().show();
    }

    private void setScanners() {

        if (Build.MANUFACTURER.equals("UBX") && (Build.MODEL.equals("SQ42") || Build.MODEL.equals("S95") || Build.MODEL.equals("S95H"))) {

            tipoScanner = 1;

            miScannerWepoy = new ScanManager();
            miScannerWepoy.openScanner();

            miScannerWepoy.unlockTrigger();

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

    private void checaInformacionAnterior() {

        helper = new AlmacenSql(getApplicationContext(), nombreTabla );
        final SQLiteDatabase db = helper.getWritableDatabase();
        final Cursor cursor = db.rawQuery("SELECT _id, Serie, descZona FROM " + nombreTabla + " ORDER BY _id DESC ", null);

        if(cursor.moveToFirst())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            Equipo miEquipo;

                            while(!cursor.isAfterLast())
                            {
                                miEquipo = new Equipo();
                                miEquipo.Serie = cursor.getString(1);
                                miEquipo.Zona = cursor.getString(2);
                                items.add(miEquipo);
                                cursor.moveToNext();
                            }

                            adapter.notifyDataSetChanged();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            db.execSQL("Delete From " + nombreTabla);
                            break;
                    }

                    cursor.close();
                    db.close();
                }
            };

            builder.setMessage(
                    "Se encontró información anterior, ¿Desea continuar usandola?")
                    .setTitle("SAC Android")
                    .setPositiveButton("Sí", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
        else
        {
            cursor.close();
            db.close();
        }
    }

    private  void procesaCadena(String line, String fecha) {

        String ls_opcion = line.substring(0, 1);
        EditText E_serie = (EditText) findViewById(R.id.edtxt_scaninventario_serie);

        if (ls_opcion.equals("Z")) {
            if (line.substring(0, 2).equals("ZX") && line.contains("+")) {
                String zonaCompleta = line.substring(2);
                int index = zonaCompleta.indexOf('+');
                idZona = zonaCompleta.substring(0, index);
                descZona = zonaCompleta.substring(index + 1);
                Toast.makeText(this,
                        "Zona Escaneada: " + descZona,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (idZona.trim().length() == 0 && descZona.trim().length() == 0) {
                errorEntrada("Primero debe Escanear una Zona");
                return;
            }
            else {
                if (!validaSerie(line)) {
                    return;
                }
            }
        }
        else if (ls_opcion.equals("1")) {
            if(idZona.trim().length() == 0 && descZona.trim().length() == 0) {
                errorEntrada("Primero debe Escanear una Zona");
                return;
            }
            else {
                if (line.substring(0, 2).equals("1P")) {
                    errorEntrada("No se escaneo Serie.");
                    return;
                }
                if (!validaSerie(line)) {
                    return;
                }
            }
        }
        else {
            if(idZona.trim().length() == 0 && descZona.trim().length() == 0) {
                errorEntrada("Primero debe Escanear una Zona");
                return;
            }
            else {
                if (ls_opcion.equals("S")) {
                    if (!validaSerie(line.substring(1))) {
                        return;
                    }
                } else if (!validaSerie(line)) {
                    return;
                }
            }
        }

        Equipo miEquipo = new Equipo();

        miEquipo.Serie = E_serie.getText().toString();
        miEquipo.Zona = descZona;
        items.add(0, miEquipo);

        SQLiteDatabase db = helper.getWritableDatabase();

        db.execSQL("INSERT INTO " + nombreTabla + " (Serie, Fecha, idZona, descZona) " +
                "VALUES ('" + miEquipo.Serie + "','" + fecha + "','" + idZona + "','" + miEquipo.Zona + "')");
        db.close();

        adapter.notifyDataSetChanged();

        E_serie.setText("");
    }

    private void errorEntrada(String texto)
    {
        try {
            EditText edtxtSerie = (EditText) findViewById(R.id.edtxt_scaninventario_serie);

            zMisSonidos.playSound(this.getBaseContext(), zMisSonidos.Wrong);
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

        EditText edtxtSerie = (EditText) findViewById(R.id.edtxt_scaninventario_serie);
        edtxtSerie.setText(temp_serie);

        return true;
    }

    private class Equipo
    {
        public String Serie;
        public String Zona;
    }
    static class misDatos
    {
        public TextView _Serie;
        public TextView _Zona;
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
	    	/*
	        if(convertView == null) {
	        	// if it's not recycled, initialize some attributes
	        	v = new misDatos();
	        	convertView = mInflater.inflate(R.layout.customgrid, parent, false);
	        	v._Modelo = (TextView)convertView.findViewById(R.id.txtModelo);
	        	v._Serie = (TextView)convertView.findViewById(R.id.txtSerie);

				if(position == 0)
				{
					convertView.setTag(v);
				}
	        } else {
	        	if (convertView.getTag() != null)
	        	{
	        		v = (misDatos) convertView.getTag();
	        	}
	        	else
	        	{
	        		return convertView;
	        	}
	        }
	        */

            v = new misDatos();
            convertView = mInflater.inflate(R.layout.layout_inventario_row_list, parent, false);
            v._Serie = (TextView)convertView.findViewById(R.id.txt_layout_inventario_serie);
            v._Zona = (TextView)convertView.findViewById(R.id.txt_layout_inventario_zona);

            if(position == 0)
            {
                convertView.setTag(v);
            }

            if (position <= items.size())
            {
                v._Serie.setText(items.get(position).Serie);
                v._Zona.setText(items.get(position).Zona);
            }

            TextView txtcount = (TextView) findViewById(R.id.txt_scaninventario_contador);

            txtcount.setText(String.valueOf(items.size()));

            return convertView;
        }
    }
}
