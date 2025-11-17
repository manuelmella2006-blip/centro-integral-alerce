package com.example.centrointegralalerce.data;

import java.util.Date;

public class Cita {

    // --- Campos principales ---
    private String id;                // <--- Necesario
    private String actividadId;       // <--- Necesario

    private Date fecha;
    private String hora;
    private String lugarId;
    private String estado;

    // Info adicional de actividad
    private String actividadNombre;
    private String tipoActividadId;

    // --- Campos opcionales (no rompen nada y evitan errores futuros) ---
    private String proyectoId;
    private String oferenteId;
    private String socioComunitarioId;

    private String fechaInicio;
    private String fechaTermino;
    private String horaInicio;
    private String horaTermino;

    private String periodicidad;

    private int cupo;
    private int diasAvisoPrevio;

    public Cita() {}

    public Cita(Date fecha, String hora, String lugarId, String estado) {
        this.fecha = fecha;
        this.hora = hora;
        this.lugarId = lugarId;
        this.estado = estado;
    }

    // --- Getters y setters obligatorios ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getActividadId() { return actividadId; }
    public void setActividadId(String actividadId) { this.actividadId = actividadId; }

    // --- Ya existentes ---
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getLugarId() { return lugarId; }
    public void setLugarId(String lugarId) { this.lugarId = lugarId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getActividadNombre() { return actividadNombre; }
    public void setActividadNombre(String actividadNombre) { this.actividadNombre = actividadNombre; }

    public String getTipoActividadId() { return tipoActividadId; }
    public void setTipoActividadId(String tipoActividadId) { this.tipoActividadId = tipoActividadId; }

    // --- Campos opcionales (seguro agregar) ---
    public String getProyectoId() { return proyectoId; }
    public void setProyectoId(String proyectoId) { this.proyectoId = proyectoId; }

    public String getOferenteId() { return oferenteId; }
    public void setOferenteId(String oferenteId) { this.oferenteId = oferenteId; }

    public String getSocioComunitarioId() { return socioComunitarioId; }
    public void setSocioComunitarioId(String socioComunitarioId) { this.socioComunitarioId = socioComunitarioId; }

    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }

    public String getFechaTermino() { return fechaTermino; }
    public void setFechaTermino(String fechaTermino) { this.fechaTermino = fechaTermino; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraTermino() { return horaTermino; }
    public void setHoraTermino(String horaTermino) { this.horaTermino = horaTermino; }

    public String getPeriodicidad() { return periodicidad; }
    public void setPeriodicidad(String periodicidad) { this.periodicidad = periodicidad; }

    public int getCupo() { return cupo; }
    public void setCupo(int cupo) { this.cupo = cupo; }

    public int getDiasAvisoPrevio() { return diasAvisoPrevio; }
    public void setDiasAvisoPrevio(int diasAvisoPrevio) { this.diasAvisoPrevio = diasAvisoPrevio; }
}
