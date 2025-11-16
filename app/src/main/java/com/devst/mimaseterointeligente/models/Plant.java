package com.devst.mimaseterointeligente.models;

import java.io.Serializable;

public class Plant implements Serializable {
    private int id;
    private int userId;
    private String name;
    private String type;
    private String species;
    private String scientificName;
    private String imageUrl;
    private boolean isConnected; // Si está conectada al masetero inteligente
    private String deviceId;     // ID del dispositivo ESP32 conectado (ej: "ESP32-A1B2C3")

    // Parámetros óptimos
    private float optimalSoilHumidityMin;
    private float optimalSoilHumidityMax;
    private float optimalTempMin;
    private float optimalTempMax;
    private float optimalAmbientHumidityMin;
    private float optimalAmbientHumidityMax;
    private String optimalLightLevel; // "Directa", "Indirecta", "Semisombra"

    private String createdAt;
    private String updatedAt;

    // Especies disponibles
    public static final String SPECIES_CHLOROPHYTUM = "Chlorophytum comosum";
    public static final String SPECIES_CACTUS = "Cactaceae";
    public static final String SPECIES_SUCCULENT = "Echeveria elegans";

    // Constructor vacío
    public Plant() {
    }

    // Constructor básico
    public Plant(String name, String type, String species) {
        this.name = name;
        this.type = type;
        this.species = species;
        setOptimalParametersBySpecies(species);
    }

    // Establecer parámetros óptimos según la especie
    public void setOptimalParametersBySpecies(String species) {
        // Si species es null, usar valores por defecto
        if (species == null) {
            this.optimalSoilHumidityMin = 30.0f;
            this.optimalSoilHumidityMax = 60.0f;
            this.optimalTempMin = 18.0f;
            this.optimalTempMax = 28.0f;
            this.optimalAmbientHumidityMin = 40.0f;
            this.optimalAmbientHumidityMax = 60.0f;
            this.optimalLightLevel = "Luz Indirecta";
            this.isConnected = false;
            return;
        }

        switch (species) {
            case SPECIES_CHLOROPHYTUM:
                // Chlorophytum comosum (Cinta/Malamadre)
                this.scientificName = "Chlorophytum comosum";
                this.optimalSoilHumidityMin = 30.0f;
                this.optimalSoilHumidityMax = 60.0f;
                this.optimalTempMin = 18.0f;
                this.optimalTempMax = 26.0f;
                this.optimalAmbientHumidityMin = 40.0f;
                this.optimalAmbientHumidityMax = 60.0f;
                this.optimalLightLevel = "Luz Brillante Indirecta";
                this.isConnected = true; // Esta especie se conecta al masetero
                break;

            case SPECIES_CACTUS:
                // Cactaceae
                this.scientificName = "Cactaceae";
                this.optimalSoilHumidityMin = 10.0f;
                this.optimalSoilHumidityMax = 30.0f;
                this.optimalTempMin = 20.0f;
                this.optimalTempMax = 35.0f;
                this.optimalAmbientHumidityMin = 30.0f;
                this.optimalAmbientHumidityMax = 50.0f;
                this.optimalLightLevel = "Luz Directa";
                this.isConnected = false;
                break;

            case SPECIES_SUCCULENT:
                // Echeveria elegans (Rosa de Alabastro)
                this.scientificName = "Echeveria elegans";
                this.optimalSoilHumidityMin = 20.0f;
                this.optimalSoilHumidityMax = 40.0f;
                this.optimalTempMin = 18.0f;
                this.optimalTempMax = 28.0f;
                this.optimalAmbientHumidityMin = 30.0f;
                this.optimalAmbientHumidityMax = 50.0f;
                this.optimalLightLevel = "Luz Brillante Indirecta";
                this.isConnected = false;
                break;

            default:
                // Valores por defecto
                this.optimalSoilHumidityMin = 30.0f;
                this.optimalSoilHumidityMax = 60.0f;
                this.optimalTempMin = 18.0f;
                this.optimalTempMax = 28.0f;
                this.optimalAmbientHumidityMin = 40.0f;
                this.optimalAmbientHumidityMax = 60.0f;
                this.optimalLightLevel = "Luz Indirecta";
                this.isConnected = false;
                break;
        }
    }

    // Métodos de validación
    public boolean isSoilHumidityOptimal(float currentHumidity) {
        return currentHumidity >= optimalSoilHumidityMin && currentHumidity <= optimalSoilHumidityMax;
    }

    public boolean isTemperatureOptimal(float currentTemp) {
        return currentTemp >= optimalTempMin && currentTemp <= optimalTempMax;
    }

    public boolean isAmbientHumidityOptimal(float currentHumidity) {
        return currentHumidity >= optimalAmbientHumidityMin && currentHumidity <= optimalAmbientHumidityMax;
    }

    public String getSoilHumidityStatus(float currentHumidity) {
        if (currentHumidity < 15) return "CRITICAL_LOW";
        if (currentHumidity < optimalSoilHumidityMin) return "LOW";
        if (currentHumidity > 70) return "CRITICAL_HIGH";
        if (currentHumidity > optimalSoilHumidityMax) return "HIGH";
        return "OPTIMAL";
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
        setOptimalParametersBySpecies(species);
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isConnected() {
        // Una planta está conectada si tiene un deviceId asignado
        return deviceId != null && !deviceId.isEmpty();
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        // Actualizar isConnected automáticamente
        this.isConnected = (deviceId != null && !deviceId.isEmpty());
    }

    public float getOptimalSoilHumidityMin() {
        return optimalSoilHumidityMin;
    }

    public void setOptimalSoilHumidityMin(float optimalSoilHumidityMin) {
        this.optimalSoilHumidityMin = optimalSoilHumidityMin;
    }

    public float getOptimalSoilHumidityMax() {
        return optimalSoilHumidityMax;
    }

    public void setOptimalSoilHumidityMax(float optimalSoilHumidityMax) {
        this.optimalSoilHumidityMax = optimalSoilHumidityMax;
    }

    public float getOptimalTempMin() {
        return optimalTempMin;
    }

    public void setOptimalTempMin(float optimalTempMin) {
        this.optimalTempMin = optimalTempMin;
    }

    public float getOptimalTempMax() {
        return optimalTempMax;
    }

    public void setOptimalTempMax(float optimalTempMax) {
        this.optimalTempMax = optimalTempMax;
    }

    public float getOptimalAmbientHumidityMin() {
        return optimalAmbientHumidityMin;
    }

    public void setOptimalAmbientHumidityMin(float optimalAmbientHumidityMin) {
        this.optimalAmbientHumidityMin = optimalAmbientHumidityMin;
    }

    public float getOptimalAmbientHumidityMax() {
        return optimalAmbientHumidityMax;
    }

    public void setOptimalAmbientHumidityMax(float optimalAmbientHumidityMax) {
        this.optimalAmbientHumidityMax = optimalAmbientHumidityMax;
    }

    public String getOptimalLightLevel() {
        return optimalLightLevel;
    }

    public void setOptimalLightLevel(String optimalLightLevel) {
        this.optimalLightLevel = optimalLightLevel;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
