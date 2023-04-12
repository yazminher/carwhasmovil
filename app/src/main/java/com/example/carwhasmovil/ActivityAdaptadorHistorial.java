package com.example.carwhasmovil;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.carwhasmovil.modelos.Historial;

import java.util.ArrayList;

public class ActivityAdaptadorHistorial extends RecyclerView.Adapter<ActivityAdaptadorHistorial.HistorialViewHolder> {
    ArrayList<Historial> items;

    public ActivityAdaptadorHistorial(ArrayList<Historial> items) {
        this.items = items;
    }

    @Override
    public HistorialViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_adaptador_historial, null, false);
        return new HistorialViewHolder(v);
    }

    @Override
    public void onBindViewHolder(HistorialViewHolder viewHolder, int i) {
        viewHolder.TVvehiculo.setText(items.get(i).getVehiculo());
        viewHolder.TVservicio.setText(items.get(i).getServicio());
        viewHolder.TVubicacion.setText(items.get(i).getUbicacion());
        viewHolder.TVfecha.setText("Fecha Emisión: "+items.get(i).getFecha());
        viewHolder.TVestado.setText(items.get(i).getEstado());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class HistorialViewHolder extends RecyclerView.ViewHolder {
        // Campos respectivos de un item
        public TextView TVvehiculo, TVservicio, TVubicacion, TVfecha, TVestado;

        public HistorialViewHolder(View v) {
            super(v);
            TVvehiculo = (TextView) v.findViewById(R.id.txtvehiculo);
            TVservicio = (TextView) v.findViewById(R.id.txtservicio);
            TVubicacion = (TextView) v.findViewById(R.id.txtTipoUbicacion);
            TVfecha = (TextView) v.findViewById(R.id.txtFechaEmision);
            TVestado = (TextView) v.findViewById(R.id.txtEstado);
        }
    }
}