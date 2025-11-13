package com.example.centrointegralalerce.ui.mantenedores;

public class SocioComunitarioItem {
    private String id;
    private String nombre;
    private String beneficiarios;

    public SocioComunitarioItem() {
        // Constructor vacío requerido por Firebase
    }

    public SocioComunitarioItem(String id, String nombre, String beneficiarios) {
        this.id = id;
        this.nombre = nombre;
        this.beneficiarios = beneficiarios;
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

    public String getBeneficiarios() {
        return beneficiarios;
    }

    public void setBeneficiarios(String beneficiarios) {
        this.beneficiarios = beneficiarios;
    }
}
