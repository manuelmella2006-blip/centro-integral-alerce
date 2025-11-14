package com.example.centrointegralalerce.ui.mantenedores;

public class ProyectoItem {
    private String id;
    private String nombre;
    private String descripcion;

    public ProyectoItem() {
        // Constructor vacío requerido por Firebase
    }

    public ProyectoItem(String id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}