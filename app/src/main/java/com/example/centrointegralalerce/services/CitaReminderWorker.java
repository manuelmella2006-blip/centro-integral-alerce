package com.example.centrointegralalerce.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.ui.MainActivity;

/**
 * Worker que ejecuta las notificaciones programadas de recordatorio de citas
 */
public class CitaReminderWorker extends Worker {
    private static final String TAG = "CitaReminderWorker";

    public CitaReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Obtener datos de la cita
            String citaId = getInputData().getString("citaId");
            String titulo = getInputData().getString("titulo");
            String lugar = getInputData().getString("lugar");
            String hora = getInputData().getString("hora");
            String tipo = getInputData().getString("tipo");

            Log.d(TAG, "Ejecutando recordatorio para: " + titulo);

            // Mostrar notificación
            mostrarNotificacion(citaId, titulo, lugar, hora, tipo);

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar recordatorio", e);
            return Result.failure();
        }
    }

    /**
     * Muestra la notificación de recordatorio
     */
    private void mostrarNotificacion(String citaId, String titulo, String lugar,
                                     String hora, String tipo) {
        Context context = getApplicationContext();

        // Intent para abrir la app
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("citaId", citaId);
        intent.putExtra("openCalendar", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                citaId.hashCode(),
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Configuración del canal según tipo
        NotificationChannelConfig config = NotificationChannelManager.getChannelConfig(tipo);

        // Construir mensaje
        String mensaje = "📍 " + lugar + "\n🕐 " + hora;

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Crear notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, config.channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("🔔 Recordatorio: " + titulo)
                .setContentText(mensaje)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(mensaje + "\n\nNo olvides confirmar tu asistencia"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setVibrate(new long[]{0, 500, 250, 500})
                .setColor(config.color)
                .setContentIntent(pendingIntent);

        // Agregar acciones
        agregarAcciones(builder, citaId);

        // Mostrar notificación
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(citaId.hashCode(), builder.build());
            Log.d(TAG, "✅ Notificación de recordatorio mostrada para: " + titulo);
        }
    }

    /**
     * Agrega acciones a la notificación
     */
    private void agregarAcciones(NotificationCompat.Builder builder, String citaId) {
        Context context = getApplicationContext();

        // Acción: Ver en calendario
        Intent verIntent = new Intent(context, MainActivity.class);
        verIntent.putExtra("citaId", citaId);
        verIntent.putExtra("openCalendar", true);

        PendingIntent verPendingIntent = PendingIntent.getActivity(
                context,
                (citaId + "_ver").hashCode(),
                verIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        builder.addAction(R.drawable.ic_view, "Ver detalles", verPendingIntent);

        // Acción: Confirmar asistencia
        Intent confirmarIntent = new Intent(context, MainActivity.class);
        confirmarIntent.putExtra("citaId", citaId);
        confirmarIntent.putExtra("accion", "confirmar");

        PendingIntent confirmarPendingIntent = PendingIntent.getActivity(
                context,
                (citaId + "_confirmar").hashCode(),
                confirmarIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        builder.addAction(R.drawable.ic_check, "Confirmar", confirmarPendingIntent);
    }
}