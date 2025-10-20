package com.example.centrointegralalerce.data;

import android.util.Log;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import java.util.Calendar;

public class Cita {
    private static final String TAG = "Cita";

    @DocumentId
    private String id;
    private String actividad;       // Nombre de la actividad
    private String lugar;           // Nombre o ID del lugar
    private String tipoActividad;   // Tipo o categoría
    private Timestamp fechaHora;    // Fecha y hora unificadas
    private String userId;

    // === Nuevos campos derivados de ActividadFirebase ===
    private String estado;          // "activa", "inactiva", "cancelada", etc.
    private int cupo;               // Cupo disponible (si aplica)
    private String tipoActividadId; // ID de la categoría o tipo (puede mapearse luego)

    // Campos temporales para UI (no se guardan en Firebase)
    private transient int diaSemana;
    private transient String hora;

    // Constructor vacío requerido por Firebase
    public Cita() {}

    // Constructor principal
    public Cita(String id, String actividad, String lugar, String tipoActividad,
                Timestamp fechaHora, String userId) {
        this.id = id;
        this.actividad = actividad;
        this.lugar = lugar;
        this.tipoActividad = tipoActividad;
        this.userId = userId;
        setFechaHora(fechaHora);
    }

    // === NUEVO CONSTRUCTOR EXTENDIDO (por si quieres pasar más datos) ===
    public Cita(String id, String actividad, String lugar, String tipoActividad,
                Timestamp fechaHora, String userId, String estado, int cupo, String tipoActividadId) {
        this(id, actividad, lugar, tipoActividad, fechaHora, userId);
        this.estado = estado;
        this.cupo = cupo;
        this.tipoActividadId = tipoActividadId;
    }

    // ======= MÉTODOS DE CÁLCULO =======

    private void calcularHoraYDia() {
        if (fechaHora == null) {
            Log.w(TAG, "Timestamp es null, no se puede calcular hora/día");
            this.hora = "00:00";
            this.diaSemana = -1;
            return;
        }

        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fechaHora.toDate());

            int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            this.hora = String.format("%02d:%02d", hourOfDay, minute);

            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            this.diaSemana = convertirDiaSemana(dayOfWeek);

        } catch (Exception e) {
            Log.e(TAG, "Error al calcular hora/día desde Timestamp: " + e.getMessage(), e);
            this.hora = "00:00";
            this.diaSemana = -1;
        }
    }

    private int convertirDiaSemana(int calendarDay) {
        return (calendarDay == Calendar.SUNDAY) ? 6 : calendarDay - 2;
    }

    public Calendar getFechaHoraCalendar() {
        if (fechaHora == null) {
            Log.w(TAG, "Timestamp es null en getFechaHoraCalendar()");
            return null;
        }
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fechaHora.toDate());
            return cal;
        } catch (Exception e) {
            Log.e(TAG, "Error al convertir Timestamp a Calendar: " + e.getMessage(), e);
            return null;
        }
    }

    // ======= VALIDACIONES =======
    public boolean esValida() {
        boolean valida = actividad != null && !actividad.trim().isEmpty()
                && lugar != null && !lugar.trim().isEmpty()
                && tipoActividad != null && !tipoActividad.trim().isEmpty()
                && fechaHora != null;

        if (!valida) {
            Log.w(TAG, String.format(
                    "Cita inválida - ID: %s, Actividad: %s, Lugar: %s, Tipo: %s, FechaHora: %s",
                    id, actividad, lugar, tipoActividad, fechaHora));
        }

        return valida;
    }

    // ======= GETTERS Y SETTERS =======
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getActividad() { return actividad != null ? actividad : ""; }
    public void setActividad(String actividad) { this.actividad = actividad; }

    public String getLugar() { return lugar != null ? lugar : ""; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public String getTipoActividad() { return tipoActividad != null ? tipoActividad : ""; }
    public void setTipoActividad(String tipoActividad) { this.tipoActividad = tipoActividad; }

    public Timestamp getFechaHora() { return fechaHora; }
    public void setFechaHora(Timestamp fechaHora) {
        this.fechaHora = fechaHora;
        calcularHoraYDia();
    }

    public String getUserId() { return userId != null ? userId : ""; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getDiaSemana() {
        if (diaSemana == 0 && fechaHora != null) {
            calcularHoraYDia();
        }
        return diaSemana;
    }

    public void setDiaSemana(int diaSemana) { this.diaSemana = diaSemana; }

    public String getHora() {
        if (hora == null && fechaHora != null) {
            calcularHoraYDia();
        }
        return hora != null ? hora : "00:00";
    }

    // ===== NUEVOS CAMPOS =====
    public String getEstado() { return estado != null ? estado : "sin estado"; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getCupo() { return cupo; }
    public void setCupo(int cupo) { this.cupo = cupo; }

    public String getTipoActividadId() { return tipoActividadId; }
    public void setTipoActividadId(String tipoActividadId) { this.tipoActividadId = tipoActividadId; }

    @Override
    public String toString() {
        return "Cita{" +
                "id='" + id + '\'' +
                ", actividad='" + actividad + '\'' +
                ", lugar='" + lugar + '\'' +
                ", tipoActividad='" + tipoActividad + '\'' +
                ", tipoActividadId='" + tipoActividadId + '\'' +
                ", fechaHora=" + fechaHora +
                ", estado='" + estado + '\'' +
                ", cupo=" + cupo +
                ", userId='" + userId + '\'' +
                ", hora='" + getHora() + '\'' +
                ", diaSemana=" + getDiaSemana() +
                '}';
    }
}
