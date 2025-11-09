package com.devst.mimaseterointeligente.models;

import java.io.Serializable;

public class SensorData implements Serializable {
    private int id;
    private int plantId;
    private float soilHumidity;      // Porcentaje 0-100
    private float temperature;       // Celsius
    private float ambientHumidity;   // Porcentaje 0-100
    private float uvLevel;           // Índice UV 0-11+
    private float waterLevel;        // Porcentaje 0-100
    private int pestCount;           // Número de plagas detectadas
    private String timestamp;

    // Estados de alerta
    private String soilHumidityStatus;    // OPTIMAL, LOW, HIGH, CRITICAL_LOW, CRITICAL_HIGH
    private String temperatureStatus;      // OPTIMAL, LOW, HIGH, CRITICAL_LOW, CRITICAL_HIGH
    private String ambientHumidityStatus;  // OPTIMAL, LOW, HIGH, CRITICAL_LOW
    private String uvStatus;              // OPTIMAL, LOW, HIGH, CRITICAL_HIGH
    private String waterLevelStatus;      // OPTIMAL, LOW, CRITICAL_LOW
    private String pestStatus;            // NONE, WARNING, CRITICAL

    // Constructor vacío
    public SensorData() {
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    // Constructor con valores
    public SensorData(float soilHumidity, float temperature, float ambientHumidity,
                      float uvLevel, float waterLevel, int pestCount) {
        this.soilHumidity = soilHumidity;
        this.temperature = temperature;
        this.ambientHumidity = ambientHumidity;
        this.uvLevel = uvLevel;
        this.waterLevel = waterLevel;
        this.pestCount = pestCount;
        this.timestamp = String.valueOf(System.currentTimeMillis());
        evaluateStatuses();
    }

    // Evaluar estados basados en los valores de los sensores
    public void evaluateStatuses() {
        // Evaluación de humedad del suelo
        if (soilHumidity < 15) {
            soilHumidityStatus = "CRITICAL_LOW";
        } else if (soilHumidity < 30) {
            soilHumidityStatus = "LOW";
        } else if (soilHumidity > 70) {
            soilHumidityStatus = "CRITICAL_HIGH";
        } else if (soilHumidity > 60) {
            soilHumidityStatus = "HIGH";
        } else {
            soilHumidityStatus = "OPTIMAL";
        }

        // Evaluación de temperatura
        if (temperature < 10) {
            temperatureStatus = "CRITICAL_LOW";
        } else if (temperature < 18) {
            temperatureStatus = "LOW";
        } else if (temperature > 30) {
            temperatureStatus = "CRITICAL_HIGH";
        } else if (temperature > 26) {
            temperatureStatus = "HIGH";
        } else {
            temperatureStatus = "OPTIMAL";
        }

        // Evaluación de humedad ambiental
        if (ambientHumidity < 35) {
            ambientHumidityStatus = "CRITICAL_LOW";
        } else if (ambientHumidity < 40) {
            ambientHumidityStatus = "LOW";
        } else if (ambientHumidity > 60) {
            ambientHumidityStatus = "HIGH";
        } else {
            ambientHumidityStatus = "OPTIMAL";
        }

        // Evaluación de luz UV
        if (uvLevel < 2) {
            uvStatus = "LOW";
        } else if (uvLevel > 8) {
            uvStatus = "CRITICAL_HIGH";
        } else if (uvLevel > 6) {
            uvStatus = "HIGH";
        } else {
            uvStatus = "OPTIMAL";
        }

        // Evaluación del nivel de agua
        if (waterLevel < 10) {
            waterLevelStatus = "CRITICAL_LOW";
        } else if (waterLevel < 30) {
            waterLevelStatus = "LOW";
        } else {
            waterLevelStatus = "OPTIMAL";
        }

        // Evaluación de plagas
        if (pestCount == 0) {
            pestStatus = "NONE";
        } else if (pestCount <= 2) {
            pestStatus = "WARNING";
        } else {
            pestStatus = "CRITICAL";
        }
    }

    // Verificar si necesita alerta de prevención de plagas
    public boolean needsPestPrevention() {
        // Condiciones para araña roja: humedad baja y temperatura alta por más de 48 horas
        return (ambientHumidity < 35 && temperature > 30) ||
                // Condiciones para hongos: humedad del suelo muy alta
                (soilHumidity > 70);
    }

    // Obtener el nivel de alerta general
    public String getOverallAlertLevel() {
        if (soilHumidityStatus.contains("CRITICAL") ||
                temperatureStatus.contains("CRITICAL") ||
                ambientHumidityStatus.contains("CRITICAL") ||
                uvStatus.contains("CRITICAL") ||
                waterLevelStatus.contains("CRITICAL") ||
                pestStatus.equals("CRITICAL")) {
            return "CRITICAL";
        } else if (soilHumidityStatus.equals("LOW") ||
                soilHumidityStatus.equals("HIGH") ||
                temperatureStatus.equals("LOW") ||
                temperatureStatus.equals("HIGH") ||
                ambientHumidityStatus.equals("LOW") ||
                uvStatus.equals("HIGH") ||
                waterLevelStatus.equals("LOW") ||
                pestStatus.equals("WARNING")) {
            return "WARNING";
        }
        return "OPTIMAL";
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlantId() {
        return plantId;
    }

    public void setPlantId(int plantId) {
        this.plantId = plantId;
    }

    public float getSoilHumidity() {
        return soilHumidity;
    }

    public void setSoilHumidity(float soilHumidity) {
        this.soilHumidity = soilHumidity;
        evaluateStatuses();
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
        evaluateStatuses();
    }

    public float getAmbientHumidity() {
        return ambientHumidity;
    }

    public void setAmbientHumidity(float ambientHumidity) {
        this.ambientHumidity = ambientHumidity;
        evaluateStatuses();
    }

    public float getUvLevel() {
        return uvLevel;
    }

    public void setUvLevel(float uvLevel) {
        this.uvLevel = uvLevel;
        evaluateStatuses();
    }

    public float getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(float waterLevel) {
        this.waterLevel = waterLevel;
        evaluateStatuses();
    }

    public int getPestCount() {
        return pestCount;
    }

    public void setPestCount(int pestCount) {
        this.pestCount = pestCount;
        evaluateStatuses();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSoilHumidityStatus() {
        return soilHumidityStatus;
    }

    public String getTemperatureStatus() {
        return temperatureStatus;
    }

    public String getAmbientHumidityStatus() {
        return ambientHumidityStatus;
    }

    public String getUvStatus() {
        return uvStatus;
    }

    public String getWaterLevelStatus() {
        return waterLevelStatus;
    }

    public String getPestStatus() {
        return pestStatus;
    }
}