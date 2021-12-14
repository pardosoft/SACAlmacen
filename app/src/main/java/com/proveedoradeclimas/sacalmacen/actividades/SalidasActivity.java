package com.proveedoradeclimas.sacalmacen.actividades;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
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

import com.proveedoradeclimas.sacalmacen.clases.AlmacenSql;
import com.proveedoradeclimas.sacalmacen.clases.Constantes;
import com.proveedoradeclimas.sacalmacen.clases.MyApplication;
import com.proveedoradeclimas.sacalmacen.adaptadores.ProductosAdapter;
import com.proveedoradeclimas.sacalmacen.clases.ScannerManager;
import com.proveedoradeclimas.sacalmacen.clases.WsMethods;
import com.proveedoradeclimas.sacalmacen.R;
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

public class SalidasActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_FIRMA = 23;

    private ScannerManager miScanner;
    private ArrayList<HashMap<String, String>> lista;
    private AlmacenSql helper;
    private boolean isGuia = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salidas);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        helper = new AlmacenSql(getApplicationContext(), Constantes.TABLA_SALIDA);

        toolbar.setNavigationIcon(R.drawable.arrow_left);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinearLayout linearEncontrado = (LinearLayout)findViewById(R.id.linear_salidas_encontrado);

                if(linearEncontrado.getVisibility() == View.VISIBLE) {
                    //borrar informacion
                    limpiarInfo();
                }
                else {
                    finish();
                }

            }
        });

        EditText edtxtBuscar = (EditText)findViewById(R.id.edtxt_salidas_remision);

        edtxtBuscar.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    LinearLayout layout = (LinearLayout)findViewById(R.id.container_salidas);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
                    buscarRemision();
                }
                return false;
            }
        });

        Button Boton = (Button) findViewById(R.id.btn_salidas_buscar);

        Boton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                LinearLayout layout = (LinearLayout)findViewById(R.id.container_salidas);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
                buscarRemision();
            }
        });
    }

    public void buscarRemision() {

        final EditText edtxtRemision = (EditText)findViewById(R.id.edtxt_salidas_remision);

        if (edtxtRemision.getText().length() == 0)
        {
            mostrarMensaje("Error", "Debe de capturar la Orden de Compra", zMisSonidos.Alert);
            return;
        }

        new AsyncTaskBuscarRemision().execute(edtxtRemision.getText().toString());
    }

    private void limpiarInfo() {

        TextView txtRemision = (TextView) findViewById(R.id.txt_salidas_remision);
        txtRemision.setText("");

        TextView txtPedido = (TextView) findViewById(R.id.txt_salidas_pedido);
        txtPedido.setText("");

        TextView txtGuia = (TextView) findViewById(R.id.txt_salidas_guia);
        txtGuia.setVisibility(View.GONE);

        isGuia = false;

        ListView listaProductos = (ListView) findViewById(R.id.lista_salidas_productos);
        listaProductos.setAdapter(null);

        LinearLayout linearEncontrado = (LinearLayout) findViewById(R.id.linear_salidas_encontrado);
        linearEncontrado.setVisibility(View.GONE);

        LinearLayout linearBuscar = (LinearLayout) findViewById(R.id.linear_salidas_buscar);
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
                    Toast.makeText(SalidasActivity.this, "Debe escribir Usuario y Contraseña", Toast.LENGTH_LONG).show();
                }
            }
        });

        AlertDialog dialog = builderPopUp.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_FIRMA) {
            if(resultCode == RESULT_OK) {
                String nombre = data.getStringExtra("Nombre");
                String firma = data.getStringExtra("Firma");
                String usuario = data.getStringExtra("Usuario");

                TextView txtNumorden = (TextView)findViewById(R.id.txt_salidas_pedido);
                TextView txtRemision = (TextView)findViewById(R.id.txt_salidas_remision);

                String numorden = txtNumorden.getText().toString();
                String remision = txtRemision.getText().toString();

                new AsyncTaskGuardar().execute(numorden, remision, usuario, nombre, firma);
            }
        }
    }

    private class AsyncTaskBuscarRemision extends android.os.AsyncTask<String, Void, Boolean> {

        private ProgressDialog progDialog;
        private String resultado;
        private boolean isOk;
        private String remision;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progDialog = ProgressDialog.show(SalidasActivity.this, "Buscando Remisión", "Espere un momento...", true);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            final MyApplication app = (MyApplication)getApplication();
            remision = params[0];

            resultado = WsMethods.buscarRemisionSalida(app, remision);
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
                    int numOrden = Integer.parseInt(_document.getElementsByTagName("Pedido").item(0).getTextContent());

                    if (numOrden > 0) {

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

                        //final boolean isGuia;

                        if(_document.getElementsByTagName("Origen").item(0).getTextContent().equals("0")) {
                            isGuia = false;
                        }
                        else {
                            isGuia = true;
                            TextView txtGuia = (TextView) findViewById(R.id.txt_salidas_guia);
                            txtGuia.setVisibility(View.VISIBLE);
                        }

                        final EditText edtxtRemision = (EditText)findViewById(R.id.edtxt_salidas_remision);
                        edtxtRemision.setText("");

                        LinearLayout linearBuscar = (LinearLayout) findViewById(R.id.linear_salidas_buscar);
                        linearBuscar.setVisibility(View.GONE);

                        TextView txtRemision = (TextView) findViewById(R.id.txt_salidas_remision);
                        txtRemision.setText(remision.toUpperCase());

                        TextView txtPedido = (TextView) findViewById(R.id.txt_salidas_pedido);
                        txtPedido.setText(Integer.toString(numOrden));

                        /**************************************************************************/
                        /**Aqui es donde se muestra la listview de productos**/
                        ListView listaProductos = (ListView) findViewById(R.id.lista_salidas_productos);

                        ProductosAdapter adapter = new ProductosAdapter(SalidasActivity.this, lista);
                        listaProductos.setAdapter(adapter);
                        /**************************************************************************/

                        LinearLayout linear = (LinearLayout)findViewById(R.id.linear_salidas_encontrado);
                        linear.setVisibility(View.VISIBLE);

                        ImageButton btnEscanear = (ImageButton)findViewById(R.id.btn_salidas_escanear);
                        btnEscanear.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(isGuia) {
                                    Intent intent = new Intent(SalidasActivity.this, ScanSerieGuiaActivity.class);
                                    intent.putExtra("NombreTabla", Constantes.TABLA_SALIDA_GUIA);
                                    startActivity(intent);
                                }
                                else {
                                    Intent intent = new Intent(SalidasActivity.this, ScanSerieActivity.class);
                                    intent.putExtra("NombreTabla", Constantes.TABLA_SALIDA);
                                    startActivity(intent);
                                }
                            }
                        });

                        ImageButton btnGuardar = (ImageButton)findViewById(R.id.btn_salidas_guardar);
                        btnGuardar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SQLiteDatabase db = helper.getWritableDatabase();
                                Cursor cursor = null;
                                if(isGuia) {
                                    cursor = db.rawQuery("SELECT _id, Serie FROM " + Constantes.TABLA_SALIDA_GUIA, null);
                                }
                                else {
                                    cursor = db.rawQuery("SELECT _id, Serie FROM " + Constantes.TABLA_SALIDA, null);
                                }

                                if(!cursor.moveToFirst()) {
                                    //no hay nada de que
                                    Toast.makeText(SalidasActivity.this, "No hay Series escaneadas para Guardar.", Toast.LENGTH_LONG).show();
                                    zMisSonidos.playSound(SalidasActivity.this, zMisSonidos.Error);
                                    return;
                                }

                                cursor.close();
                                db.close();

                                showLoginPopUp();
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

    private class AsyncTaskLogin extends android.os.AsyncTask<String, Void, Boolean> {

        private ProgressDialog progDialog;
        private String resultado;
        private boolean isOk;
        private String usuario;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progDialog = ProgressDialog.show(SalidasActivity.this, "Autorizando", "Espere un momento...", true);
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
                    + "<Tarea>" + Integer.toString(Constantes.TAREA_SALIDA) + "</Tarea>"
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
                Intent intent = new Intent(SalidasActivity.this, FirmaActivity.class);
                intent.putExtra("Tipo", "Cliente");
                intent.putExtra("id", Constantes.TIPO_FIRMA_CLIENTE);
                intent.putExtra("usuario", usuario);
                startActivityForResult(intent, REQUEST_CODE_FIRMA);
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

            progressDialog = ProgressDialog.show(SalidasActivity.this, "Guardando Salida", "Espere un momento...", true);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            String numorden = params[0];
            String remision = params[1];
            String usuario = params[2];
            String nombre = params[3];
            String firma = params[4];

            MyApplication app = (MyApplication)getApplication();
            boolean isOk;

            AlmacenSql helper = new AlmacenSql(getApplicationContext(), Constantes.TABLA_SALIDA);
            SQLiteDatabase db = helper.getWritableDatabase();
            Cursor cursor;

            if(isGuia) {
                cursor = db.rawQuery("SELECT Serie, Guia FROM " + Constantes.TABLA_SALIDA_GUIA, null);
                cursor.moveToFirst();
            }
            else {
                cursor = db.rawQuery("SELECT Serie FROM " + Constantes.TABLA_SALIDA, null);
                cursor.moveToFirst();
            }

            String ls_xml = "<Almacen>\n";

            ls_xml += "<numproy>" + app.getNumproy() + "</numproy>\n" +
                    "<origen>2</origen>\n" +
                    "<numorden>" + numorden + "</numorden>\n" +
                    "<remision>" + remision + "</remision>\n" +
                    "<usuario>" + usuario + "</usuario>\n" +
                    "<num_almacen>" + app.getNumAlmacen() + "</num_almacen>\n";

            if(isGuia) {
                ls_xml += "<folio>1</folio>\n";

                ls_xml += "<equipos>\n";
                while(!cursor.isAfterLast()) {
                    ls_xml += "<equipo serie=\"" + cursor.getString(0) + "\" "
                            + "Observacion=\"" + cursor.getString(1) + "\" "
                            + "/>\n";
                    cursor.moveToNext();
                }
            }
            else {
                ls_xml += "<folio>0</folio>\n";

                ls_xml += "<equipos>\n";
                while(!cursor.isAfterLast()) {
                    ls_xml += "<equipo serie=\"" + cursor.getString(0) + "\" "
                            + "/>\n";
                    cursor.moveToNext();
                }
            }

            cursor.close();

            ls_xml += "</equipos>\n";

            ls_xml += "<firmas>\n" +
                    "<firma>\n" +
                    "<tipo>" + Constantes.TIPO_FIRMA_CLIENTE + "</tipo>\n" +
                    "<nombre>" + nombre + "</nombre>\n" +
                    "<valor>" + firma + "</valor>\n" +
                    "</firma>\n" +
                    "</firmas>\n" +
                    "</Almacen>\n";

            Log.i("TAG", ls_xml);

            db.close();
            helper.close();

            //resultado = "0"; //getWS_SACData...
            resultado = WsMethods.guardarSalida(app, ls_xml);
            Log.i("TAG", resultado);

            /*File myXml = new File(Constantes.PATH_APP_PROVEE + File.separator + "salida_prueba.xml");
            try {
                myXml.createNewFile();
                FileOutputStream fOut = new FileOutputStream(myXml);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(ls_xml);
                myOutWriter.close();
                fOut.close();
            } catch(IOException io) {
                io.printStackTrace();
                resultado = "4" + io.getMessage();
                isOk = false;
                return isOk;
            }*/

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
                //mostrarMensaje("Éxito", "Se ha Guardado e Impreso la Salida con éxito!", zMisSonidos.Success2);
                new AsyncTaskImprimir().execute(resultado);

                //borrar las series escaneadas
                if(isGuia) {
                    SQLiteDatabase db = helper.getWritableDatabase();
                    db.execSQL("DELETE FROM " + Constantes.TABLA_SALIDA_GUIA);
                    db.close();
                }
                else {
                    SQLiteDatabase db = helper.getWritableDatabase();
                    db.execSQL("DELETE FROM " + Constantes.TABLA_SALIDA);
                    db.close();
                }

                limpiarInfo();
            }
            else {
                mostrarMensaje("Error Salida", "Error al Guardar la Salida. " + resultado, zMisSonidos.Alert);
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

            progressDialog = ProgressDialog.show(SalidasActivity.this, "Imprimiendo", "Espere un momento...", true);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            String nota = params[0];

            MyApplication app = (MyApplication)getApplication();
            isOk = true;

            Log.i("TAG", "Primer intento de Impresión");
            resultado = WsMethods.imprimirSalida(app, nota);
            Log.i("TAG", resultado);

            if(!resultado.substring(0, 1).equals("0")) {
                Log.i("TAG", "Segundo intento de Impresión");
                resultado = WsMethods.imprimirSalida(app, nota);

                if (!resultado.substring(0, 1).equals("0")) {
                    Log.i("TAG", "Tercer intento de Impresión");
                    resultado = WsMethods.imprimirSalida(app, nota);

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
                mostrarMensaje("Éxito", "Se ha Guardado e Impreso la Salida con éxito!", zMisSonidos.Success2);
            }
            else {
                mostrarMensaje("Error Impresión", "Se ha Guardado la Salida pero hubo un error al Imprimirla. Llame a TI. " + resultado, zMisSonidos.Alert);
            }
        }
    }

    public void mostrarMensaje(final String titulo, final String mensaje, final int sonido) {
        zMisSonidos.playSound(getApplicationContext(), sonido);
        zMensajes.MensajeOK(this, titulo, mensaje);
    }

}
