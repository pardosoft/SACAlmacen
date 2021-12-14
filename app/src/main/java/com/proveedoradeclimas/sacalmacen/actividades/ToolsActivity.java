package com.proveedoradeclimas.sacalmacen.actividades;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.proveedoradeclimas.sacalmacen.R;
import com.proveedoradeclimas.sacalmacen.clases.ConexionesSql;
import com.proveedoradeclimas.sacalmacen.clases.Constantes;

public class ToolsActivity extends AppCompatActivity {

    ListView list;
    String[] opciones = {
        "Reimprimir",
        "Database",
        "Cambiar Empresa"
    };
    Integer[] imageId = {
        R.drawable.printer_grey,
        R.drawable.database,
        R.drawable.empresa
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);

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

        CustomListMenu adapter = new CustomListMenu(ToolsActivity.this, opciones, imageId);
        list = (ListView)findViewById(R.id.lista_tools_opciones);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;

                switch (position) {
                    case 0:
                        intent = new Intent(ToolsActivity.this, ReimprimirActivity.class);
                        startActivity(intent);
                        break;
                    case 2:
                        showDialogCambiarEmpresa();
                }
            }
        });
    }

    private void showDialogCambiarEmpresa() {
        //aqui va el code
        final SharedPreferences preferences = getSharedPreferences(Constantes.APP_PREF, MODE_PRIVATE);
        final int empresa = preferences.getInt("empresa", 1);

        AlertDialog.Builder builder = new AlertDialog.Builder(ToolsActivity.this);

        builder.setSingleChoiceItems(R.array.listaEmpresas, empresa - 1, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                String lista[] = ToolsActivity.this.getResources().getStringArray(R.array.listaEmpresas);
                SharedPreferences.Editor editor =  preferences.edit();
                editor.putInt(Constantes.PREF_KEY_EMPRESA, which + 1);
                editor.commit();

                Log.i("TAG", "bt" + Integer.toString(which));

                Log.i("TAG", "cj" + Integer.toString(preferences.getInt(Constantes.PREF_KEY_EMPRESA, -1)));

                if(which + 1 != empresa) {
                    //eliminar los datos de conexion
                    ConexionesSql helper = new ConexionesSql(ToolsActivity.this);
                    SQLiteDatabase db = helper.getWritableDatabase();
                    db.delete(Constantes.TABLA_CONEXIONES, "1", null);
                }

                if(which == 0) {
                    //Proveedora de Climas
                }
                else if(which == 1) {
                    //Eurotyres Monterrey
                }
                else if(which == 2) {
                    //Eurotyres San Luis Potosi
                }
                else {
                    //default no hay nada de khe >:v
                }

                dialog.dismiss();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub

            }
        });

        builder.setTitle("Elija Empresa");
        builder.create().show();
    }

    public class CustomListMenu extends ArrayAdapter<String> {

        private final Activity context;
        private final String [] opciones;
        private final Integer[] imagenes;

        public CustomListMenu(Context context, String [] opciones, Integer [] imagenes) {
            // TODO Auto-generated constructor stub
            super(context, R.layout.layout_imagenlabel_row_list, opciones);
            this.context = (Activity) context;
            this.opciones = opciones;
            this.imagenes = imagenes;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView= inflater.inflate(R.layout.layout_imagenlabel_row_list, null, true);

            ImageView imageView = (ImageView) rowView.findViewById(R.id.img_layout_imagenlabel_imagen);
            TextView txtTitle = (TextView) rowView.findViewById(R.id.txt_layout_imagenlabel_label);

            imageView.setImageResource(imagenes[position]);
            txtTitle.setText(opciones[position]);

            return rowView;
        }
    }
}
