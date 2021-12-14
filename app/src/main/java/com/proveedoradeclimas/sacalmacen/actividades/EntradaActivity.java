package com.proveedoradeclimas.sacalmacen.actividades;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.proveedoradeclimas.sacalmacen.R;
import com.proveedoradeclimas.sacalmacen.clases.AlmacenSql;
import com.proveedoradeclimas.sacalmacen.clases.Constantes;
import com.proveedoradeclimas.sacalmacen.clases.MyApplication;
import com.proveedoradeclimas.sacalmacen.adaptadores.ProductosAdapter;
import com.proveedoradeclimas.sacalmacen.clases.WsMethods;
import com.proveedoradeclimas.sacalmacen.clases.zMensajes;
import com.proveedoradeclimas.sacalmacen.clases.zMisSonidos;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class EntradaActivity extends AppCompatActivity {

    //private ArrayList<HashMap<String, String>> lista;
    private AlmacenSql helper;

    public static final int REQUEST_CODE_FIRMA_1 = 71;
    public static final int REQUEST_CODE_FIRMA_2 = 72;

    private String nombre_1;
    private String firma_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrada);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        helper = new AlmacenSql(getApplicationContext(), Constantes.TABLA_ENTRADA);

        toolbar.setNavigationIcon(R.drawable.arrow_left);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinearLayout linearEncontrado = (LinearLayout)findViewById(R.id.linear_entrada_encontrado);
                LinearLayout linearAdicional = (LinearLayout)findViewById(R.id.linear_entrada_extras);

                if(linearAdicional.getVisibility() == View.VISIBLE) {
                    //cambiar pantalla
                    linearAdicional.setVisibility(View.GONE);
                    linearEncontrado.setVisibility(View.VISIBLE);
                }
                else if(linearEncontrado.getVisibility() == View.VISIBLE) {
                    //borrar informacion
                    limpiarInfo();
                }
                else {
                    finish();
                }
            }
        });

        EditText edtxtBuscar = (EditText)findViewById(R.id.edtxt_entrada_ordencompra);
        if(edtxtBuscar == null) {
            Toast.makeText(EntradaActivity.this, "Elemento de la UI no encontrado.", Toast.LENGTH_LONG).show();
            return;
        }
        else {
            edtxtBuscar.setOnEditorActionListener(new TextView.OnEditorActionListener() {

                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    // TODO Auto-generated method stub
                    if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                        LinearLayout layout = (LinearLayout)findViewById(R.id.container_entrada);
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
                        buscarOc();
                    }
                    return false;
                }
            });
        }

        Button btnBuscar = (Button) findViewById(R.id.btn_entrada_buscar);
        if(btnBuscar == null) {
            //Toast.makeText(EntradaActivity.this, "Elemento de la UI no encontrado.", Toast.LENGTH_LONG).show();
        }
        else {
            btnBuscar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = (LinearLayout)findViewById(R.id.container_entrada);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
                    buscarOc();
                }
            });
        }

        /*if(btnBuscar.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, "VISIBLE", Toast.LENGTH_LONG).show();
        }
        else if(btnBuscar.getVisibility() == View.INVISIBLE) {
            Toast.makeText(this, "INVISIBLE", Toast.LENGTH_LONG).show();
        }
        else if(btnBuscar.getVisibility() == View.GONE) {
            Toast.makeText(this, "GONE", Toast.LENGTH_LONG).show();
        }*/

        btnBuscar.setVisibility(View.GONE);
        btnBuscar.setVisibility(View.VISIBLE);
    }

    private void buscarOc() {

        final EditText edtxtOc = (EditText)findViewById(R.id.edtxt_entrada_ordencompra);

        if (edtxtOc.getText().length() == 0)
        {
            mostrarMensaje("Error", "Debe de capturar la Orden de Compra", zMisSonidos.Alert);
            return;
        }

        new AsyncTaskBuscarOc().execute(edtxtOc.getText().toString());
    }

    private void limpiarInfo() {

        TextView txtOc = (TextView)findViewById(R.id.txt_entrada_ordencompra);
        txtOc.setText("");

        TextView txtPedido = (TextView)findViewById(R.id.txt_entrada_pedido);
        txtPedido.setText("");

        ListView listaProductos = (ListView) findViewById(R.id.lista_entrada_productos);
        listaProductos.setAdapter(null);

        LinearLayout linearEncontrado = (LinearLayout)findViewById(R.id.linear_entrada_encontrado);
        linearEncontrado.setVisibility(View.GONE);

        LinearLayout linearExtras = (LinearLayout)findViewById(R.id.linear_entrada_extras);
        linearExtras.setVisibility(View.GONE);

        LinearLayout linearBuscar = (LinearLayout)findViewById(R.id.linear_entrada_buscar);
        linearBuscar.setVisibility(View.VISIBLE);

        EditText edtxtRemision = (EditText)findViewById(R.id.edtxt_entradas_remision);
        edtxtRemision.setText("");

        EditText edtxtFolio = (EditText)findViewById(R.id.edtxt_entradas_folio);
        edtxtFolio.setText("");

        EditText edtxtPlacas = (EditText)findViewById(R.id.edtxt_entradas_placas);
        edtxtPlacas.setText("");
    }

    private void showInfoAdicional() {

        LinearLayout linearEncontrado = (LinearLayout)findViewById(R.id.linear_entrada_encontrado);
        LinearLayout linearAdicional = (LinearLayout)findViewById(R.id.linear_entrada_extras);

        linearEncontrado.setVisibility(View.GONE);
        linearAdicional.setVisibility(View.VISIBLE);

        EditText edtxtPlacas = (EditText)findViewById(R.id.edtxt_entradas_placas);
        if(edtxtPlacas == null) {
            Toast.makeText(EntradaActivity.this, "Elemento de la UI no encontrado.", Toast.LENGTH_LONG).show();
            return;
        }

        edtxtPlacas.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    LinearLayout layout = (LinearLayout)findViewById(R.id.container_entrada);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
                }
                return false;
            }
        });

        //angel de la warda
        ImageButton btnWard = (ImageButton)findViewById(R.id.btn_entrada_guardar);
        if(btnWard == null) {
            Toast.makeText(EntradaActivity.this, "Elemento de la UI no encontrado.", Toast.LENGTH_LONG).show();
            return;
        }
        btnWard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validar que tengamos los datos necesarios para la ward
                SQLiteDatabase db = helper.getWritableDatabase();
                Cursor cursor = db.rawQuery("SELECT _id, Modelo, Serie FROM " + Constantes.TABLA_ENTRADA, null);

                if(!cursor.moveToFirst()) {
                    //no hay nada de que
                    Toast.makeText(EntradaActivity.this, "No hay Series escaneadas para Guardar.", Toast.LENGTH_LONG).show();
                    zMisSonidos.playSound(EntradaActivity.this, zMisSonidos.Error);
                    return;
                }

                cursor.close();
                db.close();

                EditText edtxtRemision = (EditText)findViewById(R.id.edtxt_entradas_remision);
                EditText edtxtFolio = (EditText)findViewById(R.id.edtxt_entradas_folio);
                EditText edtxtPlacas = (EditText)findViewById(R.id.edtxt_entradas_placas);

                if(edtxtRemision.getText().toString().trim().length() == 0) {
                    Toast.makeText(EntradaActivity.this, "El campo de Remisión es necesario para Guardar.", Toast.LENGTH_LONG).show();
                    zMisSonidos.playSound(EntradaActivity.this, zMisSonidos.Error);
                    return;
                }
                if(edtxtFolio.getText().toString().trim().length() == 0) {
                    Toast.makeText(EntradaActivity.this, "El campo de Folio es necesario para Guardar.", Toast.LENGTH_LONG).show();
                    zMisSonidos.playSound(EntradaActivity.this, zMisSonidos.Error);
                    return;
                }
                if(edtxtPlacas.getText().toString().trim().length() == 0) {
                    Toast.makeText(EntradaActivity.this, "El campo de Placas es necesario para Guardar.", Toast.LENGTH_LONG).show();
                    zMisSonidos.playSound(EntradaActivity.this, zMisSonidos.Error);
                    return;
                }

                //Abrir AutorizacionActivity
                LinearLayout layout = (LinearLayout)findViewById(R.id.container_entrada);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
                showLoginPopUp();
            }
        });
    }

    private void showLoginPopUp() {

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
                LinearLayout layout = (LinearLayout)findViewById(R.id.container_entrada);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
            }
        });

        builderPopUp.setPositiveButton("Autorizar", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                LinearLayout layout = (LinearLayout)findViewById(R.id.container_entrada);
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(layout.getWindowToken(), 0);

                if(edtxtUser.getText().toString().length() > 0 && edtxtPass.getText().toString().length() > 0) {
                    //enviar info a WS
                    new AsyncTaskLogin().execute(edtxtUser.getText().toString(), edtxtPass.getText().toString());
                }
                else {
                    Toast.makeText(EntradaActivity.this, "Debe escribir Usuario y Contraseña", Toast.LENGTH_LONG).show();
                }
            }
        });

        AlertDialog dialog = builderPopUp.create();
        dialog.show();

    }

    private class AsyncTaskBuscarOc extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog progDialog;
        private String resultado;
        private boolean isOk;
        private String ordenCompra;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progDialog = ProgressDialog.show(EntradaActivity.this, "Buscando Orden de Compra", "Espere un momento...", true);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            final MyApplication app = (MyApplication)getApplication();
            String oc = params[0];
            ordenCompra = oc;

            resultado = WsMethods.buscarOcEntrada(app, oc);
            Log.i("TAG", resultado);

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

            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            progDialog.dismiss();

            if(isOk) {
                //mostrar la informacion informativa
                try {
                    ArrayList<HashMap<String, String>> lista;

                    DocumentBuilderFactory parser = DocumentBuilderFactory.newInstance();
                    InputStream is = new ByteArrayInputStream(resultado.getBytes("UTF-8"));
                    DocumentBuilder Builder = parser.newDocumentBuilder();

                    Builder.isValidating();

                    Document _document = Builder.parse(is, null);
                    int NumOrden = Integer.parseInt(_document.getElementsByTagName("Pedido").item(0).getTextContent());

                    if (NumOrden > 0) {

                        NodeList equipos = _document.getElementsByTagName("Equipo");

                        lista = new ArrayList<HashMap<String, String>>();

                        for(int i=0; i<equipos.getLength(); i++) {

                            Node nodoEquipos = equipos.item(i);

                            Element elementoEquipos = (Element)nodoEquipos;
                            HashMap<String, String> temp1 = new HashMap<String, String>();
                            temp1.put("modelo", elementoEquipos.getAttribute("modelo"));
                            temp1.put("cantidad", elementoEquipos.getAttribute("cantidad"));
                            lista.add(temp1);
                        }

                        final EditText edtxtOC = (EditText)findViewById(R.id.edtxt_entrada_ordencompra);
                        edtxtOC.setText("");

                        LinearLayout linearBuscar = (LinearLayout) findViewById(R.id.linear_entrada_buscar);
                        linearBuscar.setVisibility(View.GONE);

                        TextView txtOc = (TextView) findViewById(R.id.txt_entrada_ordencompra);
                        txtOc.setText(ordenCompra.toUpperCase());

                        TextView txtPedido = (TextView) findViewById(R.id.txt_entrada_pedido);
                        txtPedido.setText(Integer.toString(NumOrden));

                        /**************************************************************************/
                        /**Aqui es donde se muestra la listview de productos**/
                        ListView listaProductos = (ListView) findViewById(R.id.lista_entrada_productos);
                        if(Build.MANUFACTURER.equals("geofanci") && Build.MODEL.equals("PDA")) {
                            listaProductos.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80));
                        }
                        else {
                            listaProductos.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200));
                        }


                        ProductosAdapter adapter = new ProductosAdapter(EntradaActivity.this, lista);
                        listaProductos.setAdapter(adapter);
                        /**************************************************************************/

                        LinearLayout linear = (LinearLayout)findViewById(R.id.linear_entrada_encontrado);
                        linear.setVisibility(View.VISIBLE);

                        ImageButton btnEscanear = (ImageButton)findViewById(R.id.btn_entrada_escanear);
                        btnEscanear.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(EntradaActivity.this, ScanModeloSerieActivity.class);
                                intent.putExtra("NombreTabla", Constantes.TABLA_ENTRADA);
                                startActivity(intent);
                            }
                        });

                        ImageButton btnGuardar = (ImageButton)findViewById(R.id.btn_entrada_next);
                        btnGuardar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showInfoAdicional();
                            }
                        });
                    }
                    else {
                        mostrarMensaje("Error", "No se encontró Número de Orden.", zMisSonidos.Alert);
                    }
                } catch (UnsupportedEncodingException e) {
                    mostrarMensaje("Error", "(UnsupportedEncodingException) " + e.getMessage(), zMisSonidos.Alert);
                } catch (ParserConfigurationException e) {
                    mostrarMensaje("Error", "(ParserConfigurationException) " + e.getMessage(), zMisSonidos.Alert);
                } catch (SAXException e) {
                    mostrarMensaje("Error", "(SAXException) " + e.getMessage(), zMisSonidos.Alert);
                } catch (IOException e) {
                    mostrarMensaje("Error", "(IOException) " + e.getMessage(), zMisSonidos.Alert);
                } catch (Exception e) {
                    mostrarMensaje("Error", "(Exception) " + e.getMessage(), zMisSonidos.Alert);
                }
            }
            else {
                mostrarMensaje("Error", resultado, zMisSonidos.Alert);
            }
        }
    }

    private class AsyncTaskLogin extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog progDialog;
        private String resultado;
        private boolean isOk;
        private String usuario;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progDialog = ProgressDialog.show(EntradaActivity.this, "Autorizando", "Espere un momento...", true);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            final MyApplication app = (MyApplication)getApplication();

            String user = params[0];
            String pass = params[1];

            String xml = "<Login>"
                    + "<Usuario>" + user + "</Usuario>"
                    + "<Pssd>" + pass + "</Pssd>"
                    + "<Mod>" + Integer.toString(Constantes.MODULO_ALMACEN) + "</Mod>"
                    + "<Tarea>" + Integer.toString(Constantes.TAREA_ENTRADA) + "</Tarea>"
                    + "</Login>";

            resultado = WsMethods.autorizarUsuario(app, xml);
            Log.i("TAG", resultado);

            if(resultado.substring(0, 1).equals("0")) {
                //resultado fue bueno
                resultado = resultado.substring(1);
                usuario = user;
                isOk = true;
            }
            else {
                //hubo dificultades que afrontar
                resultado = resultado.substring(1);
                isOk = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            progDialog.dismiss();

            if(isOk) {
                //mandar a la actividad de firma
                //Toast.makeText(EntradaActivity.this, "Exito", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(EntradaActivity.this, FirmaActivity.class);
                intent.putExtra("Tipo", "Chofer");
                intent.putExtra("id", Constantes.TIPO_FIRMA_CHOFER);
                intent.putExtra("usuario", usuario);
                startActivityForResult(intent, REQUEST_CODE_FIRMA_1);
            }
            else {
                mostrarMensaje("Error", resultado, zMisSonidos.Alert);
            }
        }
    }

    private class AsyncTaskGuardar extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog progDialog;
        private String resultado;
        private String excepcion;
        private boolean isOk;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progDialog = ProgressDialog.show(EntradaActivity.this, "Guardando Entrada", "Espere un momento...", true);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            final MyApplication app = (MyApplication)getApplication();

            //numorden, oc, remision, folio, placas, nombre_1, firma_1, nombre_2, firma_2
            String numorden = params[0];
            String oc = params[1];
            String remision = params[2];
            String folio = params[3];
            String placas = params[4];
            String usuario = params[5];
            String nombre_1 = params[6];
            String firma_1 = params[7];
            String nombre_2 = params[8];
            String firma_2 = params[9];
            String numproy = app.getNumproy();
            String num_almacen = app.getNumAlmacen();

            SQLiteDatabase db = helper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT _id, Modelo, Serie FROM " + Constantes.TABLA_ENTRADA, null);

            String ls_xml = "<Almacen>\n";

            ls_xml += "<numproy>" + numproy + "</numproy>\n" +
                    "<origen>1</origen>\n" +
                    "<numorden>" + numorden + "</numorden>\n" +
                    "<remision>" + remision + "</remision>\n" +
                    "<usuario>" + usuario + "</usuario>\n" +
                    "<placa>" + placas + "</placa>\n" +
                    "<folio>" + folio + "</folio>\n" +
                    "<num_almacen>" + num_almacen + "</num_almacen>\n";

            ls_xml += "<equipos>\n";

            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                ls_xml += "<equipo modelo=\"" + cursor.getString(1) + "\" serie=\"" + cursor.getString(2) + "\"/>\n";
                cursor.moveToNext();
            }
            cursor.close();

            ls_xml += "</equipos>\n";

            ls_xml += "<firmas>\n" +
                    "<firma>\n" +
                    "<tipo>" + Constantes.TIPO_FIRMA_CHOFER + "</tipo>\n" +
                    "<nombre>" + nombre_1 + "</nombre>\n" +
                    "<valor>" + firma_1 + "</valor>\n" +
                    "</firma>\n" +
                    "<firma>\n" +
                    "<tipo>" + Constantes.TIPO_FIRMA_ALMACENISTA + "</tipo>\n" +
                    "<nombre>" + nombre_2 + "</nombre>\n" +
                    "<valor>" + firma_2 + "</valor>\n" +
                    "</firma>\n" +
                    "</firmas>\n" +
                    "</Almacen>\n";

            Log.i("TAG", ls_xml);

            /*File myXml = new File(Constantes.PATH_APP_PROVEE + File.separator + "entrada.xml");
            try {
                myXml.createNewFile();
                FileOutputStream fOut = new FileOutputStream(myXml);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(ls_xml);
                myOutWriter.close();
                fOut.close();
            } catch(IOException io) {
                io.printStackTrace();
                resultado = io.getMessage();
                isOk = false;
                return isOk;
            }*/

            //resultado = "0"; //getWS_SACData...
            resultado = WsMethods.guardarEntrada(app, ls_xml);
            Log.i("TAG", resultado);

            if(resultado.substring(0, 1).equals("0")) {
                resultado = resultado.substring(1);
                isOk = true;
            }
            else {
                resultado = resultado.substring(1);
                isOk = false;
            }

            return isOk;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            progDialog.dismiss();

            if(isOk) {
                // Mandar a imprimir la hoja
                new AsyncTaskImprimir().execute(resultado);
                //mostrarMensaje("Éxito", "Se ha Guardado e Impreso la Entrada con éxito!", zMisSonidos.Success2);
                //borrar las series escaneadas

                SQLiteDatabase db = helper.getWritableDatabase();
                db.execSQL("DELETE FROM " + Constantes.TABLA_ENTRADA);
                db.close();

                limpiarInfo();
            }
            else {
                mostrarMensaje("Error Entrada", "Error al Guardar la Entrada. " + resultado, zMisSonidos.Alert);
            }
        }
    }

    private class AsyncTaskImprimir extends AsyncTask<String, Void, Boolean> {

        ProgressDialog progressDialog;
        private String resultado;
        private boolean isOk;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(EntradaActivity.this, "Imprimiendo", "Espere un momento...", true);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            String nota = params[0];

            MyApplication app = (MyApplication)getApplication();
            isOk = true;

            Log.i("TAG", "Primer intento de Impresión");
            resultado = WsMethods.imprimirEntrada(app, nota);
            Log.i("TAG", resultado);

            if(!resultado.substring(0, 1).equals("0")) {
                Log.i("TAG", "Segundo intento de Impresión");
                resultado = WsMethods.imprimirEntrada(app, nota);

                if (!resultado.substring(0, 1).equals("0")) {
                    Log.i("TAG", "Tercer intento de Impresión");
                    resultado = WsMethods.imprimirEntrada(app, nota);

                    if(!resultado.substring(0, 1).equals("0")) {
                        Log.i("TAG", "YA VALIO!!!");
                        isOk = false;
                        resultado = resultado.substring(1);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            progressDialog.dismiss();

            if(isOk) {
                mostrarMensaje("Éxito", "Se ha Guardado e Impreso la Entrada con éxito!", zMisSonidos.Success2);
            }
            else {
                mostrarMensaje("Error Impresión", "Se ha Guardado la Entrada pero hubo un error al Imprimirla. Llame a TI. " + resultado, zMisSonidos.Alert);
            }
        }
    }

    public void mostrarMensaje(final String titulo, final String mensaje, final int sonido) {
        zMisSonidos.playSound(getApplicationContext(), sonido);
        zMensajes.MensajeOK(this, titulo, mensaje);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_FIRMA_1) {
            if(resultCode == RESULT_OK) {
                nombre_1 = data.getStringExtra("Nombre");
                firma_1 = data.getStringExtra("Firma");
                String usuario = data.getStringExtra("Usuario");

                Intent intent = new Intent(EntradaActivity.this, FirmaActivity.class);
                intent.putExtra("Tipo", "Almacenista");
                intent.putExtra("id", Constantes.TIPO_FIRMA_ALMACENISTA);
                intent.putExtra("usuario", usuario);
                startActivityForResult(intent, REQUEST_CODE_FIRMA_2);
            }
        }
        else if(requestCode == REQUEST_CODE_FIRMA_2) {
            if(resultCode == RESULT_OK) {
                //WARD
                String nombre_2 = data.getStringExtra("Nombre");
                String firma_2 = data.getStringExtra("Firma");
                String usuario = data.getStringExtra("Usuario");

                TextView txtNumorden = (TextView)findViewById(R.id.txt_entrada_pedido);
                TextView txtOc = (TextView)findViewById(R.id.txt_entrada_ordencompra);
                EditText edtxtRemision = (EditText)findViewById(R.id.edtxt_entradas_remision);
                EditText edtxtFolio = (EditText)findViewById(R.id.edtxt_entradas_folio);
                EditText edtxtPlacas = (EditText)findViewById(R.id.edtxt_entradas_placas);

                String numorden = txtNumorden.getText().toString();
                String oc = txtOc.getText().toString();
                String remision = edtxtRemision.getText().toString();
                String folio = edtxtFolio.getText().toString();
                String placas = edtxtPlacas.getText().toString();

                new AsyncTaskGuardar().execute(numorden, oc, remision, folio, placas, usuario, nombre_1, firma_1, nombre_2, firma_2);
            }
        }
    }

    @Override
    public void onBackPressed() {
        LinearLayout linearBuscar = (LinearLayout)findViewById(R.id.linear_entrada_buscar);
        LinearLayout linearEncontrado = (LinearLayout)findViewById(R.id.linear_entrada_encontrado);
        LinearLayout linearAdicional = (LinearLayout)findViewById(R.id.linear_entrada_extras);

        if(linearAdicional.getVisibility() == View.VISIBLE) {
            //cambiar pantalla
            linearAdicional.setVisibility(View.GONE);
            linearBuscar.setVisibility(View.GONE);
            linearEncontrado.setVisibility(View.VISIBLE);
        }
        else if(linearEncontrado.getVisibility() == View.VISIBLE) {
            //borrar informacion
            limpiarInfo();
        }
        else {
            finish();
        }
    }
}
