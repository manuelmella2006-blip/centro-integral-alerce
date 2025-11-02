package com.example.centrointegralalerce.data;

import android.util.Log;
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
        Log.d("USER_SESSION", "✅ Rol asignado: " + rolId);
        Log.d("USER_SESSION", "✅ Permisos cargados: " + permisos);
    }

    public String getRolId() {
        return rolId;
    }

    public boolean puede(String permiso) {
        boolean tiene = permisos != null && permisos.getOrDefault(permiso, false);
        Log.d("USER_SESSION", "🔎 Verificando permiso [" + permiso + "] → " + tiene);
        return tiene;
    }

    public Map<String, Boolean> getPermisos() {
        return permisos;
    }
}
