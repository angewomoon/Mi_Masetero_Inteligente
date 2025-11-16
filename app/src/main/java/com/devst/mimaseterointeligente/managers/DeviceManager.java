package com.devst.mimaseterointeligente.managers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager para sincronizar dispositivos ESP32 con Firebase.
 * Permite asignar/desasignar plantas a dispositivos y mantener estados actualizados.
 */
public class DeviceManager {

    private static final String TAG = "DeviceManager";
    private static DeviceManager instance;

    private DatabaseReference devicesRef;
    private DatabaseReference plantsRef;

    private DeviceManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        devicesRef = database.getReference("devices");
        plantsRef = database.getReference("plants");
    }

    /**
     * Obtener instancia singleton
     */
    public static synchronized DeviceManager getInstance() {
        if (instance == null) {
            instance = new DeviceManager();
        }
        return instance;
    }

    /**
     * Asignar una planta a un dispositivo ESP32
     * @param deviceId ID del dispositivo ESP32 (ej: "ESP32-A1B2C3")
     * @param plantId ID de la planta
     * @param callback Callback con resultado
     */
    public void assignPlantToDevice(String deviceId, String plantId, AssignmentCallback callback) {
        Log.d(TAG, "Asignando planta " + plantId + " a dispositivo " + deviceId);

        // Verificar que el dispositivo esté disponible
        devicesRef.child(deviceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    if (callback != null) {
                        callback.onError("El dispositivo no existe");
                    }
                    return;
                }

                Boolean isAvailable = snapshot.child("is_available").getValue(Boolean.class);
                if (isAvailable == null || !isAvailable) {
                    if (callback != null) {
                        callback.onError("El dispositivo no está disponible");
                    }
                    return;
                }

                // Actualizar dispositivo
                Map<String, Object> deviceUpdates = new HashMap<>();
                deviceUpdates.put("is_available", false);
                deviceUpdates.put("current_plant_id", String.valueOf(plantId));
                deviceUpdates.put("last_seen", String.valueOf(System.currentTimeMillis()));

                devicesRef.child(deviceId).updateChildren(deviceUpdates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Dispositivo actualizado exitosamente");

                        // También actualizar la planta en Firebase (opcional, si la usas)
                        updatePlantDevice(plantId, deviceId);

                        if (callback != null) {
                            callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al actualizar dispositivo: " + e.getMessage());
                        if (callback != null) {
                            callback.onError(e.getMessage());
                        }
                    });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al leer dispositivo: " + error.getMessage());
                if (callback != null) {
                    callback.onError(error.getMessage());
                }
            }
        });
    }

    /**
     * Desasignar una planta de un dispositivo ESP32
     * @param deviceId ID del dispositivo
     * @param callback Callback con resultado
     */
    public void unassignPlantFromDevice(String deviceId, UnassignmentCallback callback) {
        Log.d(TAG, "Desasignando planta del dispositivo " + deviceId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("is_available", true);
        updates.put("current_plant_id", "");

        devicesRef.child(deviceId).updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Planta desasignada exitosamente");
                if (callback != null) {
                    callback.onSuccess();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al desasignar: " + e.getMessage());
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }

    /**
     * Actualizar el deviceId de una planta en Firebase (opcional)
     */
    private void updatePlantDevice(String plantId, String deviceId) {
        Map<String, Object> plantUpdates = new HashMap<>();
        plantUpdates.put("device_id", deviceId);
        plantUpdates.put("is_connected", true);

        plantsRef.child(plantId).updateChildren(plantUpdates)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "Planta actualizada en Firebase"))
            .addOnFailureListener(e -> Log.e(TAG, "Error al actualizar planta: " + e.getMessage()));
    }

    /**
     * Verificar si un dispositivo está online (última conexión < 5 minutos)
     * @param deviceId ID del dispositivo
     * @param callback Callback con resultado
     */
    public void isDeviceOnline(String deviceId, OnlineCheckCallback callback) {
        devicesRef.child(deviceId).child("last_seen")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        if (callback != null) {
                            callback.onResult(false);
                        }
                        return;
                    }

                    try {
                        String lastSeenStr = snapshot.getValue(String.class);
                        long lastSeen = Long.parseLong(lastSeenStr);
                        long now = System.currentTimeMillis();
                        long fiveMinutes = 5 * 60 * 1000;

                        boolean isOnline = (now - lastSeen) < fiveMinutes;

                        if (callback != null) {
                            callback.onResult(isOnline);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al verificar estado online: " + e.getMessage());
                        if (callback != null) {
                            callback.onResult(false);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error al leer last_seen: " + error.getMessage());
                    if (callback != null) {
                        callback.onResult(false);
                    }
                }
            });
    }

    /**
     * Marcar un dispositivo como offline manualmente
     * @param deviceId ID del dispositivo
     */
    public void markDeviceOffline(String deviceId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("last_seen", "0");  // Timestamp muy antiguo

        devicesRef.child(deviceId).updateChildren(updates);
    }

    // ============================================
    // INTERFACES DE CALLBACKS
    // ============================================

    public interface AssignmentCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface UnassignmentCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface OnlineCheckCallback {
        void onResult(boolean isOnline);
    }
}
