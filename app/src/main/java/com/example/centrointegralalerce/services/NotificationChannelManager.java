package com.example.centrointegralalerce.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

/**
 * Administrador de canales de notificación
 */
public class NotificationChannelManager {

    private static final String TAG = "NotificationChannelMgr";

    // IDs de canales por tipo de actividad
    public static final String CHANNEL_GENERAL = "citas_general";
    public static final String CHANNEL_DEPORTE = "citas_deporte";
    public static final String CHANNEL_SALUD = "citas_salud";
    public static final String CHANNEL_EDUCACION = "citas_educacion";
    public static final String CHANNEL_CULTURA = "citas_cultura";

    /**
     * Inicializa todos los canales de notificación (Android 8.0+)
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) {
                Log.e(TAG, "❌ NotificationManager no disponible");
                return;
            }

            // Canal General
            createChannel(manager, CHANNEL_GENERAL, "Recordatorios Generales",
                    "Notificaciones de actividades generales",
                    NotificationManager.IMPORTANCE_HIGH,
                    Color.parseColor("#4CAF50")); // Verde

            // Canal Deporte
            createChannel(manager, CHANNEL_DEPORTE, "Actividades Deportivas",
                    "Recordatorios de actividades deportivas",
                    NotificationManager.IMPORTANCE_HIGH,
                    Color.parseColor("#FF5722")); // Naranja

            // Canal Salud
            createChannel(manager, CHANNEL_SALUD, "Salud y Bienestar",
                    "Recordatorios de citas médicas y salud",
                    NotificationManager.IMPORTANCE_HIGH,
                    Color.parseColor("#2196F3")); // Azul

            // Canal Educación
            createChannel(manager, CHANNEL_EDUCACION, "Actividades Educativas",
                    "Recordatorios de talleres y cursos",
                    NotificationManager.IMPORTANCE_HIGH,
                    Color.parseColor("#9C27B0")); // Morado

            // Canal Cultura
            createChannel(manager, CHANNEL_CULTURA, "Actividades Culturales",
                    "Recordatorios de eventos culturales",
                    NotificationManager.IMPORTANCE_HIGH,
                    Color.parseColor("#FF9800")); // Ámbar

            Log.d(TAG, "✅ Canales de notificación creados");
        }
    }

    /**
     * Crea un canal individual
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void createChannel(NotificationManager manager, String channelId,
                                      String name, String description, int importance, int color) {
        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        channel.setDescription(description);
        channel.enableLights(true);
        channel.setLightColor(color);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 500, 250, 500});
        channel.setShowBadge(true);

        manager.createNotificationChannel(channel);
        Log.d(TAG, "📢 Canal creado: " + channelId);
    }

    /**
     * Obtiene la configuración del canal según el tipo de actividad
     */
    public static NotificationChannelConfig getChannelConfig(String tipoActividadId) {
        if (tipoActividadId == null) {
            return new NotificationChannelConfig(CHANNEL_GENERAL, Color.parseColor("#4CAF50"));
        }

        // Mapear según tus tipos de actividad en Firebase
        switch (tipoActividadId.toLowerCase()) {
            case "deporte":
            case "deportiva":
            case "gimnasia":
            case "futbol":
            case "yoga":
                return new NotificationChannelConfig(CHANNEL_DEPORTE, Color.parseColor("#FF5722"));

            case "salud":
            case "medica":
            case "doctor":
            case "terapia":
            case "consulta":
                return new NotificationChannelConfig(CHANNEL_SALUD, Color.parseColor("#2196F3"));

            case "educacion":
            case "educativa":
            case "taller":
            case "curso":
            case "capacitacion":
                return new NotificationChannelConfig(CHANNEL_EDUCACION, Color.parseColor("#9C27B0"));

            case "cultura":
            case "cultural":
            case "arte":
            case "musica":
            case "teatro":
                return new NotificationChannelConfig(CHANNEL_CULTURA, Color.parseColor("#FF9800"));

            default:
                return new NotificationChannelConfig(CHANNEL_GENERAL, Color.parseColor("#4CAF50"));
        }
    }

    /**
     * Elimina todos los canales (útil para testing)
     */
    public static void deleteAllChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.deleteNotificationChannel(CHANNEL_GENERAL);
                manager.deleteNotificationChannel(CHANNEL_DEPORTE);
                manager.deleteNotificationChannel(CHANNEL_SALUD);
                manager.deleteNotificationChannel(CHANNEL_EDUCACION);
                manager.deleteNotificationChannel(CHANNEL_CULTURA);
                Log.d(TAG, "🗑️ Todos los canales eliminados");
            }
        }
    }
}