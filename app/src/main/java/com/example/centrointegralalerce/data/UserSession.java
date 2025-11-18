package com.example.centrointegralalerce.data;

import android.util.Log;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class UserSession {
    private static UserSession instance;
    private String rolId;
    private Map<String, Boolean> permisos;

    // 🔥 NUEVO: Callback para notificar cuando los permisos estén cargados
    private List<OnPermisosCargadosListener> listeners = new ArrayList<>();

    public interface OnPermisosCargadosListener {
        void onPermisosCargados();
    }

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // 🔥 NUEVO: Método para registrar listeners
    public void setOnPermisosCargadosListener(OnPermisosCargadosListener listener) {
        if (permisosCargados()) {
            // Si ya están cargados, notificar inmediatamente
            listener.onPermisosCargados();
        } else {
            this.listeners.add(listener);
        }
    }

    // 🔥 NUEVO: Notificar a todos los listeners
    private void notificarPermisosCargados() {
        Log.d("USER_SESSION", "🔔 Notificando " + listeners.size() + " listeners de permisos cargados");
        for (OnPermisosCargadosListener listener : listeners) {
            listener.onPermisosCargados();
        }
        listeners.clear();
    }

    // 🔥 MODIFICADO: setRol para notificar cuando se carguen los permisos
    public void setRol(String rolId, Map<String, Boolean> permisos) {
        this.rolId = rolId;
        this.permisos = permisos;

        // DEBUG: Mostrar todos los permisos cargados
        Log.d("USER_SESSION_DEBUG", "=== PERMISOS CARGADOS PARA ROL: " + rolId + " ===");
        if (permisos != null) {
            for (Map.Entry<String, Boolean> entry : permisos.entrySet()) {
                Log.d("USER_SESSION_DEBUG", entry.getKey() + ": " + entry.getValue());
            }
        } else {
            Log.d("USER_SESSION_DEBUG", "❌ permisos es NULL");
        }

        notificarPermisosCargados();
    }

    // 🔥 NUEVO: Método para esperar permisos de forma asíncrona
    public void esperarPermisos(Runnable onCargados) {
        if (permisosCargados()) {
            Log.d("USER_SESSION", "✅ Permisos YA cargados, ejecutando callback inmediatamente");
            onCargados.run();
        } else {
            Log.d("USER_SESSION", "⏳ Permisos NO cargados, registrando callback para ejecutar después");
            setOnPermisosCargadosListener(new OnPermisosCargadosListener() {
                @Override
                public void onPermisosCargados() {
                    Log.d("USER_SESSION", "🎯 Ejecutando callback de permisos cargados");
                    onCargados.run();
                }
            });
        }
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
        this.listeners.clear();
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
        Log.d("USER_SESSION_DEBUG", "Listeners esperando: " + listeners.size());

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

    public String getRolId() {
        return rolId;
    }
}