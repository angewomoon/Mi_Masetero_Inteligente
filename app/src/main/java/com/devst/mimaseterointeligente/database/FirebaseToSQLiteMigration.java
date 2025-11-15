package com.devst.mimaseterointeligente.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.devst.mimaseterointeligente.models.Alert;
import com.devst.mimaseterointeligente.models.Plant;
import com.devst.mimaseterointeligente.models.SensorData;
import com.devst.mimaseterointeligente.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase para importar datos desde Firebase Realtime Database hacia SQLite local.
 * Incluye callbacks de progreso, manejo de errores, y verificación de duplicados.
 */
public class FirebaseToSQLiteMigration {

    private static final String TAG = "FirebaseToSQLite";
    private static final int TIMEOUT_SECONDS = 30; // Timeout para operaciones de Firebase

    private DatabaseHelper dbHelper;
    private DatabaseReference firebaseRef;
    private Context context;

    // Callbacks
    private MigrationCallback callback;

    /**
     * Constructor
     * @param context Contexto de la aplicación
     */
    public FirebaseToSQLiteMigration(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.firebaseRef = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Interface para callbacks de migración
     */
    public interface MigrationCallback {
        void onProgress(String tableName, int current, int total);
        void onTableComplete(String tableName, int recordsMigrated, int errors);
        void onComplete(int totalRecords, int totalErrors);
        void onError(String tableName, String error);
    }

    /**
     * Establece el callback de migración
     */
    public void setCallback(MigrationCallback callback) {
        this.callback = callback;
    }

    /**
     * Importar todas las tablas desde Firebase a SQLite
     */
    public void importAllTables() {
        Log.d(TAG, "Iniciando importación completa desde Firebase a SQLite");

        new Thread(() -> {
            AtomicInteger totalRecords = new AtomicInteger(0);
            AtomicInteger totalErrors = new AtomicInteger(0);

            // Importar usuarios primero (por Foreign Keys)
            MigrationResult usersResult = importUsers();
            totalRecords.addAndGet(usersResult.successCount);
            totalErrors.addAndGet(usersResult.errorCount);

            // Importar plantas (dependen de users)
            MigrationResult plantsResult = importPlants();
            totalRecords.addAndGet(plantsResult.successCount);
            totalErrors.addAndGet(plantsResult.errorCount);

            // Importar datos de sensores (dependen de plants)
            MigrationResult sensorsResult = importSensorData();
            totalRecords.addAndGet(sensorsResult.successCount);
            totalErrors.addAndGet(sensorsResult.errorCount);

            // Importar alertas (dependen de plants)
            MigrationResult alertsResult = importAlerts();
            totalRecords.addAndGet(alertsResult.successCount);
            totalErrors.addAndGet(alertsResult.errorCount);

            // Callback final
            if (callback != null) {
                callback.onComplete(totalRecords.get(), totalErrors.get());
            }

            Log.d(TAG, "Importación completa finalizada: " + totalRecords.get() +
                  " registros, " + totalErrors.get() + " errores");
        }).start();
    }

    /**
     * Importar usuarios desde Firebase
     */
    public MigrationResult importUsers() {
        Log.d(TAG, "Importando usuarios desde Firebase");
        MigrationResult result = new MigrationResult();
        CountDownLatch latch = new CountDownLatch(1);

        firebaseRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    int totalRecords = (int) snapshot.getChildrenCount();
                    int currentRecord = 0;

                    Log.d(TAG, "Usuarios encontrados en Firebase: " + totalRecords);

                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        try {
                            // Extraer datos del usuario
                            User user = new User();

                            // ID
                            if (userSnapshot.hasChild("id")) {
                                user.setId(userSnapshot.child("id").getValue(Integer.class));
                            }

                            // Campos requeridos
                            if (userSnapshot.hasChild("name")) {
                                user.setName(userSnapshot.child("name").getValue(String.class));
                            }
                            if (userSnapshot.hasChild("email")) {
                                user.setEmail(userSnapshot.child("email").getValue(String.class));
                            }
                            if (userSnapshot.hasChild("password")) {
                                user.setPassword(userSnapshot.child("password").getValue(String.class));
                            }

                            // Campos opcionales
                            if (userSnapshot.hasChild("profile_image")) {
                                user.setProfileImage(userSnapshot.child("profile_image").getValue(String.class));
                            }
                            if (userSnapshot.hasChild("google_id")) {
                                user.setGoogleId(userSnapshot.child("google_id").getValue(String.class));
                            }
                            if (userSnapshot.hasChild("created_at")) {
                                user.setCreatedAt(userSnapshot.child("created_at").getValue(String.class));
                            }
                            if (userSnapshot.hasChild("updated_at")) {
                                user.setUpdatedAt(userSnapshot.child("updated_at").getValue(String.class));
                            }

                            // Verificar si el usuario ya existe
                            User existingUser = dbHelper.getUserByEmail(user.getEmail());

                            if (existingUser == null) {
                                // Insertar nuevo usuario
                                long userId = dbHelper.createUser(user);
                                if (userId > 0) {
                                    result.successCount++;
                                    Log.d(TAG, "Usuario insertado: " + user.getEmail());
                                } else {
                                    result.errorCount++;
                                }
                            } else {
                                // Actualizar usuario existente
                                user.setId(existingUser.getId());
                                int updated = dbHelper.updateUser(user);
                                if (updated > 0) {
                                    result.successCount++;
                                    Log.d(TAG, "Usuario actualizado: " + user.getEmail());
                                } else {
                                    result.errorCount++;
                                }
                            }

                            currentRecord++;
                            if (callback != null && currentRecord % 5 == 0) {
                                final int current = currentRecord;
                                callback.onProgress("users", current, totalRecords);
                            }

                        } catch (Exception e) {
                            result.errorCount++;
                            Log.e(TAG, "Error al importar usuario: " + e.getMessage());
                            if (callback != null) {
                                callback.onError("users", "Error en registro: " + e.getMessage());
                            }
                        }
                    }

                    if (callback != null) {
                        callback.onTableComplete("users", result.successCount, result.errorCount);
                    }

                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al leer usuarios de Firebase: " + error.getMessage());
                if (callback != null) {
                    callback.onError("users", "Error de Firebase: " + error.getMessage());
                }
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Timeout al importar usuarios");
        }

        return result;
    }

    /**
     * Importar plantas desde Firebase
     */
    public MigrationResult importPlants() {
        Log.d(TAG, "Importando plantas desde Firebase");
        MigrationResult result = new MigrationResult();
        CountDownLatch latch = new CountDownLatch(1);

        firebaseRef.child("plants").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    int totalRecords = (int) snapshot.getChildrenCount();
                    int currentRecord = 0;

                    Log.d(TAG, "Plantas encontradas en Firebase: " + totalRecords);

                    for (DataSnapshot plantSnapshot : snapshot.getChildren()) {
                        try {
                            Plant plant = new Plant();

                            // Mapear campos
                            if (plantSnapshot.hasChild("id")) {
                                plant.setId(plantSnapshot.child("id").getValue(Integer.class));
                            }
                            if (plantSnapshot.hasChild("user_id")) {
                                plant.setUserId(plantSnapshot.child("user_id").getValue(Integer.class));
                            }
                            if (plantSnapshot.hasChild("plant_name")) {
                                plant.setName(plantSnapshot.child("plant_name").getValue(String.class));
                            }
                            if (plantSnapshot.hasChild("type")) {
                                plant.setType(plantSnapshot.child("type").getValue(String.class));
                            }
                            if (plantSnapshot.hasChild("species")) {
                                plant.setSpecies(plantSnapshot.child("species").getValue(String.class));
                            }
                            if (plantSnapshot.hasChild("scientific_name")) {
                                plant.setScientificName(plantSnapshot.child("scientific_name").getValue(String.class));
                            }
                            if (plantSnapshot.hasChild("image_url")) {
                                plant.setImageUrl(plantSnapshot.child("image_url").getValue(String.class));
                            }
                            if (plantSnapshot.hasChild("is_connected")) {
                                Long isConnected = plantSnapshot.child("is_connected").getValue(Long.class);
                                plant.setConnected(isConnected != null && isConnected == 1);
                            }

                            // Parámetros óptimos
                            if (plantSnapshot.hasChild("optimal_soil_hum_min")) {
                                plant.setOptimalSoilHumidityMin(plantSnapshot.child("optimal_soil_hum_min").getValue(Float.class));
                            }
                            if (plantSnapshot.hasChild("optimal_soil_hum_max")) {
                                plant.setOptimalSoilHumidityMax(plantSnapshot.child("optimal_soil_hum_max").getValue(Float.class));
                            }
                            if (plantSnapshot.hasChild("optimal_temp_min")) {
                                plant.setOptimalTempMin(plantSnapshot.child("optimal_temp_min").getValue(Float.class));
                            }
                            if (plantSnapshot.hasChild("optimal_temp_max")) {
                                plant.setOptimalTempMax(plantSnapshot.child("optimal_temp_max").getValue(Float.class));
                            }
                            if (plantSnapshot.hasChild("optimal_amb_hum_min")) {
                                plant.setOptimalAmbientHumidityMin(plantSnapshot.child("optimal_amb_hum_min").getValue(Float.class));
                            }
                            if (plantSnapshot.hasChild("optimal_amb_hum_max")) {
                                plant.setOptimalAmbientHumidityMax(plantSnapshot.child("optimal_amb_hum_max").getValue(Float.class));
                            }
                            if (plantSnapshot.hasChild("optimal_light")) {
                                plant.setOptimalLightLevel(plantSnapshot.child("optimal_light").getValue(String.class));
                            }

                            // Verificar si existe por ID
                            Plant existingPlant = dbHelper.getPlantById(plant.getId());

                            if (existingPlant == null) {
                                long plantId = dbHelper.createPlant(plant);
                                if (plantId > 0) {
                                    result.successCount++;
                                    Log.d(TAG, "Planta insertada: " + plant.getName());
                                } else {
                                    result.errorCount++;
                                }
                            } else {
                                int updated = dbHelper.updatePlant(plant);
                                if (updated > 0) {
                                    result.successCount++;
                                    Log.d(TAG, "Planta actualizada: " + plant.getName());
                                } else {
                                    result.errorCount++;
                                }
                            }

                            currentRecord++;
                            if (callback != null && currentRecord % 5 == 0) {
                                final int current = currentRecord;
                                callback.onProgress("plants", current, totalRecords);
                            }

                        } catch (Exception e) {
                            result.errorCount++;
                            Log.e(TAG, "Error al importar planta: " + e.getMessage());
                            if (callback != null) {
                                callback.onError("plants", "Error en registro: " + e.getMessage());
                            }
                        }
                    }

                    if (callback != null) {
                        callback.onTableComplete("plants", result.successCount, result.errorCount);
                    }

                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al leer plantas de Firebase: " + error.getMessage());
                if (callback != null) {
                    callback.onError("plants", "Error de Firebase: " + error.getMessage());
                }
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Timeout al importar plantas");
        }

        return result;
    }

    /**
     * Importar datos de sensores desde Firebase
     */
    public MigrationResult importSensorData() {
        Log.d(TAG, "Importando datos de sensores desde Firebase");
        MigrationResult result = new MigrationResult();
        CountDownLatch latch = new CountDownLatch(1);

        firebaseRef.child("sensor_data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    int totalRecords = (int) snapshot.getChildrenCount();
                    int currentRecord = 0;

                    Log.d(TAG, "Datos de sensores encontrados en Firebase: " + totalRecords);

                    for (DataSnapshot sensorSnapshot : snapshot.getChildren()) {
                        try {
                            SensorData sensorData = new SensorData();

                            if (sensorSnapshot.hasChild("id")) {
                                sensorData.setId(sensorSnapshot.child("id").getValue(Integer.class));
                            }
                            if (sensorSnapshot.hasChild("plant_id")) {
                                sensorData.setPlantId(sensorSnapshot.child("plant_id").getValue(Integer.class));
                            }
                            if (sensorSnapshot.hasChild("soil_humidity")) {
                                Double value = sensorSnapshot.child("soil_humidity").getValue(Double.class);
                                sensorData.setSoilHumidity(value != null ? value.floatValue() : 0f);
                            }
                            if (sensorSnapshot.hasChild("temperature")) {
                                Double value = sensorSnapshot.child("temperature").getValue(Double.class);
                                sensorData.setTemperature(value != null ? value.floatValue() : 0f);
                            }
                            if (sensorSnapshot.hasChild("ambient_humidity")) {
                                Double value = sensorSnapshot.child("ambient_humidity").getValue(Double.class);
                                sensorData.setAmbientHumidity(value != null ? value.floatValue() : 0f);
                            }
                            if (sensorSnapshot.hasChild("uv_level")) {
                                Double value = sensorSnapshot.child("uv_level").getValue(Double.class);
                                sensorData.setUvLevel(value != null ? value.floatValue() : 0f);
                            }
                            if (sensorSnapshot.hasChild("water_level")) {
                                Double value = sensorSnapshot.child("water_level").getValue(Double.class);
                                sensorData.setWaterLevel(value != null ? value.floatValue() : 0f);
                            }
                            if (sensorSnapshot.hasChild("pest_count")) {
                                sensorData.setPestCount(sensorSnapshot.child("pest_count").getValue(Integer.class));
                            }
                            if (sensorSnapshot.hasChild("timestamp")) {
                                sensorData.setTimestamp(sensorSnapshot.child("timestamp").getValue(String.class));
                            }

                            // Insertar datos de sensor (siempre insertar nuevos)
                            long id = dbHelper.insertSensorData(sensorData);
                            if (id > 0) {
                                result.successCount++;
                            } else {
                                result.errorCount++;
                            }

                            currentRecord++;
                            if (callback != null && currentRecord % 10 == 0) {
                                final int current = currentRecord;
                                callback.onProgress("sensor_data", current, totalRecords);
                            }

                        } catch (Exception e) {
                            result.errorCount++;
                            Log.e(TAG, "Error al importar dato de sensor: " + e.getMessage());
                            if (callback != null) {
                                callback.onError("sensor_data", "Error en registro: " + e.getMessage());
                            }
                        }
                    }

                    if (callback != null) {
                        callback.onTableComplete("sensor_data", result.successCount, result.errorCount);
                    }

                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al leer datos de sensores de Firebase: " + error.getMessage());
                if (callback != null) {
                    callback.onError("sensor_data", "Error de Firebase: " + error.getMessage());
                }
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Timeout al importar datos de sensores");
        }

        return result;
    }

    /**
     * Importar alertas desde Firebase
     */
    public MigrationResult importAlerts() {
        Log.d(TAG, "Importando alertas desde Firebase");
        MigrationResult result = new MigrationResult();
        CountDownLatch latch = new CountDownLatch(1);

        firebaseRef.child("alerts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    int totalRecords = (int) snapshot.getChildrenCount();
                    int currentRecord = 0;

                    Log.d(TAG, "Alertas encontradas en Firebase: " + totalRecords);

                    for (DataSnapshot alertSnapshot : snapshot.getChildren()) {
                        try {
                            Alert alert = new Alert();

                            if (alertSnapshot.hasChild("id")) {
                                alert.setId(alertSnapshot.child("id").getValue(Integer.class));
                            }
                            if (alertSnapshot.hasChild("plant_id")) {
                                alert.setPlantId(alertSnapshot.child("plant_id").getValue(Integer.class));
                            }
                            if (alertSnapshot.hasChild("alert_type")) {
                                alert.setType(alertSnapshot.child("alert_type").getValue(String.class));
                            }
                            if (alertSnapshot.hasChild("title")) {
                                alert.setTitle(alertSnapshot.child("title").getValue(String.class));
                            }
                            if (alertSnapshot.hasChild("message")) {
                                alert.setMessage(alertSnapshot.child("message").getValue(String.class));
                            }
                            if (alertSnapshot.hasChild("severity")) {
                                alert.setSeverity(alertSnapshot.child("severity").getValue(String.class));
                            }
                            if (alertSnapshot.hasChild("is_read")) {
                                Long isRead = alertSnapshot.child("is_read").getValue(Long.class);
                                alert.setRead(isRead != null && isRead == 1);
                            }
                            if (alertSnapshot.hasChild("icon_type")) {
                                alert.setIconType(alertSnapshot.child("icon_type").getValue(String.class));
                            }
                            if (alertSnapshot.hasChild("timestamp")) {
                                alert.setTimestamp(alertSnapshot.child("timestamp").getValue(String.class));
                            }

                            // Insertar alerta (siempre insertar nuevas)
                            long id = dbHelper.createAlert(alert);
                            if (id > 0) {
                                result.successCount++;
                            } else {
                                result.errorCount++;
                            }

                            currentRecord++;
                            if (callback != null && currentRecord % 10 == 0) {
                                final int current = currentRecord;
                                callback.onProgress("alerts", current, totalRecords);
                            }

                        } catch (Exception e) {
                            result.errorCount++;
                            Log.e(TAG, "Error al importar alerta: " + e.getMessage());
                            if (callback != null) {
                                callback.onError("alerts", "Error en registro: " + e.getMessage());
                            }
                        }
                    }

                    if (callback != null) {
                        callback.onTableComplete("alerts", result.successCount, result.errorCount);
                    }

                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al leer alertas de Firebase: " + error.getMessage());
                if (callback != null) {
                    callback.onError("alerts", "Error de Firebase: " + error.getMessage());
                }
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Timeout al importar alertas");
        }

        return result;
    }

    /**
     * Obtener estadísticas de Firebase
     */
    public void getFirebaseStats(final FirebaseStatsCallback callback) {
        final Map<String, Integer> stats = new HashMap<>();
        final CountDownLatch latch = new CountDownLatch(4);

        // Contar usuarios
        firebaseRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stats.put("users", (int) snapshot.getChildrenCount());
                latch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                stats.put("users", 0);
                latch.countDown();
            }
        });

        // Contar plantas
        firebaseRef.child("plants").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stats.put("plants", (int) snapshot.getChildrenCount());
                latch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                stats.put("plants", 0);
                latch.countDown();
            }
        });

        // Contar sensor_data
        firebaseRef.child("sensor_data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stats.put("sensor_data", (int) snapshot.getChildrenCount());
                latch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                stats.put("sensor_data", 0);
                latch.countDown();
            }
        });

        // Contar alerts
        firebaseRef.child("alerts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stats.put("alerts", (int) snapshot.getChildrenCount());
                latch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                stats.put("alerts", 0);
                latch.countDown();
            }
        });

        new Thread(() -> {
            try {
                latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (callback != null) {
                    callback.onStatsReady(stats);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Timeout al obtener estadísticas de Firebase");
            }
        }).start();
    }

    /**
     * Interface para callback de estadísticas
     */
    public interface FirebaseStatsCallback {
        void onStatsReady(Map<String, Integer> stats);
    }

    /**
     * Clase interna para resultados de migración
     */
    public static class MigrationResult {
        public int successCount = 0;
        public int errorCount = 0;
    }
}
