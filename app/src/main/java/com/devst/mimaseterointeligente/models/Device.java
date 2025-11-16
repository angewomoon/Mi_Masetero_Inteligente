package com.devst.mimaseterointeligente.models;

import java.io.Serializable;

/**
 * Modelo para representar un dispositivo ESP32 (Masetero Inteligente).
 * Los dispositivos se auto-registran en Firebase al encender.
 */
public class Device implements Serializable {

    private String deviceId;        // ID único del dispositivo (ej: "ESP32-A1B2C3")
    private String deviceName;      // Nombre amigable (ej: "Masetero Principal")
    private boolean isAvailable;    // true si no está asignado a ninguna planta
    private String currentPlantId;  // ID de la planta conectada (null si disponible)
    private String lastSeen;        // Timestamp de última conexión
    private String ipAddress;       // Dirección IP del dispositivo (opcional)
    private String firmwareVersion; // Versión del firmware (opcional)

    // Constructor vacío (requerido por Firebase)
    public Device() {
    }

    // Constructor básico
    public Device(String deviceId, String deviceName) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.isAvailable = true;
        this.currentPlantId = null;
        this.lastSeen = String.valueOf(System.currentTimeMillis());
    }

    // Getters y Setters
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getCurrentPlantId() {
        return currentPlantId;
    }

    public void setCurrentPlantId(String currentPlantId) {
        this.currentPlantId = currentPlantId;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    /**
     * Verifica si el dispositivo está online (última conexión en los últimos 5 minutos)
     */
    public boolean isOnline() {
        if (lastSeen == null) return false;

        try {
            long lastSeenTimestamp = Long.parseLong(lastSeen);
            long currentTime = System.currentTimeMillis();
            long fiveMinutes = 5 * 60 * 1000; // 5 minutos en milisegundos

            return (currentTime - lastSeenTimestamp) < fiveMinutes;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Obtiene el estado del dispositivo como texto
     */
    public String getStatusText() {
        if (!isOnline()) {
            return "Offline";
        } else if (!isAvailable) {
            return "En uso";
        } else {
            return "Disponible";
        }
    }

    @Override
    public String toString() {
        return "Device{" +
                "deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", isAvailable=" + isAvailable +
                ", currentPlantId='" + currentPlantId + '\'' +
                ", isOnline=" + isOnline() +
                '}';
    }
}
