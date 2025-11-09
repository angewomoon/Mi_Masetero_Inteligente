package com.devst.mimaseterointeligente.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.devst.mimaseterointeligente.models.Alert;

import java.util.List;

/**
 * Servicio para gestión de alertas
 *
 * UBICACIÓN: app/src/main/java/com/devst/mimaseterointeligente/services/AlertService.java
 * PROPÓSITO: Procesar alertas y enviar notificaciones
 */
public class AlertService extends Service {

    private static final String TAG = "AlertService";

    private DatabaseHelper databaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AlertService creado");

        databaseHelper = new DatabaseHelper(this);

        // Crear canales de notificación
        NotificationHelper.createNotificationChannels(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "AlertService iniciado");

        // Procesar alertas pendientes
        processUnreadAlerts();

        // El servicio no se reinicia automáticamente si es terminado
        return START_NOT_STICKY;
    }

    /**
     * Procesar alertas no leídas
     */
    private void processUnreadAlerts() {
        // Obtener todas las alertas no leídas
        List<Alert> unreadAlerts = databaseHelper.getUnreadAlerts();

        if (unreadAlerts.isEmpty()) {
            Log.d(TAG, "No hay alertas pendientes");
            stopSelf();
            return;
        }

        Log.d(TAG, "Procesando " + unreadAlerts.size() + " alertas");

        int criticalCount = 0;
        int warningCount = 0;

        // Mostrar notificación para cada alerta
        for (Alert alert : unreadAlerts) {
            NotificationHelper.showAlertNotification(this, alert);

            // Contar por severidad
            if (alert.getSeverity().equals("CRITICAL")) {
                criticalCount++;
            } else if (alert.getSeverity().equals("WARNING")) {
                warningCount++;
            }
        }

        // Si hay múltiples alertas, mostrar notificación de resumen
        if (unreadAlerts.size() > 3) {
            NotificationHelper.showSummaryNotification(this, criticalCount, warningCount);
        }

        // Detener el servicio
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Este servicio no se vincula con otros componentes
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "AlertService destruido");

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}