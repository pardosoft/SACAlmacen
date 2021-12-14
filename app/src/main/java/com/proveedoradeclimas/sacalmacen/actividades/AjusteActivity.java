package com.proveedoradeclimas.sacalmacen.actividades;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.proveedoradeclimas.sacalmacen.R;
import com.proveedoradeclimas.sacalmacen.clases.AlmacenSql;
import com.proveedoradeclimas.sacalmacen.clases.Constantes;
import com.proveedoradeclimas.sacalmacen.clases.MyApplication;
import com.proveedoradeclimas.sacalmacen.clases.WsMethods;
import com.proveedoradeclimas.sacalmacen.clases.zMensajes;
import com.proveedoradeclimas.sacalmacen.clases.zMisSonidos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class AjusteActivity extends AppCompatActivity {

    private AlmacenSql helper;
    static final int MAX_CHARS = 250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajuste);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        helper = new AlmacenSql(getApplicationContext(), Constantes.TABLA_AJUSTE);

        toolbar.setNavigationIcon(R.drawable.arrow_left);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        EditText edtxtNotas = (EditText)findViewById(R.id.edtxtNotas_Ajustes);
        final TextView txtCount = (TextView)findViewById(R.id.txtCountChars_Ajustes);
        txtCount.setText("Caracteres restantes: " + Integer.toString(MAX_CHARS));

        final ImageView imageOk = (ImageView)findViewById(R.id.imageview_ajuste_ok);

        imageOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //aqui va esconder teclado
                LinearLayout layout = (LinearLayout)findViewById(R.id.container_ajuste);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
                imageOk.setVisibility(View.GONE);
            }
        });

        edtxtNotas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                imageOk.setVisibility(View.VISIBLE);
            }
        });

        edtxtNotas.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable edtxtNotas) {
                // TODO Auto-generated method stub
                txtCount.setText("Caracteres restantes: " + Integer.toString(250 - edtxtNotas.length()));
            }
        });



        ImageButton btnEscanear = (ImageButton)findViewById(R.id.btn_ajuste_escanear);
        btnEscanear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AjusteActivity.this, ScanAjusteActivity.class);
                intent.putExtra("NombreTabla", Constantes.TABLA_AJUSTE);
                startActivity(intent);
            }
        });

        ImageButton btnGuardar = (ImageButton)findViewById(R.id.btn_ajuste_guardar);
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    Toast.makeText(AjusteActivity.this, "Debe escribir Usuario y Contraseña", Toast.LENGTH_LONG).show();
                }
            }
        });

        AlertDialog dialog = builderPopUp.create();
        dialog.show();
    }

    private class AsyncTaskLogin extends android.os.AsyncTask<String, Void, Boolean> {

        private ProgressDialog progDialog;
        private String resultado;
        private boolean isOk;
        private String usuario;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progDialog = ProgressDialog.show(AjusteActivity.this, "Autorizando", "Espere un momento...", true);
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
                    + "<Tarea>" + Integer.toString(Constantes.TAREA_AJUSTE) + "</Tarea>"
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
                //asyncTaskGuardar}
                EditText txtNotas = (EditText)findViewById(R.id.edtxtNotas_Ajustes);
                new AsyncTaskGuardar().execute(txtNotas.getText().toString(), usuario);
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

            progressDialog = ProgressDialog.show(AjusteActivity.this, "Guardando Ajuste", "Espere un momento...", true);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            String notas = params[0];
            String usuario = params[1];

            MyApplication app = (MyApplication)getApplication();
            boolean isOk;

            AlmacenSql helper = new AlmacenSql(getApplicationContext(), Constantes.TABLA_AJUSTE);
            SQLiteDatabase db = helper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT Modelo, Serie, idAjuste FROM " + Constantes.TABLA_AJUSTE, null);
            cursor.moveToFirst();

            String ls_xml = "<Almacen>\n";

            ls_xml += "<numproy>" + app.getNumproy() + "</numproy>\n" +
                    "<usuario>" + usuario + "</usuario>\n" +
                    "<placa>" + notas + "</placa>\n" +
                    "<num_almacen>" + app.getNumAlmacen() + "</num_almacen>\n";

            ls_xml += "<equipos>\n";
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                ls_xml += "<equipo "
                        + "modelo=\"" + cursor.getString(0) + "\" "
                        + "serie=\"" + cursor.getString(1) + "\" "
                        + "Observacion=\"" + cursor.getString(2) + "\" "
                        + "/>\n";
                cursor.moveToNext();
            }

            cursor.close();

            ls_xml += "</equipos>\n" +
                    "</Almacen>\n";

            Log.i("TAG", ls_xml);

            db.close();
            helper.close();

            //resultado = "0"; //getWS_SACData...
            resultado = WsMethods.guardarAjuste(app, ls_xml);
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
                mostrarMensaje("Éxito", "Se ha Guardado el Ajuste con éxito!", zMisSonidos.Success2);

                SQLiteDatabase db = helper.getWritableDatabase();
                db.execSQL("DELETE FROM " + Constantes.TABLA_AJUSTE);
                db.close();

                EditText edtxtNotas = (EditText)findViewById(R.id.edtxtNotas_Ajustes);
                edtxtNotas.setText("");
            }
            else {
                mostrarMensaje("Error Ajuste", "Error al Guardar la Ajuste. " + resultado, zMisSonidos.Alert);
            }
        }
    }

    public void mostrarMensaje(final String titulo, final String mensaje, final int sonido) {
        zMisSonidos.playSound(getApplicationContext(), sonido);
        zMensajes.MensajeOK(this, titulo, mensaje);
    }
}
