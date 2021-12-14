package com.proveedoradeclimas.sacalmacen.actividades;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.proveedoradeclimas.sacalmacen.R;
import com.proveedoradeclimas.sacalmacen.adaptadores.ProductosAdapter;
import com.proveedoradeclimas.sacalmacen.clases.AlmacenSql;
import com.proveedoradeclimas.sacalmacen.clases.Constantes;
import com.proveedoradeclimas.sacalmacen.clases.MyApplication;
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

public class TransferenciaActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_FIRMA = 17;

    private ArrayList<HashMap<String, String>> lista;
    private AlmacenSql helper;
    private int gi_origen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transferencia);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        helper = new AlmacenSql(getApplicationContext(), Constantes.TABLA_TRANSFERENCIA);

        toolbar.setNavigationIcon(R.drawable.arrow_left);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinearLayout linearEncontrado = (LinearLayout)findViewById(R.id.linear_transferencia_encontrado);

                if(linearEncontrado.getVisibility() == View.VISIBLE) {
                    //borrar informacion
                    limpiarInfo();
                }
                else {
                    finish();
                }

            }
        });

        EditText edtxtBuscar = (EditText)findViewById(R.id.edtxt_transferencia_remision);

        edtxtBuscar.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    LinearLayout layout = (LinearLayout)findViewById(R.id.container_transferencia);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
                    buscarRemision();
                }
                return false;
            }
        });

        Button Boton = (Button) findViewById(R.id.btn_transferencia_buscar);

        Boton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                LinearLayout layout = (LinearLayout)findViewById(R.id.container_transferencia);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
                buscarRemision();
            }
        });
    }

    public void buscarRemision() {

        final EditText edtxtRemision = (EditText)findViewById(R.id.edtxt_transferencia_remision);

        if (edtxtRemision.getText().length() == 0)
        {
            mostrarMensaje("Error", "Debe de capturar la Orden de Compra", zMisSonidos.Alert);
            return;
        }

        new AsyncTaskBuscarRemision().execute(edtxtRemision.getText().toString());
    }

    private void limpiarInfo() {

        TextView txtPedido = (TextView) findViewById(R.id.txt_transferencia_remision);
        txtPedido.setText("");

        ListView listaProductos = (ListView) findViewById(R.id.lista_transferencia_productos);
        listaProductos.setAdapter(null);

        LinearLayout linearEncontrado = (LinearLayout) findViewById(R.id.linear_transferencia_encontrado);
        linearEncontrado.setVisibility(View.GONE);

        LinearLayout linearBuscar = (LinearLayout) findViewById(R.id.linear_transferencia_buscar);
        linearBuscar.setVisibility(View.VISIBLE);
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
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(popUp.getWindowToken(), 0);
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
                    new AsyncTaskLogin().execute(edtxtUser.getText().toString(), edtxtPass.getText().toString());
                }
                else {
                    Toast.makeText(TransferenciaActivity.this, "Debe escribir Usuario y Contraseña", Toast.LENGTH_LONG).show();
                }
            }
        });

        AlertDialog dialog = builderPopUp.create();
        dialog.show();
    }

    private class AsyncTaskBuscarRemision extends android.os.AsyncTask<String, Void, Boolean> {

        private ProgressDialog progDialog;
        private String resultado;
        private boolean isOk;
        private String remision;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progDialog = ProgressDialog.show(TransferenciaActivity.this, "Buscando Remisión", "Espere un momento...", true);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            final MyApplication app = (MyApplication)getApplication();
            remision = params[0];

            resultado = WsMethods.buscarRemisionTransferencia(app, remision);
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
                    DocumentBuilderFactory parser = DocumentBuilderFactory.newInstance();
                    InputStream is = new ByteArrayInputStream(resultado.getBytes("UTF-8"));
                    DocumentBuilder Builder = parser.newDocumentBuilder();

                    Builder.isValidating();

                    Document _document = Builder.parse(is, null);

                    int li_origen = Integer.parseInt(_document.getElementsByTagName("Origen").item(0).getTextContent());
                    String entrada =_document.getElementsByTagName("Entrada").item(0).getTextContent();
                    String salida =_document.getElementsByTagName("Salida").item(0).getTextContent();

                    if( li_origen > 0 && entrada != null && salida != null ) {

                        gi_origen = li_origen;

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

                        final EditText edtxtRemision = (EditText)findViewById(R.id.edtxt_transferencia_remision);
                        edtxtRemision.setText("");

                        LinearLayout linearBuscar = (LinearLayout) findViewById(R.id.linear_transferencia_buscar);
                        linearBuscar.setVisibility(View.GONE);

                        TextView txtRemision = (TextView) findViewById(R.id.txt_transferencia_remision);
                        txtRemision.setText(remision.toUpperCase());

                        EditText edtxtOrigen = (EditText)findViewById(R.id.edtxt_transferencia_origen);
                        edtxtOrigen.setText(salida);

                        EditText edtxtDestino = (EditText)findViewById(R.id.edtxt_transferencia_destino);
                        edtxtDestino.setText(entrada);

                        /**************************************************************************/
                        /**Aqui es donde se muestra la listview de productos**/
                        ListView listaProductos = (ListView) findViewById(R.id.lista_transferencia_productos);

                        ProductosAdapter adapter = new ProductosAdapter(TransferenciaActivity.this, lista);
                        listaProductos.setAdapter(adapter);
                        /**************************************************************************/

                        LinearLayout linear = (LinearLayout)findViewById(R.id.linear_transferencia_encontrado);
                        linear.setVisibility(View.VISIBLE);

                        ImageButton btnEscanear = (ImageButton)findViewById(R.id.btn_transferencia_escanear);
                        btnEscanear.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(TransferenciaActivity.this, ScanSerieActivity.class);
                                intent.putExtra("NombreTabla", Constantes.TABLA_TRANSFERENCIA);
                                startActivity(intent);
                            }
                        });

                        ImageButton btnGuardar = (ImageButton)findViewById(R.id.btn_transferencia_guardar);
                        btnGuardar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showLoginPopUp();
                            }
                        });
                    }
                    else {
                        mostrarMensaje("Error", "Información no válida.", zMisSonidos.Alert);
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

    private class AsyncTaskLogin extends android.os.AsyncTask<String, Void, Boolean> {

        private ProgressDialog progDialog;
        private String resultado;
        private boolean isOk;
        private String usuario;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progDialog = ProgressDialog.show(TransferenciaActivity.this, "Autorizando", "Espere un momento...", true);
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
                    + "<Tarea>" + Integer.toString(Constantes.TAREA_TRANSFERENCIA) + "</Tarea>"
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
            TextView txtRemision = (TextView)findViewById(R.id.txt_transferencia_remision);

            if(isOk) {
                new AsyncTaskGuardar().execute(Integer.toString(gi_origen), txtRemision.getText().toString(), usuario);
            }
            else {
                mostrarMensaje("Error", resultado, zMisSonidos.Alert);
            }
        }
    }

    private class AsyncTaskGuardar extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog progressDialog;
        private String resultado;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(TransferenciaActivity.this, "Guardando Transferencia", "Espere un momento...", true);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            String origen = params[0];
            String remision = params[1];
            String usuario = params[2];

            MyApplication app = (MyApplication)getApplication();
            boolean isOk;

            AlmacenSql helper = new AlmacenSql(getApplicationContext(), Constantes.TABLA_TRANSFERENCIA);
            SQLiteDatabase db = helper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT Serie FROM " + Constantes.TABLA_TRANSFERENCIA, null);

            cursor.moveToFirst();

            String ls_xml = "<Almacen>\n";

            ls_xml += "<numproy>" + app.getNumproy() + "</numproy>\n" +
                    "<origen>" + origen + "</origen>\n" +
                    "<numorden>0</numorden>\n" +
                    "<remision>" + remision + "</remision>\n" +
                    "<usuario>" + usuario + "</usuario>\n" +
                    "<num_almacen>" + app.getNumAlmacen() + "</num_almacen>\n";

            ls_xml += "<equipos>\n";

            while(!cursor.isAfterLast()) {
                ls_xml += "<equipo serie=\"" + cursor.getString(0) + "\" "
                        + "/>\n";
                cursor.moveToNext();
            }

            ls_xml += "</equipos>\n" +
                    "</Almacen>\n";

            cursor.close();

            Log.i("TAG", ls_xml);

            db.close();
            helper.close();

            //resultado = "0"; //getWS_SACData...
            resultado = WsMethods.guardarTransferencia(app, ls_xml);
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
        protected void onPostExecute(Boolean isOk) {
            super.onPostExecute(isOk);

            progressDialog.dismiss();

            if(isOk) {
                // Mandar a imprimir la hoja
                if(gi_origen == 1) {
                    new AsyncTaskImprimir().execute(resultado);
                }
                else {
                    mostrarMensaje("Éxito", "Se ha Guardado la Transferencia con éxito!", zMisSonidos.Success2);
                }
                //mostrarMensaje("Éxito", "Se ha Guardado e Impreso la Transferencia con éxito!", zMisSonidos.Success2);
                //borrar las series escaneadas

                SQLiteDatabase db = helper.getWritableDatabase();
                db.execSQL("DELETE FROM " + Constantes.TABLA_TRANSFERENCIA);
                db.close();

                limpiarInfo();
            }
            else {
                mostrarMensaje("Error Transferencia", "Error al Guardar la Transferencia. " + resultado, zMisSonidos.Alert);
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

            progressDialog = ProgressDialog.show(TransferenciaActivity.this, "Imprimiendo", "Espere un momento...", true);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            String nota = params[0];

            MyApplication app = (MyApplication)getApplication();
            isOk = true;

            Log.i("TAG", "Primer intento de Impresión");
            resultado = WsMethods.imprimirTransferencia(app, nota);
//            resultado = "0";
            Log.i("TAG", resultado);

            if(!resultado.substring(0, 1).equals("0")) {
                Log.i("TAG", "Segundo intento de Impresión");
                resultado = WsMethods.imprimirTransferencia(app, nota);

                if (!resultado.substring(0, 1).equals("0")) {
                    Log.i("TAG", "Tercer intento de Impresión");
                    resultado = WsMethods.imprimirTransferencia(app, nota);

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
                mostrarMensaje("Éxito", "Se ha Guardado e Impreso la Transferencia con éxito!", zMisSonidos.Success2);
            }
            else {
                mostrarMensaje("Error Impresión", "Se ha Guardado la Transferencia pero hubo un error al Imprimirla. Llame a TI. " + resultado, zMisSonidos.Alert);
            }
        }
    }

    public void mostrarMensaje(final String titulo, final String mensaje, final int sonido) {
        zMisSonidos.playSound(getApplicationContext(), sonido);
        zMensajes.MensajeOK(this, titulo, mensaje);
    }
}