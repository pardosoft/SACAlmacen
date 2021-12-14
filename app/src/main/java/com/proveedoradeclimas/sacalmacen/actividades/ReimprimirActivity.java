package com.proveedoradeclimas.sacalmacen.actividades;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.proveedoradeclimas.sacalmacen.R;
import com.proveedoradeclimas.sacalmacen.clases.MyApplication;
import com.proveedoradeclimas.sacalmacen.clases.WsMethods;
import com.proveedoradeclimas.sacalmacen.clases.zMensajes;
import com.proveedoradeclimas.sacalmacen.clases.zMisSonidos;

public class ReimprimirActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reimprimir);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.title_activity_tools);

        toolbar.setNavigationIcon(R.drawable.arrow_left);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final EditText edtxtRemision = (EditText)findViewById(R.id.edtxt_reimprimir_remision);
        final EditText edtxtCamion = (EditText)findViewById(R.id.edtxt_reimprimir_camion);

        final Spinner spinnerOrigen = (Spinner)findViewById(R.id.spinner_reimprimir_origen);
        spinnerOrigen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                EditText edtxtCamion = (EditText)findViewById(R.id.edtxt_reimprimir_camion);
                if(position == 0) {
                    edtxtCamion.setText("");
                    edtxtCamion.setVisibility(View.VISIBLE);
                }
                else {
                    edtxtCamion.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ImageButton btnImprimir = (ImageButton)findViewById(R.id.btn_reimprimir_imprimir);
        btnImprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String remision = edtxtRemision.getText().toString();
                String camion = edtxtCamion.getText().toString();
                String opcion = Long.toString(spinnerOrigen.getSelectedItemId());

                new AsyncTaskNotaImpresion().execute(remision, camion, opcion);
            }
        });
    }

    private class AsyncTaskNotaImpresion extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog progressDialog;
        private String resultado;
        private String remision;
        private String camion;
        private String opcion;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ReimprimirActivity.this, "Obteniendo Info", "Espere un momento...", true);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            remision = params[0];
            camion = params[1];
            opcion = params[2];

            MyApplication app = (MyApplication)getApplication();
            boolean isOk;

            if(opcion.equals("0")) {
                //Entrada
                resultado = WsMethods.getXmlNotaEntrada(app, remision, Integer.parseInt(camion));
            }
            else if(opcion.equals("1")) {
                //Salida
                resultado = WsMethods.getXmlNotaSalida(app, remision);
            }
            else if(opcion.equals("2")) {
                //Transfer
                resultado = WsMethods.getXmlNotaTransferencia(app, remision);
            }
            Log.i("TAG", resultado);

            if(resultado.substring(0, 1).equals("0")) {
                isOk = true;
                resultado = resultado.substring(1);
            }
            else {
                isOk = false;
                resultado = resultado.substring(1);
            }

            return isOk;
        }

        @Override
        protected void onPostExecute(Boolean isOk) {
            super.onPostExecute(isOk);

            progressDialog.dismiss();

            if(isOk) {
                new AsyncTaskImprimir().execute(resultado, opcion);
            }
            else {
                mostrarMensaje("Error Información", "Hubo un Error al traer la Información. " + resultado, zMisSonidos.Alert);
            }
        }
    }

    private class AsyncTaskImprimir extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog progressDialog;
        private String resultado;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ReimprimirActivity.this, "Imprimiendo", "Espere un momento...", true);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            String nota = params[0];
            String opcion = params[1];

            MyApplication app = (MyApplication)getApplication();
            boolean isOk;

            if(opcion.equals("0")) {
                resultado = WsMethods.imprimirEntrada(app, nota);
            }
            else if(opcion.equals("1")) {
                resultado = WsMethods.imprimirSalida(app, nota);
            }
            else if(opcion.equals("2")) {
                resultado = WsMethods.imprimirTransferencia(app, nota);
            }
            Log.i("TAG", resultado);

            if(resultado.substring(0, 1).equals("0")) {
                isOk = true;
            }
            else {
                isOk = false;
                resultado = resultado.substring(1);
            }

            return isOk;
        }

        @Override
        protected void onPostExecute(Boolean isOk) {
            super.onPostExecute(isOk);

            progressDialog.dismiss();

            if(isOk) {
                mostrarMensaje("Éxito", "Se ha impreso la Hoja de Series con éxito.", zMisSonidos.Alert);
            }
            else {
                mostrarMensaje("Error Impresión", "Hubo un Error al imprimir la Hoja de Series. " + resultado, zMisSonidos.Alert);
            }
        }
    }

    public void mostrarMensaje(final String titulo, final String mensaje, final int sonido) {
        zMisSonidos.playSound(getApplicationContext(), sonido);
        zMensajes.MensajeOK(this, titulo, mensaje);
    }
}
