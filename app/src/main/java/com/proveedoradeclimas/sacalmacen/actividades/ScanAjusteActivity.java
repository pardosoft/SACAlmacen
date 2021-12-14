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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import java.util.ArrayList;

public class ScanAjusteActivity extends AppCompatActivity {

    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action

    private ScannerManager miScannerMT02; //scanner viejito, el de siempre :D
    private ScanManager miScannerWepoy; //scanner el nuevo :/
    private int tipoScanner;
    private ArrayList<Equipo> items;
    private GridViewAdapter adapter;
    private AlmacenSql helper;
    private String nombreTabla;
    private int ajusteDefault = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_ajuste);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.arrow_left);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ScanAjusteActivity.this)
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

        items = new ArrayList<Equipo>();
        adapter = new GridViewAdapter(this);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        ListView gridview = (ListView) findViewById(R.id.lista_scanajuste_equipo);
        gridview.setAdapter(adapter);
        gridview.setLongClickable(true);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                // TODO Auto-generated method stub
                String serieSeleccionada = items.get(position).serie;
                showCustomDialog(1, serieSeleccionada, "", position);
            }

        });

        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                final int serie_seleccionada = position;

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:

                                SQLiteDatabase db = helper.getWritableDatabase();

                                db.execSQL("DELETE FROM "+ nombreTabla + " WHERE Serie='" + items.get(serie_seleccionada).serie + "'");
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
                        "¿Desea Borrar la Serie: " + items.get(position).serie
                                + "?")
                        .setTitle("SAC Android")
                        .setPositiveButton("Sí", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

                return false;
            }
        });

        gridview.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if(keyCode == KeyEvent.KEYCODE_ENTER) {
                    return true;
                }
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

                //Toast.makeText(ScanModeloSerieActivity.this, "----codetype--" + temp, Toast.LENGTH_LONG).show();
                String barcodeStr = new String(barcode, 0, barcodelen);
                //Toast.makeText(ScanModeloSerieActivity.this, barcodeStr, Toast.LENGTH_LONG).show();

                zMisSonidos.playSound(getApplicationContext(), zMisSonidos.Scan);
                procesaCadena(barcodeStr);

            } catch(Exception e) {
                e.printStackTrace();
                Toast.makeText(ScanAjusteActivity.this, e.getMessage() + ". - " + e.getCause(), Toast.LENGTH_LONG).show();
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
            procesaCadena(linea);
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
                        ScanAjusteActivity.super.onBackPressed();
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

        helper = new AlmacenSql(getApplicationContext(), nombreTabla);
        final SQLiteDatabase mDB = helper.getWritableDatabase();
        final Cursor cursor = mDB.rawQuery("SELECT Modelo, Serie, descAjuste FROM " + nombreTabla, null);

        if(cursor.moveToFirst())
        {
            int count = cursor.getCount();

            if(count > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Equipo miEquipo;

                                while(!cursor.isAfterLast()) {
                                    miEquipo = new Equipo();
                                    miEquipo.modelo = cursor.getString(0);
                                    miEquipo.serie = cursor.getString(1);
                                    miEquipo.tipoAjuste = cursor.getString(2);
                                    items.add(miEquipo);
                                    cursor.moveToNext();
                                }

                                adapter.notifyDataSetChanged();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                mDB.execSQL("DELETE FROM " + nombreTabla);
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

    private void procesaCadena(String line) {

        String ls_opcion = line.substring(0, 1);
        EditText edtxtModelo = (EditText) findViewById(R.id.edtxt_scanajuste_modelo);
        EditText edtxtSerie = (EditText) findViewById(R.id.edtxt_scanajuste_serie);

        if (ls_opcion.equals("1")) {
            if(edtxtModelo.getText().length() > 0) {
                if(line.substring(0, 2).equals("1P")) {
                    errorEntrada("No se escaneo Serie.");
                    return;
                }
                if(edtxtModelo.getText().toString().equals(line.substring(2))) {
                    errorEntrada("Modelo escaneado de nuevo.");
                    return;
                }
                if(!validaSerie(line)){return;}
            }
            else {
                edtxtModelo.setText(line.substring(2));
            }
        }
        else {
            if (ls_opcion.equals("S")) {
                if(edtxtModelo.getText().length() > 0) {
                    if(!validaSerie(line.substring(1))){return;}
                }
                else {
                    errorEntrada("Capture Primero el Modelo.");
                    return;
                }
            }
            else {
                if(edtxtModelo.getText().length() == 0) {
                    edtxtModelo.setText(line);
                }
                else {
                    if(edtxtModelo.getText().toString().equals(line)) {
                        errorEntrada("Modelo escaneado de nuevo.");
                        return;
                    }

                    if(edtxtSerie.getText().length() == 0) {
                        if(!validaSerie(line)){return;}
                    }
                }
            }
        }

        Log.i("TAG", "Entrada: " + line);

        if(edtxtSerie.getText().toString().length() > 0) {
            showCustomDialog(0, "", "", 0);
        }

    }

    public void showCustomDialog(final int opcion, final String serie, final String fecha, final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ScanAjusteActivity.this);

        builder.setSingleChoiceItems(R.array.tipoAjustes, ajusteDefault, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                String lista[] = ScanAjusteActivity.this.getResources().getStringArray(R.array.tipoAjustes);
                if(opcion == 0) {
                    insertSerie(which + 1, lista[which], fecha);
                }
                else if(opcion == 1) {
                    updateSerie(serie, which + 1, lista[which], position);
                }
                ajusteDefault = which;
                dialog.dismiss();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
                EditText edtxtModelo = (EditText)findViewById(R.id.edtxt_scanajuste_modelo);
                EditText edtxtSerie = (EditText)findViewById(R.id.edtxt_scanajuste_serie);
                edtxtModelo.setText("");
                edtxtSerie.setText("");
                edtxtModelo.requestFocus();
            }
        });

        builder.setTitle("Elija el tipo de Ajuste");
        builder.create().show();
    }

    public void insertSerie(int idAjuste, String descAjuste, String fecha) {

        EditText edtxtModelo = (EditText)findViewById(R.id.edtxt_scanajuste_modelo);
        EditText edtxtSerie = (EditText)findViewById(R.id.edtxt_scanajuste_serie);

        Equipo miEquipo = new Equipo();

        miEquipo.modelo = edtxtModelo.getText().toString();
        miEquipo.serie = edtxtSerie.getText().toString();
        miEquipo.tipoAjuste = descAjuste;
        items.add(0, miEquipo);

        SQLiteDatabase db = helper.getWritableDatabase();

        db.execSQL("INSERT INTO " + nombreTabla + " (Modelo, Serie, idAjuste, descAjuste) "
                + "VALUES ('" + miEquipo.modelo + "', "
                + "'" + miEquipo.serie + "', "
                + Integer.toString(idAjuste) + ", "
                + "'" + miEquipo.tipoAjuste + "')");
        db.close();

        adapter.notifyDataSetChanged();

        edtxtSerie.setText("");
        edtxtModelo.setText("");
        edtxtModelo.requestFocus();
    }

    public void updateSerie(String serie, int idAjuste, String descAjuste, int position) {

        SQLiteDatabase mDB = helper.getWritableDatabase();

        mDB.execSQL("UPDATE " + nombreTabla
                + " SET idAjuste = " + Integer.toString(idAjuste)
                + ", descAjuste = '" + descAjuste
                + "' WHERE Serie = '" + serie + "'");
        mDB.close();

        items.get(position).tipoAjuste = descAjuste;
        adapter.notifyDataSetChanged();
    }

    private void errorEntrada(String texto) {

        try {
            EditText edtxtModelo = (EditText)findViewById(R.id.edtxt_scanajuste_modelo);
            EditText edtxtSerie = (EditText)findViewById(R.id.edtxt_scanajuste_serie);

            zMisSonidos.playSound(this.getBaseContext(), zMisSonidos.Wrong);
            Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
            edtxtModelo.setText("");
            edtxtSerie.setText("");
        } catch (Exception e)
        {
            Log.d("TAG", "ErrorEntrada", e);
        }
    }

    private boolean validaSerie(String temp_serie) {

        for(int i = 0; i < items.size(); i++)
        {
            if(items.get(i).serie.equals(temp_serie))
            {
                errorEntrada("La serie " + temp_serie + " ya fue escaneada.");
                return false;
            }
        }

        EditText edtxtSerie = (EditText) findViewById(R.id.edtxt_scanajuste_serie);
        edtxtSerie.setText(temp_serie);

        return true;
    }

    private class Equipo {
        public String modelo;
        public String serie;
        public String tipoAjuste;
    }

    static class MisDatos {
        public TextView _modelo;
        public TextView _serie;
        public TextView _tipoAjuste;
    }

    public class GridViewAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater mInflater;

        public GridViewAdapter(Context c) {
            mContext = c;
            mInflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            MisDatos v = null;
            v = new MisDatos();

            convertView = mInflater.inflate(R.layout.layout_ajuste_row_list, parent, false);
            v._modelo = (TextView)convertView.findViewById(R.id.txt_layout_ajuste_modelo);
            v._serie = (TextView)convertView.findViewById(R.id.txt_layout_ajuste_serie);
            v._tipoAjuste = (TextView)convertView.findViewById(R.id.txt_layout_ajuste_tipoajuste);

            if(position == 0)
            {
                convertView.setTag(v);
            }

            if (position <= items.size())
            {
                v._modelo.setText(items.get(position).modelo);
                v._serie.setText(items.get(position).serie);
                v._tipoAjuste.setText(items.get(position).tipoAjuste);
            }

            TextView txtContador = (TextView)findViewById(R.id.txt_scanajuste_contador);
            txtContador.setText(String.valueOf(items.size()));

            return convertView;
        }

    }
}
