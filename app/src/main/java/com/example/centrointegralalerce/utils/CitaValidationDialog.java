package com.example.centrointegralalerce.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Diálogos de validación para citas
 */
public class CitaValidationDialog {

    /**
     * Muestra una advertencia si la fecha está en el pasado
     * Retorna true si el usuario acepta continuar, false si cancela
     */
    public static void mostrarAdvertenciaFechaPasada(Context context, Date fecha,
                                                     OnValidacionListener listener) {
        if (context == null || fecha == null) {
            if (listener != null) listener.onCancelado();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));
        String fechaStr = sdf.format(fecha);

        long diasAtrasados = CitaDateValidator.getDiasAtrasados(fecha);

        String mensaje;
        if (diasAtrasados == 0) {
            mensaje = "La fecha seleccionada es hoy pero ya pasó.\n\n" +
                    "¿Estás seguro de que deseas crear esta cita?";
        } else if (diasAtrasados == 1) {
            mensaje = "⚠️ La fecha seleccionada (" + fechaStr + ") fue ayer.\n\n" +
                    "No puedes crear citas en el pasado.";
        } else {
            mensaje = "⚠️ La fecha seleccionada (" + fechaStr + ") fue hace " +
                    diasAtrasados + " días.\n\n" +
                    "No puedes crear citas en el pasado.";
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle("Fecha en el pasado")
                .setMessage(mensaje)
                .setIcon(R.drawable.ic_notification)
                .setPositiveButton("Entendido", (dialog, which) -> {
                    if (listener != null) listener.onCancelado();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Muestra confirmación para cita muy adelantada
     */
    public static void mostrarConfirmacionFechaLejana(Context context, Date fecha,
                                                      OnValidacionListener listener) {
        if (context == null || fecha == null) {
            if (listener != null) listener.onCancelado();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));
        String fechaStr = sdf.format(fecha);

        long diasFuturos = CitaDateValidator.getDiasFaltantes(fecha);
        long mesesFuturos = diasFuturos / 30;

        String mensaje = "La fecha seleccionada (" + fechaStr + ") es en " +
                mesesFuturos + " meses.\n\n" +
                "¿Estás seguro de que deseas crear esta cita tan adelantada?";

        new MaterialAlertDialogBuilder(context)
                .setTitle("Confirmar fecha")
                .setMessage(mensaje)
                .setIcon(R.drawable.ic_notification)
                .setPositiveButton("Sí, crear", (dialog, which) -> {
                    if (listener != null) listener.onConfirmado();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    if (listener != null) listener.onCancelado();
                })
                .show();
    }

    /**
     * Muestra información sobre una cita atrasada existente
     */
    public static void mostrarInfoCitaAtrasada(Context context, Cita cita) {
        if (context == null || cita == null || cita.getFecha() == null) {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));
        String fechaStr = sdf.format(cita.getFecha());

        long diasAtrasados = CitaDateValidator.getDiasAtrasados(cita.getFecha());

        String titulo = "⚠️ Cita atrasada";
        String mensaje;

        if (diasAtrasados == 1) {
            mensaje = "Esta cita estaba programada para ayer (" + fechaStr + ").\n\n" +
                    "Actividad: " + cita.getActividadNombre() + "\n" +
                    "Hora: " + cita.getHora() + "\n\n" +
                    "¿Deseas reprogramarla o marcarla como completada?";
        } else {
            mensaje = "Esta cita estaba programada hace " + diasAtrasados + " días (" + fechaStr + ").\n\n" +
                    "Actividad: " + cita.getActividadNombre() + "\n" +
                    "Hora: " + cita.getHora() + "\n\n" +
                    "¿Deseas reprogramarla o marcarla como completada?";
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setIcon(R.drawable.ic_notification)
                .setPositiveButton("Reprogramar", (dialog, which) -> {
                    // El listener se encargará de abrir el diálogo de edición
                })
                .setNeutralButton("Marcar completada", (dialog, which) -> {
                    // El listener se encargará de marcar como completada
                })
                .setNegativeButton("Cerrar", null)
                .show();
    }

    /**
     * Muestra un diálogo de resumen del estado de la cita
     */
    public static void mostrarResumenCita(Context context, Cita cita) {
        if (context == null || cita == null) {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd 'de' MMMM 'de' yyyy",
                new Locale("es", "ES"));
        String fechaStr = cita.getFecha() != null ? sdf.format(cita.getFecha()) : "Sin fecha";

        CitaDateValidator.EstadoTemporal estado = CitaDateValidator.getEstadoTemporal(cita);
        String estadoStr = CitaDateValidator.getMensajeEstado(estado);
        String descripcion = CitaDateValidator.getMensajeDescriptivo(cita);
        String tiempoRestante = CitaDateValidator.getTiempoRestante(cita.getFecha());

        String mensaje = "📅 " + fechaStr + "\n" +
                "🕐 " + cita.getHora() + "\n" +
                "📍 " + cita.getLugarId() + "\n\n" +
                "Estado: " + estadoStr + "\n" +
                descripcion + "\n\n" +
                "⏰ " + tiempoRestante;

        new MaterialAlertDialogBuilder(context)
                .setTitle(cita.getActividadNombre())
                .setMessage(mensaje)
                .setIcon(R.drawable.ic_notification)
                .setPositiveButton("Entendido", null)
                .show();
    }

    /**
     * Muestra advertencia de cita próxima (hoy o en 24h)
     */
    public static void mostrarAdvertenciaCitaProxima(Context context, Cita cita) {
        if (context == null || cita == null) {
            return;
        }

        CitaDateValidator.EstadoTemporal estado = CitaDateValidator.getEstadoTemporal(cita);

        if (estado != CitaDateValidator.EstadoTemporal.HOY &&
                estado != CitaDateValidator.EstadoTemporal.PROXIMA_24H) {
            return; // Solo para citas próximas
        }

        String titulo = estado == CitaDateValidator.EstadoTemporal.HOY ?
                "📍 Cita HOY" : "⏰ Cita MAÑANA";

        String mensaje = cita.getActividadNombre() + "\n\n" +
                "📅 " + CitaDateValidator.getMensajeDescriptivo(cita) + "\n" +
                "📍 " + cita.getLugarId() + "\n\n" +
                "¡No olvides asistir!";

        new MaterialAlertDialogBuilder(context)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setIcon(R.drawable.ic_notification)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Interface para manejar respuestas de validación
     */
    public interface OnValidacionListener {
        void onConfirmado();
        void onCancelado();
    }

    /**
     * Muestra un badge o indicador visual según el estado
     */
    public static String getBadgeText(Cita cita) {
        if (cita == null) return "";

        CitaDateValidator.EstadoTemporal estado = CitaDateValidator.getEstadoTemporal(cita);

        switch (estado) {
            case ATRASADA:
                long dias = CitaDateValidator.getDiasAtrasados(cita.getFecha());
                return dias == 1 ? "Ayer" : "Hace " + dias + "d";
            case HOY:
                return "HOY";
            case PROXIMA_24H:
                return "Mañana";
            case PROXIMA_SEMANA:
                long diasFaltantes = CitaDateValidator.getDiasFaltantes(cita.getFecha());
                return diasFaltantes + "d";
            case FUTURA:
                long diasFuturos = CitaDateValidator.getDiasFaltantes(cita.getFecha());
                if (diasFuturos <= 30) {
                    return diasFuturos + "d";
                } else {
                    return (diasFuturos / 7) + "sem";
                }
            default:
                return "";
        }
    }
}