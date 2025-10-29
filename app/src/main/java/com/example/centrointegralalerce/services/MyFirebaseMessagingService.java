package com.example.centrointegralalerce.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.ui.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para recibir notificaciones push de Firebase
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Servicio de Firebase Messaging creado");

        // Crear canales de notificación al iniciar el servicio
        NotificationChannelManager.createNotificationChannels(this);
    }

    /**
     * Se llama cuando llega una nueva notificación push
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "Notificación recibida de: " + remoteMessage.getFrom());

        // Verificar si tiene datos
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Datos de notificación: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }

        // Verificar si tiene notificación
        if (remoteMessage.getNotification() != null) {
            String titulo = remoteMessage.getNotification().getTitle();
            String mensaje = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Título: " + titulo);
            Log.d(TAG, "Mensaje: " + mensaje);

            // Obtener tipo de actividad de los datos (si existe)
            String tipoActividad = remoteMessage.getData().get("tipoActividad");
            if (tipoActividad == null) {
                tipoActividad = "general";
            }

            mostrarNotificacion(titulo, mensaje, tipoActividad);
        }
    }

    /**
     * Maneja los datos personalizados de la notificación
     */
    private void handleDataMessage(Map<String, String> data) {
        String tipo = data.get("tipo");
        String tipoActividad = data.get("tipoActividad");
        String citaId = data.get("citaId");
        String titulo = data.get("titulo");
        String mensaje = data.get("mensaje");

        Log.d(TAG, "Tipo: " + tipo);
        Log.d(TAG, "Tipo Actividad: " + tipoActividad);
        Log.d(TAG, "Cita ID: " + citaId);

        // Mostrar notificación con los datos
        if (titulo != null && mensaje != null) {
            mostrarNotificacion(titulo, mensaje, tipoActividad != null ? tipoActividad : "general");
        }
    }

    /**
     * Muestra la notificación al usuario
     */
    private void mostrarNotificacion(String titulo, String mensaje, String tipoActividad) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("tipoActividad", tipoActividad);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Obtener configuración del canal según tipo de actividad
        NotificationChannelConfig config = NotificationChannelManager.getChannelConfig(tipoActividad);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, config.channelId)
                .setSmallIcon(R.drawable.ic_notification) // Crea este icono
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .setPriority(config.priority)
                .setColor(config.color)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje));

        // Agregar acciones según tipo
        agregarAcciones(notificationBuilder, tipoActividad);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // ID único para cada notificación
        int notificationId = (int) System.currentTimeMillis();

        notificationManager.notify(notificationId, notificationBuilder.build());

        Log.d(TAG, "Notificación mostrada - ID: " + notificationId + " Canal: " + config.channelId);
    }

    /**
     * Agrega acciones personalizadas según el tipo de actividad
     */
    private void agregarAcciones(NotificationCompat.Builder builder, String tipoActividad) {
        // Acción "Ver detalles"
        Intent verIntent = new Intent(this, MainActivity.class);
        verIntent.putExtra("accion", "ver_detalle");
        verIntent.putExtra("tipoActividad", tipoActividad);

        PendingIntent verPendingIntent = PendingIntent.getActivity(
                this,
                1,
                verIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        builder.addAction(R.drawable.ic_view, "Ver detalles", verPendingIntent);

        // Acción específica según tipo
        if ("Taller".equals(tipoActividad) || "Capacitación".equals(tipoActividad)) {
            Intent confirmarIntent = new Intent(this, MainActivity.class);
            confirmarIntent.putExtra("accion", "confirmar_asistencia");

            PendingIntent confirmarPendingIntent = PendingIntent.getActivity(
                    this,
                    2,
                    confirmarIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            builder.addAction(R.drawable.ic_check, "Confirmar", confirmarPendingIntent);
        }
    }

    /**
     * Se llama cuando se genera un nuevo token de FCM
     * Este token identifica de forma única al dispositivo
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Nuevo token FCM generado: " + token);

        // Guardar el token en Firebase Firestore
        guardarTokenEnFirestore(token);

        // También puedes guardarlo localmente
        getSharedPreferences("FCM", MODE_PRIVATE)
                .edit()
                .putString("token", token)
                .apply();
    }

    /**
     * Guarda el token de FCM en Firestore asociado al usuario
     */
    private void guardarTokenEnFirestore(String token) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "Usuario no autenticado, no se puede guardar el token");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("fcmToken", token);
        tokenData.put("dispositivo", Build.MODEL);
        tokenData.put("sistemaOperativo", "Android " + Build.VERSION.RELEASE);
        tokenData.put("ultimaActualizacion", System.currentTimeMillis());

        db.collection("usuarios")
                .document(userId)
                .update(tokenData)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Token FCM guardado en Firestore correctamente"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar token en Firestore", e);

                    // Si falla el update, intentar set
                    db.collection("usuarios")
                            .document(userId)
                            .set(tokenData, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(aVoid2 ->
                                    Log.d(TAG, "Token FCM guardado con merge"))
                            .addOnFailureListener(e2 ->
                                    Log.e(TAG, "Error definitivo al guardar token", e2));
                });
    }

    /**
     * Se llama cuando el servicio se destruye
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Servicio de Firebase Messaging destruido");
    }
}