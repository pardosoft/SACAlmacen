package com.proveedoradeclimas.sacalmacen.actividades;

import android.app.ProgressDialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
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

public class InventarioActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_FIRMA_1 = 63;
    private AlmacenSql helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.arrow_left);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    finish();
                }
            });
        }

        helper = new AlmacenSql(InventarioActivity.this, Constantes.TABLA_INVENTARIO);

        Spinner spinnerTipo = ((Spinner)findViewById(R.id.spinner_inventario_toma));
        ArrayAdapter myAdap = (ArrayAdapter) spinnerTipo.getAdapter();
        myAdap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        int pos = myAdap.getPosition("Inventario");
        spinnerTipo.setSelection(pos);

        ImageButton btnEscanear = (ImageButton)findViewById(R.id.btn_inventario_escanear);
        if(btnEscanear != null) {
            btnEscanear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(InventarioActivity.this, ScanInventarioActivity.class);
                    intent.putExtra("NombreTabla", Constantes.TABLA_INVENTARIO);
                    startActivity(intent);
                }
            });
        }

        ImageButton btnGuardar = (ImageButton)findViewById(R.id.btn_inventario_guardar);
        if(btnGuardar != null) {
            btnGuardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLoginPopUp();
                }
            });
        }
    }

    private void showLoginPopUp() {

        LayoutInflater inflater = getLayoutInflater();
        View popUp = inflater.inflate(R.layout.layout_login_popup_dialog, null);

        final EditText edtxtUser = (EditText)popUp.findViewById(R.id.edtxt_layout_login_user);
        final EditText edtxtPass = (EditText)popUp.findViewById(R.id.edtxt_layout_login_pass);

        AlertDialog.Builder builderPopUp = new AlertDialog.Builder(this);
        builderPopUp.setTitle("Autorización");
        builderPopUp.setView(popUp);
        builderPopUp.setCancelable(false);

        builderPopUp.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });

        builderPopUp.setPositiveButton("Autorizar", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if(edtxtUser.getText().toString().length() > 0 && edtxtPass.getText().toString().length() > 0) {
                    new AsyncTaskLogin().execute(edtxtUser.getText().toString(), edtxtPass.getText().toString());
                }
                else {
                    Toast.makeText(InventarioActivity.this, "Debe escribir Usuario y Contraseña", Toast.LENGTH_LONG).show();
                }
            }
        });

        AlertDialog dialog = builderPopUp.create();
        dialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_FIRMA_1){
            if(resultCode == RESULT_OK) {

                Spinner spinnerToma = (Spinner)findViewById(R.id.spinner_inventario_toma);
                String toma = spinnerToma.getSelectedItem().toString();
                String usuario = data.getStringExtra("Usuario");
                String nombre = data.getStringExtra("Nombre");
                String firma = data.getStringExtra("Firma");

                new AsyncTaskGuardar().execute(toma, usuario, nombre, firma);
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

            progDialog = ProgressDialog.show(InventarioActivity.this, "Autorizando", "Espere un momento...", true);
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
                    + "<Tarea>" + Integer.toString(Constantes.TAREA_INVENTARIO) + "</Tarea>"
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

            return isOk;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            progDialog.dismiss();

            if(isOk) {
                //mandar a la actividad de firma
                //Toast.makeText(EntradaActivity.this, "Exito", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(InventarioActivity.this, FirmaActivity.class);
                intent.putExtra("Tipo", "Contador");
                intent.putExtra("id", Constantes.TIPO_FIRMA_CONTADOR);
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

            progDialog = ProgressDialog.show(InventarioActivity.this, "Guardando Entrada", "Espere un momento...", true);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            final MyApplication app = (MyApplication)getApplication();

            String toma = params[0];
            String usuario = params[1];
            String nombre = params[2];
            String firma = params[3];
            String numproy = app.getNumproy();
            String num_almacen = app.getNumAlmacen();

            SQLiteDatabase db = helper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT _id, Serie, Fecha, idZona FROM " + Constantes.TABLA_INVENTARIO + " ORDER BY _id DESC", null);

            String ls_xml = "<AlmacenInventario>\n";

            ls_xml += "<Numproy>" + numproy + "</Numproy>\n"
                    + "<Referencia>" + toma.substring(0, 6) + "</Referencia>"
                    + "<Registro>" + usuario + "</Registro>\n"
                    + "<num_almacen>" + num_almacen + "</num_almacen>\n";

            ls_xml += "<equipos>\n";

            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                ls_xml += "<equipo "
                        + "serie=\"" + cursor.getString(1) + "\" "
                        + "ubicacion=\"" + cursor.getString(3) + "\" "
                        + "FechaScaner=\"" + cursor.getString(2) + "\" "
                        + "/>\n";
                cursor.moveToNext();
            }
            cursor.close();
            db.close();

            ls_xml += "</equipos>\n";

            ls_xml += "<firmas>\n" +
                                "<firma>\n" +
                                "<tipo>" + Constantes.TIPO_FIRMA_CONTADOR + "</tipo>\n" +
                                "<nombre>" + nombre + "</nombre>\n" +
                                "<valor>" + firma + "</valor>\n" +
                            "</firma>\n" +
                        "</firmas>\n" +
                    "</AlmacenInventario>\n";

            Log.i("TAG", ls_xml);

            File myXml = new File(Constantes.PATH_APP_PROVEE + File.separator + "descarga_inventario.xml");

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
            }

            resultado = WsMethods.guardarInventario(app, ls_xml);
            //resultado = "0";

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
                mostrarMensaje("Éxito", "Se ha guardado con éxito la Entrada!", zMisSonidos.Success2);
            }
            else {
                mostrarMensaje("Error", resultado, zMisSonidos.Alert);
            }
        }
    }

    public void mostrarMensaje(final String titulo, final String mensaje, final int sonido) {
        zMisSonidos.playSound(getApplicationContext(), sonido);
        zMensajes.MensajeOK(this, titulo, mensaje);
    }
}
