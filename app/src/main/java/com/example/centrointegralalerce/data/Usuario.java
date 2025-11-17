package com.example.centrointegralalerce.data;

import com.google.firebase.Timestamp;

public class Usuario {
    private String email;
    private Timestamp fechaCreacion;
    private String nombre;
    private String rolId;
    private String uid;
    public Usuario() {} // Requerido por Firestore

    public Usuario(String email, Timestamp fechaCreacion, String nombre, String rolId) {
        this.email = email;
        this.fechaCreacion = fechaCreacion;
        this.nombre = nombre;
        this.rolId = rolId;
    }

    public String getEmail() { return email; }
    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public String getNombre() { return nombre; }
    public String getRolId() { return rolId; }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public void setEmail(String email) { this.email = email; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setRolId(String rolId) { this.rolId = rolId; }
}

