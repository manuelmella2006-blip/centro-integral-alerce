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

    // ===============================
    // 🔹 ASIGNACIÓN DE ROL Y PERMISOS
    // ===============================
    public void setRol(String rolId, Map<String, Boolean> permisos) {
        this.rolId = rolId;
        this.permisos = permisos;
        Log.d("USER_SESSION", "✅ Rol asignado: " + rolId);
        Log.d("USER_SESSION", "✅ Permisos cargados: " + permisos);
    }

    public String getRolId() {
        return rolId;
    }

    // ===============================
    // 🔹 MÉTODOS DE COMPROBACIÓN DE ROLES
    // ===============================
    public boolean esAdmin() { return "admin".equalsIgnoreCase(rolId); }

    public boolean puedeGestionarUsuarios() { return puede("gestionar_usuarios"); }

    public boolean puedeGestionarMantenedores() { return puede("gestionar_mantenedores"); }

    // ===============================
    // 🔹 MÉTODO CENTRAL DE VERIFICACIÓN DE PERMISOS
    // ===============================
    public boolean puede(String permiso) {
        boolean permisosCargados = permisosCargados();
        boolean tienePermiso = permisos != null && permisos.getOrDefault(permiso, false);
        Log.d("USER_SESSION", "🔍 Verificando permiso [" + permiso + "] → " + tienePermiso +
                " | Permisos cargados: " + permisosCargados + " | Rol: " + rolId);
        if (!permisosCargados) {
            Log.e("USER_SESSION", "❌ ERROR: Permisos no cargados al verificar: " + permiso);
        }
        return tienePermiso;
    }

    // ✅ Verificar permisos de forma segura
    public boolean puedeSeguro(String permiso) {
        if (!permisosCargados()) {
            Log.e("USER_SESSION", "🚫 Permisos no cargados - no se puede verificar: " + permiso);
            return false;
        }
        return puede(permiso);
    }

    // ===============================
    // 🔹 UTILIDADES
    // ===============================
    public Map<String, Boolean> getPermisos() { return permisos; }

    // ✅ Verificar si los permisos están cargados correctamente
    public boolean permisosCargados() {
        boolean cargados = permisos != null && !permisos.isEmpty();
        Log.d("USER_SESSION", "📦 Permisos cargados: " + cargados);
        return cargados;
    }

    // ===============================
    // 🔹 MÉTODOS ESPECÍFICOS DE PERMISOS
    // ===============================
    public boolean puedeCrearActividades() { return puede("crear_actividades"); }
    public boolean puedeModificarActividades() { return puede("modificar_actividades"); }
    public boolean puedeEliminarActividades() { return puede("eliminar_actividades"); }
    public boolean puedeVerTodasActividades() { return puede("ver_todas_actividades"); }
    public boolean puedeCancelarActividades() { return puede("cancelar_actividades"); }
    public boolean puedeReagendarActividades() { return puede("reagendar_actividades"); }
    public boolean puedeAdjuntarComunicaciones() { return puede("adjuntar_comunicaciones"); }
    public boolean puedeCrearUsuarios() { return puede("crear_usuarios"); }
    public boolean puedeModificarUsuarios() { return puede("modificar_usuarios"); }
    public boolean puedeEliminarUsuarios() { return puede("eliminar_usuarios"); }

    // ===============================
    // 🔹 VERIFICACIÓN DE ESTADO
    // ===============================
    public boolean estaAutenticado() { return rolId != null && !rolId.isEmpty(); }

    public boolean tienePermisosCargados() { return permisosCargados(); }

    // ===============================
    // 🔹 NUEVOS MÉTODOS DE SINCRONIZACIÓN
    // ===============================
    public boolean estaAutenticadoConPermisos() {
        return estaAutenticado() && permisosCargados();
    }

    public void limpiarSesion() {
        this.rolId = null;
        this.permisos = null;
        Log.d("USER_SESSION", "🧹 Sesión limpiada");
    }

    // ===============================
    // ✅ NUEVOS MÉTODOS ADICIONALES
    // ===============================

    // 🎯 Debug completo del estado de la sesión y permisos
    public void debugPermisos() {
        Log.d("USER_SESSION_DEBUG", "=== 🎯 ESTADO DE USER SESSION ===");
        Log.d("USER_SESSION_DEBUG", "Rol: " + rolId);
        Log.d("USER_SESSION_DEBUG", "Permisos cargados: " + permisosCargados());
        Log.d("USER_SESSION_DEBUG", "Autenticado: " + estaAutenticado());

        if (permisos != null) {
            Log.d("USER_SESSION_DEBUG", "=== 📊 PERMISOS DISPONIBLES ===");
            for (Map.Entry<String, Boolean> entry : permisos.entrySet()) {
                Log.d("USER_SESSION_DEBUG", entry.getKey() + ": " + entry.getValue());
            }
        } else {
            Log.d("USER_SESSION_DEBUG", "❌ permisos es NULL");
        }
    }

    // 🧩 Verificar múltiples permisos al mismo tiempo
    public boolean puedeTodos(String... permisosRequeridos) {
        for (String permiso : permisosRequeridos) {
            if (!puede(permiso)) {
                return false;
            }
        }
        return true;
    }
}
