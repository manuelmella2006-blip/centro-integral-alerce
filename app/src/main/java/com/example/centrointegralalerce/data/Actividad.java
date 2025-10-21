package com.example.centrointegralalerce.data;

import java.util.List;

public class Actividad {
    private String nombre;
    private String tipoActividadId;
    private String periodicidad;
    private int cupo;
    private String proyectoId;
    private String oferenteId;
    private String socioComunitarioId;
    private List<String> archivosAdjuntos;
    private int diasAvisoPrevio;
    private String estado;

    // Nuevos atributos
    private String fecha; // "dd/MM/yyyy"
    private String hora;  // "HH:mm"
    private String lugar; // Nombre del lugar

    // Constructor vacío requerido por Firestore
    public Actividad() {}

    // Constructor completo incluyendo los nuevos atributos
    public Actividad(String nombre,
                     String tipoActividadId,
                     String periodicidad,
                     int cupo,
                     String proyectoId,
                     String oferenteId,
                     String socioComunitarioId,
                     List<String> archivosAdjuntos,
                     int diasAvisoPrevio,
                     String estado,
                     String fecha,
                     String hora,
                     String lugar) {
        this.nombre = nombre;
        this.tipoActividadId = tipoActividadId;
        this.periodicidad = periodicidad;
        this.cupo = cupo;
        this.proyectoId = proyectoId;
        this.oferenteId = oferenteId;
        this.socioComunitarioId = socioComunitarioId;
        this.archivosAdjuntos = archivosAdjuntos;
        this.diasAvisoPrevio = diasAvisoPrevio;
        this.estado = estado;
        this.fecha = fecha;
        this.hora = hora;
        this.lugar = lugar;
    }

    // Getters y setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoActividadId() {
        return tipoActividadId;
    }

    public void setTipoActividadId(String tipoActividadId) {
        this.tipoActividadId = tipoActividadId;
    }

    public String getPeriodicidad() {
        return periodicidad;
    }

    public void setPeriodicidad(String periodicidad) {
        this.periodicidad = periodicidad;
    }

    public int getCupo() {
        return cupo;
    }

    public void setCupo(int cupo) {
        this.cupo = cupo;
    }

    public String getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(String proyectoId) {
        this.proyectoId = proyectoId;
    }

    public String getOferenteId() {
        return oferenteId;
    }

    public void setOferenteId(String oferenteId) {
        this.oferenteId = oferenteId;
    }

    public String getSocioComunitarioId() {
        return socioComunitarioId;
    }

    public void setSocioComunitarioId(String socioComunitarioId) {
        this.socioComunitarioId = socioComunitarioId;
    }

    public List<String> getArchivosAdjuntos() {
        return archivosAdjuntos;
    }

    public void setArchivosAdjuntos(List<String> archivosAdjuntos) {
        this.archivosAdjuntos = archivosAdjuntos;
    }

    public int getDiasAvisoPrevio() {
        return diasAvisoPrevio;
    }

    public void setDiasAvisoPrevio(int diasAvisoPrevio) {
        this.diasAvisoPrevio = diasAvisoPrevio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Nuevos getters y setters
    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }
}
