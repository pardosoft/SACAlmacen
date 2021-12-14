package com.proveedoradeclimas.sacalmacen.adaptadores;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.proveedoradeclimas.sacalmacen.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrador on 26/09/2016.
 */
public class ProductosAdapter extends BaseAdapter {

    public ArrayList<HashMap<String, String>> lista;
    Activity activity;

    public ProductosAdapter(Activity activity, ArrayList<HashMap<String, String>> lista ) {
        super();
        this.activity = activity;
        this.lista = lista;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return lista.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        ViewHolder holder;
        LayoutInflater inflater = activity.getLayoutInflater();

        if( convertView == null) {
            convertView = inflater.inflate(R.layout.layout_productos_row_list, null);
            holder = new ViewHolder();
            holder.txtProductos = (TextView) convertView.findViewById(R.id.txt_layout_producto_modelo);
            holder.txtCantidad = (TextView) convertView.findViewById(R.id.txt_layout_producto_cantidad);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        HashMap<String, String> map = lista.get(position);
        holder.txtProductos.setText((CharSequence) map.get("modelo"));
        holder.txtCantidad.setText((CharSequence) map.get("cantidad"));

        return convertView;
    }

    private class ViewHolder {
        TextView txtProductos;
        TextView txtCantidad;
    }
}
