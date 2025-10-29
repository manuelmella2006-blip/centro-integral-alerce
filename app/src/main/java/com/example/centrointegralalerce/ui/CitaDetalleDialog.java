package com.example.centrointegralalerce.ui;

import android.content.res.ColorStateList;
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
import com.example.centrointegralalerce.utils.AlertManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Diálogo que muestra los detalles de una cita y permite marcarla como completada.
 */
public class CitaDetalleDialog extends DialogFragment {

    private final Cita cita;
    private Chip chipEstado;

    public CitaDetalleDialog(Cita cita) {
        this.cita = cita;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_cita_detalle, container, false);

        // Referencias a la UI
        TextView tvNombreActividad = view.findViewById(R.id.tv_nombre_actividad);
        TextView tvLugar = view.findViewById(R.id.tv_lugar);
        TextView tvHora = view.findViewById(R.id.tv_hora);
        chipEstado = view.findViewById(R.id.chip_estado);

        MaterialButton btnVerDetalle = view.findViewById(R.id.btn_ver_detalle);
        MaterialButton btnCerrar = view.findViewById(R.id.btn_cerrar);
        MaterialButton btnMarcarCompletada = view.findViewById(R.id.btn_marcar_completada);

        // ---------- CABECERA ----------
        String fechaTexto = "";
        if (cita.getFecha() != null) {
            SimpleDateFormat df = new SimpleDateFormat("EEEE dd 'de' MMMM yyyy", new Locale("es", "ES"));
            fechaTexto = df.format(cita.getFecha());
        }
        tvNombreActividad.setText(!fechaTexto.isEmpty() ? fechaTexto : "Cita");

        // ---------- LUGAR ----------
        String lugar = cita.getLugarId();
        tvLugar.setText((lugar == null || lugar.isEmpty()) ? "Sin lugar" : lugar);

        // ---------- HORA ----------
        String hora = cita.getHora();
        tvHora.setText((hora == null || hora.isEmpty()) ? "Sin hora" : hora);

        // ---------- ESTADO ----------
        actualizarChipEstado(cita.getEstado());

        // ---------- BOTONES ----------
        btnCerrar.setOnClickListener(v -> dismiss());

        btnVerDetalle.setOnClickListener(v -> {
            AlertManager.showInfoSnackbar(
                    AlertManager.getRootViewSafe(this),
                    "Funcionalidad de detalle aún no implementada."
            );
            dismiss();
        });

        btnMarcarCompletada.setOnClickListener(v -> confirmarMarcadoCompletada());

        return view;
    }

    /**
     * Diálogo de confirmación para marcar la cita como completada.
     */
    private void confirmarMarcadoCompletada() {
        AlertManager.showConfirmDialog(
                requireContext(),
                "Marcar como completada",
                "¿Deseas marcar esta cita como completada?",
                new AlertManager.OnConfirmListener() {
                    @Override
                    public void onConfirm() {
                        marcarComoCompletada();
                    }

                    @Override
                    public void onCancel() {
                        AlertManager.showInfoToast(requireContext(), "Acción cancelada");
                    }
                }
        );
    }

    /**
     * Cambia el estado visual y el texto del chip a "Completada".
     */
    private void marcarComoCompletada() {
        if (chipEstado == null) return;

        cita.setEstado("Completada");
        actualizarChipEstado("Completada");

        AlertManager.showSuccessSnackbar(
                AlertManager.getRootViewSafe(this),
                "Cita marcada como completada ✅"
        );
    }

    /**
     * Actualiza el color y texto del chip según el estado de la cita.
     */
    private void actualizarChipEstado(String estado) {
        if (chipEstado == null || getContext() == null) return;

        String texto = (estado == null || estado.isEmpty()) ? "Sin estado" : estado;
        chipEstado.setText(texto);

        int color;
        switch (texto.toLowerCase()) {
            case "completada":
                color = requireContext().getColor(R.color.verde_exito);
                break;
            case "cancelada":
                color = requireContext().getColor(R.color.rojo_error);
                break;
            case "pendiente":
            case "programada":
                color = requireContext().getColor(R.color.amarillo_advertencia);
                break;
            default:
                color = requireContext().getColor(R.color.cian_info);
                break;
        }

        // ✅ Aplica el color correctamente (sin recurso 0x0)
        chipEstado.setChipBackgroundColor(ColorStateList.valueOf(color));
        chipEstado.setTextColor(requireContext().getColor(android.R.color.white));

        // Pequeña animación visual
        chipEstado.animate().alpha(0f).setDuration(100).withEndAction(() -> {
            chipEstado.setText(texto);
            chipEstado.setChipBackgroundColor(ColorStateList.valueOf(color));
            chipEstado.animate().alpha(1f).setDuration(150).start();
        }).start();
    }
}
