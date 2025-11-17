package com.example.centrointegralalerce.utils;

import com.example.centrointegralalerce.data.Cita;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 🔧 VERSIÓN MEJORADA: Valida estados temporales considerando citas completadas
 */
public class CitaDateValidator {

    public enum EstadoTemporal {
        COMPLETADA,      // 🆕 NUEVO: Cita ya completada
        ATRASADA,        // Fecha pasada y NO completada
        HOY,             // Es hoy
        PROXIMA_24H,     // Dentro de las próximas 24 horas
        PROXIMA_SEMANA,  // Dentro de los próximos 7 días
        FUTURA           // Más de 7 días en el futuro
    }

    /**
     * 🔧 MÉTODO ACTUALIZADO: Determina el estado temporal de una cita
     * Ahora considera si la cita está completada
     */
    public static EstadoTemporal getEstadoTemporal(Cita cita) {
        if (cita == null || cita.getFecha() == null) {
            return EstadoTemporal.FUTURA; // Default seguro
        }

        // 🆕 PRIMERO: Verificar si está completada
        String estado = cita.getEstado();
        if (estado != null && "completada".equalsIgnoreCase(estado.trim())) {
            return EstadoTemporal.COMPLETADA;
        }

        // Si NO está completada, validar temporalmente
        Calendar ahora = Calendar.getInstance();
        Calendar citaCal = Calendar.getInstance();
        citaCal.setTime(cita.getFecha());

        // Normalizar ambos a medianoche para comparar solo fechas
        normalizarAMedianoche(ahora);
        normalizarAMedianoche(citaCal);

        // Comparar fechas
        if (citaCal.before(ahora)) {
            return EstadoTemporal.ATRASADA;
        }

        if (citaCal.equals(ahora)) {
            return EstadoTemporal.HOY;
        }

        // Calcular diferencia en días
        long diffMillis = citaCal.getTimeInMillis() - ahora.getTimeInMillis();
        long diffDias = TimeUnit.MILLISECONDS.toDays(diffMillis);

        if (diffDias <= 1) {
            return EstadoTemporal.PROXIMA_24H;
        } else if (diffDias <= 7) {
            return EstadoTemporal.PROXIMA_SEMANA;
        } else {
            return EstadoTemporal.FUTURA;
        }
    }

    /**
     * 🆕 NUEVO: Verifica si una cita debe mostrar advertencias
     * Las citas completadas NO muestran advertencias
     */
    public static boolean debeMotrarAdvertencia(Cita cita) {
        if (cita == null) return false;

        EstadoTemporal estado = getEstadoTemporal(cita);

        // No mostrar advertencias para citas completadas o futuras normales
        return estado == EstadoTemporal.ATRASADA ||
                estado == EstadoTemporal.HOY ||
                estado == EstadoTemporal.PROXIMA_24H;
    }

    /**
     * Obtiene un mensaje descriptivo del estado temporal
     */
    public static String getMensajeEstado(EstadoTemporal estado) {
        switch (estado) {
            case COMPLETADA:
                return "✅ Completada";
            case ATRASADA:
                return "⚠️ Atrasada";
            case HOY:
                return "📍 Hoy";
            case PROXIMA_24H:
                return "⏰ Mañana";
            case PROXIMA_SEMANA:
                return "📅 Esta semana";
            case FUTURA:
                return "📆 Próximamente";
            default:
                return "📅 Programada";
        }
    }

    /**
     * 🆕 Obtiene un mensaje descriptivo más detallado del estado de una cita
     * Incluye tiempo restante/transcurrido
     *
     * @param cita Cita a evaluar
     * @return Mensaje descriptivo con contexto temporal
     */
    public static String getMensajeDescriptivo(Cita cita) {
        if (cita == null || cita.getFecha() == null) {
            return "Sin fecha programada";
        }

        EstadoTemporal estado = getEstadoTemporal(cita);
        String tiempoRestante = getTiempoRestante(cita.getFecha());

        switch (estado) {
            case COMPLETADA:
                return "✅ Cita completada • " + tiempoRestante;

            case ATRASADA:
                long diasAtrasados = getDiasAtrasados(cita.getFecha());
                if (diasAtrasados == 0) {
                    return "⚠️ Era para hoy • No completada";
                } else if (diasAtrasados == 1) {
                    return "⚠️ Era ayer • No completada";
                } else {
                    return "⚠️ " + tiempoRestante + " • No completada";
                }

            case HOY:
                return "📍 Es hoy • " + (cita.getHora() != null ? cita.getHora() : "Sin hora");

            case PROXIMA_24H:
                return "⏰ " + tiempoRestante + " • " +
                        (cita.getHora() != null ? cita.getHora() : "Sin hora");

            case PROXIMA_SEMANA:
                return "📅 " + tiempoRestante;

            case FUTURA:
                return "📆 " + tiempoRestante;

            default:
                return tiempoRestante;
        }
    }

    /**
     * Obtiene un mensaje con el tiempo restante o transcurrido
     */
    public static String getTiempoRestante(Date fecha) {
        if (fecha == null) return "Sin fecha";

        Calendar ahora = Calendar.getInstance();
        Calendar citaCal = Calendar.getInstance();
        citaCal.setTime(fecha);

        normalizarAMedianoche(ahora);
        normalizarAMedianoche(citaCal);

        long diffMillis = citaCal.getTimeInMillis() - ahora.getTimeInMillis();
        long diffDias = TimeUnit.MILLISECONDS.toDays(diffMillis);

        if (diffDias < 0) {
            long diasAtrasados = Math.abs(diffDias);
            if (diasAtrasados == 0) {
                return "Era hoy (pasó)";
            } else if (diasAtrasados == 1) {
                return "Hace 1 día";
            } else {
                return "Hace " + diasAtrasados + " días";
            }
        } else if (diffDias == 0) {
            return "Es hoy";
        } else if (diffDias == 1) {
            return "Mañana";
        } else if (diffDias <= 7) {
            return "En " + diffDias + " días";
        } else {
            long semanas = diffDias / 7;
            if (semanas == 1) {
                return "En 1 semana";
            } else {
                return "En " + semanas + " semanas";
            }
        }
    }

    /**
     * Verifica si una fecha es del pasado (sin considerar estado)
     */
    public static boolean esFechaPasada(Date fecha) {
        if (fecha == null) return false;

        Calendar ahora = Calendar.getInstance();
        Calendar citaCal = Calendar.getInstance();
        citaCal.setTime(fecha);

        normalizarAMedianoche(ahora);
        normalizarAMedianoche(citaCal);

        return citaCal.before(ahora);
    }

    /**
     * Verifica si una fecha es hoy
     */
    public static boolean esFechaHoy(Date fecha) {
        if (fecha == null) return false;

        Calendar ahora = Calendar.getInstance();
        Calendar citaCal = Calendar.getInstance();
        citaCal.setTime(fecha);

        normalizarAMedianoche(ahora);
        normalizarAMedianoche(citaCal);

        return citaCal.equals(ahora);
    }

    /**
     * Obtiene cuántos días han pasado desde una fecha
     */
    public static long getDiasAtrasados(Date fecha) {
        if (fecha == null) return 0;

        Calendar ahora = Calendar.getInstance();
        Calendar citaCal = Calendar.getInstance();
        citaCal.setTime(fecha);

        normalizarAMedianoche(ahora);
        normalizarAMedianoche(citaCal);

        if (!citaCal.before(ahora)) {
            return 0; // No está atrasada
        }

        long diffMillis = ahora.getTimeInMillis() - citaCal.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(diffMillis);
    }

    /**
     * 🆕 Obtiene cuántos días faltan hasta una fecha futura
     * Retorna 0 si la fecha es hoy o pasada
     */
    public static long getDiasFaltantes(Date fecha) {
        if (fecha == null) return 0;

        Calendar ahora = Calendar.getInstance();
        Calendar citaCal = Calendar.getInstance();
        citaCal.setTime(fecha);

        normalizarAMedianoche(ahora);
        normalizarAMedianoche(citaCal);

        if (citaCal.before(ahora) || citaCal.equals(ahora)) {
            return 0; // Ya pasó o es hoy
        }

        long diffMillis = citaCal.getTimeInMillis() - ahora.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(diffMillis);
    }

    /**
     * 🆕 Valida una fecha para la creación de una nueva actividad
     * Retorna null si es válida, o un mensaje de error si no lo es
     *
     * @param fecha Fecha a validar
     * @param context Context de Android (puede ser null)
     * @return null si válida, String con mensaje de error si no
     */
    public static String validarFechaParaCreacion(Date fecha, android.content.Context context) {
        if (fecha == null) {
            return "❌ Debes seleccionar una fecha";
        }

        Calendar ahora = Calendar.getInstance();
        Calendar fechaCal = Calendar.getInstance();
        fechaCal.setTime(fecha);

        normalizarAMedianoche(ahora);
        normalizarAMedianoche(fechaCal);

        // Permitir fechas pasadas solo si el usuario confirma
        // (esto debería manejarse con un diálogo antes de llamar a este método)

        // No hay validaciones que impidan guardar, solo advertencias
        // que se manejan en el UI con diálogos de confirmación

        return null; // Válida
    }

    /**
     * Normaliza un Calendar a medianoche (00:00:00.000)
     */
    private static void normalizarAMedianoche(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }
}