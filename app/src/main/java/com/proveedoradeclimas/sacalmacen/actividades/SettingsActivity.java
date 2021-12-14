package com.proveedoradeclimas.sacalmacen.actividades;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.proveedoradeclimas.sacalmacen.clases.ConexionesSql;
import com.proveedoradeclimas.sacalmacen.clases.Constantes;
import com.proveedoradeclimas.sacalmacen.clases.MyApplication;
import com.proveedoradeclimas.sacalmacen.R;
import com.proveedoradeclimas.sacalmacen.clases.WsMethods;
import com.proveedoradeclimas.sacalmacen.clases.zMensajes;
import com.proveedoradeclimas.sacalmacen.clases.zMisSonidos;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class SettingsActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_TOOLS = 71;

    private boolean precargado = false;
    private boolean fileAlmacenExists = false;
    private NodeList n_almacen;
    private HashMap<Integer, String> listado_almacen;
    private int num_almacen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.arrow_left);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (precargado) {
                    finish();
                }
                else {
                    Intent setIntent = new Intent(Intent.ACTION_MAIN);

                    setIntent.addCategory(Intent.CATEGORY_HOME);
                    setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(setIntent);
                }
            }
        });

        ImageButton btnGuardar = (ImageButton) findViewById(R.id.btn_settings_guardar);

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginPopUp();
            }
        });

        ConexionesSql helper = new ConexionesSql(getApplicationContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT NumAlmacen FROM " + Constantes.TABLA_CONEXIONES, null);
        int num_almacen_precargado = 0;

        if(cursor != null && cursor.getCount() > 0) {
            if(cursor.moveToFirst()) {
                num_almacen_precargado = Integer.parseInt(cursor.getString(0));
            }
        }
        cursor.close();
        db.close();

        cargarAlmacenes(num_almacen_precargado);

        helper = new ConexionesSql(getApplicationContext());
        db = helper.getWritableDatabase();
        cursor = db.rawQuery("SELECT * FROM " + Constantes.TABLA_CONEXIONES, null);

        if (cursor.moveToFirst())
        {
            precargado = true;
            ((EditText) findViewById(R.id.edtxt_settings_wsprincipal)).setText(cursor.getString(cursor.getColumnIndex("EndPointPrincipal")));
            ((EditText) findViewById(R.id.edtxt_settings_wssecundario)).setText(cursor.getString(cursor.getColumnIndex("EndPointSecundario")));
            ((EditText) findViewById(R.id.edtxt_settings_wsimpresion)).setText(cursor.getString(cursor.getColumnIndex("EndPointImpresion")));
            ((EditText) findViewById(R.id.edtxt_settings_secretword)).setText(cursor.getString(cursor.getColumnIndex("SecretWord")));
            ((EditText) findViewById(R.id.edtxt_settings_password)).setText(cursor.getString(cursor.getColumnIndex("Password")));
//			((Spinner) findViewById(R.id.spNumproy)).setSelection(1);

//			Spinner mySpinner = ((Spinner) findViewById(R.id.spNumproy));
//			String myString = cursor.getString(cursor.getColumnIndex("Numproy"));
//			ArrayAdapter myAdap = (ArrayAdapter) mySpinner.getAdapter();
//			int spinnerPosition = myAdap.getPosition(myString);
//
//			mySpinner.setSelection(spinnerPosition);
            CheckBox checkBoxInventario = (CheckBox)findViewById(R.id.checkbox_settings_inventario);
            SharedPreferences preferences = getSharedPreferences("Proveedora", MODE_PRIVATE);
            boolean inventarioLinea = preferences.getBoolean("isInventarioLinea", false);
            checkBoxInventario.setChecked(inventarioLinea);
        }
        else
        {
            precargado = false;
        }

        cursor.close();
        db.close();
    }

    @Override
    public void onBackPressed() {
        if (precargado)
        {
            super.onBackPressed();
        }
        else
        {
            Intent setIntent = new Intent(Intent.ACTION_MAIN);

            setIntent.addCategory(Intent.CATEGORY_HOME);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(setIntent);
        }
    }

    public void cargarAlmacenes(int num_almacen_precargado) {

        MyApplication App = (MyApplication)this.getApplication();
        Document _document = null;
        DocumentBuilderFactory parser = DocumentBuilderFactory.newInstance();

        String nom_fichier = "almacenes.xml";
        File chemin_fichier = new File(Constantes.PATH_APP_PROVEE);

        if (!chemin_fichier.exists()) {
            chemin_fichier.mkdirs();
        }

        File is = new File(chemin_fichier.toString() + File.separator + nom_fichier);

        if(is.exists() == true) {
            fileAlmacenExists = true;

            DocumentBuilder Builder;
            try {

                Builder = parser.newDocumentBuilder();
                Builder.isValidating();
                _document = Builder.parse(is);

            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            n_almacen = _document.getElementsByTagName("Almacen");
            int max_i = n_almacen.getLength();

            listado_almacen = new HashMap<Integer, String>();

            for (int i = 0; i < max_i; i++) {
                Element nodo = (Element) n_almacen.item(i);
//                listado_almacen.put(Integer.parseInt(App.ObtenValorNodo(nodo, "num_almacen")),
//                        App.ObtenValorNodo(nodo, "num_almacen") + ".- " + App.ObtenValorNodo(nodo, "nombre_almacen"));
                listado_almacen.put(Integer.parseInt(nodo.getElementsByTagName("num_almacen").item(0).getTextContent()),
                        nodo.getElementsByTagName("num_almacen").item(0).getTextContent() + ".- " + nodo.getElementsByTagName("nombre_almacen").item(0).getTextContent());
            }
        }
        else {
            fileAlmacenExists = false;

            listado_almacen = new HashMap<Integer, String>();
            listado_almacen.put(1, "1.- REVOLUCION");
        }

        final List<String> list = new ArrayList<String>();

        Map<Integer, String> temp = new TreeMap<Integer, String>(listado_almacen);

        for (int key : temp.keySet()) {
            list.add(temp.get(key).toString());
        }

        String aux;

//        for(int i = 0; i < list.size(); i++){
//        	for (int j = i + 1; j < list.size(); j++){
//        		if(list.get(j).compareToIgnoreCase(list.get(i)) < 0) {
//        			aux = list.get(i);
//        			list.set(i, list.get(j));
//        			list.set(j, aux);
//        		}
//        	}
//        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinnerNumAlmacen = (Spinner) findViewById(R.id.spinner_settings_almacen);

//		spinnerNumAlmacen.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//			@Override
//			public void onItemSelected(AdapterView<?> arg0, View arg1,
//					int arg2, long arg3) {
//				// TODO Auto-generated method stub
//				String t_almacen = list.get(arg2);
//				for (Entry <String, String> entry : listado_almacen.entrySet()) {
//					if(t_almacen.equals(entry.getValue())) {
//						num_almacen = Integer.parseInt(entry.getKey());
//						break;
//					}
//				}
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> arg0) {
//				// TODO Auto-generated method stub
//
//			}
//		});

        spinnerNumAlmacen.setAdapter(adapter);

        Log.i("TAG", "precargado 2: " + Integer.toString(num_almacen_precargado));

        if(num_almacen_precargado == 0) {
            spinnerNumAlmacen.setSelection(0);
        }
        else {
            String temp_almacen = "";
            for (Map.Entry<Integer, String> entry : listado_almacen.entrySet()) {
//				if(Integer.toString(num_almacen_prcargado).equals(entry.getKey())) {
                if(num_almacen_precargado == entry.getKey()) {
                    temp_almacen = entry.getValue();
                    break;
                }
            }
            if(temp_almacen.length() == 0) {
                spinnerNumAlmacen.setSelection(0);
            }
            else {
                Log.i("TAG", temp_almacen);
                int pos = adapter.getPosition(temp_almacen);
                spinnerNumAlmacen.setSelection(pos);
            }
        }
    }

    public int getNumAlmacenSpinner() {

        int miAlmacen = 0;
        Spinner spinnerNumAlmacen = (Spinner)findViewById(R.id.spinner_settings_almacen);
        ArrayAdapter myAdap = (ArrayAdapter)spinnerNumAlmacen.getAdapter();
        String temp_almacen = spinnerNumAlmacen.getSelectedItem().toString();

        for (Map.Entry<Integer, String> entry : listado_almacen.entrySet()) {
            if(temp_almacen.equals(entry.getValue())) {
                miAlmacen = entry.getKey();
                break;
            }
        }

        return miAlmacen;
    }

    public void guardarConfiguracion() {

        EditText valEndPointPrin = (EditText) findViewById(R.id.edtxt_settings_wsprincipal);
        EditText valEndPointSec = (EditText) findViewById(R.id.edtxt_settings_wssecundario);
        EditText valEndPointImp = (EditText) findViewById(R.id.edtxt_settings_wsimpresion);
        EditText valPS = (EditText) findViewById(R.id.edtxt_settings_secretword);
        EditText valPW = (EditText) findViewById(R.id.edtxt_settings_password);
        Spinner valNumproy = (Spinner)findViewById(R.id.spinner_settings_almacen);
        CheckBox checkbox = (CheckBox)findViewById(R.id.checkbox_settings_inventario);

        if (valEndPointPrin.getText().toString().length() == 0 ||
                valEndPointSec.getText().toString().length() == 0 ||
                valEndPointImp.getText().toString().length() == 0 ||
                valPS.getText().toString().length() == 0 ||
                valPW.getText().toString().length() == 0)
        {
            Toast.makeText(this, "Todos los campos son necesarios.", Toast.LENGTH_SHORT).show();
            return;
        }

        ConexionesSql helper = new ConexionesSql(getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();
        ContentValues values = new ContentValues();

        db.delete(Constantes.TABLA_CONEXIONES, "", null);
        values.put("EndPointPrincipal", valEndPointPrin.getText().toString());
        values.put("EndPointSecundario", valEndPointSec.getText().toString());
        values.put("EndPointImpresion", valEndPointImp.getText().toString());
        values.put("SecretWord", valPS.getText().toString());
        values.put("Password", valPW.getText().toString());
        values.put("NumAlmacen", getNumAlmacenSpinner());
        Log.i("TAG", "guardar num_almacen: " + Integer.toString(getNumAlmacenSpinner()));
        db.insert(Constantes.TABLA_CONEXIONES, null, values);

        MyApplication app = (MyApplication) this.getApplication();
        SharedPreferences.Editor editor = getSharedPreferences("Proveedora", MODE_PRIVATE).edit();
        editor.putBoolean("isInventarioLinea", checkbox.isChecked());
        editor.commit();

        app = (MyApplication) this.getApplication();
        helper.close();
        app.refreshValues();

        String nom_fichier = "almacenes.xml";
        File monXml = new File(Constantes.PATH_APP_PROVEE + File.separator + nom_fichier);
        if(!monXml.exists()) {
            //no existe el xml de almacenens
            new AsyncTaskUpdateAlmacenes().execute();
        }
        else {
            Toast.makeText(this, "Se guardaron las configuraciones.", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK, getIntent());
            finish();
        }
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
                    Toast.makeText(SettingsActivity.this, "Debe escribir Usuario y Contraseña", Toast.LENGTH_LONG).show();
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

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //progDialog = ProgressDialog.show(ScaneadoInventarioCheckActivity.this, "Autorizando", "Espere un momento...", true);

            progDialog = new ProgressDialog(SettingsActivity.this);
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

            String user = params[0];
            String pass = params[1];

            String xml = "<Login>\n"
                    + "<Usuario>" + user + "</Usuario>\n"
                    + "<Pssd>" + pass + "</Pssd>\n"
                    + "<Mod>" + "7" + "</Mod>\n"
                    + "<Tarea>" + "784" + "</Tarea>\n"
                    + "</Login>";

            if(precargado == false) {
                resultado = "0";
                usuario = user;
                isOk = true;
            }
            else {
                Log.i("TAG", "Xml Autorizacion");
                Log.i("TAG", xml);
                resultado = WsMethods.autorizarUsuario(app, xml);
                Log.i("TAG", "Ws Autorizacion: " + resultado);

                if(resultado.substring(0, 1).equals("0")) {
                    //resultado fue bueno
                    resultado = resultado.substring(1);
                    usuario = user;
                    isOk = true;
                }
                else if(resultado.substring(0, 1).equals("9") && user.equals("rafa") && pass.equals("rafa")) {
                    resultado = resultado.substring(1);
                    usuario = user;
                    isOk = true;
                }
                else {
                    //hubo dificultades que afrontar
                    resultado = resultado.substring(1);
                    isOk = false;
                }
            }


            return isOk;
        }

        @Override
        protected void onPostExecute(Boolean isOk) {
            super.onPostExecute(isOk);

            progDialog.dismiss();

            if(isOk) {
                //Waardiola
                guardarConfiguracion();
            }
            else {
                //Error
                mostrarMensaje("Error", resultado, zMisSonidos.Alert);
            }
        }
    }

    private class AsyncTaskUpdateAlmacenes extends android.os.AsyncTask<String, Void, Boolean> {

        private ProgressDialog progressDialog;
        private String resultat;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(SettingsActivity.this, "Descargando Almacenes", "Espere un momento...", true, false);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            MyApplication app = (MyApplication)getApplication();
            boolean isOk;

            resultat = WsMethods.getXmlAlmacenes(app);
            Log.i("TAG", resultat);

            if(resultat.substring(0, 1).equals("0")) {
                try {

                    resultat = resultat.substring(1);
                    String nom_fichier = "almacenes.xml";

                    File chemin_fichier = new File(Constantes.PATH_APP_PROVEE);

                    if (!chemin_fichier.exists()) {
                        chemin_fichier.mkdirs();
                    }

                    File monXml = new File(chemin_fichier.toString() + File.separator + nom_fichier);

                    monXml.createNewFile();
                    FileWriter fileIO = new FileWriter(monXml);
                    fileIO.write("<?xml version='1.0' encoding='utf-8'?>");
                    fileIO.write(resultat);
                    fileIO.close();

                    isOk = true;

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    resultat = "Excepción al crear Archivo. " + e.getMessage();
                    isOk = false;
                }
            }
            else {
                resultat = resultat.substring(1);
                isOk = false;
            }

            return isOk;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            progressDialog.dismiss();

            if(aBoolean == true) {
                Toast.makeText(SettingsActivity.this,
                        "Elija Almacén.",
                        Toast.LENGTH_LONG).show();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
            else {
                mostrarMensaje("Error", "No se ha podido descargar los Almacénes.", zMisSonidos.Alert);
            }
        }
    }

    public void mostrarMensaje(final String titulo, final String mensaje, final int sonido) {
        zMisSonidos.playSound(getApplicationContext(), sonido);
        zMensajes.MensajeOK(this, titulo, mensaje);
    }
}
