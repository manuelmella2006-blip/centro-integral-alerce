package com.example.centrointegralalerce.services;

/**
 * Clase de configuración del canal
 */
public class NotificationChannelConfig {

    public String channelId;
    public String channelName;
    public int importance;   // Para NotificationChannel (Android 8+)
    public int priority;     // Para NotificationCompat.Builder
    public int color;

    public NotificationChannelConfig(String channelId,
                                     String channelName,
                                     int importance,
                                     int priority,
                                     int color) {

        this.channelId = channelId;
        this.channelName = channelName;
        this.importance = importance;
        this.priority = priority;
        this.color = color;
    }
    public NotificationChannelConfig(String channelId, int color) {
        this.channelId = channelId;
        this.color = color;
    }
}