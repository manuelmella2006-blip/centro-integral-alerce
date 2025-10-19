package com.example.centrointegralalerce.data;

import android.util.Log;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.util.Calendar;
import java.util.Date;

public class Cita {
    private static final String TAG = "Cita";

    @DocumentId
    private String id;
    private String actividad;
    private String lugar;
    private String tipoActividad;
    private Timestamp fechaHora; // FUENTE ÚNICA DE VERDAD
    private String userId;

    // Campos temporales para UI (no se guardan en Firebase)
    private transient int diaSemana;
    private transient String hora;

    // Constructor vacío requerido por Firebase
    public Cita() {
    }

    // Constructor principal con validación
    public Cita(String id, String actividad, String lugar, String tipoActividad, Timestamp fechaHora, String userId) {
        this.id = id;
        this.actividad = actividad;
        this.lugar = lugar;
        this.tipoActividad = tipoActividad;
        this.userId = userId;
        setFechaHora(fechaHora); // Usa el setter con validación
    }

    /**
     * Calcula hora y día de la semana desde Timestamp con manejo de errores
     */
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

            // Extraer hora
            int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            this.hora = String.format("%02d:%02d", hourOfDay, minute);

            // Calcular día de la semana (Lunes=0, Domingo=6)
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            this.diaSemana = convertirDiaSemana(dayOfWeek);

        } catch (Exception e) {
            Log.e(TAG, "Error al calcular hora/día desde Timestamp: " + e.getMessage(), e);
            this.hora = "00:00";
            this.diaSemana = -1;
        }
    }

    /**
     * Convierte Calendar.DAY_OF_WEEK a formato 0=Lun, 6=Dom
     */
    private int convertirDiaSemana(int calendarDay) {
        // Calendar: 1=Dom, 2=Lun, ..., 7=Sáb
        // Nuestro formato: 0=Lun, 6=Dom
        return (calendarDay == Calendar.SUNDAY) ? 6 : calendarDay - 2;
    }

    /**
     * Convierte Timestamp a Calendar con validación
     */
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

    /**
     * Valida que todos los campos obligatorios estén presentes
     */
    public boolean esValida() {
        boolean valida = actividad != null && !actividad.trim().isEmpty()
                && lugar != null && !lugar.trim().isEmpty()
                && tipoActividad != null && !tipoActividad.trim().isEmpty()
                && fechaHora != null
                && userId != null && !userId.trim().isEmpty();

        if (!valida) {
            Log.w(TAG, String.format("Cita inválida - ID: %s, Actividad: %s, Lugar: %s, Tipo: %s, FechaHora: %s, UserId: %s",
                    id, actividad, lugar, tipoActividad, fechaHora, userId));
        }

        return valida;
    }

    // ===== GETTERS Y SETTERS CON VALIDACIÓN =====

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActividad() {
        return actividad != null ? actividad : "";
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public String getLugar() {
        return lugar != null ? lugar : "";
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getTipoActividad() {
        return tipoActividad != null ? tipoActividad : "";
    }

    public void setTipoActividad(String tipoActividad) {
        this.tipoActividad = tipoActividad;
    }

    public Timestamp getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Timestamp fechaHora) {
        this.fechaHora = fechaHora;
        calcularHoraYDia(); // Recalcula automáticamente
    }

    public String getUserId() {
        return userId != null ? userId : "";
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getDiaSemana() {
        if (diaSemana == 0 && fechaHora != null) {
            calcularHoraYDia(); // Calcula si no está inicializado
        }
        return diaSemana;
    }

    public void setDiaSemana(int diaSemana) {
        this.diaSemana = diaSemana;
    }

    public String getHora() {
        if (hora == null && fechaHora != null) {
            calcularHoraYDia(); // Calcula bajo demanda
        }
        return hora != null ? hora : "00:00";
    }

    @Override
    public String toString() {
        return "Cita{" +
                "id='" + id + '\'' +
                ", actividad='" + actividad + '\'' +
                ", lugar='" + lugar + '\'' +
                ", tipoActividad='" + tipoActividad + '\'' +
                ", fechaHora=" + fechaHora +
                ", userId='" + userId + '\'' +
                ", hora='" + getHora() + '\'' +
                ", diaSemana=" + getDiaSemana() +
                '}';
    }
}