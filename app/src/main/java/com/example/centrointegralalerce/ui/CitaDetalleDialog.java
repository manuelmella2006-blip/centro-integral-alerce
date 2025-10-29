package com.example.centrointegralalerce.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

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

        // Referencias a la UI según dialog_cita_detalle.xml actualizado
        TextView tvNombreActividad = view.findViewById(R.id.tv_nombre_actividad);
        TextView tvLugar           = view.findViewById(R.id.tv_lugar);
        TextView tvHora            = view.findViewById(R.id.tv_hora);
        Chip chipEstado            = view.findViewById(R.id.chip_estado);

        MaterialButton btnVerDetalle = view.findViewById(R.id.btn_ver_detalle);
        MaterialButton btnCerrar     = view.findViewById(R.id.btn_cerrar);

        // ---------- TÍTULO / CABECERA ----------
        // Si no tenemos nombre de la actividad en Cita (correcto, no lo tienes),
        // usamos la fecha bonita (lunes 27 de octubre 2025).
        String fechaTexto = "";
        if (cita.getFecha() != null) {
            SimpleDateFormat df = new SimpleDateFormat(
                    "EEEE dd 'de' MMMM yyyy",
                    new Locale("es", "ES")
            );
            fechaTexto = df.format(cita.getFecha());
        }

        if (tvNombreActividad != null) {
            if (!fechaTexto.isEmpty()) {
                tvNombreActividad.setText(fechaTexto);
            } else {
                tvNombreActividad.setText("Cita");
            }
        }

        // ---------- LUGAR ----------
        if (tvLugar != null) {
            // tu modelo Cita trae lugarId, no un nombre de lugar amigable todavía
            String lugar = cita.getLugarId();
            if (lugar == null || lugar.isEmpty()) {
                lugar = "Sin lugar";
            }
            tvLugar.setText(lugar);
        }

        // ---------- HORA ----------
        if (tvHora != null) {
            String hora = cita.getHora();
            if (hora == null || hora.isEmpty()) {
                hora = "Sin hora";
            }
            tvHora.setText(hora);
        }

        // ---------- ESTADO ----------
        if (chipEstado != null) {
            String estado = cita.getEstado();
            if (estado == null || estado.isEmpty()) {
                estado = "Sin estado";
            }
            chipEstado.setText(estado);
        }

        // ---------- BOTONES ACCIÓN ----------
        if (btnCerrar != null) {
            btnCerrar.setOnClickListener(v -> dismiss());
        }

        if (btnVerDetalle != null) {
            btnVerDetalle.setOnClickListener(v -> {
                // Aquí idealmente irías al detalle de la Actividad completa.
                // Para eso necesitas el ID de la actividad relacionada con esta cita.
                // Ese ID lo tienes en CitaFirebase (campo actividadId), pero no en Cita.
                // Así que de momento no navegamos, sólo cerramos.
                dismiss();
            });
        }

        return view;
    }
}
