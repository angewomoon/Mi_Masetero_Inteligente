package com.devst.mimaseterointeligente.api;

public class ApiConfig {

    // ==================== ARDUINO API ====================

    /**
     * URL base del Arduino Wemos D1 R2 (ESP8266)
     * IMPORTANTE: Reemplazar con la IP real de tu Arduino en tu red local
     * Ejemplo: "http://192.168.1.100:80" o "http://10.0.0.50:80"
     */
    public static final String ARDUINO_BASE_URL = "http://192.168.1.100:80";

    /**
     * Endpoints del Arduino
     */
    public static final String ARDUINO_SENSORS_ENDPOINT = "/api/sensors";
    public static final String ARDUINO_STATUS_ENDPOINT = "/api/status";
    public static final String ARDUINO_CONTROL_ENDPOINT = "/api/control";

    /**
     * Timeout para conexión con Arduino (en segundos)
     */
    public static final int ARDUINO_CONNECT_TIMEOUT = 10;
    public static final int ARDUINO_READ_TIMEOUT = 10;
    public static final int ARDUINO_WRITE_TIMEOUT = 10;


    // ==================== WEATHER API (OpenWeatherMap) ====================

    /**
     * URL base de OpenWeatherMap
     */
    public static final String WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/";

    /**
     * API Key de OpenWeatherMap
     * IMPORTANTE: Reemplazar con tu propia API key
     * Obtener en: https://openweathermap.org/api
     */
    public static final String WEATHER_API_KEY = "TU_API_KEY_AQUI";

    /**
     * Unidades para la API del clima
     * "metric" = Celsius, "imperial" = Fahrenheit, "standard" = Kelvin
     */
    public static final String WEATHER_UNITS = "metric";

    /**
     * Idioma para respuestas del clima
     * "es" = Español, "en" = English
     */
    public static final String WEATHER_LANG = "es";

    /**
     * Timeout para API del clima (en segundos)
     */
    public static final int WEATHER_CONNECT_TIMEOUT = 15;
    public static final int WEATHER_READ_TIMEOUT = 15;
    public static final int WEATHER_WRITE_TIMEOUT = 15;


    // ==================== CONFIGURACIÓN GENERAL ====================

    /**
     * Habilitar logs de Retrofit (para debug)
     */
    public static final boolean ENABLE_LOGGING = true;

    /**
     * Intervalo de actualización de sensores (en milisegundos)
     * 30000 ms = 30 segundos
     */
    public static final long SENSOR_UPDATE_INTERVAL = 30000; // 30 segundos

    /**
     * Intervalo de actualización del clima (en milisegundos)
     * 1800000 ms = 30 minutos
     */
    public static final long WEATHER_UPDATE_INTERVAL = 1800000; // 30 minutos

    /**
     * Máximo de reintentos en caso de error
     */
    public static final int MAX_RETRIES = 3;

    /**
     * Tiempo de espera entre reintentos (en milisegundos)
     */
    public static final long RETRY_DELAY = 2000; // 2 segundos


    // ==================== MÉTODOS HELPER ====================

    /**
     * Obtener URL completa del Arduino para sensores
     */
    public static String getArduinoSensorsUrl() {
        return ARDUINO_BASE_URL + ARDUINO_SENSORS_ENDPOINT;
    }

    /**
     * Obtener URL completa del Arduino para status
     */
    public static String getArduinoStatusUrl() {
        return ARDUINO_BASE_URL + ARDUINO_STATUS_ENDPOINT;
    }

    /**
     * Verificar si la API key del clima está configurada
     */
    public static boolean isWeatherApiKeyConfigured() {
        return !WEATHER_API_KEY.equals("TU_API_KEY_AQUI") &&
                !WEATHER_API_KEY.isEmpty();
    }

    /**
     * Verificar si la URL del Arduino está configurada
     */
    public static boolean isArduinoUrlConfigured() {
        return !ARDUINO_BASE_URL.equals("http://192.168.1.100:80") &&
                !ARDUINO_BASE_URL.isEmpty();
    }

    /**
     * Obtener mensaje de error para configuración faltante
     */
    public static String getConfigurationErrorMessage() {
        StringBuilder message = new StringBuilder("Configuración incompleta:\n");

        if (!isArduinoUrlConfigured()) {
            message.append("- Falta configurar la URL del Arduino\n");
        }

        if (!isWeatherApiKeyConfigured()) {
            message.append("- Falta configurar la API Key del clima\n");
        }

        return message.toString();
    }
}