package com.example.centrointegralalerce.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestor de tokens de Firebase Cloud Messaging
 * Maneja el registro, actualización y eliminación de tokens por usuario
 */
public class FCMTokenManager {
    private static final String TAG = "FCMTokenManager";
    private static final String PREFS_NAME = "FCMPrefs";
    private static final String KEY_TOKEN = "fcm_token";
    private static final String KEY_TOKEN_SENT = "token_sent_to_server";

    private final Context context;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final SharedPreferences prefs;

    public FCMTokenManager(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Obtiene el token FCM actual y lo registra en Firestore
     */
    public void obtenerYRegistrarToken() {
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "Usuario no autenticado");
            return;
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error al obtener token FCM", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d(TAG, "Token FCM obtenido: " + token);

                    // Guardar localmente
                    guardarTokenLocal(token);

                    // Registrar en Firestore
                    registrarTokenEnFirestore(token);
                });
    }

    /**
     * Registra el token en Firestore asociado al usuario actual
     */
    public void registrarTokenEnFirestore(String token) {
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "No se puede registrar token: usuario no autenticado");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String email = auth.getCurrentUser().getEmail();

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("fcmToken", token);
        tokenData.put("email", email);
        tokenData.put("dispositivo", Build.MODEL);
        tokenData.put("fabricante", Build.MANUFACTURER);
        tokenData.put("sistemaOperativo", "Android " + Build.VERSION.RELEASE);
        tokenData.put("versionSDK", Build.VERSION.SDK_INT);
        tokenData.put("ultimaActualizacion", System.currentTimeMillis());
        tokenData.put("activo", true);

        db.collection("usuarios")
                .document(userId)
                .update(tokenData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Token registrado en Firestore");
                    marcarTokenEnviado(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al registrar token", e);

                    // Intentar con merge si el documento no existe
                    db.collection("usuarios")
                            .document(userId)
                            .set(tokenData, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "✅ Token registrado con merge");
                                marcarTokenEnviado(true);
                            })
                            .addOnFailureListener(e2 ->
                                    Log.e(TAG, "❌ Error definitivo al registrar token", e2));
                });
    }

    /**
     * Elimina el token del usuario (al cerrar sesión)
     */
    public void eliminarToken() {
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "No hay usuario para eliminar token");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // Marcar token como inactivo en Firestore
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("activo", false);
        updateData.put("fechaDesactivacion", System.currentTimeMillis());

        db.collection("usuarios")
                .document(userId)
                .update(updateData)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Token marcado como inactivo"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al desactivar token", e));

        // Eliminar token de FCM
        FirebaseMessaging.getInstance().deleteToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Token FCM eliminado");
                        limpiarTokenLocal();
                    } else {
                        Log.e(TAG, "Error al eliminar token FCM", task.getException());
                    }
                });
    }

    /**
     * Actualiza el token si ha cambiado
     */
    public void actualizarTokenSiNecesario(String nuevoToken) {
        String tokenActual = getTokenLocal();

        if (tokenActual == null || !tokenActual.equals(nuevoToken)) {
            Log.d(TAG, "Token cambió, actualizando...");
            guardarTokenLocal(nuevoToken);
            registrarTokenEnFirestore(nuevoToken);
        } else {
            Log.d(TAG, "Token sin cambios");
        }
    }

    /**
     * Verifica si el token ya fue enviado al servidor
     */
    public boolean tokenFueEnviado() {
        return prefs.getBoolean(KEY_TOKEN_SENT, false);
    }

    /**
     * Marca si el token fue enviado al servidor
     */
    private void marcarTokenEnviado(boolean enviado) {
        prefs.edit().putBoolean(KEY_TOKEN_SENT, enviado).apply();
    }

    /**
     * Guarda el token localmente
     */
    private void guardarTokenLocal(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
        Log.d(TAG, "Token guardado localmente");
    }

    /**
     * Obtiene el token guardado localmente
     */
    public String getTokenLocal() {
        return prefs.getString(KEY_TOKEN, null);
    }

    /**
     * Limpia el token local
     */
    private void limpiarTokenLocal() {
        prefs.edit()
                .remove(KEY_TOKEN)
                .putBoolean(KEY_TOKEN_SENT, false)
                .apply();
        Log.d(TAG, "Token local limpiado");
    }

    /**
     * Suscribe al usuario a un tema específico
     */
    public void suscribirseATema(String tema) {
        FirebaseMessaging.getInstance().subscribeToTopic(tema)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "✅ Suscrito al tema: " + tema);
                    } else {
                        Log.e(TAG, "❌ Error al suscribirse al tema: " + tema, task.getException());
                    }
                });
    }

    /**
     * Desuscribe al usuario de un tema
     */
    public void desuscribirseDeTema(String tema) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(tema)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "✅ Desuscrito del tema: " + tema);
                    } else {
                        Log.e(TAG, "❌ Error al desuscribirse del tema: " + tema, task.getException());
                    }
                });
    }

    /**
     * Suscribe al usuario a temas según su rol
     */
    public void suscribirseATemasSegunRol(String rol) {
        if (rol == null) return;

        // Todos reciben notificaciones generales
        suscribirseATema("todos");

        switch (rol.toLowerCase()) {
            case "admin":
            case "administrador":
                suscribirseATema("admin");
                suscribirseATema("talleres");
                suscribirseATema("charlas");
                suscribirseATema("atenciones");
                break;

            case "usuario":
                suscribirseATema("usuarios");
                suscribirseATema("talleres");
                suscribirseATema("charlas");
                break;

            case "invitado":
                suscribirseATema("invitados");
                break;

            default:
                Log.w(TAG, "Rol desconocido: " + rol);
        }
    }

    /**
     * Obtiene información del dispositivo
     */
    public static Map<String, Object> getInfoDispositivo() {
        Map<String, Object> info = new HashMap<>();
        info.put("modelo", Build.MODEL);
        info.put("fabricante", Build.MANUFACTURER);
        info.put("dispositivo", Build.DEVICE);
        info.put("android", Build.VERSION.RELEASE);
        info.put("sdk", Build.VERSION.SDK_INT);
        return info;
    }
}