package com.proveedoradeclimas.sacalmacen.actividades;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.proveedoradeclimas.sacalmacen.R;
import com.proveedoradeclimas.sacalmacen.clases.DrawingView;

public class FirmaActivity extends AppCompatActivity {

    private DrawingView drawView;
    private String usuario;
    private String Password;
    private String idZona;
    private String descZona;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firma);

        Bundle extra = getIntent().getExtras();
        String tipo = extra.getString("Tipo");
        usuario = extra.getString("usuario");
        if(getIntent().hasExtra("Password")) {
            Password = getIntent().getStringExtra("Password");
        }
        if(getIntent().hasExtra("idZona")) {
            idZona = getIntent().getStringExtra("idZona");
        }
        if(getIntent().hasExtra("descZona")) {
            descZona = getIntent().getStringExtra("descZona");
        }
        /***/

        TextView txtTipo = (TextView)findViewById(R.id.txt_firma_tipo);
        txtTipo.setText(txtTipo.getText() + " " + tipo);

        drawView = (DrawingView)findViewById(R.id.drawing);
        ImageButton btnLimpiar = (ImageButton)findViewById(R.id.btn_firma_limpiar);
        ImageButton btnGuardar = (ImageButton)findViewById(R.id.btn_firma_guardar);

        if(Build.MANUFACTURER.equals("geofanci") && Build.MODEL.equals("PDA")) {
            drawView.setBrushSize(1.5f);
            drawView.setTouchTolerance(4);
            drawView.setDivisor(1);
            drawView.setSmoothActive(true);
            drawView.setTranslationActive(false);
            btnLimpiar.setImageResource(R.drawable.eraser_24_white);
            btnGuardar.setImageResource(R.drawable.floppy_24);
        }
        else {
            drawView.setBrushSize(3f);
            drawView.setTouchTolerance(1);
            drawView.setDivisor(2);
            drawView.setSmoothActive(false);
            drawView.setTranslationActive(true);
        }

        btnLimpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                limpiar();
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardar();
            }
        });
    }

    private void limpiar() {
        drawView.startNew();
        drawView.setCoordenadas();
        EditText edtxtNombre = (EditText)findViewById(R.id.edtxt_firma_nombre);
        edtxtNombre.setText("");
    }

    private void guardar() {

        EditText edtxtNombre = (EditText)findViewById(R.id.edtxt_firma_nombre);

        if(edtxtNombre.getText().toString().length() > 0 && drawView.getCoordenadas().length() > 0) {

            Intent i = getIntent();

            i.putExtra("Nombre", edtxtNombre.getText().toString());
            i.putExtra("Firma", drawView.getCoordenadas());
            i.putExtra("Usuario", usuario);
            if(Password != null) {
                i.putExtra("Password", Password);
            }
            if(idZona != null) {
                i.putExtra("idZona", idZona);
            }
            if(descZona != null) {
                i.putExtra("descZona", descZona);
            }
            setResult(RESULT_OK, i);
            finish();
        }
        else {
            //mensaje de mal proceder
            Toast.makeText(getApplicationContext(), "Información incompleta", Toast.LENGTH_SHORT ).show();
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        return;
    }
}
