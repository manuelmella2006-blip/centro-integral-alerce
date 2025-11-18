package com.example.centrointegralalerce.ui.mantenedores;

/**
 * Modelo simple para representar un Lugar en la lista
 */
public class LugarItem {
    private String id;
    private String nombre;

    public LugarItem() {
    }

    public LugarItem(String id, String nombre) {
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