package com.example.centrointegralalerce.utils;

import android.content.Context;

import com.example.centrointegralalerce.data.Cita;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Validador de fechas para citas
 * Determina el estado temporal de una cita y proporciona mensajes apropiados
 */
public class CitaDateValidator {

    public enum EstadoTemporal {
        ATRASADA,           // Fecha ya pasó
        HOY,                // Es hoy
        PROXIMA_24H,        // En las próximas 24 horas
        PROXIMA_SEMANA,     // En los próximos 7 días
        FUTURA              // Más de 7 días en el futuro
    }

    /**
     * Valida si una fecha está en el pasado
     */
    public static boolean esFechaPasada(Date fecha) {
        if (fecha == null) return false;

        Calendar ahora = Calendar.getInstance();
        Calendar fechaCita = Calendar.getInstance();
        fechaCita.setTime(fecha);

        // Comparar solo fecha, sin hora
        ahora.set(Calendar.HOUR_OF_DAY, 0);
        ahora.set(Calendar.MINUTE, 0);
        ahora.set(Calendar.SECOND, 0);
        ahora.set(Calendar.MILLISECOND, 0);

        fechaCita.set(Calendar.HOUR_OF_DAY, 0);
        fechaCita.set(Calendar.MINUTE, 0);
        fechaCita.set(Calendar.SECOND, 0);
        fechaCita.set(Calendar.MILLISECOND, 0);

        return fechaCita.before(ahora);
    }

    /**
     * Valida si una fecha es hoy
     */
    public static boolean esFechaHoy(Date fecha) {
        if (fecha == null) return false;

        Calendar ahora = Calendar.getInstance();
        Calendar fechaCita = Calendar.getInstance();
        fechaCita.setTime(fecha);

        return ahora.get(Calendar.YEAR) == fechaCita.get(Calendar.YEAR) &&
                ahora.get(Calendar.DAY_OF_YEAR) == fechaCita.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Determina el estado temporal de una cita
     */
    public static EstadoTemporal getEstadoTemporal(Cita cita) {
        if (cita == null || cita.getFecha() == null) {
            return EstadoTemporal.FUTURA;
        }

        Date fechaCita = cita.getFecha();

        if (esFechaPasada(fechaCita)) {
            return EstadoTemporal.ATRASADA;
        }

        if (esFechaHoy(fechaCita)) {
            return EstadoTemporal.HOY;
        }

        long diffMillis = fechaCita.getTime() - System.currentTimeMillis();
        long diffHoras = TimeUnit.MILLISECONDS.toHours(diffMillis);
        long diffDias = TimeUnit.MILLISECONDS.toDays(diffMillis);

        if (diffHoras <= 24) {
            return EstadoTemporal.PROXIMA_24H;
        } else if (diffDias <= 7) {
            return EstadoTemporal.PROXIMA_SEMANA;
        } else {
            return EstadoTemporal.FUTURA;
        }
    }

    /**
     * Obtiene un mensaje apropiado según el estado temporal
     */
    public static String getMensajeEstado(EstadoTemporal estado) {
        switch (estado) {
            case ATRASADA:
                return "⚠️ Cita atrasada";
            case HOY:
                return "📍 Hoy";
            case PROXIMA_24H:
                return "⏰ Próximas 24 horas";
            case PROXIMA_SEMANA:
                return "📅 Esta semana";
            case FUTURA:
                return "📆 Próximamente";
            default:
                return "";
        }
    }

    /**
     * Obtiene un mensaje descriptivo según el estado temporal
     */
    public static String getMensajeDescriptivo(Cita cita) {
        if (cita == null || cita.getFecha() == null) {
            return "";
        }

        EstadoTemporal estado = getEstadoTemporal(cita);

        switch (estado) {
            case ATRASADA:
                long diasAtrasados = getDiasAtrasados(cita.getFecha());
                if (diasAtrasados == 1) {
                    return "Esta cita estaba programada para ayer";
                } else {
                    return "Esta cita estaba programada hace " + diasAtrasados + " días";
                }

            case HOY:
                if (cita.getHora() != null) {
                    return "Hoy a las " + cita.getHora();
                }
                return "Hoy";

            case PROXIMA_24H:
                if (cita.getHora() != null) {
                    return "Mañana a las " + cita.getHora();
                }
                return "Mañana";

            case PROXIMA_SEMANA:
                long diasFaltantes = getDiasFaltantes(cita.getFecha());
                return "En " + diasFaltantes + " días";

            case FUTURA:
                long diasFuturos = getDiasFaltantes(cita.getFecha());
                if (diasFuturos <= 30) {
                    return "En " + diasFuturos + " días";
                } else {
                    long semanas = diasFuturos / 7;
                    return "En " + semanas + " semanas";
                }

            default:
                return "";
        }
    }

    /**
     * Calcula cuántos días han pasado desde la fecha
     */
    public static long getDiasAtrasados(Date fecha) {
        if (fecha == null) return 0;

        long diffMillis = System.currentTimeMillis() - fecha.getTime();
        return TimeUnit.MILLISECONDS.toDays(diffMillis);
    }

    /**
     * Calcula cuántos días faltan para la fecha
     */
    public static long getDiasFaltantes(Date fecha) {
        if (fecha == null) return 0;

        long diffMillis = fecha.getTime() - System.currentTimeMillis();
        return TimeUnit.MILLISECONDS.toDays(diffMillis);
    }

    /**
     * Valida si se puede crear una cita en la fecha especificada
     * Retorna mensaje de error o null si es válida
     */
    public static String validarFechaParaCreacion(Date fecha, Context context) {
        if (fecha == null) {
            return "❌ Debes seleccionar una fecha";
        }

        if (esFechaPasada(fecha)) {
            return "⚠️ No puedes crear una cita en el pasado";
        }

        // Opcional: validar que no sea más de X meses en el futuro
        Calendar maxFecha = Calendar.getInstance();
        maxFecha.add(Calendar.MONTH, 6); // Máximo 6 meses adelante

        if (fecha.after(maxFecha.getTime())) {
            return "⚠️ No puedes crear citas con más de 6 meses de anticipación";
        }

        return null; // Fecha válida
    }

    /**
     * Obtiene un color según el estado temporal (para UI)
     */
    public static int getColorByEstado(EstadoTemporal estado, Context context) {
        switch (estado) {
            case ATRASADA:
                return context.getResources().getColor(android.R.color.holo_red_light);
            case HOY:
                return context.getResources().getColor(android.R.color.holo_orange_light);
            case PROXIMA_24H:
                return context.getResources().getColor(android.R.color.holo_orange_dark);
            case PROXIMA_SEMANA:
                return context.getResources().getColor(android.R.color.holo_blue_light);
            case FUTURA:
                return context.getResources().getColor(android.R.color.holo_green_light);
            default:
                return context.getResources().getColor(android.R.color.darker_gray);
        }
    }

    /**
     * Verifica si una cita requiere atención urgente
     */
    public static boolean requiereAtencionUrgente(Cita cita) {
        if (cita == null) return false;

        EstadoTemporal estado = getEstadoTemporal(cita);
        return estado == EstadoTemporal.ATRASADA ||
                estado == EstadoTemporal.HOY ||
                estado == EstadoTemporal.PROXIMA_24H;
    }

    /**
     * Obtiene el tiempo restante en formato legible
     */
    public static String getTiempoRestante(Date fecha) {
        if (fecha == null) return "";

        long diffMillis = fecha.getTime() - System.currentTimeMillis();

        if (diffMillis < 0) {
            // Tiempo pasado
            long diasAtrasados = Math.abs(TimeUnit.MILLISECONDS.toDays(diffMillis));
            if (diasAtrasados == 0) {
                return "Hoy (atrasada)";
            } else if (diasAtrasados == 1) {
                return "Hace 1 día";
            } else {
                return "Hace " + diasAtrasados + " días";
            }
        } else {
            // Tiempo futuro
            long dias = TimeUnit.MILLISECONDS.toDays(diffMillis);
            long horas = TimeUnit.MILLISECONDS.toHours(diffMillis) % 24;

            if (dias == 0) {
                if (horas == 0) {
                    long minutos = TimeUnit.MILLISECONDS.toMinutes(diffMillis);
                    return "En " + minutos + " minutos";
                } else if (horas == 1) {
                    return "En 1 hora";
                } else {
                    return "En " + horas + " horas";
                }
            } else if (dias == 1) {
                return "En 1 día";
            } else {
                return "En " + dias + " días";
            }
        }
    }
}