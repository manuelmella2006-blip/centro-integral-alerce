package com.example.centrointegralalerce.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;
import android.widget.TextView;

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

        TextView tvNombre = view.findViewById(R.id.tv_nombre_actividad);
        TextView tvLugar = view.findViewById(R.id.tv_lugar);
        TextView tvHora = view.findViewById(R.id.tv_hora);
        TextView tvTipo = view.findViewById(R.id.tv_tipo);

        tvNombre.setText(cita.getActividad());
        tvLugar.setText(cita.getLugar());
        tvHora.setText(cita.getHora());
        tvTipo.setText(cita.getTipoActividad());

        return view;
    }
}
