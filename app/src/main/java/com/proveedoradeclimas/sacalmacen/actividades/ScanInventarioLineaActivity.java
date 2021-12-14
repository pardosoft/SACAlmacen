package com.proveedoradeclimas.sacalmacen.actividades;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.device.ScanManager;
import android.os.AsyncTask;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.proveedoradeclimas.sacalmacen.R;
import com.proveedoradeclimas.sacalmacen.clases.AlmacenSql;
import com.proveedoradeclimas.sacalmacen.clases.Constantes;
import com.proveedoradeclimas.sacalmacen.clases.MyApplication;
import com.proveedoradeclimas.sacalmacen.clases.ScannerManager;
import com.proveedoradeclimas.sacalmacen.clases.WsMethods;
import com.proveedoradeclimas.sacalmacen.clases.zMensajes;
import com.proveedoradeclimas.sacalmacen.clases.zMisSonidos;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.proveedoradeclimas.sacalmacen.clases.zMisSonidos.Scan;
import static com.proveedoradeclimas.sacalmacen.clases.zMisSonidos.Wrong;

public class ScanInventarioLineaActivity extends AppCompatActivity {

    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action
    public static final int REQUEST_CODE_FIRMA = 23;

    private ScannerManager miScannerMT02; //scanner viejito, el de siempre :D
    private ScanManager miScannerWepoy; //scanner el nuevo :/
    private int tipoScanner;
    private ArrayList<Equipo> items;
    private ScanInventarioLineaActivity.GridViewAdapter adapter;
    private AlmacenSql helper;
    private String nombreTabla;
    private String idZona = "";
    private String descZona = "";
    private String numDescarga = "";
    private String user = "";
    private EditText edtxtSerie;

    private boolean isScannerLocked;
    private boolean isAfterOnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_inventario_linea);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.arrow_left);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        edtxtSerie = (EditText)findViewById(R.id.edtxt_scanlinea_serie);

        nombreTabla = getIntent().getExtras().getString("NombreTabla");

        items = new ArrayList<Equipo>();
        adapter = new GridViewAdapter(this);
        helper = new AlmacenSql(getApplicationContext(), nombreTabla);

        ListView listViewScan = (ListView)findViewById(R.id.list_scanlinea_series);
        listViewScan.setAdapter(adapter);

        setScanners();

        isAfterOnCreate = true;

        listViewScan.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                if(items.get(arg2).descripcion.length() > 0) {
                    //Abrir popup con la descipcion del error >:v
                    //if(tipoScanner == 1) miScannerWepoy.lockTrigger();
                    mostrarMensajeNoScan(items.get(arg2).serie, items.get(arg2).descripcion, zMisSonidos.Pop);
                }
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        listViewScan.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int serie_seleccionada = position;

                if(items.get(position).status != R.drawable.ff_check) {

                    if(tipoScanner != 0) bloquearScanner();

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:

                                    /*SQLiteDatabase db = helper.getWritableDatabase();

                                    db.execSQL("DELETE FROM "+ nombreTabla + " WHERE serie='" + items.get(serie_seleccionada).serie + "'");
                                    db.close();*/

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
                            items.get(position).descripcion + " \n¿Desea Borrar la serie: " + items.get(position).serie + "?")
                            .setTitle("SAM Android")
                            .setCancelable(false)
                            .setPositiveButton("Sí", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }

                return false;
            }
        });
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
                Toast.makeText(ScanInventarioLineaActivity.this, e.getMessage() + ". - " + e.getCause(), Toast.LENGTH_LONG).show();
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

    protected void onResume() {
        super.onResume();
        if(isAfterOnCreate) {
            isAfterOnCreate = false;
        }
        else {
            setScanners();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String linea = "";

        if (tipoScanner == 0 && (keyCode == 115 || keyCode == 135 || keyCode == 80)) {
            linea = miScannerMT02.ScanneLine();
        }

        if (linea.length() > 0) {
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
                        ScanInventarioLineaActivity.super.onBackPressed();
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

    private void procesaCadena(String line, String fecha) {

        //if(tipoScanner == 1) miScannerWepoy.lockTrigger();
        if(tipoScanner != 0) bloquearScanner();
        String ls_opcion = line.substring(0, 1);

        if (ls_opcion.equals("Z")) {
            if (line.substring(0, 2).equals("ZX") && line.contains("+")) {
                String zonaCompleta = line.substring(2);
                int index = zonaCompleta.indexOf('+');
                String _idZona = zonaCompleta.substring(0, index);
                String _descZona = zonaCompleta.substring(index + 1);

                /** LLAMAR AL METODO DE LOGIN Y FIRMA  */
                showLoginPopUp(_idZona, _descZona);

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

        //Aqui mandamos a llamar el webmethod
        line = edtxtSerie.getText().toString();
        new AsyncTaskGetModelo().execute(line, idZona, user, numDescarga);
    }

    private void errorEntrada(String texto) {
        zMisSonidos.playSound(getApplicationContext(), Wrong);
        zMisSonidos.playSound(getApplicationContext(), Wrong);
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
        edtxtSerie.setText("");
        //if(tipoScanner == 1) miScannerWepoy.unlockTrigger();
        if(tipoScanner != 0) desbloquearScanner();
    }

    private boolean validaSerie(String temp_serie) {

        for(int i = 0; i < items.size(); i++) {
            if(items.get(i).serie.equals(temp_serie))
            {
                errorEntrada("La serie " + temp_serie + " ya fue escaneada.");
                return false;
            }
        }

        edtxtSerie.setText(temp_serie);

        return true;
    }

    private class AsyncTaskGetModelo extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog progDialog;
        private String resultado;
        private String serie;
        private String ubicacion;
        private boolean isNetworkError = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progDialog = ProgressDialog.show(ScaneadoInventarioCheckActivity.this, "Validando Serie", "Espere un momento...", true);

            progDialog = new ProgressDialog(ScanInventarioLineaActivity.this);
            progDialog.setTitle("Validando Serie");
            progDialog.setMessage("Espere un momento...");
            progDialog.setIndeterminate(true);
            progDialog.setCancelable(true);
            progDialog.setCanceledOnTouchOutside(false);
            progDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel(DialogInterface dialog)
                {
                    // cancel AsyncTask
                    cancel(false);
                }
            });

            progDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            final MyApplication app = (MyApplication)getApplication();
            boolean isOk = false;
            serie = params[0];
            ubicacion = params[1];
            String usuario = params[2];
            String numdescarga = params[3];
            resultado = WsMethods.guardarSerieInventario(app, serie, numdescarga, ubicacion, usuario);

            Log.i("TAG", "Ws Modelo(" + serie + "): " + resultado);

            if(resultado.equals("0")) {
                resultado = "Éxito!";
                isOk = true;
            }
            /*else if(resultado.equals("1")) {
            	resultado = "La Serie escaneada no está activa en este Almacén.";
            }
            else if(resultado.equals("2")) {
            	resultado = "La Serie escaneada pertenece a otro Almacén.";
            }
            else if(resultado.equals("3")) {
            	resultado = "La Serie escaneada no existe o no se tiene registro.";
            }
            else if(resultado.equals("4")) {
            	resultado = "La Serie ya se encuentra escaneada.";
            }*/
            else if(resultado.substring(0, 1).equals("9")) {
                resultado = resultado.substring(1);
                isNetworkError = true;
                Log.i("TAG", "9 Networking");
            }

            return isOk;
        }

        @Override
        protected void onPostExecute(Boolean isOk) {
            super.onPostExecute(isOk);

            progDialog.dismiss();

            Equipo miEquipo = new Equipo();

            if(isOk) {
                //mostrarMensaje("Éxito", resultado, zMisSonidos.Success2);
                //zMisSonidos.playSound(getApplicationContext(), zMisSonidos.Scan);

                miEquipo.serie = serie;
                miEquipo.status = R.drawable.ff_check;
                miEquipo.descripcion = "";
                items.add(0, miEquipo);

                adapter.notifyDataSetChanged();
                edtxtSerie.setText("");
                //if(tipoScanner == 1) miScannerWepoy.unlockTrigger();
                if(tipoScanner != 0) desbloquearScanner();
            }
            else {
                mostrarMensajeNoScan("Error", resultado, Wrong);

                if(!isNetworkError) {
                    serie = serie.replace("\"", "").replace("'", "");
                    miEquipo.serie = serie;
                    miEquipo.status = R.drawable.ff_cross;
                    miEquipo.descripcion = resultado;
                    items.add(0, miEquipo);

                    adapter.notifyDataSetChanged();
                }

                edtxtSerie.setText("");
            }
        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();

            if (progDialog != null) {
                progDialog.dismiss();
            }

            mostrarMensaje("Error", "Se ha cancelado la carga.", zMisSonidos.Alert);
            edtxtSerie.setText("");
        }
    }

    private void showLoginPopUp(final String _idZona, final String _descZona) {

        LayoutInflater inflater = getLayoutInflater();
        final View popUp = inflater.inflate(R.layout.layout_login_popup_dialog, null);

        final EditText edtxtUser = (EditText)popUp.findViewById(R.id.edtxt_layout_login_user);
        final EditText edtxtPass = (EditText)popUp.findViewById(R.id.edtxt_layout_login_pass);

        edtxtPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    LinearLayout layout = (LinearLayout)popUp.findViewById(R.id.linear_layout_login_container);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
                }
                return false;
            }
        });

        AlertDialog.Builder builderPopUp = new AlertDialog.Builder(this);
        builderPopUp.setTitle("Autorización");
        builderPopUp.setView(popUp);
        builderPopUp.setCancelable(false);

        builderPopUp.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(popUp.getWindowToken(), 0);
                //if(tipoScanner == 1) miScannerWepoy.unlockTrigger();
                if(tipoScanner != 0) desbloquearScanner();
            }
        });

        builderPopUp.setPositiveButton("Autorizar", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(popUp.getWindowToken(), 0);

                if(edtxtUser.getText().toString().length() > 0 && edtxtPass.getText().toString().length() > 0) {
                    //enviar info a WS
                    new AsyncTaskLogin().execute(edtxtUser.getText().toString(),
                            edtxtPass.getText().toString(),
                            _idZona,
                            _descZona);

                    //enviar a la pantalla de firma con las credenciales
//                    Intent intent = new Intent(ScanInventarioLineaActivity.this, FirmaActivity.class);
//                    intent.putExtra("Tipo", "Contador");
//                    intent.putExtra("id", "5");
//                    intent.putExtra("Usuario", edtxtUser.getText().toString());
//                    intent.putExtra("Password", edtxtPass.getText().toString());
//                    intent.putExtra("idZona", _idZona);
//                    intent.putExtra("descZona", _descZona);
//                    startActivityForResult(intent, REQUEST_CODE_FIRMA);
                }
                else {
                    Toast.makeText(ScanInventarioLineaActivity.this, "Debe escribir Usuario y Contraseña", Toast.LENGTH_LONG).show();
                    //if(tipoScanner == 1) miScannerWepoy.unlockTrigger();
                    if(tipoScanner != 0) desbloquearScanner();
                }
            }
        });

        AlertDialog dialog = builderPopUp.create();
        dialog.show();
    }

    private class AsyncTaskLogin extends android.os.AsyncTask<String, Void, Boolean> {

        private ProgressDialog progDialog;
        private String resultado;
        private String usuario;
        private String password;
        private String _idZona;
        private String _descZona;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //progDialog = ProgressDialog.show(ScaneadoInventarioCheckActivity.this, "Autorizando", "Espere un momento...", true);

            progDialog = new ProgressDialog(ScanInventarioLineaActivity.this);
            progDialog.setTitle("Autorizando");
            progDialog.setMessage("Espere un momento...");
            progDialog.setIndeterminate(true);
            progDialog.setCancelable(false);

            progDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            final MyApplication app = (MyApplication)getApplication();
            boolean isOk = false;

            usuario = params[0];
            password = params[1];
            _idZona = params[2];
            _descZona = params[3];

            String xml = "<Login>\n"
                    + "<Usuario>" + usuario + "</Usuario>\n"
                    + "<Pssd>" + password + "</Pssd>\n"
                    + "<Mod>" + "7" + "</Mod>\n"
                    + "<Tarea>" + "779" + "</Tarea>\n"
                    + "</Login>";

            Log.i("TAG", "Xml Autorizacion");
            Log.i("TAG", xml);
            resultado = WsMethods.autorizarUsuario(app, xml);
            Log.i("TAG", "Ws Autorizacion: " + resultado);

            if(resultado.substring(0, 1).equals("0")) {
                //resultado fue bueno
                resultado = resultado.substring(1);
                isOk = true;
            }
            else {
                //hubo dificultades que afrontar
                resultado = resultado.substring(1);
                isOk = false;
            }

            return isOk;
        }

        @Override
        protected void onPostExecute(Boolean isOk) {
            super.onPostExecute(isOk);

            progDialog.dismiss();

            if(isOk) {
                /** HAY QUE ABRIR LA ACTIVIDAD DE LA FIRMA Y HACER LO NECESARIO PARA RECIBIR SU INFO **/
                Intent intent = new Intent(ScanInventarioLineaActivity.this, FirmaActivity.class);
                intent.putExtra("Tipo", "Contador");
                intent.putExtra("id", "5");
                intent.putExtra("usuario", usuario);
                intent.putExtra("Password", password);
                intent.putExtra("idZona", _idZona);
                intent.putExtra("descZona", _descZona);
                startActivityForResult(intent, REQUEST_CODE_FIRMA);

                /*Intent intent = new Intent(SalidasActivity.this, FirmaActivity.class);
                intent.putExtra("Tipo", "Cliente");
                intent.putExtra("id", Constantes.TIPO_FIRMA_CLIENTE);
                intent.putExtra("usuario", usuario);
                startActivityForResult(intent, REQUEST_CODE_FIRMA);*/
            }
            else {
                mostrarMensajeNoScan("Error", resultado, zMisSonidos.Alert);

                //Resetear valores
                idZona = "";
                descZona = "";
                user = "";
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_FIRMA) {
            if(resultCode == RESULT_OK) {
                String nombre = data.getStringExtra("Nombre");
                String firma = data.getStringExtra("Firma");
                String tipo = data.getStringExtra("Tipo");
                String usuario = data.getStringExtra("Usuario");
                String password = data.getStringExtra("Password");
                String _idZona = data.getStringExtra("idZona");
                String _descZona = data.getStringExtra("descZona");

                //ejecutar asynctask
                new AsyncTaskFirma().execute(usuario, password, nombre, firma, tipo, _idZona, _descZona);
            }
        }
    }

    private class AsyncTaskFirma extends android.os.AsyncTask<String, Void, Boolean> {

        private ProgressDialog progDialog;
        private String resultado;
        private String usuario;
        private String _idZona;
        private String _descZona;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //progDialog = ProgressDialog.show(ScaneadoInventarioCheckActivity.this, "Autorizando", "Espere un momento...", true);

            progDialog = new ProgressDialog(ScanInventarioLineaActivity.this);
            progDialog.setTitle("Validando");
            progDialog.setMessage("Espere un momento...");
            progDialog.setIndeterminate(true);
            progDialog.setCancelable(false);

            progDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            usuario = params[0];
            String password = params[1];
            String nombre = params[2];
            String firma = params[3];
            String tipo_firma = params[4];
            _idZona = params[5];
            _descZona = params[6];

            final MyApplication app = (MyApplication)getApplication();
            boolean isOk = false;

            String xml = "<Inventario_Header>\n"
                    + "<Usuario>" + usuario + "</Usuario>\n"
                    + "<Password>" + password + "</Password>\n"
                    + "<Ubicacion>" + _idZona + "</Ubicacion>\n"
                    + "<firmas>\n"
                    + "<firma>\n"
                    + "<tipo>" + Constantes.TIPO_FIRMA_CONTADOR + "</tipo>\n"
                    + "<nombre>" + nombre + "</nombre>\n"
                    + "<valor>" + firma + "</valor>\n"
                    + "</firma>\n"
                    + "</firmas>\n"
                    + "</Inventario_Header>";

            Log.i("TAG", "Xml Descarga");
            Log.i("TAG", xml);
            resultado = WsMethods.firmaInventario(app, xml);
            Log.i("TAG", "Ws Firma: " + resultado);

            if(resultado.substring(0, 1).equals("0")) {
                //resultado fue bueno
                resultado = resultado.substring(1);
                isOk = true;
            }
            else {
                //hubo dificultades que afrontar
                resultado = resultado.substring(1);
                isOk = false;
            }

            return isOk;
        }

        @Override
        protected void onPostExecute(Boolean isOk) {
            super.onPostExecute(isOk);

            progDialog.dismiss();

            if(isOk) {
                /** YA PUEDEN EMPEZAR A ESCANEAR EQUIPOS **/
                idZona = _idZona;
                descZona = _descZona;
                user = usuario;
                numDescarga = resultado;

                TextView txtUbicacion = (TextView)findViewById(R.id.txt_scanlinea_ubicacion);
                txtUbicacion.setText(descZona);

                items.clear();
                adapter.notifyDataSetChanged();

                //if(tipoScanner == 1) miScannerWepoy.unlockTrigger();
                if(tipoScanner != 0) desbloquearScanner();
            }
            else {
                /** HUBO UN ERROR Y NO PUEDEN EMPREZAR A ESCANEAR EQUIPOS **/
                mostrarMensajeNoScan("Error", resultado, zMisSonidos.Alert);

                //Resetear Valores
                idZona = "";
                descZona = "";
                user = "";
                numDescarga = "";

                TextView txtUbicacion = (TextView)findViewById(R.id.txt_scanlinea_ubicacion);
                txtUbicacion.setText(R.string.no_ubicacion);
            }
        }
    }

    public void mostrarMensaje(final String titulo, final String mensaje, final int sonido) {
        zMisSonidos.playSound(getApplicationContext(), sonido);
        zMensajes.MensajeOK(this, titulo, mensaje);
    }

    public void mostrarMensajeNoScan(final String titulo, final String mensaje, final int sonido) {

        if(tipoScanner != 0) bloquearScanner();

        zMisSonidos.playSound(getApplicationContext(), sonido);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(mensaje);
        builder.setTitle(titulo);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(tipoScanner != 0) desbloquearScanner();
            }
        });
        builder.show();
    }

    private class Equipo {
        public String serie;
        public int status;
        public String descripcion;
    }

    static class misDatos {
        public TextView _serie;
        public ImageView _status;
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
            convertView = mInflater.inflate(R.layout.layout_serie_check_row_list, parent, false);
            v._serie = (TextView)convertView.findViewById(R.id.txt_layout_serie_check_serie);
            v._status = (ImageView)convertView.findViewById(R.id.image_layout_serie_check_status);

            if(position == 0)
            {
                convertView.setTag(v);
            }

            if (position <= items.size())
            {
                v._serie.setText(items.get(position).serie);
                v._status.setImageResource(items.get(position).status);
            }

            TextView txtcount = (TextView) findViewById(R.id.txt_scanlinea_count);

            int contador_correctas = 0;
            for(int i = 0; i < items.size(); i++) {
                if(items.get(i).status == R.drawable.ff_check)
                    contador_correctas++;
            }

            //txtcount.setText(String.valueOf(items.size()));
            txtcount.setText(Integer.toString(contador_correctas));

            return convertView;
        }
    }
}
