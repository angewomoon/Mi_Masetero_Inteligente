package com.devst.mimaseterointeligente.models;

/**
 * Modelo de datos para Alertas
 */
public class Alert {
    
    // Constantes de tipo de alerta
    public static final String TYPE_LOW_SOIL_HUMIDITY = "low_soil_humidity";
    public static final String TYPE_HIGH_SOIL_HUMIDITY = "high_soil_humidity";
    public static final String TYPE_LOW_TEMPERATURE = "low_temperature";
    public static final String TYPE_HIGH_TEMPERATURE = "high_temperature";
    public static final String TYPE_LOW_LIGHT = "low_light";
    public static final String TYPE_HIGH_LIGHT = "high_light";
    public static final String TYPE_LOW_WATER = "low_water";
    public static final String TYPE_PEST_DETECTED = "pest_detected";
    public static final String TYPE_GENERAL = "general";
    
    // Constantes de severidad
    public static final String SEVERITY_INFO = "info";
    public static final String SEVERITY_WARNING = "warning";
    public static final String SEVERITY_CRITICAL = "critical";
    
    private int id;
    private int plantId;
    private String alertType;
    private String title;
    private String message;
    private String severity;
    private boolean isRead;
    private String iconType;
    private String timestamp;
    
    // Constructor vacío
    public Alert() {
    }
    
    // Constructor completo
    public Alert(int id, int plantId, String alertType, String title, String message, 
                 String severity, boolean isRead, String iconType, String timestamp) {
        this.id = id;
        this.plantId = plantId;
        this.alertType = alertType;
        this.title = title;
        this.message = message;
        this.severity = severity;
        this.isRead = isRead;
        this.iconType = iconType;
        this.timestamp = timestamp;
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
    
    public String getAlertType() {
        return alertType;
    }
    
    public void setType(String alertType) {
        this.alertType = alertType;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public String getIconType() {
        return iconType;
    }
    
    public void setIconType(String iconType) {
        this.iconType = iconType;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
