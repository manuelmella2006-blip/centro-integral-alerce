package com.example.centrointegralalerce.ui.mantenedores;

public class OferenteItem {
    private String id;
    private String nombre;

    public OferenteItem() {
        // Constructor vacío requerido por Firebase
    }

    public OferenteItem(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
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
}
