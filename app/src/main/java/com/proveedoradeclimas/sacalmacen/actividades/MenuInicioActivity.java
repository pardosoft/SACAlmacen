package com.proveedoradeclimas.sacalmacen.actividades;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.proveedoradeclimas.sacalmacen.clases.ConexionesSql;
import com.proveedoradeclimas.sacalmacen.clases.Constantes;
import com.proveedoradeclimas.sacalmacen.clases.MyApplication;
import com.proveedoradeclimas.sacalmacen.clases.WsMethods;
import com.proveedoradeclimas.sacalmacen.clases.zMensajes;
import com.proveedoradeclimas.sacalmacen.clases.zMisSonidos;
import com.proveedoradeclimas.sacalmacen.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MenuInicioActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_SETTINGS = 93;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_inicio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!settingsExists()) {
            startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_CODE_SETTINGS);
        }

        Button btnSalidas = (Button)findViewById(R.id.button_salidas);
        Button btnEntradas = (Button)findViewById(R.id.button_entradas);
        Button btnInventario = (Button)findViewById(R.id.button_inventario);
        Button btnTransferencia = (Button)findViewById(R.id.button_transferencia);
        Button btnDevolucion = (Button)findViewById(R.id.button_devolucion);
        Button btnAjuste = (Button)findViewById(R.id.button_ajuste);
        Button btnApartado = (Button)findViewById(R.id.button_apartado);
//        Button btnTest = (Button)findViewById(R.id.button_test);

        btnSalidas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuInicioActivity.this, SalidasActivity.class);
                startActivity(intent);
            }
        });

        btnEntradas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuInicioActivity.this, EntradaActivity.class);
                startActivity(intent);
            }
        });

        btnInventario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTaskIsInventarioAbierto().execute();
                /*Intent intent = new Intent(MenuInicioActivity.this, InventarioActivity.class);
                startActivity(intent);*/
            }
        });

        btnTransferencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuInicioActivity.this, TransferenciaActivity.class);
                startActivity(intent);
            }
        });

        btnDevolucion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuInicioActivity.this, DevolucionActivity.class);
                startActivity(intent);
            }
        });

        btnAjuste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuInicioActivity.this, AjusteActivity.class);
                startActivity(intent);
            }
        });

        btnApartado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuInicioActivity.this, ApartadoActivity.class);
                startActivity(intent);
            }
        });

//        btnTest.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MenuInicioActivity.this, TestActivity.class);
//                startActivity(intent);
//            }
//        });

        /*TextView txtManufacturer = (TextView)findViewById(R.id.txt_inicio_manufacturer);
        TextView txtModel = (TextView)findViewById(R.id.txt_inicio_model);
        TextView txtBrand = (TextView)findViewById(R.id.txt_inicio_brand);
        TextView txtDevice = (TextView)findViewById(R.id.txt_inicio_model_device);
        TextView txtProduct = (TextView)findViewById(R.id.txt_inicio_model_product);

        txtManufacturer.setText(Build.MANUFACTURER);
        txtModel.setText(Build.MODEL);
        txtBrand.setText(Build.BRAND);
        txtDevice.setText(Build.DEVICE);
        txtProduct.setText(Build.PRODUCT);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_actualizar_app) {
            /*Intent intent = new Intent(MainActivity.this, ConfiguracionActivity.class);
            startActivity(intent);*/
            new AsyncTaskUpdateApp().execute();
        }
        else if(id == R.id.action_actualizar_lista) {
            new AsyncTaskUpdateAlmacenes().execute();
        }
        else if(id == R.id.action_conexiones) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean settingsExists()
    {
        ConexionesSql helper = new ConexionesSql(getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + Constantes.TABLA_CONEXIONES, null);

        cursor.moveToFirst();

        if(cursor.getInt(0) > 0)
        {
            cursor.close();
            db.close();
            return true;
        }
        else
        {
            cursor.close();
            db.close();
            return false;
        }
    }

    private class AsyncTaskUpdateApp extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog progressDialog;
        private String resultado;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            progressDialog = new ProgressDialog(MenuInicioActivity.this);
            progressDialog.setTitle("Descargando Aplicación");
            progressDialog.setMessage("Espere un momento...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);

            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            MyApplication app = (MyApplication)getApplication();
            boolean isOk = false;
            URL url;

            try {
                url = new URL((app).getEndPoint() + File.separator + Constantes.FILE_APK_PROVEE);

                URLConnection c = url.openConnection();
                int count;

                c.connect();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(Constantes.PATH_APP_PROVEE + File.separator + Constantes.FILE_APK_PROVEE);

                byte data[] = new byte[1024];
                @SuppressWarnings("unused")
                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                isOk = true;

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                resultado = "Error al descargar la Aplicación. " + e.getMessage();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                resultado = "Error al descargar la Aplicación. " + e.getMessage();
            }

            return isOk;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            progressDialog.dismiss();

            if(result) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(Constantes.PATH_APP_PROVEE + File.separator + Constantes.FILE_APK_PROVEE)), "application/vnd.android.package-archive");
                startActivity(intent);
            }
            else {
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
            progressDialog = ProgressDialog.show(MenuInicioActivity.this, "Descargando Almacenes", "Espere un momento...", true, false);
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
                Toast.makeText(MenuInicioActivity.this,
                        "Se ha descargado los Almacénes con éxito.",
                        Toast.LENGTH_LONG).show();
            }
            else {
                mostrarMensaje("Error", resultat, zMisSonidos.Alert);
            }
        }
    }

    private class AsyncTaskIsInventarioAbierto extends AsyncTask<String, Void, Boolean> {

        ProgressDialog progressDialog;
        private String resultado;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(MenuInicioActivity.this);
            progressDialog.setTitle("Validando Inventario");
            progressDialog.setMessage("Espere un momento...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            MyApplication app = (MyApplication)getApplication();

            resultado = WsMethods.isInventarioAbierto(app);
            //resultado = "050";
            Log.i("TAG", resultado);

            if(!resultado.substring(0, 1).equals("0")) {
                resultado = resultado.substring(1);
                return false;
            }

            resultado = resultado.substring(1);

            if(resultado.length() == 0) {
                resultado = "No se pudo obtener el Número de Toma.";
                return false;
            }

            if(resultado.substring(0, 1).equals("0")) {
                resultado = "No hay Toma de Inventario abierta para esta Sucursal.";
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            super.onPostExecute(ok);

            progressDialog.dismiss();

            if(ok) {
                //abrir activity
                SharedPreferences preferences = getSharedPreferences("Proveedora", MODE_PRIVATE);
                boolean inventarioLinea = preferences.getBoolean("isInventarioLinea", false);
                if(inventarioLinea) {
                    Intent i = new Intent(MenuInicioActivity.this, ScanInventarioLineaActivity.class);
                    i.putExtra("NombreTabla", "Inventario");
                    startActivity(i);
                }
                else {
                    Intent i = new Intent(MenuInicioActivity.this, InventarioActivity.class);
                    startActivity(i);
                }
            }
            else {
                //showMensaje();
                mostrarMensaje("Error", resultado, zMisSonidos.Alert);
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
        if(requestCode == REQUEST_CODE_SETTINGS) {
            if(resultCode == RESULT_OK) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }
    }
}
