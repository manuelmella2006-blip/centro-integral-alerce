package com.example.centrointegralalerce.data;

import android.util.Log;

import com.example.centrointegralalerce.data.Cita; // ajusta al paquete real de Cita
import com.google.firebase.firestore.DocumentId;

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

            return new Cita(
                    fechaFinal,
                    hora != null ? hora : "",
                    lugarId != null ? lugarId : "",
                    estado != null ? estado : (estadoActividad != null ? estadoActividad : "")
            );
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

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("es", "ES"));
            sdf.setLenient(false);

            String fechaHoraStr = fecha + " " + (hora != null ? hora : "00:00");
            return sdf.parse(fechaHoraStr);

        } catch (ParseException e) {
            Log.e(TAG, "Error parseando fecha: " + fecha + " " + hora, e);
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
}
