package com.devst.mimaseterointeligente.utils;

import com.devst.mimaseterointeligente.models.Alert;
import com.devst.mimaseterointeligente.models.ArduinoResponse;
import com.devst.mimaseterointeligente.models.Plant;

import java.util.ArrayList;
import java.util.List;

/**
 * Generador de alertas basado en condiciones de sensores
 *
 * UBICACIÓN: app/src/main/java/com/devst/mimaseterointeligente/utils/AlertGenerator.java
 * PROPÓSITO: Generar alertas automáticas según umbrales definidos por especie de planta
 */
public class AlertGenerator {

    // Constantes para tipos de alerta
    public static final String ALERT_TYPE_WATERING = "WATERING";
    public static final String ALERT_TYPE_WATER_LEVEL = "WATER_LEVEL";
    public static final String ALERT_TYPE_TEMPERATURE = "TEMPERATURE";
    public static final String ALERT_TYPE_HUMIDITY = "HUMIDITY";
    public static final String ALERT_TYPE_LIGHT = "LIGHT";
    public static final String ALERT_TYPE_PEST = "PEST";

    // Severidades
    public static final String SEVERITY_INFO = "INFO";
    public static final String SEVERITY_WARNING = "WARNING";
    public static final String SEVERITY_CRITICAL = "CRITICAL";

    // Íconos
    public static final String ICON_WATER = "💧";
    public static final String ICON_TEMPERATURE = "🌡";
    public static final String ICON_HUMIDITY = "💨";
    public static final String ICON_LIGHT = "☀";
    public static final String ICON_PEST = "🐛";
    public static final String ICON_TANK = "🪣";

    /**
     * Generar alertas basadas en datos de sensores y parámetros de la planta
     *
     * @param plant Planta con parámetros óptimos
     * @param sensorData Datos actuales de los sensores
     * @return Lista de alertas generadas
     */
    public static List<Alert> generateAlerts(Plant plant, ArduinoResponse sensorData) {
        List<Alert> alerts = new ArrayList<>();

        if (plant == null || sensorData == null) {
            return alerts;
        }

        // Verificar humedad del suelo
        alerts.addAll(checkSoilHumidity(plant, sensorData));

        // Verificar temperatura
        alerts.addAll(checkTemperature(plant, sensorData));

        // Verificar humedad ambiental
        alerts.addAll(checkAmbientHumidity(plant, sensorData));

        // Verificar nivel de agua en depósito
        alerts.addAll(checkWaterLevel(plant, sensorData));

        // Verificar plagas
        alerts.addAll(checkPests(plant, sensorData));

        // Verificar alerta combinada de plagas (condiciones favorables)
        alerts.addAll(checkPestConditions(plant, sensorData));

        return alerts;
    }

    /**
     * Verificar humedad del suelo y generar alertas de riego
     */
    private static List<Alert> checkSoilHumidity(Plant plant, ArduinoResponse data) {
        List<Alert> alerts = new ArrayList<>();
        float soilHumidity = data.getSoilHumidity();

        // Crítico bajo - Necesita riego urgente (<15%)
        if (soilHumidity < 15) {
            alerts.add(createAlert(
                    plant.getId(),
                    ALERT_TYPE_WATERING,
                    "¡Riego Urgente!",
                    "La humedad del suelo es crítica (" + (int)soilHumidity + "%). Tu planta necesita agua inmediatamente.",
                    SEVERITY_CRITICAL,
                    ICON_WATER
            ));
        }
        // Bajo - Considerar riego (15-30%)
        else if (soilHumidity < plant.getOptimalSoilHumidityMin()) {
            alerts.add(createAlert(
                    plant.getId(),
                    ALERT_TYPE_WATERING,
                    "Humedad Baja",
                    "La humedad del suelo está por debajo del nivel óptimo (" + (int)soilHumidity + "%). Considera regar.",
                    SEVERITY_WARNING,
                    ICON_WATER
            ));
        }
        // Crítico alto - Exceso de agua (>70%)
        else if (soilHumidity > 70) {
            alerts.add(createAlert(
                    plant.getId(),
                    ALERT_TYPE_WATERING,
                    "¡Exceso de Agua!",
                    "Hay exceso de agua en el suelo (" + (int)soilHumidity + "%). Riesgo de pudrición de raíz. Verifica el drenaje.",
                    SEVERITY_CRITICAL,
                    ICON_WATER
            ));
        }

        return alerts;
    }

    /**
     * Verificar temperatura ambiental
     */
    private static List<Alert> checkTemperature(Plant plant, ArduinoResponse data) {
        List<Alert> alerts = new ArrayList<>();
        float temp = data.getTemperature();

        // Temperatura muy baja (<10°C)
        if (temp < 10) {
            alerts.add(createAlert(
                    plant.getId(),
                    ALERT_TYPE_TEMPERATURE,
                    "¡Peligro de Frío!",
                    "La temperatura es muy baja (" + (int)temp + "°C). Protege tu planta de corrientes frías.",
                    SEVERITY_CRITICAL,
                    ICON_TEMPERATURE
            ));
        }
        // Temperatura baja (10-18°C)
        else if (temp < plant.getOptimalTempMin()) {
            alerts.add(createAlert(
                    plant.getId(),
                    ALERT_TYPE_TEMPERATURE,
                    "Temperatura Baja",
                    "La temperatura está bajando (" + (int)temp + "°C). Considera mover la planta a un lugar más cálido.",
                    SEVERITY_WARNING,
                    ICON_TEMPERATURE
            ));
        }
        // Temperatura muy alta (>30°C)
        else if (temp > 30) {
            alerts.add(createAlert(
                    plant.getId(),
                    ALERT_TYPE_TEMPERATURE,
                    "¡Estrés por Calor!",
                    "La temperatura es muy alta (" + (int)temp + "°C). Mueve tu planta a un lugar más fresco y aumenta la humedad.",
                    SEVERITY_CRITICAL,
                    ICON_TEMPERATURE
            ));
        }

        return alerts;
    }

    /**
     * Verificar humedad ambiental
     */
    private static List<Alert> checkAmbientHumidity(Plant plant, ArduinoResponse data) {
        List<Alert> alerts = new ArrayList<>();
        float humidity = data.getAmbientHumidity();

        // Humedad ambiental crítica baja (<35%)
        if (humidity < 35) {
            alerts.add(createAlert(
                    plant.getId(),
                    ALERT_TYPE_HUMIDITY,
                    "Ambiente Muy Seco",
                    "La humedad ambiental es muy baja (" + (int)humidity + "%). Las puntas de las hojas pueden secarse. Pulveriza agua o usa un humidificador.",
                    SEVERITY_CRITICAL,
                    ICON_HUMIDITY
            ));
        }

        return alerts;
    }

    /**
     * Verificar nivel de agua en el depósito
     */
    private static List<Alert> checkWaterLevel(Plant plant, ArduinoResponse data) {
        List<Alert> alerts = new ArrayList<>();
        float waterLevel = data.getWaterLevel();

        // Nivel de agua crítico (<20%)
        if (waterLevel < 20) {
            alerts.add(createAlert(
                    plant.getId(),
                    ALERT_TYPE_WATER_LEVEL,
                    "Nivel de Agua Bajo",
                    "El depósito de agua está casi vacío (" + (int)waterLevel + "%). Rellénalo pronto.",
                    SEVERITY_WARNING,
                    ICON_TANK
            ));
        }

        return alerts;
    }

    /**
     * Verificar detección de plagas
     */
    private static List<Alert> checkPests(Plant plant, ArduinoResponse data) {
        List<Alert> alerts = new ArrayList<>();
        int pestCount = data.getPestCount();

        // Plagas detectadas
        if (pestCount > 0) {
            alerts.add(createAlert(
                    plant.getId(),
                    ALERT_TYPE_PEST,
                    "¡Plaga Detectada!",
                    "Se han detectado " + pestCount + " posible(s) plaga(s). Revisa tu planta y aplica tratamiento si es necesario.",
                    SEVERITY_CRITICAL,
                    ICON_PEST
            ));
        }

        return alerts;
    }

    /**
     * Verificar condiciones favorables para plagas
     */
    private static List<Alert> checkPestConditions(Plant plant, ArduinoResponse data) {
        List<Alert> alerts = new ArrayList<>();

        // Condiciones para Araña Roja: humedad baja + temperatura alta
        if (data.getAmbientHumidity() < 35 && data.getTemperature() > 30) {
            alerts.add(createAlert(
                    plant.getId(),
                    ALERT_TYPE_PEST,
                    "Riesgo de Araña Roja",
                    "Las condiciones actuales (baja humedad + alta temperatura) favorecen la aparición de araña roja. Aumenta la humedad y revisa el envés de las hojas.",
                    SEVERITY_WARNING,
                    ICON_PEST
            ));
        }

        // Condiciones para hongos: humedad del suelo alta
        if (data.getSoilHumidity() > 70) {
            alerts.add(createAlert(
                    plant.getId(),
                    ALERT_TYPE_PEST,
                    "Riesgo de Hongos",
                    "El exceso de humedad en el suelo favorece enfermedades fúngicas. Verifica el drenaje y asegura buena ventilación.",
                    SEVERITY_WARNING,
                    ICON_PEST
            ));
        }

        return alerts;
    }

    /**
     * Crear un objeto Alert
     */
    private static Alert createAlert(int plantId, String alertType, String title,
                                     String message, String severity, String iconType) {
        Alert alert = new Alert();
        alert.setPlantId(plantId);
        alert.setType(alertType);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setSeverity(severity);
        alert.setIconType(iconType);
        alert.setRead(false);
        alert.setTimestamp(String.valueOf(System.currentTimeMillis()));
        return alert;
    }

    /**
     * Filtrar alertas por severidad
     */
    public static List<Alert> filterBySeverity(List<Alert> alerts, String severity) {
        List<Alert> filtered = new ArrayList<>();
        for (Alert alert : alerts) {
            if (alert.getSeverity().equals(severity)) {
                filtered.add(alert);
            }
        }
        return filtered;
    }

    /**
     * Obtener alertas críticas
     */
    public static List<Alert> getCriticalAlerts(List<Alert> alerts) {
        return filterBySeverity(alerts, SEVERITY_CRITICAL);
    }

    /**
     * Verificar si hay alertas críticas
     */
    public static boolean hasCriticalAlerts(List<Alert> alerts) {
        return !getCriticalAlerts(alerts).isEmpty();
    }
}