package com.proveedoradeclimas.sacalmacen.actividades;

import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.proveedoradeclimas.sacalmacen.clases.Constantes;

import java.io.File;

public class SplashScreenActivity extends AppCompatActivity {

    private ScanManager scanManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.MANUFACTURER.equals("UBX") && (Build.MODEL.equals("SQ42") || Build.MODEL.equals("S95"))) {

            scanManager = new ScanManager();
            scanManager.openScanner();

            scanManager.lockTrigger();
            scanManager.closeScanner();
        }

        File file = new File (Constantes.PATH_APP_PROVEE);

        if (!file.exists()) {
            file.mkdirs();
        }

        Intent intent = new Intent(SplashScreenActivity.this, MenuInicioActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }
}
