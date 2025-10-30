package com.example.centrointegralalerce.data;
import java.util.List;
public class Actividad {
    private String id;
    private String nombre;
    private String tipoActividadId;
    private String periodicidad;
    private int cupo;
    private String proyectoId;
    private String oferenteId;
    private String socioComunitarioId;
    private String lugarId;
    private int diasAvisoPrevio;
    private String estado;

    // ⭐ Campos que necesitas para mostrar en la lista
    private String fechaInicio;    // Cambiado de "fecha"
    private String horaInicio;     // Cambiado de "hora"
    private String fechaTermino;
    private String horaTermino;

    // ⭐ Campos adicionales para mostrar nombres en vez de IDs
    private String tipoActividadNombre;
    private String lugarNombre;
    private List<String> archivosAdjuntos;
    // Constructor vacío
    public Actividad() {}

    // Getters y Setters para todos los campos
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getLugarId() {
        return lugarId;
    }

    public void setLugarId(String lugarId) {
        this.lugarId = lugarId;
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

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getFechaTermino() {
        return fechaTermino;
    }

    public void setFechaTermino(String fechaTermino) {
        this.fechaTermino = fechaTermino;
    }

    public String getHoraTermino() {
        return horaTermino;
    }

    public void setHoraTermino(String horaTermino) {
        this.horaTermino = horaTermino;
    }

    public String getTipoActividadNombre() {
        return tipoActividadNombre;
    }

    public void setTipoActividadNombre(String tipoActividadNombre) {
        this.tipoActividadNombre = tipoActividadNombre;
    }

    public String getLugarNombre() {
        return lugarNombre;
    }

    public void setLugarNombre(String lugarNombre) {
        this.lugarNombre = lugarNombre;
    }
    public List<String> getArchivosAdjuntos() {
        return archivosAdjuntos;
    }

    public void setArchivosAdjuntos(List<String> archivosAdjuntos) {
        this.archivosAdjuntos = archivosAdjuntos;
    }
}
