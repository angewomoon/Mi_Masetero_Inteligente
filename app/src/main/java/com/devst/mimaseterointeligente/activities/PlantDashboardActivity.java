package com.devst.mimaseterointeligente.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.api.RetrofitClient;
import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.devst.mimaseterointeligente.models.Alert;
import com.devst.mimaseterointeligente.models.ArduinoResponse;
import com.devst.mimaseterointeligente.models.Plant;
import com.devst.mimaseterointeligente.models.SensorData;
import com.devst.mimaseterointeligente.utils.AlertGenerator;
import com.devst.mimaseterointeligente.utils.SensorStatusHelper;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dashboard principal de monitoreo de planta
 *
 * UBICACIÓN: app/src/main/java/com/devst/mimaseterointeligente/activities/PlantDashboardActivity.java
 * PROPÓSITO: Mostrar datos en tiempo real de sensores y estado de la planta
 */
public class PlantDashboardActivity extends AppCompatActivity {

    private static final String TAG = "PlantDashboard";
    private static final long UPDATE_INTERVAL = 30000; // 30 segundos

    // Views
    private TextView tvPlantTitle, tvPlantName, tvScientificName;
    private TextView tvSoilHumidity, tvSoilHumidityStatus;
    private TextView tvTemperature, tvTemperatureStatus;
    private TextView tvAmbientHumidity, tvAmbientHumidityStatus;
    private TextView tvUvLevel, tvUvLevelStatus;
    private TextView tvWaterLevel, tvWaterLevelStatus;
    private TextView tvPestCount, tvPestStatus;
    private TextView tvLastUpdate;
    private ImageView ivPlantImage;
    private ImageButton btnBack, btnEdit;

    // Data
    private Plant plant;
    private int plantId;
    private DatabaseHelper databaseHelper;

    // Auto-update handler
    private Handler updateHandler;
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_dashboard);

        // Inicializar base de datos
        databaseHelper = new DatabaseHelper(this);

        // Obtener ID de la planta
        plantId = getIntent().getIntExtra("plant_id", -1);
        if (plantId == -1) {
            Toast.makeText(this, "Error: Planta no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cargar planta
        plant = databaseHelper.getPlantById(plantId);
        if (plant == null) {
            Toast.makeText(this, "Error: No se pudo cargar la planta", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar views
        initViews();

        // Configurar botones
        setupButtons();

        // Mostrar información de la planta
        displayPlantInfo();

        // Cargar datos iniciales
        loadSensorData();

        // Iniciar actualización automática
        startAutoUpdate();
    }

    /**
     * Inicializar todas las views
     */
    private void initViews() {
        tvPlantTitle = findViewById(R.id.tvPlantTitle);
        tvPlantName = findViewById(R.id.tvPlantName);
        tvScientificName = findViewById(R.id.tvScientificName);
        ivPlantImage = findViewById(R.id.ivPlantImage);

        tvSoilHumidity = findViewById(R.id.tvSoilHumidity);
        tvSoilHumidityStatus = findViewById(R.id.tvSoilHumidityStatus);

        tvTemperature = findViewById(R.id.tvTemperature);
        tvTemperatureStatus = findViewById(R.id.tvTemperatureStatus);

        tvAmbientHumidity = findViewById(R.id.tvAmbientHumidity);
        tvAmbientHumidityStatus = findViewById(R.id.tvAmbientHumidityStatus);

        tvUvLevel = findViewById(R.id.tvUvLevel);
        tvUvLevelStatus = findViewById(R.id.tvUvLevelStatus);

        tvWaterLevel = findViewById(R.id.tvWaterLevel);
        tvWaterLevelStatus = findViewById(R.id.tvWaterLevelStatus);

        tvPestCount = findViewById(R.id.tvPestCount);
        tvPestStatus = findViewById(R.id.tvPestStatus);

        tvLastUpdate = findViewById(R.id.tvLastUpdate);

        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
    }

    /**
     * Configurar botones
     */
    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(PlantDashboardActivity.this, EditPlantActivity.class);
            intent.putExtra("plant_id", plantId);
            startActivity(intent);
        });
    }

    /**
     * Mostrar información básica de la planta
     */
    private void displayPlantInfo() {
        tvPlantTitle.setText(plant.getName());
        tvPlantName.setText(plant.getName());
        tvScientificName.setText(plant.getScientificName());

        // TODO: Cargar imagen de la planta usando Glide
        // if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
        //     Glide.with(this).load(plant.getImageUrl()).into(ivPlantImage);
        // }
    }

    /**
     * Cargar datos de sensores desde Arduino
     */
    private void loadSensorData() {
        Log.d(TAG, "Cargando datos de sensores...");

        // Verificar si la planta está conectada al masetero
        if (!plant.isConnected()) {
            Toast.makeText(this, "Esta planta no está conectada al masetero inteligente", Toast.LENGTH_SHORT).show();
            return;
        }

        // Llamada a la API del Arduino
        RetrofitClient.getArduinoApiService().getSensorData().enqueue(new Callback<ArduinoResponse>() {
            @Override
            public void onResponse(Call<ArduinoResponse> call, Response<ArduinoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArduinoResponse data = response.body();
                    Log.d(TAG, "Datos recibidos: " + data.toString());

                    // Verificar validez de datos
                    if (data.isValid()) {
                        // Guardar en base de datos
                        saveSensorData(data);

                        // Mostrar datos en UI
                        displaySensorData(data);

                        // Generar y guardar alertas
                        generateAlerts(data);

                        // Actualizar timestamp
                        updateLastUpdateTime();
                    } else {
                        Log.e(TAG, "Datos de sensores inválidos");
                        Toast.makeText(PlantDashboardActivity.this, "Datos inválidos del sensor", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Error en respuesta: " + response.code());
                    Toast.makeText(PlantDashboardActivity.this, "Error al obtener datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArduinoResponse> call, Throwable t) {
                Log.e(TAG, "Error de conexión: " + t.getMessage());
                Toast.makeText(PlantDashboardActivity.this,
                        "No se pudo conectar al masetero. Verifica la conexión.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Guardar datos de sensores en base de datos
     */
    private void saveSensorData(ArduinoResponse data) {
        SensorData sensorData = data.toSensorData(plantId);
        long result = databaseHelper.insertSensorData(sensorData); // <--- corregido

        if (result > 0) {
            Log.d(TAG, "Datos de sensores guardados en BD");
        } else {
            Log.e(TAG, "Error al guardar datos de sensores");
        }
    }

    /**
     * Mostrar datos de sensores en UI
     */
    private void displaySensorData(ArduinoResponse data) {
        // Humedad del suelo
        SensorStatusHelper.SensorStatus soilStatus =
                SensorStatusHelper.evaluateSoilHumidity(plant, data.getSoilHumidity());
        tvSoilHumidity.setText(String.format(Locale.getDefault(), "%.0f%%", data.getSoilHumidity()));
        tvSoilHumidityStatus.setText(soilStatus.getMessage());
        tvSoilHumidity.setTextColor(soilStatus.getColor());

        // Temperatura
        SensorStatusHelper.SensorStatus tempStatus =
                SensorStatusHelper.evaluateTemperature(plant, data.getTemperature());
        tvTemperature.setText(String.format(Locale.getDefault(), "%.1f°C", data.getTemperature()));
        tvTemperatureStatus.setText(tempStatus.getMessage());
        tvTemperature.setTextColor(tempStatus.getColor());

        // Humedad ambiental
        SensorStatusHelper.SensorStatus ambHumStatus =
                SensorStatusHelper.evaluateAmbientHumidity(plant, data.getAmbientHumidity());
        tvAmbientHumidity.setText(String.format(Locale.getDefault(), "%.0f%%", data.getAmbientHumidity()));
        tvAmbientHumidityStatus.setText(ambHumStatus.getMessage());
        tvAmbientHumidity.setTextColor(ambHumStatus.getColor());

        // Nivel UV
        SensorStatusHelper.SensorStatus uvStatus =
                SensorStatusHelper.evaluateUvLevel(data.getUvLevel());
        tvUvLevel.setText(String.format(Locale.getDefault(), "%.1f", data.getUvLevel()));
        tvUvLevelStatus.setText(SensorStatusHelper.getUvLevelDescription(data.getUvLevel()));
        tvUvLevel.setTextColor(uvStatus.getColor());

        // Nivel de agua
        SensorStatusHelper.SensorStatus waterStatus =
                SensorStatusHelper.evaluateWaterLevel(data.getWaterLevel());
        tvWaterLevel.setText(String.format(Locale.getDefault(), "%.0f%%", data.getWaterLevel()));
        tvWaterLevelStatus.setText(waterStatus.getMessage());
        tvWaterLevel.setTextColor(waterStatus.getColor());

        // Plagas
        SensorStatusHelper.SensorStatus pestStatus =
                SensorStatusHelper.evaluatePests(data.getPestCount());
        tvPestCount.setText(String.valueOf(data.getPestCount()));
        tvPestStatus.setText(pestStatus.getMessage());
        tvPestCount.setTextColor(pestStatus.getColor());
    }

    /**
     * Generar y guardar alertas
     */
    private void generateAlerts(ArduinoResponse data) {
        List<Alert> alerts = AlertGenerator.generateAlerts(plant, data);

        for (Alert alert : alerts) {
            long result = databaseHelper.createAlert(alert);// ← corregido
            if (result > 0) {
                Log.d(TAG, "Alerta guardada: " + alert.getTitle());
            } else {
                Log.e(TAG, "Error al guardar alerta: " + alert.getTitle());
            }
        }
        // Mostrar notificación si hay alertas críticas
        if (AlertGenerator.hasCriticalAlerts(alerts)) {
            Toast.makeText(this, "¡Atención! Tu planta necesita cuidados urgentes", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Actualizar timestamp de última actualización
     */
    private void updateLastUpdateTime() {
        String timeStr = "Última actualización: Ahora";
        tvLastUpdate.setText(timeStr);
    }

    /**
     * Iniciar actualización automática de datos
     */
    private void startAutoUpdate() {
        updateHandler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                loadSensorData();
                updateHandler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
        updateHandler.postDelayed(updateRunnable, UPDATE_INTERVAL);
    }

    /**
     * Detener actualización automática
     */
    private void stopAutoUpdate() {
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar información de la planta
        plant = databaseHelper.getPlantById(plantId);
        displayPlantInfo();
        loadSensorData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Detener actualización automática cuando la actividad no está visible
        stopAutoUpdate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}