package com.devst.mimaseterointeligente.utils;

import com.devst.mimaseterointeligente.models.ArduinoResponse;
import com.devst.mimaseterointeligente.models.Plant;

/**
 * Helper para evaluar el estado de los sensores
 *
 * UBICACIÓN: app/src/main/java/com/devst/mimaseterointeligente/utils/SensorStatusHelper.java
 * PROPÓSITO: Proporcionar métodos para determinar el estado (OK, WARNING, CRITICAL) de cada sensor
 */
public class SensorStatusHelper {

    // Estados posibles
    public static final String STATUS_OPTIMAL = "OPTIMAL";      // Verde
    public static final String STATUS_WARNING = "WARNING";      // Amarillo
    public static final String STATUS_CRITICAL = "CRITICAL";    // Rojo

    // Colores asociados (para UI)
    public static final int COLOR_GREEN = 0xFF4CAF50;   // Verde
    public static final int COLOR_YELLOW = 0xFFFFC107;  // Amarillo
    public static final int COLOR_RED = 0xFFF44336;     // Rojo

    /**
     * Clase auxiliar para almacenar el estado de un sensor
     */
    public static class SensorStatus {
        private String status;
        private String message;
        private int color;

        public SensorStatus(String status, String message, int color) {
            this.status = status;
            this.message = message;
            this.color = color;
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public int getColor() { return color; }

        public boolean isOptimal() { return status.equals(STATUS_OPTIMAL); }
        public boolean isWarning() { return status.equals(STATUS_WARNING); }
        public boolean isCritical() { return status.equals(STATUS_CRITICAL); }
    }

    // ==================== HUMEDAD DEL SUELO ====================

    /**
     * Evaluar estado de humedad del suelo
     */
    public static SensorStatus evaluateSoilHumidity(Plant plant, float currentValue) {
        if (currentValue < 15) {
            return new SensorStatus(
                    STATUS_CRITICAL,
                    "Sequía crítica - ¡Riego urgente!",
                    COLOR_RED
            );
        } else if (currentValue < plant.getOptimalSoilHumidityMin()) {
            return new SensorStatus(
                    STATUS_WARNING,
                    "Nivel bajo - Considera regar",
                    COLOR_YELLOW
            );
        } else if (currentValue > 70) {
            return new SensorStatus(
                    STATUS_CRITICAL,
                    "¡Exceso de agua! Riesgo de pudrición",
                    COLOR_RED
            );
        } else if (currentValue > plant.getOptimalSoilHumidityMax()) {
            return new SensorStatus(
                    STATUS_WARNING,
                    "Nivel alto - Reduce el riego",
                    COLOR_YELLOW
            );
        } else {
            return new SensorStatus(
                    STATUS_OPTIMAL,
                    "Nivel ideal de humedad",
                    COLOR_GREEN
            );
        }
    }

    /**
     * Obtener mensaje para humedad del suelo
     */
    public static String getSoilHumidityMessage(Plant plant, float currentValue) {
        return String.format("Humedad: %.0f%% (Óptimo: %.0f%%-%.0f%%)",
                currentValue,
                plant.getOptimalSoilHumidityMin(),
                plant.getOptimalSoilHumidityMax());
    }

    // ==================== TEMPERATURA ====================

    /**
     * Evaluar estado de temperatura
     */
    public static SensorStatus evaluateTemperature(Plant plant, float currentValue) {
        if (currentValue < 10) {
            return new SensorStatus(
                    STATUS_CRITICAL,
                    "¡Peligro de frío extremo!",
                    COLOR_RED
            );
        } else if (currentValue < plant.getOptimalTempMin()) {
            return new SensorStatus(
                    STATUS_WARNING,
                    "Temperatura baja - Protege tu planta",
                    COLOR_YELLOW
            );
        } else if (currentValue > 30) {
            return new SensorStatus(
                    STATUS_CRITICAL,
                    "¡Estrés por calor! Mueve a lugar fresco",
                    COLOR_RED
            );
        } else if (currentValue > plant.getOptimalTempMax()) {
            return new SensorStatus(
                    STATUS_WARNING,
                    "Temperatura alta - Ventila el espacio",
                    COLOR_YELLOW
            );
        } else {
            return new SensorStatus(
                    STATUS_OPTIMAL,
                    "Temperatura ideal",
                    COLOR_GREEN
            );
        }
    }

    /**
     * Obtener mensaje para temperatura
     */
    public static String getTemperatureMessage(Plant plant, float currentValue) {
        return String.format("Temperatura: %.1f°C (Óptimo: %.0f°C-%.0f°C)",
                currentValue,
                plant.getOptimalTempMin(),
                plant.getOptimalTempMax());
    }

    // ==================== HUMEDAD AMBIENTAL ====================

    /**
     * Evaluar estado de humedad ambiental
     */
    public static SensorStatus evaluateAmbientHumidity(Plant plant, float currentValue) {
        if (currentValue < 35) {
            return new SensorStatus(
                    STATUS_CRITICAL,
                    "Ambiente muy seco - Aumenta humedad",
                    COLOR_RED
            );
        } else if (currentValue < plant.getOptimalAmbientHumidityMin()) {
            return new SensorStatus(
                    STATUS_WARNING,
                    "Humedad baja - Pulveriza agua",
                    COLOR_YELLOW
            );
        } else if (currentValue > plant.getOptimalAmbientHumidityMax()) {
            return new SensorStatus(
                    STATUS_WARNING,
                    "Humedad alta - Mejora ventilación",
                    COLOR_YELLOW
            );
        } else {
            return new SensorStatus(
                    STATUS_OPTIMAL,
                    "Humedad ambiental ideal",
                    COLOR_GREEN
            );
        }
    }

    /**
     * Obtener mensaje para humedad ambiental
     */
    public static String getAmbientHumidityMessage(Plant plant, float currentValue) {
        return String.format("Humedad Amb.: %.0f%% (Óptimo: %.0f%%-%.0f%%)",
                currentValue,
                plant.getOptimalAmbientHumidityMin(),
                plant.getOptimalAmbientHumidityMax());
    }

    // ==================== NIVEL DE LUZ UV ====================

    /**
     * Evaluar nivel de luz UV
     */
    public static SensorStatus evaluateUvLevel(float currentValue) {
        if (currentValue < 1) {
            return new SensorStatus(
                    STATUS_CRITICAL,
                    "Muy poca luz - Mueve a lugar luminoso",
                    COLOR_RED
            );
        } else if (currentValue < 3) {
            return new SensorStatus(
                    STATUS_WARNING,
                    "Luz insuficiente - Acerca a ventana",
                    COLOR_YELLOW
            );
        } else if (currentValue > 8) {
            return new SensorStatus(
                    STATUS_CRITICAL,
                    "¡Sol directo! Riesgo de quemadura",
                    COLOR_RED
            );
        } else if (currentValue > 6) {
            return new SensorStatus(
                    STATUS_WARNING,
                    "Luz intensa - Considera filtrar sol",
                    COLOR_YELLOW
            );
        } else {
            return new SensorStatus(
                    STATUS_OPTIMAL,
                    "Nivel de luz perfecto",
                    COLOR_GREEN
            );
        }
    }

    /**
     * Obtener descripción del nivel de UV
     */
    public static String getUvLevelDescription(float uvLevel) {
        if (uvLevel < 1) return "Muy bajo (Sombra)";
        else if (uvLevel < 3) return "Bajo (Sombra parcial)";
        else if (uvLevel < 6) return "Moderado (Luz indirecta)";
        else if (uvLevel < 8) return "Alto (Luz brillante)";
        else return "Muy alto (Sol directo)";
    }

    // ==================== NIVEL DE AGUA ====================

    /**
     * Evaluar nivel de agua en depósito
     */
    public static SensorStatus evaluateWaterLevel(float currentValue) {
        if (currentValue < 20) {
            return new SensorStatus(
                    STATUS_CRITICAL,
                    "¡Depósito casi vacío! Rellena ahora",
                    COLOR_RED
            );
        } else if (currentValue < 40) {
            return new SensorStatus(
                    STATUS_WARNING,
                    "Nivel bajo - Rellena pronto",
                    COLOR_YELLOW
            );
        } else {
            return new SensorStatus(
                    STATUS_OPTIMAL,
                    "Nivel de agua adecuado",
                    COLOR_GREEN
            );
        }
    }

    /**
     * Obtener mensaje para nivel de agua
     */
    public static String getWaterLevelMessage(float currentValue) {
        return String.format("Nivel de Agua: %.0f%%", currentValue);
    }

    // ==================== PLAGAS ====================

    /**
     * Evaluar detección de plagas
     */
    public static SensorStatus evaluatePests(int pestCount) {
        if (pestCount > 5) {
            return new SensorStatus(
                    STATUS_CRITICAL,
                    "¡Infestación severa! Trata inmediatamente",
                    COLOR_RED
            );
        } else if (pestCount > 0) {
            return new SensorStatus(
                    STATUS_WARNING,
                    "Plagas detectadas - Inspecciona tu planta",
                    COLOR_YELLOW
            );
        } else {
            return new SensorStatus(
                    STATUS_OPTIMAL,
                    "Sin plagas detectadas",
                    COLOR_GREEN
            );
        }
    }

    // ==================== ESTADO GENERAL ====================

    /**
     * Evaluar estado general de la planta
     */
    public static SensorStatus evaluateOverallStatus(Plant plant, ArduinoResponse data) {
        int criticalCount = 0;
        int warningCount = 0;

        // Contar problemas
        if (evaluateSoilHumidity(plant, data.getSoilHumidity()).isCritical()) criticalCount++;
        else if (evaluateSoilHumidity(plant, data.getSoilHumidity()).isWarning()) warningCount++;

        if (evaluateTemperature(plant, data.getTemperature()).isCritical()) criticalCount++;
        else if (evaluateTemperature(plant, data.getTemperature()).isWarning()) warningCount++;

        if (evaluateAmbientHumidity(plant, data.getAmbientHumidity()).isCritical()) criticalCount++;
        else if (evaluateAmbientHumidity(plant, data.getAmbientHumidity()).isWarning()) warningCount++;

        if (evaluateUvLevel(data.getUvLevel()).isCritical()) criticalCount++;
        else if (evaluateUvLevel(data.getUvLevel()).isWarning()) warningCount++;

        if (evaluatePests(data.getPestCount()).isCritical()) criticalCount++;
        else if (evaluatePests(data.getPestCount()).isWarning()) warningCount++;

        // Determinar estado general
        if (criticalCount > 0) {
            return new SensorStatus(
                    STATUS_CRITICAL,
                    "¡Tu planta necesita atención urgente! (" + criticalCount + " problema(s) crítico(s))",
                    COLOR_RED
            );
        } else if (warningCount > 0) {
            return new SensorStatus(
                    STATUS_WARNING,
                    "Tu planta necesita algunos ajustes (" + warningCount + " advertencia(s))",
                    COLOR_YELLOW
            );
        } else {
            return new SensorStatus(
                    STATUS_OPTIMAL,
                    "¡Tu planta está saludable! 🌱",
                    COLOR_GREEN
            );
        }
    }
}