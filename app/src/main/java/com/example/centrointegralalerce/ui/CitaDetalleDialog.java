package com.example.centrointegralalerce.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;
import com.example.centrointegralalerce.utils.AlertManager;
import com.example.centrointegralalerce.utils.CitaDateValidator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Diálogo que muestra los detalles de una cita y permite marcarla como completada.
 * Ahora incluye validación de estado temporal (atrasada, hoy, próxima, futura)
 */
public class CitaDetalleDialog extends DialogFragment {

    private static final String TAG = "CitaDetalleDialog";

    private final Cita cita;
    private Chip chipEstado;
    private Chip chipEstadoTemporal;
    private FirebaseFirestore db;
    private TextView tvLugar;
    private TextView tvTiempoRestante;
    private CardView cardAdvertencia;
    private TextView tvAdvertenciaTexto;
    private MaterialButton btnMarcarCompletada;

    public CitaDetalleDialog(Cita cita) {
        this.cita = cita;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_cita_detalle, container, false);

        // Referencias a la UI
        TextView tvNombreActividad = view.findViewById(R.id.tv_nombre_actividad);
        tvLugar = view.findViewById(R.id.tv_lugar);
        TextView tvHora = view.findViewById(R.id.tv_hora);
        chipEstado = view.findViewById(R.id.chip_estado);
        chipEstadoTemporal = view.findViewById(R.id.chip_estado_temporal);
        tvTiempoRestante = view.findViewById(R.id.tv_tiempo_restante);
        cardAdvertencia = view.findViewById(R.id.card_advertencia);
        tvAdvertenciaTexto = view.findViewById(R.id.tv_advertencia_texto);

        MaterialButton btnVerDetalle = view.findViewById(R.id.btn_ver_detalle);
        MaterialButton btnCerrar = view.findViewById(R.id.btn_cerrar);
        btnMarcarCompletada = view.findViewById(R.id.btn_marcar_completada);

        // ---------- CABECERA ----------
        String fechaTexto = "";
        if (cita.getFecha() != null) {
            SimpleDateFormat df = new SimpleDateFormat("EEEE dd 'de' MMMM yyyy", new Locale("es", "ES"));
            fechaTexto = df.format(cita.getFecha());
        }
        tvNombreActividad.setText(!fechaTexto.isEmpty() ? fechaTexto : "Cita");

        // ---------- LUGAR ----------
        cargarNombreLugar(cita.getLugarId());

        // ---------- HORA ----------
        String hora = cita.getHora();
        tvHora.setText((hora == null || hora.isEmpty()) ? "Sin hora" : hora);

        // ---------- ESTADO ----------
        actualizarChipEstado(cita.getEstado());

        // 🎯 NUEVO: VALIDAR ESTADO TEMPORAL
        validarYMostrarEstadoTemporal();

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
     * 🎯 NUEVO: Valida el estado temporal de la cita y actualiza la UI
     */
    private void validarYMostrarEstadoTemporal() {
        if (cita == null || cita.getFecha() == null) {
            Log.w(TAG, "Cita sin fecha, no se puede validar estado temporal");
            return;
        }

        // Obtener estado temporal
        CitaDateValidator.EstadoTemporal estadoTemporal =
                CitaDateValidator.getEstadoTemporal(cita);

        Log.d(TAG, "Estado temporal de la cita: " + estadoTemporal);

        // Actualizar chip de estado temporal (si existe en tu layout)
        if (chipEstadoTemporal != null) {
            String mensajeEstado = CitaDateValidator.getMensajeEstado(estadoTemporal);
            chipEstadoTemporal.setText(mensajeEstado);
            configurarColorChipTemporal(estadoTemporal);
        }

        // Actualizar tiempo restante (si existe en tu layout)
        if (tvTiempoRestante != null) {
            String tiempoRestante = CitaDateValidator.getTiempoRestante(cita.getFecha());
            tvTiempoRestante.setText("⏰ " + tiempoRestante);
            configurarColorTiempoRestante(estadoTemporal);
        }

        // Mostrar advertencia según el estado
        mostrarAdvertenciaSegunEstado(estadoTemporal);
    }

    /**
     * 🎯 NUEVO: Configura el color del chip temporal según el estado
     */
    private void configurarColorChipTemporal(CitaDateValidator.EstadoTemporal estado) {
        if (chipEstadoTemporal == null || getContext() == null) return;

        int color;
        switch (estado) {
            case ATRASADA:
                color = requireContext().getColor(android.R.color.holo_red_light);
                break;
            case HOY:
                color = requireContext().getColor(android.R.color.holo_orange_light);
                break;
            case PROXIMA_24H:
                color = requireContext().getColor(android.R.color.holo_orange_dark);
                break;
            case PROXIMA_SEMANA:
                color = requireContext().getColor(android.R.color.holo_blue_light);
                break;
            case FUTURA:
                color = requireContext().getColor(android.R.color.holo_green_light);
                break;
            default:
                color = requireContext().getColor(R.color.grisMedio);
                break;
        }

        chipEstadoTemporal.setChipBackgroundColor(ColorStateList.valueOf(color));
        chipEstadoTemporal.setTextColor(requireContext().getColor(android.R.color.white));
    }

    /**
     * 🎯 NUEVO: Configura el color del texto de tiempo restante
     */
    private void configurarColorTiempoRestante(CitaDateValidator.EstadoTemporal estado) {
        if (tvTiempoRestante == null || getContext() == null) return;

        int color;
        switch (estado) {
            case ATRASADA:
                color = requireContext().getColor(android.R.color.holo_red_dark);
                break;
            case HOY:
            case PROXIMA_24H:
                color = requireContext().getColor(android.R.color.holo_orange_dark);
                break;
            case PROXIMA_SEMANA:
                color = requireContext().getColor(android.R.color.holo_blue_dark);
                break;
            case FUTURA:
                color = requireContext().getColor(android.R.color.holo_green_dark);
                break;
            default:
                color = requireContext().getColor(R.color.grisMedio);
                break;
        }

        tvTiempoRestante.setTextColor(color);
    }

    /**
     * 🎯 NUEVO: Muestra advertencias según el estado temporal
     */
    private void mostrarAdvertenciaSegunEstado(CitaDateValidator.EstadoTemporal estado) {
        if (cardAdvertencia == null || tvAdvertenciaTexto == null || getContext() == null) {
            return;
        }

        switch (estado) {
            case ATRASADA:
                mostrarAdvertenciaAtrasada();
                break;

            case HOY:
                mostrarAdvertenciaHoy();
                break;

            case PROXIMA_24H:
                mostrarAdvertenciaProxima();
                break;

            default:
                // Ocultar advertencias para citas futuras normales
                cardAdvertencia.setVisibility(View.GONE);
                if (btnMarcarCompletada != null) {
                    btnMarcarCompletada.setVisibility(View.GONE);
                }
                break;
        }
    }

    /**
     * 🎯 NUEVO: Muestra advertencia para citas atrasadas
     */
    private void mostrarAdvertenciaAtrasada() {
        if (cardAdvertencia == null || tvAdvertenciaTexto == null) return;

        cardAdvertencia.setVisibility(View.VISIBLE);
        cardAdvertencia.setCardBackgroundColor(
                requireContext().getColor(android.R.color.holo_red_light));

        long diasAtrasados = CitaDateValidator.getDiasAtrasados(cita.getFecha());

        String mensaje;
        if (diasAtrasados == 0) {
            mensaje = "⚠️ Esta cita era para hoy y ya pasó.\n\n" +
                    "¿Asististe? Márcala como completada.";
        } else if (diasAtrasados == 1) {
            mensaje = "⚠️ Esta cita estaba programada para AYER.\n\n" +
                    "📋 " + cita.getActividadNombre() + "\n\n" +
                    "¿Asististe? Márcala como completada o reprograma.";
        } else {
            mensaje = "⚠️ Esta cita estaba programada hace " + diasAtrasados + " días.\n\n" +
                    "📋 " + cita.getActividadNombre() + "\n\n" +
                    "¿Asististe? Márcala como completada o reprograma.";
        }

        tvAdvertenciaTexto.setText(mensaje);
        tvAdvertenciaTexto.setTextColor(
                requireContext().getColor(android.R.color.white));

        // Mostrar botón de marcar completada
        if (btnMarcarCompletada != null) {
            btnMarcarCompletada.setVisibility(View.VISIBLE);
        }

        Log.w(TAG, "Cita atrasada detectada: " + diasAtrasados + " días");
    }

    /**
     * 🎯 NUEVO: Muestra advertencia para citas de hoy
     */
    private void mostrarAdvertenciaHoy() {
        if (cardAdvertencia == null || tvAdvertenciaTexto == null) return;

        cardAdvertencia.setVisibility(View.VISIBLE);
        cardAdvertencia.setCardBackgroundColor(
                requireContext().getColor(android.R.color.holo_orange_light));

        String mensaje = "📍 Esta cita es HOY a las " + cita.getHora() + "\n\n" +
                "📋 " + cita.getActividadNombre() + "\n\n" +
                "¡No olvides asistir!";

        tvAdvertenciaTexto.setText(mensaje);
        tvAdvertenciaTexto.setTextColor(
                requireContext().getColor(android.R.color.white));

        // Ocultar botón de marcar completada (aún no está atrasada)
        if (btnMarcarCompletada != null) {
            btnMarcarCompletada.setVisibility(View.GONE);
        }

        Log.d(TAG, "Cita de hoy detectada");
    }

    /**
     * 🎯 NUEVO: Muestra advertencia para citas próximas (24h)
     */
    private void mostrarAdvertenciaProxima() {
        if (cardAdvertencia == null || tvAdvertenciaTexto == null) return;

        cardAdvertencia.setVisibility(View.VISIBLE);
        cardAdvertencia.setCardBackgroundColor(
                requireContext().getColor(android.R.color.holo_orange_dark));

        String mensaje = "⏰ Esta cita es MAÑANA a las " + cita.getHora() + "\n\n" +
                "📋 " + cita.getActividadNombre() + "\n\n" +
                "Prepárate con anticipación.";

        tvAdvertenciaTexto.setText(mensaje);
        tvAdvertenciaTexto.setTextColor(
                requireContext().getColor(android.R.color.white));

        // Ocultar botón de marcar completada
        if (btnMarcarCompletada != null) {
            btnMarcarCompletada.setVisibility(View.GONE);
        }

        Log.d(TAG, "Cita próxima detectada (24h)");
    }

    /**
     * Carga el nombre del lugar desde Firestore basado en el lugarId
     */
    private void cargarNombreLugar(String lugarId) {
        if (lugarId == null || lugarId.isEmpty() || db == null || tvLugar == null) {
            tvLugar.setText("Sin lugar");
            return;
        }

        tvLugar.setText("Cargando...");

        db.collection("lugares").document(lugarId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (getContext() == null || !documentSnapshot.exists()) {
                        tvLugar.setText("Lugar no encontrado");
                        return;
                    }

                    String nombreLugar = documentSnapshot.getString("nombre");
                    if (nombreLugar != null && !nombreLugar.isEmpty()) {
                        tvLugar.setText(nombreLugar);
                    } else {
                        String direccion = documentSnapshot.getString("direccion");
                        if (direccion != null && !direccion.isEmpty()) {
                            tvLugar.setText(direccion);
                        } else {
                            tvLugar.setText("Lugar sin nombre");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        tvLugar.setText("Error al cargar lugar");
                        Log.e(TAG, "Error al cargar lugar: " + e.getMessage());
                    }
                });
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
     * También actualiza el estado en Firebase.
     */
    private void marcarComoCompletada() {
        if (chipEstado == null || cita == null) return;

        // Actualizar en Firebase
        db.collection("actividades")
                .document(cita.getActividadId())
                .collection("citas")
                .document(cita.getId())
                .update("estado", "completada")
                .addOnSuccessListener(aVoid -> {
                    if (getContext() == null) return;

                    // Actualizar localmente
                    cita.setEstado("completada");
                    actualizarChipEstado("completada");

                    // Ocultar advertencia
                    if (cardAdvertencia != null) {
                        cardAdvertencia.setVisibility(View.GONE);
                    }
                    if (btnMarcarCompletada != null) {
                        btnMarcarCompletada.setVisibility(View.GONE);
                    }

                    AlertManager.showSuccessSnackbar(
                            AlertManager.getRootViewSafe(this),
                            "✅ Cita marcada como completada"
                    );

                    Log.d(TAG, "Cita marcada como completada en Firebase");

                    // Cerrar diálogo después de un momento
                    new android.os.Handler().postDelayed(this::dismiss, 1500);
                })
                .addOnFailureListener(e -> {
                    if (getContext() == null) return;

                    AlertManager.showErrorToast(requireContext(),
                            "Error al actualizar: " + e.getMessage());
                    Log.e(TAG, "Error al actualizar estado en Firebase", e);
                });
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