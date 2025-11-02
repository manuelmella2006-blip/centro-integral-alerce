package com.example.centrointegralalerce.data;

import android.util.Log;

import com.google.firebase.firestore.DocumentId;
import java.util.Calendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Modelo que representa una cita tal como está en Firebase.
 * Puede venir de /citas o de /actividades/{actividadId}/citas/{citaId}.
 */
public class CitaFirebase {
    private static final String TAG = "CitaFirebase";

    @DocumentId
    private String id;

    private String estado;        // "activo", "cancelado", etc.
    private String fecha;         // ahora como String con formato "dd/MM/yyyy"
    private String hora;          // "HH:mm"
    private String lugarId;       // ID del lugar

    // Campos informativos de la actividad padre (opcionales)
    private transient String actividadId;
    private transient String actividadNombre;
    private transient String tipoActividadId;
    private transient String estadoActividad;
    private transient Integer cupo;

    // Nuevos campos
    private String fechaInicio;
    private String fechaTermino;
    private String horaInicio;
    private String horaTermino;
    private String periodicidad;
    private String oferenteId;
    private String proyectoId;
    private String socioComunitarioId;
    private Integer diasAvisoPrevio;

    public CitaFirebase() {}

    /**
     * Convierte este DTO de Firestore al modelo de dominio Cita.
     */
    public Cita toCita() {
        try {
            Date fechaFinal = parsearFechaHora();
            if (fechaFinal == null) {
                Log.e(TAG, "No se pudo parsear fecha: " + fecha + " " + hora);
                return null;
            }

            Cita cita = new Cita(
                    fechaFinal,
                    hora != null ? hora : "",
                    lugarId != null ? lugarId : "",
                    estado != null ? estado : (estadoActividad != null ? estadoActividad : "")
            );

            // Agregar información adicional
            cita.setActividadNombre(actividadNombre);
            cita.setTipoActividadId(tipoActividadId);

            return cita;
        } catch (Exception e) {
            Log.e(TAG, "Error toCita(): " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Parsea fecha ("dd/MM/yyyy") y hora ("HH:mm") a java.util.Date.
     */
    private Date parsearFechaHora() {
        try {
            if (fecha == null || fecha.isEmpty()) return null;

            // Parsear manualmente para evitar problemas de zona horaria
            String[] fechaParts = fecha.split("/");
            if (fechaParts.length != 3) {
                Log.e(TAG, "Formato de fecha inválido: " + fecha);
                return null;
            }

            int day = Integer.parseInt(fechaParts[0]);
            int month = Integer.parseInt(fechaParts[1]) - 1; // Calendar.MONTH es 0-based
            int year = Integer.parseInt(fechaParts[2]);

            // Parsear hora
            int hour = 0;
            int minute = 0;
            if (hora != null && !hora.isEmpty()) {
                String[] horaParts = hora.split(":");
                if (horaParts.length >= 1) hour = Integer.parseInt(horaParts[0]);
                if (horaParts.length >= 2) minute = Integer.parseInt(horaParts[1]);
            }

            // Crear Calendar con la fecha/hora exacta
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            Log.d(TAG, "✅ Fecha parseada: " + cal.getTime() + " | Original: " + fecha + " " + hora);

            return cal.getTime();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error parseando fecha: " + fecha + " " + hora, e);
            return null;
        }
    }

    // Getters/Setters mínimos para Firebase
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

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

    public Integer getCupo() { return cupo; }
    public void setCupo(Integer cupo) { this.cupo = cupo; }

    // Setters adicionales
    public void setFechaString(String fecha) {
        this.fecha = fecha;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaTermino(String fechaTermino) {
        this.fechaTermino = fechaTermino;
    }

    public String getFechaTermino() {
        return fechaTermino;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraTermino(String horaTermino) {
        this.horaTermino = horaTermino;
    }

    public String getHoraTermino() {
        return horaTermino;
    }

    public void setPeriodicidad(String periodicidad) {
        this.periodicidad = periodicidad;
    }

    public String getPeriodicidad() {
        return periodicidad;
    }

    public void setOferenteId(String oferenteId) {
        this.oferenteId = oferenteId;
    }

    public String getOferenteId() {
        return oferenteId;
    }

    public void setProyectoId(String proyectoId) {
        this.proyectoId = proyectoId;
    }

    public String getProyectoId() {
        return proyectoId;
    }

    public void setSocioComunitarioId(String socioComunitarioId) {
        this.socioComunitarioId = socioComunitarioId;
    }

    public String getSocioComunitarioId() {
        return socioComunitarioId;
    }

    public void setDiasAvisoPrevio(Integer diasAvisoPrevio) {
        this.diasAvisoPrevio = diasAvisoPrevio;
    }

    public Integer getDiasAvisoPrevio() {
        return diasAvisoPrevio;
    }
}
