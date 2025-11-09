package com.devst.mimaseterointeligente.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.devst.mimaseterointeligente.api.ApiConfig;
import com.devst.mimaseterointeligente.api.RetrofitClient;
import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.devst.mimaseterointeligente.models.Alert;
import com.devst.mimaseterointeligente.models.ArduinoResponse;
import com.devst.mimaseterointeligente.models.Plant;
import com.devst.mimaseterointeligente.models.SensorData;
import com.devst.mimaseterointeligente.utils.AlertGenerator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Servicio de monitoreo de sensores en background
 *
 * UBICACIÓN: app/src/main/java/com/devst/mimaseterointeligente/services/SensorMonitorService.java
 * PROPÓSITO: Monitorear sensores periódicamente y generar alertas automáticas
 */
public class SensorMonitorService extends Service {

    private static final String TAG = "SensorMonitor";

    private DatabaseHelper databaseHelper;
    private Handler monitorHandler;
    private Runnable monitorRunnable;
    private boolean isMonitoring = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SensorMonitorService creado");

        databaseHelper = new DatabaseHelper(this);
        monitorHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SensorMonitorService iniciado");

        if (!isMonitoring) {
            startMonitoring();
        }

        // El servicio se reinicia si es terminado por el sistema
        return START_STICKY;
    }

    /**
     * Iniciar monitoreo periódico
     */
    private void startMonitoring() {
        isMonitoring = true;

        monitorRunnable = new Runnable() {
            @Override
            public void run() {
                checkAllConnectedPlants();

                // Programar siguiente ejecución
                monitorHandler.postDelayed(this, ApiConfig.SENSOR_UPDATE_INTERVAL);
            }
        };

        // Iniciar monitoreo inmediatamente
        monitorHandler.post(monitorRunnable);

        Log.d(TAG, "Monitoreo iniciado con intervalo de " +
                (ApiConfig.SENSOR_UPDATE_INTERVAL / 1000) + " segundos");
    }

    /**
     * Detener monitoreo
     */
    private void stopMonitoring() {
        if (monitorHandler != null && monitorRunnable != null) {
            monitorHandler.removeCallbacks(monitorRunnable);
            isMonitoring = false;
            Log.d(TAG, "Monitoreo detenido");
        }
    }

    /**
     * Verificar todas las plantas conectadas
     */
    private void checkAllConnectedPlants() {
        Log.d(TAG, "Verificando plantas conectadas...");

        // Obtener todas las plantas conectadas al masetero
        List<Plant> connectedPlants = databaseHelper.getConnectedPlants();

        if (connectedPlants.isEmpty()) {
            Log.d(TAG, "No hay plantas conectadas al masetero");
            return;
        }

        Log.d(TAG, "Verificando " + connectedPlants.size() + " planta(s) conectada(s)");

        // Por cada planta conectada, obtener datos de sensores
        for (Plant plant : connectedPlants) {
            fetchSensorDataForPlant(plant);
        }
    }

    /**
     * Obtener datos de sensores para una planta específica
     */
    private void fetchSensorDataForPlant(final Plant plant) {
        Log.d(TAG, "Obteniendo datos para planta: " + plant.getName());

        // Llamada a API del Arduino
        RetrofitClient.getArduinoApiService().getSensorData().enqueue(new Callback<ArduinoResponse>() {
            @Override
            public void onResponse(Call<ArduinoResponse> call, Response<ArduinoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArduinoResponse data = response.body();

                    if (data.isValid()) {
                        Log.d(TAG, "Datos válidos recibidos para " + plant.getName());

                        // Guardar datos de sensores
                        saveSensorData(plant.getId(), data);

                        // Generar alertas
                        generateAndSaveAlerts(plant, data);
                    } else {
                        Log.w(TAG, "Datos inválidos para " + plant.getName());
                    }
                } else {
                    Log.e(TAG, "Error en respuesta para " + plant.getName() + ": " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ArduinoResponse> call, Throwable t) {
                Log.e(TAG, "Error al obtener datos para " + plant.getName() + ": " + t.getMessage());
            }
        });
    }

    /**
     * Guardar datos de sensores en base de datos
     */
    private void saveSensorData(int plantId, ArduinoResponse data) {
        SensorData sensorData = data.toSensorData(plantId);
        long result = databaseHelper.insertSensorData(sensorData);

        if (result > 0) {
            Log.d(TAG, "Datos de sensores guardados para planta ID: " + plantId);
        } else {
            Log.e(TAG, "Error al guardar datos de sensores para planta ID: " + plantId);
        }
    }

    /**
     * Generar y guardar alertas
     */
    private void generateAndSaveAlerts(Plant plant, ArduinoResponse data) {
        List<Alert> alerts = AlertGenerator.generateAlerts(plant, data);

        if (alerts.isEmpty()) {
            Log.d(TAG, "No se generaron alertas para " + plant.getName());
            return;
        }

        Log.d(TAG, "Generadas " + alerts.size() + " alerta(s) para " + plant.getName());

        boolean hasNewAlerts = false;

        for (Alert alert : alerts) {
            long result = databaseHelper.createAlert(alert);
            if (result > 0) {
                Log.d(TAG, "Alerta guardada: " + alert.getTitle());
                hasNewAlerts = true;
            }
        }

        // Si hay nuevas alertas, iniciar servicio de notificaciones
        if (hasNewAlerts) {
            Intent alertServiceIntent = new Intent(this, AlertService.class);
            startService(alertServiceIntent);
        }
    }

    /**
     * Obtener estadísticas del monitoreo
     */
    public int getMonitoringInterval() {
        return (int) (ApiConfig.SENSOR_UPDATE_INTERVAL / 1000);
    }

    public boolean isCurrentlyMonitoring() {
        return isMonitoring;
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
        Log.d(TAG, "SensorMonitorService destruido");

        stopMonitoring();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}