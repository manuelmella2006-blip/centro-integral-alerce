package com.example.centrointegralalerce.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

/**
 * Gestor de canales de notificación
 * Crea diferentes canales según el tipo de actividad
 */
public class NotificationChannelManager {
    private static final String TAG = "NotificationChannels";

    // IDs de canales
    public static final String CHANNEL_GENERAL = "canal_general";
    public static final String CHANNEL_TALLERES = "canal_talleres";
    public static final String CHANNEL_CHARLAS = "canal_charlas";
    public static final String CHANNEL_ATENCIONES = "canal_atenciones";
    public static final String CHANNEL_CAPACITACION = "canal_capacitacion";
    public static final String CHANNEL_OPERATIVO = "canal_operativo";
    public static final String CHANNEL_URGENTE = "canal_urgente";

    /**
     * Crea todos los canales de notificación
     * Debe llamarse una vez al iniciar la app
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            if (manager == null) {
                Log.e(TAG, "NotificationManager es null");
                return;
            }

            // Canal General
            crearCanal(manager, CHANNEL_GENERAL,
                    "Notificaciones generales",
                    "Notificaciones generales del centro integral",
                    NotificationManager.IMPORTANCE_DEFAULT,
                    Color.parseColor("#2E7D32"));

            // Canal Talleres
            crearCanal(manager, CHANNEL_TALLERES,
                    "Talleres",
                    "Notificaciones sobre talleres programados",
                    NotificationManager.IMPORTANCE_HIGH,
                    Color.parseColor("#2E7D32"));

            // Canal Charlas
            crearCanal(manager, CHANNEL_CHARLAS,
                    "Charlas",
                    "Notificaciones sobre charlas y conferencias",
                    NotificationManager.IMPORTANCE_DEFAULT,
                    Color.parseColor("#388E3C"));

            // Canal Atenciones
            crearCanal(manager, CHANNEL_ATENCIONES,
                    "Atenciones",
                    "Recordatorios de atenciones médicas o sociales",
                    NotificationManager.IMPORTANCE_HIGH,
                    Color.parseColor("#00796B"));

            // Canal Capacitación
            crearCanal(manager, CHANNEL_CAPACITACION,
                    "Capacitación",
                    "Notificaciones de capacitaciones",
                    NotificationManager.IMPORTANCE_DEFAULT,
                    Color.parseColor("#1B5E20"));

            // Canal Operativo
            crearCanal(manager, CHANNEL_OPERATIVO,
                    "Operativo",
                    "Notificaciones operativas del centro",
                    NotificationManager.IMPORTANCE_LOW,
                    Color.parseColor("#F57C00"));

            // Canal Urgente
            crearCanal(manager, CHANNEL_URGENTE,
                    "Urgente",
                    "Notificaciones urgentes que requieren atención inmediata",
                    NotificationManager.IMPORTANCE_MAX,
                    Color.parseColor("#E64A19"));

            Log.d(TAG, "Todos los canales de notificación creados");
        }
    }

    /**
     * Crea un canal de notificación individual
     */
    private static void crearCanal(NotificationManager manager, String channelId,
                                   String nombre, String descripcion,
                                   int importancia, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, nombre, importancia);
            channel.setDescription(descripcion);
            channel.enableLights(true);
            channel.setLightColor(color);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});

            // Sonido por defecto
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);

            manager.createNotificationChannel(channel);
            Log.d(TAG, "Canal creado: " + channelId);
        }
    }

    /**
     * Obtiene la configuración del canal según el tipo de actividad
     */
    public static NotificationChannelConfig getChannelConfig(String tipoActividad) {
        if (tipoActividad == null) {
            return new NotificationChannelConfig(
                    CHANNEL_GENERAL,
                    NotificationCompat.PRIORITY_DEFAULT,
                    Color.parseColor("#2E7D32")
            );
        }

        switch (tipoActividad.trim()) {
            case "Taller":
                return new NotificationChannelConfig(
                        CHANNEL_TALLERES,
                        NotificationCompat.PRIORITY_HIGH,
                        Color.parseColor("#2E7D32")
                );

            case "Charlas":
                return new NotificationChannelConfig(
                        CHANNEL_CHARLAS,
                        NotificationCompat.PRIORITY_DEFAULT,
                        Color.parseColor("#388E3C")
                );

            case "Atenciones":
                return new NotificationChannelConfig(
                        CHANNEL_ATENCIONES,
                        NotificationCompat.PRIORITY_HIGH,
                        Color.parseColor("#00796B")
                );

            case "Capacitación":
                return new NotificationChannelConfig(
                        CHANNEL_CAPACITACION,
                        NotificationCompat.PRIORITY_DEFAULT,
                        Color.parseColor("#1B5E20")
                );

            case "Operativo":
                return new NotificationChannelConfig(
                        CHANNEL_OPERATIVO,
                        NotificationCompat.PRIORITY_LOW,
                        Color.parseColor("#F57C00")
                );

            case "Diagnóstico":
                return new NotificationChannelConfig(
                        CHANNEL_ATENCIONES,
                        NotificationCompat.PRIORITY_HIGH,
                        Color.parseColor("#E64A19")
                );

            case "Práctica profesional":
                return new NotificationChannelConfig(
                        CHANNEL_CAPACITACION,
                        NotificationCompat.PRIORITY_DEFAULT,
                        Color.parseColor("#5E35B1")
                );

            case "Urgente":
                return new NotificationChannelConfig(
                        CHANNEL_URGENTE,
                        NotificationCompat.PRIORITY_MAX,
                        Color.parseColor("#E64A19")
                );

            default:
                return new NotificationChannelConfig(
                        CHANNEL_GENERAL,
                        NotificationCompat.PRIORITY_DEFAULT,
                        Color.parseColor("#2E7D32")
                );
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
                manager.deleteNotificationChannel(CHANNEL_TALLERES);
                manager.deleteNotificationChannel(CHANNEL_CHARLAS);
                manager.deleteNotificationChannel(CHANNEL_ATENCIONES);
                manager.deleteNotificationChannel(CHANNEL_CAPACITACION);
                manager.deleteNotificationChannel(CHANNEL_OPERATIVO);
                manager.deleteNotificationChannel(CHANNEL_URGENTE);
                Log.d(TAG, "Todos los canales eliminados");
            }
        }
    }
}

/**
 * Configuración de un canal de notificación
 */
class NotificationChannelConfig {
    public final String channelId;
    public final int priority;
    public final int color;

    public NotificationChannelConfig(String channelId, int priority, int color) {
        this.channelId = channelId;
        this.priority = priority;
        this.color = color;
    }
}