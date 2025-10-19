package com.example.centrointegralalerce.data;
import com.google.firebase.Timestamp;
import java.util.List;
public class Actividad {
    private String nombre;
    private String proyectos;
    private String periodicidad;
    private String tiposActividad;
    private int cupo;
    private String oferentesActividad;
    private String socioComunitario;
    private String beneficiariosServicio;
    private List<String> archivosAdjuntos;
    private int diasAvisoPrevio;
    private Timestamp fechaCreacion;

    // Constructor vacío requerido por Firebase
    public Actividad() {
    }

    // Constructor con parámetros
    public Actividad(String nombre, String proyectos, String periodicidad,
                     String tiposActividad, int cupo, String oferentesActividad,
                     String socioComunitario, String beneficiariosServicio,
                     List<String> archivosAdjuntos, int diasAvisoPrevio) {
        this.nombre = nombre;
        this.proyectos = proyectos;
        this.periodicidad = periodicidad;
        this.tiposActividad = tiposActividad;
        this.cupo = cupo;
        this.oferentesActividad = oferentesActividad;
        this.socioComunitario = socioComunitario;
        this.beneficiariosServicio = beneficiariosServicio;
        this.archivosAdjuntos = archivosAdjuntos;
        this.diasAvisoPrevio = diasAvisoPrevio;
        this.fechaCreacion = Timestamp.now();
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getProyectos() { return proyectos; }
    public void setProyectos(String proyectos) { this.proyectos = proyectos; }

    public String getPeriodicidad() { return periodicidad; }
    public void setPeriodicidad(String periodicidad) { this.periodicidad = periodicidad; }

    public String getTiposActividad() { return tiposActividad; }
    public void setTiposActividad(String tiposActividad) { this.tiposActividad = tiposActividad; }

    public int getCupo() { return cupo; }
    public void setCupo(int cupo) { this.cupo = cupo; }

    public String getOferentesActividad() { return oferentesActividad; }
    public void setOferentesActividad(String oferentesActividad) { this.oferentesActividad = oferentesActividad; }

    public String getSocioComunitario() { return socioComunitario; }
    public void setSocioComunitario(String socioComunitario) { this.socioComunitario = socioComunitario; }

    public String getBeneficiariosServicio() { return beneficiariosServicio; }
    public void setBeneficiariosServicio(String beneficiariosServicio) { this.beneficiariosServicio = beneficiariosServicio; }

    public List<String> getArchivosAdjuntos() { return archivosAdjuntos; }
    public void setArchivosAdjuntos(List<String> archivosAdjuntos) { this.archivosAdjuntos = archivosAdjuntos; }

    public int getDiasAvisoPrevio() { return diasAvisoPrevio; }
    public void setDiasAvisoPrevio(int diasAvisoPrevio) { this.diasAvisoPrevio = diasAvisoPrevio; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}