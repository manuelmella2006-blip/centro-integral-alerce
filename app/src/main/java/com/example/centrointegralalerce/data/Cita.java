package com.example.centrointegralalerce.data;

import java.util.Calendar;

public class Cita {
    private String id; // ID único
    private String actividad;
    private String lugar;
    private String fechaHora; // Formato: "Lun 09:00"
    private String tipoActividad;
    private int diaSemana; // 0=Lun, 1=Mar, ..., 6=Dom
    private String hora; // "09:00"

    private Calendar fechaHoraCalendar; // Nueva implementación con Calendar

    // Constructor por defecto (Firebase, etc.)
    public Cita() {}

    // Constructor original
    public Cita(String actividad, String lugar, String fechaHora, String tipoActividad) {
        this.actividad = actividad;
        this.lugar = lugar;
        this.fechaHora = fechaHora;
        this.tipoActividad = tipoActividad;
        parseFechaHora();
    }

    // Nuevo constructor con ID
    public Cita(String id, String actividad, String lugar, String fechaHora, String tipoActividad) {
        this.id = id;
        this.actividad = actividad;
        this.lugar = lugar;
        this.fechaHora = fechaHora;
        this.tipoActividad = tipoActividad;
        parseFechaHora();
    }

    // Analiza fechaHora en diaSemana y hora
    private void parseFechaHora() {
        if (fechaHora != null && fechaHora.length() >= 8) {
            String dia = fechaHora.substring(0, 3);
            this.hora = fechaHora.substring(4);

            switch (dia) {
                case "Lun": this.diaSemana = 0; break;
                case "Mar": this.diaSemana = 1; break;
                case "Mié": case "Mie": this.diaSemana = 2; break;
                case "Jue": this.diaSemana = 3; break;
                case "Vie": this.diaSemana = 4; break;
                case "Sáb": case "Sab": this.diaSemana = 5; break;
                case "Dom": this.diaSemana = 6; break;
                default: this.diaSemana = 0;
            }

            // Crear Calendar basado en día y hora
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, diaSemana + 2); // lunes=2, domingo=1
            if (hora != null && hora.contains(":")) {
                String[] partes = hora.split(":");
                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(partes[0]));
                cal.set(Calendar.MINUTE, Integer.parseInt(partes[1]));
                cal.set(Calendar.SECOND, 0);
            }
            fechaHoraCalendar = cal;
        }
    }

    // Setter público para Calendar
    public void setFechaHoraCalendar(Calendar fechaHoraCalendar) {
        this.fechaHoraCalendar = fechaHoraCalendar;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getActividad() { return actividad; }
    public void setActividad(String actividad) { this.actividad = actividad; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
        parseFechaHora();
    }

    public String getTipoActividad() { return tipoActividad; }
    public void setTipoActividad(String tipoActividad) { this.tipoActividad = tipoActividad; }

    public int getDiaSemana() { return diaSemana; }
    public void setDiaSemana(int diaSemana) { this.diaSemana = diaSemana; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; parseFechaHora(); }

    public Calendar getFechaHoraCalendar() { return fechaHoraCalendar; }

    // Método útil para debugging
    @Override
    public String toString() {
        return "Cita{" +
                "id='" + id + '\'' +
                ", actividad='" + actividad + '\'' +
                ", lugar='" + lugar + '\'' +
                ", fechaHora='" + fechaHora + '\'' +
                ", tipoActividad='" + tipoActividad + '\'' +
                ", diaSemana=" + diaSemana +
                ", hora='" + hora + '\'' +
                ", fechaHoraCalendar=" + (fechaHoraCalendar != null ? fechaHoraCalendar.getTime() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cita cita = (Cita) o;
        return id != null && id.equals(cita.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // Devuelve el nombre completo del día
    public String getDiaNombre() {
        switch (diaSemana) {
            case 0: return "Lunes";
            case 1: return "Martes";
            case 2: return "Miércoles";
            case 3: return "Jueves";
            case 4: return "Viernes";
            case 5: return "Sábado";
            case 6: return "Domingo";
            default: return "Lunes";
        }
    }

    // Devuelve hora en minutos para ordenar
    public int getHoraNumerica() {
        if (hora != null && hora.contains(":")) {
            String[] partes = hora.split(":");
            return Integer.parseInt(partes[0]) * 60 + Integer.parseInt(partes[1]);
        }
        return 0;
    }
}
