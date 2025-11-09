package com.devst.mimaseterointeligente.models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de respuesta de la API del Arduino
 *
 * UBICACIÓN: app/src/main/java/com/devst/mimaseterointeligente/models/ArduinoResponse.java
 * PROPÓSITO: Mapear la respuesta JSON del Arduino a un objeto Java
 */
public class ArduinoResponse {

    /**
     * Humedad del suelo (%)
     * Rango: 0-100
     * Sensor: Sensor de humedad de suelo
     */
    @SerializedName("soilHumidity")
    private float soilHumidity;

    /**
     * Temperatura ambiente (°C)
     * Sensor: DHT11/DHT22
     */
    @SerializedName("temperature")
    private float temperature;

    /**
     * Humedad ambiente (%)
     * Rango: 0-100
     * Sensor: DHT11/DHT22
     */
    @SerializedName("ambientHumidity")
    private float ambientHumidity;

    /**
     * Nivel de radiación UV
     * Rango: 0-15 (índice UV)
     * Sensor: Sensor UV
     */
    @SerializedName("uvLevel")
    private float uvLevel;

    /**
     * Nivel de agua en el depósito (%)
     * Rango: 0-100
     * Sensor: Sensor ultrasónico HC-SR04
     */
    @SerializedName("waterLevel")
    private float waterLevel;

    /**
     * Contador de posibles plagas detectadas
     * Sensor: TCRT5000
     */
    @SerializedName("pestCount")
    private int pestCount;

    /**
     * Timestamp de la lectura (milisegundos desde epoch)
     */
    @SerializedName("timestamp")
    private long timestamp;

    /**
     * Estado de la conexión del dispositivo (opcional)
     */
    @SerializedName("status")
    private String status;

    /**
     * Versión del firmware del Arduino (opcional)
     */
    @SerializedName("version")
    private String version;

    // ==================== CONSTRUCTORES ====================

    /**
     * Constructor vacío
     */
    public ArduinoResponse() {
        this.timestamp = System.currentTimeMillis();
        this.status = "unknown";
    }

    /**
     * Constructor completo
     */
    public ArduinoResponse(float soilHumidity, float temperature, float ambientHumidity,
                           float uvLevel, float waterLevel, int pestCount, long timestamp) {
        this.soilHumidity = soilHumidity;
        this.temperature = temperature;
        this.ambientHumidity = ambientHumidity;
        this.uvLevel = uvLevel;
        this.waterLevel = waterLevel;
        this.pestCount = pestCount;
        this.timestamp = timestamp;
        this.status = "ok";
    }

    // ==================== GETTERS Y SETTERS ====================

    public float getSoilHumidity() {
        return soilHumidity;
    }

    public void setSoilHumidity(float soilHumidity) {
        this.soilHumidity = soilHumidity;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getAmbientHumidity() {
        return ambientHumidity;
    }

    public void setAmbientHumidity(float ambientHumidity) {
        this.ambientHumidity = ambientHumidity;
    }

    public float getUvLevel() {
        return uvLevel;
    }

    public void setUvLevel(float uvLevel) {
        this.uvLevel = uvLevel;
    }

    public float getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(float waterLevel) {
        this.waterLevel = waterLevel;
    }

    public int getPestCount() {
        return pestCount;
    }

    public void setPestCount(int pestCount) {
        this.pestCount = pestCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Verificar si los datos son válidos
     */
    public boolean isValid() {
        return soilHumidity >= 0 && soilHumidity <= 100 &&
                temperature >= -40 && temperature <= 85 &&
                ambientHumidity >= 0 && ambientHumidity <= 100 &&
                uvLevel >= 0 && uvLevel <= 15 &&
                waterLevel >= 0 && waterLevel <= 100 &&
                pestCount >= 0;
    }

    /**
     * Convertir a objeto SensorData para guardar en base de datos
     */
    public SensorData toSensorData(int plantId) {
        SensorData sensorData = new SensorData();
        sensorData.setPlantId(plantId);
        sensorData.setSoilHumidity(this.soilHumidity);
        sensorData.setTemperature(this.temperature);
        sensorData.setAmbientHumidity(this.ambientHumidity);
        sensorData.setUvLevel(this.uvLevel);
        sensorData.setWaterLevel(this.waterLevel);
        sensorData.setPestCount(this.pestCount);
        sensorData.setTimestamp(String.valueOf(this.timestamp));
        return sensorData;
    }

    /**
     * Obtener representación en texto
     */
    @Override
    public String toString() {
        return "ArduinoResponse{" +
                "soilHumidity=" + soilHumidity + "%" +
                ", temperature=" + temperature + "°C" +
                ", ambientHumidity=" + ambientHumidity + "%" +
                ", uvLevel=" + uvLevel +
                ", waterLevel=" + waterLevel + "%" +
                ", pestCount=" + pestCount +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                '}';
    }
}