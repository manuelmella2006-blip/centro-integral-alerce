package com.example.centrointegralalerce.data;

import java.util.Date;

public class Cita {
    private Date fecha;
    private String hora;
    private String lugarId;
    private String estado;

    // Información adicional de la actividad (nuevos campos)
    private String actividadNombre;
    private String tipoActividadId;

    public Cita() {}

    public Cita(Date fecha, String hora, String lugarId, String estado) {
        this.fecha = fecha;
        this.hora = hora;
        this.lugarId = lugarId;
        this.estado = estado;
    }

    // Getters y setters
    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getLugarId() {
        return lugarId;
    }

    public void setLugarId(String lugarId) {
        this.lugarId = lugarId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Nuevos getters y setters
    public String getActividadNombre() {
        return actividadNombre;
    }

    public void setActividadNombre(String actividadNombre) {
        this.actividadNombre = actividadNombre;
    }

    public String getTipoActividadId() {
        return tipoActividadId;
    }

    public void setTipoActividadId(String tipoActividadId) {
        this.tipoActividadId = tipoActividadId;
    }
}
