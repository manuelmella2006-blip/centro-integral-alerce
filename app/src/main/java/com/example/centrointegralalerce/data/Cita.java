package com.example.centrointegralalerce.data;

public class Cita {
    private String actividad;
    private String lugar;
    private String fechaHora; // Simplificado para mostrar fecha y hora en una String
    private String tipoActividad;

    public Cita(String actividad, String lugar, String fechaHora, String tipoActividad) {
        this.actividad = actividad;
        this.lugar = lugar;
        this.fechaHora = fechaHora;
        this.tipoActividad = tipoActividad;
    }

    public String getActividad() { return actividad; }
    public String getLugar() { return lugar; }
    public String getFechaHora() { return fechaHora; }
    public String getTipoActividad() { return tipoActividad; }
}