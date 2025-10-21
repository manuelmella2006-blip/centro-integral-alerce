package com.example.centrointegralalerce.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.centrointegralalerce.R;

// IMPORTA Cita desde el paquete donde la definiste.
// Si está en "model":
import com.example.centrointegralalerce.data.Cita;
// Si la dejaste en "data", cambia el import anterior por:
// import com.example.centrointegralalerce.data.Cita;

import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class CitaDetalleDialog extends DialogFragment {

    private final Cita cita;

    public CitaDetalleDialog(Cita cita) {
        this.cita = cita;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_cita_detalle, container, false);

        TextView tvNombre = view.findViewById(R.id.tv_nombre_actividad); // si tu layout muestra un nombre genérico
        TextView tvLugar  = view.findViewById(R.id.tv_lugar);
        TextView tvHora   = view.findViewById(R.id.tv_hora);
        TextView tvTipo   = view.findViewById(R.id.tv_tipo); // lo reutilizamos para estado

        // Formatear fecha si existe
        String fechaTexto = "";
        if (cita.getFecha() != null) {
            SimpleDateFormat df = new SimpleDateFormat("EEEE dd 'de' MMMM yyyy", new Locale("es", "ES"));
            fechaTexto = df.format(cita.getFecha());
        }

        // Asignar valores disponibles en el nuevo modelo
        // Si no tienes "nombre de actividad" en Cita, muestra la fecha como título
        if (tvNombre != null) tvNombre.setText(fechaTexto.isEmpty() ? "Cita" : fechaTexto);
        if (tvLugar  != null) tvLugar.setText(cita.getLugarId() != null ? cita.getLugarId() : "Sin lugar");
        if (tvHora   != null) tvHora.setText(cita.getHora() != null ? cita.getHora() : "Sin hora");
        if (tvTipo   != null) tvTipo.setText(cita.getEstado() != null ? cita.getEstado() : "Sin estado");

        return view;
    }
}
