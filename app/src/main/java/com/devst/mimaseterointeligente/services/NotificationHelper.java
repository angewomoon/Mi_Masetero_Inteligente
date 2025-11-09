package com.devst.mimaseterointeligente.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.activities.PlantDashboardActivity;
import com.devst.mimaseterointeligente.models.Alert;

/**
 * Helper para gestión de notificaciones push
 *
 * UBICACIÓN: app/src/main/java/com/devst/mimaseterointeligente/services/NotificationHelper.java
 * PROPÓSITO: Crear y mostrar notificaciones del sistema para alertas de plantas
 */
public class NotificationHelper {

    // IDs de canales de notificación
    private static final String CHANNEL_ID_CRITICAL = "masetero_critical";
    private static final String CHANNEL_ID_WARNING = "masetero_warning";
    private static final String CHANNEL_ID_INFO = "masetero_info";

    // Nombres de canales
    private static final String CHANNEL_NAME_CRITICAL = "Alertas Críticas";
    private static final String CHANNEL_NAME_WARNING = "Advertencias";
    private static final String CHANNEL_NAME_INFO = "Información";

    // Descripciones
    private static final String CHANNEL_DESC_CRITICAL = "Notificaciones para problemas urgentes de tus plantas";
    private static final String CHANNEL_DESC_WARNING = "Advertencias sobre el estado de tus plantas";
    private static final String CHANNEL_DESC_INFO = "Información general sobre tus plantas";

    /**
     * Crear canales de notificación (Android 8.0+)
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            // Canal para alertas críticas
            NotificationChannel channelCritical = new NotificationChannel(
                    CHANNEL_ID_CRITICAL,
                    CHANNEL_NAME_CRITICAL,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelCritical.setDescription(CHANNEL_DESC_CRITICAL);
            channelCritical.enableVibration(true);
            channelCritical.setVibrationPattern(new long[]{0, 500, 200, 500});
            manager.createNotificationChannel(channelCritical);

            // Canal para advertencias
            NotificationChannel channelWarning = new NotificationChannel(
                    CHANNEL_ID_WARNING,
                    CHANNEL_NAME_WARNING,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channelWarning.setDescription(CHANNEL_DESC_WARNING);
            channelWarning.enableVibration(true);
            manager.createNotificationChannel(channelWarning);

            // Canal para información
            NotificationChannel channelInfo = new NotificationChannel(
                    CHANNEL_ID_INFO,
                    CHANNEL_NAME_INFO,
                    NotificationManager.IMPORTANCE_LOW
            );
            channelInfo.setDescription(CHANNEL_DESC_INFO);
            manager.createNotificationChannel(channelInfo);
        }
    }

    /**
     * Mostrar notificación para una alerta
     */
    public static void showAlertNotification(Context context, Alert alert) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) return;

        // Determinar canal según severidad
        String channelId = getChannelIdForSeverity(alert.getSeverity());

        // Crear intent para abrir el dashboard al tocar la notificación
        Intent intent = new Intent(context, PlantDashboardActivity.class);
        intent.putExtra("plant_id", alert.getPlantId());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                alert.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Construir notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(getIconForAlertType(alert.getAlertType()))
                .setContentTitle(alert.getTitle())
                .setContentText(alert.getMessage())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(alert.getMessage()))
                .setPriority(getPriorityForSeverity(alert.getSeverity()))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(getColorForSeverity(alert.getSeverity()));

        // Vibración para alertas críticas
        if (alert.getSeverity().equals("CRITICAL")) {
            builder.setVibrate(new long[]{0, 500, 200, 500});
        }

        // Mostrar notificación
        manager.notify(alert.getId(), builder.build());
    }

    /**
     * Mostrar notificación de resumen (múltiples alertas)
     */
    public static void showSummaryNotification(Context context, int criticalCount, int warningCount) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) return;

        String title = "Alertas de tus plantas";
        String message;

        if (criticalCount > 0 && warningCount > 0) {
            message = String.format("%d alertas críticas y %d advertencias",
                    criticalCount, warningCount);
        } else if (criticalCount > 0) {
            message = String.format("%d alertas críticas", criticalCount);
        } else {
            message = String.format("%d advertencias", warningCount);
        }

        Intent intent = new Intent(context, PlantDashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_CRITICAL)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        manager.notify(9999, builder.build());
    }

    /**
     * Cancelar notificación específica
     */
    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(notificationId);
        }
    }

    /**
     * Cancelar todas las notificaciones
     */
    public static void cancelAllNotifications(Context context) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancelAll();
        }
    }

    /**
     * Obtener canal ID según severidad
     */
    private static String getChannelIdForSeverity(String severity) {
        switch (severity) {
            case "CRITICAL":
                return CHANNEL_ID_CRITICAL;
            case "WARNING":
                return CHANNEL_ID_WARNING;
            default:
                return CHANNEL_ID_INFO;
        }
    }

    /**
     * Obtener prioridad según severidad
     */
    private static int getPriorityForSeverity(String severity) {
        switch (severity) {
            case "CRITICAL":
                return NotificationCompat.PRIORITY_HIGH;
            case "WARNING":
                return NotificationCompat.PRIORITY_DEFAULT;
            default:
                return NotificationCompat.PRIORITY_LOW;
        }
    }

    /**
     * Obtener color según severidad
     */
    private static int getColorForSeverity(String severity) {
        switch (severity) {
            case "CRITICAL":
                return 0xFFF44336; // Rojo
            case "WARNING":
                return 0xFFFFC107; // Amarillo
            default:
                return 0xFF4CAF50; // Verde
        }
    }

    /**
     * Obtener ícono según tipo de alerta
     */
    private static int getIconForAlertType(String alertType) {
        // Nota: Reemplazar con íconos personalizados de tu proyecto
        switch (alertType) {
            case "WATERING":
                return android.R.drawable.ic_dialog_info; // Ícono de agua
            case "TEMPERATURE":
                return android.R.drawable.ic_dialog_alert; // Ícono de temperatura
            case "HUMIDITY":
                return android.R.drawable.ic_dialog_info; // Ícono de humedad
            case "LIGHT":
                return android.R.drawable.ic_dialog_info; // Ícono de luz
            case "PEST":
                return android.R.drawable.ic_dialog_alert; // Ícono de plaga
            case "WATER_LEVEL":
                return android.R.drawable.ic_dialog_info; // Ícono de nivel de agua
            default:
                return android.R.drawable.ic_dialog_info;
        }
    }
}