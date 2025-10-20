package com.example.centrointegralalerce.data;

import android.util.Log;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import java.util.Calendar;

/**
 * Modelo que representa una cita tal como está en Firebase.
 * Ruta: actividades/{actividadId}/citas/{citaId}
 */
public class CitaFirebase {
    private static final String TAG = "CitaFirebase";

    @DocumentId
    private String id;

    private String estado;        // "activo", "cancelado", etc.
    private Timestamp fecha;      // Fecha y hora de la cita
    private String hora;          // "14:00" (formato String)
    private String lugarId;       // ID del lugar

    // === Campos de la actividad padre (no están dentro del doc de la cita) ===
    private transient String actividadId;        // ID de la actividad padre
    private transient String actividadNombre;    // Nombre de la actividad
    private transient String tipoActividadId;    // ID del tipo de actividad
    private transient String estadoActividad;    // "activa", "inactiva"
    private transient int cupo;                  // Cupo disponible (si aplica)

    // === Campos calculados para la interfaz ===
    private transient int diaSemana;
    private transient String horaCalculada;

    public CitaFirebase() {
        // Constructor vacío requerido por Firebase
    }

    /**
     * Convierte CitaFirebase al modelo Cita que usa el calendario.
     */
    public Cita toCita() {
        try {
            Timestamp timestampFinal = combinarFechaHora();
            if (timestampFinal == null) {
                Log.e(TAG, "No se pudo crear Timestamp para cita: " + id);
                return null;
            }

            // Construir el objeto Cita completo
            Cita cita = new Cita(
                    id,
                    actividadNombre != null ? actividadNombre : "Actividad sin nombre",
                    lugarId != null ? lugarId : "Sin lugar",
                    tipoActividadId != null ? tipoActividadId : "Desconocido",
                    timestampFinal,
                    "" // userId vacío por ahora
            );

            // Propagar estado y cupo
            cita.setEstado(
                    estadoActividad != null ? estadoActividad : (estado != null ? estado : "indefinido")
            );
            cita.setCupo(cupo);

            return cita;

        } catch (Exception e) {
            Log.e(TAG, "Error al convertir CitaFirebase a Cita: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Combina el campo fecha (Timestamp) con el campo hora (String)
     * para crear un Timestamp completo.
     */
    private Timestamp combinarFechaHora() {
        try {
            if (fecha == null) {
                Log.w(TAG, "Fecha es null");
                return null;
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(fecha.toDate());

            // Si hay hora en formato String, parsearla
            if (hora != null && !hora.isEmpty()) {
                String[] partes = hora.split(":");
                if (partes.length >= 2) {
                    int horas = Integer.parseInt(partes[0]);
                    int minutos = Integer.parseInt(partes[1]);

                    cal.set(Calendar.HOUR_OF_DAY, horas);
                    cal.set(Calendar.MINUTE, minutos);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                }
            }

            return new Timestamp(cal.getTime());

        } catch (Exception e) {
            Log.e(TAG, "Error al combinar fecha y hora: " + e.getMessage(), e);
            return fecha; // Devolver al menos la fecha original
        }
    }

    // === Getters y Setters ===
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getLugarId() { return lugarId; }
    public void setLugarId(String lugarId) { this.lugarId = lugarId; }

    public String getActividadId() { return actividadId; }
    public void setActividadId(String actividadId) { this.actividadId = actividadId; }

    public String getActividadNombre() { return actividadNombre; }
    public void setActividadNombre(String actividadNombre) { this.actividadNombre = actividadNombre; }

    public String getTipoActividadId() { return tipoActividadId; }
    public void setTipoActividadId(String tipoActividadId) { this.tipoActividadId = tipoActividadId; }

    public String getEstadoActividad() { return estadoActividad; }
    public void setEstadoActividad(String estadoActividad) { this.estadoActividad = estadoActividad; }

    public int getCupo() { return cupo; }
    public void setCupo(int cupo) { this.cupo = cupo; }

    public boolean esActiva() {
        return "activa".equalsIgnoreCase(estadoActividad) || "activo".equalsIgnoreCase(estado);
    }

    @Override
    public String toString() {
        return "CitaFirebase{" +
                "id='" + id + '\'' +
                ", estado='" + estado + '\'' +
                ", fecha=" + fecha +
                ", hora='" + hora + '\'' +
                ", lugarId='" + lugarId + '\'' +
                ", actividadNombre='" + actividadNombre + '\'' +
                ", tipoActividadId='" + tipoActividadId + '\'' +
                ", estadoActividad='" + estadoActividad + '\'' +
                ", cupo=" + cupo +
                '}';
    }
}
