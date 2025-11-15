package com.example.centrointegralalerce.utils;

import android.content.Context;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.centrointegralalerce.data.Cita;
import com.example.centrointegralalerce.services.CitaReminderWorker;
import com.example.centrointegralalerce.ui.ConfiguracionFragment;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Programador de notificaciones basado en tu clase Cita real
 */
public class NotificationScheduler {

    private static final String TAG = "NotificationScheduler";
    private static final String WORK_TAG_PREFIX = "cita_reminder_";

    private final Context context;

    public NotificationScheduler(Context context) {
        this.context = context;
    }

    /**
     * Genera un ID único para la cita usando su fecha, hora y lugarId
     */
    private String generateUniqueId(Cita cita) {
        return "cita_" + cita.getFecha().getTime() + "_" +
                cita.getHora() + "_" +
                cita.getLugarId();
    }

    /**
     * Convierte la fecha+hora de la cita a Calendar
     */
    private Calendar toCalendar(Cita cita) {
        if (cita.getFecha() == null || cita.getHora() == null) return null;

        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(cita.getFecha());

            // Parsear hora (HH:mm)
            String[] parts = cita.getHora().split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);

            return cal;

        } catch (Exception e) {
            Log.e(TAG, "Error convirtiendo fecha/hora", e);
            return null;
        }
    }

    /**
     * Programa una notificación de recordatorio
     */
    public void scheduleNotification(Cita cita) {

        if (!ConfiguracionFragment.areNotificationsEnabled(context)) {
            Log.d(TAG, "🔕 Notificaciones deshabilitadas");
            return;
        }

        Calendar citaCal = toCalendar(cita);
        if (citaCal == null) {
            Log.w(TAG, "❌ Cita inválida para notificación");
            return;
        }

        int diasAviso = ConfiguracionFragment.getDiasAviso(context);

        Calendar reminderTime = (Calendar) citaCal.clone();
        reminderTime.add(Calendar.DAY_OF_MONTH, -diasAviso);

        reminderTime.set(Calendar.HOUR_OF_DAY, 9);
        reminderTime.set(Calendar.MINUTE, 0);
        reminderTime.set(Calendar.SECOND, 0);

        long delayMillis = reminderTime.getTimeInMillis() - System.currentTimeMillis();

        if (delayMillis <= 0) {
            Log.d(TAG, "⏩ Aviso ya pasó, no se programa");
            return;
        }

        scheduleWithWorkManager(cita, delayMillis);
    }

    private void scheduleWithWorkManager(Cita cita, long delayMillis) {

        String idUnico = generateUniqueId(cita);

        Data inputData = new Data.Builder()
                .putString("citaId", idUnico)
                .putString("titulo", cita.getActividadNombre())
                .putString("lugar", cita.getLugarId())
                .putString("hora", cita.getHora())
                .putString("tipo", cita.getTipoActividadId())
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(CitaReminderWorker.class)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(WORK_TAG_PREFIX + idUnico)
                .build();

        WorkManager.getInstance(context).enqueue(work);

        Log.d(TAG, "🔔 Notificación programada: " + idUnico);
    }

    /**
     * Cancelar una notificación
     */
    public void cancelNotification(Cita cita) {
        String idUnico = generateUniqueId(cita);

        WorkManager.getInstance(context)
                .cancelAllWorkByTag(WORK_TAG_PREFIX + idUnico);

        Log.d(TAG, "❌ Notificación cancelada para: " + idUnico);
    }

    /**
     * Cancelar todas las notificaciones
     */
    public void cancelAllNotifications() {
        WorkManager.getInstance(context).cancelAllWork();
        Log.d(TAG, "🗑️ Todas las notificaciones canceladas");
    }

    /**
     * Reprogramar todas las citas futuras
     */
    public void rescheduleAllNotifications(List<Cita> citas) {
        cancelAllNotifications();

        for (Cita cita : citas) {
            Calendar cal = toCalendar(cita);
            if (cal != null && cal.after(Calendar.getInstance())) {
                scheduleNotification(cita);
            }
        }

        Log.d(TAG, "🔄 Reprogramación completada: " + citas.size());
    }
}
