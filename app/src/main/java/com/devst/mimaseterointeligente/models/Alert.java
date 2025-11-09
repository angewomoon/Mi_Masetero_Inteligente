package com.devst.mimaseterointeligente.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Alert implements Serializable {
    private int id;
    private int plantId;
    private String type;       // LOW_HUMIDITY, LOW_LIGHT, PEST_DETECTED, LOW_WATER, etc.
    private String title;
    private String message;
    private String severity;   // INFO, WARNING, CRITICAL
    private boolean isRead;
    private String timestamp;
    private String iconType;   // water, sun, bug, thermometer, humidity

    // Tipos de alerta
    public static final String TYPE_LOW_SOIL_HUMIDITY = "LOW_SOIL_HUMIDITY";
    public static final String TYPE_HIGH_SOIL_HUMIDITY = "HIGH_SOIL_HUMIDITY";
    public static final String TYPE_LOW_TEMPERATURE = "LOW_TEMPERATURE";
    public static final String TYPE_HIGH_TEMPERATURE = "HIGH_TEMPERATURE";
    public static final String TYPE_LOW_AMBIENT_HUMIDITY = "LOW_AMBIENT_HUMIDITY";
    public static final String TYPE_LOW_LIGHT = "LOW_LIGHT";
    public static final String TYPE_HIGH_LIGHT = "HIGH_LIGHT";
    public static final String TYPE_LOW_WATER = "LOW_WATER";
    public static final String TYPE_PEST_DETECTED = "PEST_DETECTED";
    public static final String TYPE_PEST_PREVENTION = "PEST_PREVENTION";

    // Severidades
    public static final String SEVERITY_INFO = "INFO";
    public static final String SEVERITY_WARNING = "WARNING";
    public static final String SEVERITY_CRITICAL = "CRITICAL";

    // Constructor vacío
    public Alert() {
        this.timestamp = String.valueOf(System.currentTimeMillis());
        this.isRead = false;
    }

    // Constructor con tipo
    public Alert(String type, int plantId) {
        this();
        this.type = type;
        this.plantId = plantId;
        setAlertContent(type);
    }

    // Establecer contenido según el tipo de alerta
    private void setAlertContent(String type) {
        switch (type) {
            case TYPE_LOW_SOIL_HUMIDITY:
                this.title = "Humedad Baja";
                this.message = "La humedad de tu planta está por debajo del nivel óptimo. Considera regar.";
                this.severity = SEVERITY_WARNING;
                this.iconType = "water";
                break;

            case TYPE_HIGH_SOIL_HUMIDITY:
                this.title = "Exceso de Humedad";
                this.message = "¡Exceso de agua! Detener el riego y verificar el drenaje. Riesgo de pudrición.";
                this.severity = SEVERITY_CRITICAL;
                this.iconType = "water";
                break;

            case TYPE_LOW_TEMPERATURE:
                this.title = "Temperatura Baja";
                this.message = "La temperatura está bajando. Protege tu planta de corrientes frías.";
                this.severity = SEVERITY_WARNING;
                this.iconType = "thermometer";
                break;

            case TYPE_HIGH_TEMPERATURE:
                this.title = "Temperatura Alta";
                this.message = "La temperatura es demasiado alta. Mueve tu planta a un lugar más fresco.";
                this.severity = SEVERITY_CRITICAL;
                this.iconType = "thermometer";
                break;

            case TYPE_LOW_AMBIENT_HUMIDITY:
                this.title = "Ambiente Seco";
                this.message = "Ambiente muy seco. Las puntas de las hojas pueden secarse. Recomendación: Pulverizar agua.";
                this.severity = SEVERITY_WARNING;
                this.iconType = "humidity";
                break;

            case TYPE_LOW_LIGHT:
                this.title = "Poca luz";
                this.message = "Tu planta necesita más luz solar. Muévela a un lugar más iluminado.";
                this.severity = SEVERITY_WARNING;
                this.iconType = "sun";
                break;

            case TYPE_HIGH_LIGHT:
                this.title = "Exceso de Luz";
                this.message = "¡Sol directo! Las hojas pueden quemarse. Recomendación: Mover a semisombra.";
                this.severity = SEVERITY_CRITICAL;
                this.iconType = "sun";
                break;

            case TYPE_LOW_WATER:
                this.title = "Nivel de Agua baja";
                this.message = "El nivel de agua actualmente es baja. Considere rellenar.";
                this.severity = SEVERITY_WARNING;
                this.iconType = "water";
                break;

            case TYPE_PEST_DETECTED:
                this.title = "¡Plaga Detectada!";
                this.message = "Se ha detectado una posible plaga. Revisa tu planta!";
                this.severity = SEVERITY_CRITICAL;
                this.iconType = "bug";
                break;

            case TYPE_PEST_PREVENTION:
                this.title = "Riesgo de Plagas";
                this.message = "Riesgo alto de Araña Roja. Aumente la humedad y revise el envés de las hojas.";
                this.severity = SEVERITY_WARNING;
                this.iconType = "bug";
                break;

            default:
                this.title = "Alerta";
                this.message = "Revisa tu planta";
                this.severity = SEVERITY_INFO;
                this.iconType = "info";
                break;
        }
    }

    // Obtener tiempo relativo (hace X minutos/horas)
    public String getRelativeTime() {
        try {
            long timestamp = Long.parseLong(this.timestamp);
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long days = TimeUnit.MILLISECONDS.toDays(diff);

            if (minutes < 1) {
                return "Ahora";
            } else if (minutes < 60) {
                return "Hace " + minutes + " m";
            } else if (hours < 24) {
                return "Hace " + hours + " h";
            } else {
                return "Hace " + days + " d";
            }
        } catch (Exception e) {
            return "Reciente";
        }
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
        return type;
    }

    public void setType(String type) {
        this.type = type;
        setAlertContent(type);
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getIconType() {
        return iconType;
    }

    public void setIconType(String iconType) {
        this.iconType = iconType;
    }
}
