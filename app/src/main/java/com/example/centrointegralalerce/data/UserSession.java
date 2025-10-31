package com.example.centrointegralalerce.data;

import java.util.Map;

public class UserSession {
    private static UserSession instance;
    private String rolId;
    private Map<String, Boolean> permisos;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setRol(String rolId, Map<String, Boolean> permisos) {
        this.rolId = rolId;
        this.permisos = permisos;
    }

    public String getRolId() {
        return rolId;
    }

    public boolean puede(String permiso) {
        return permisos != null && permisos.getOrDefault(permiso, false);
    }
}
