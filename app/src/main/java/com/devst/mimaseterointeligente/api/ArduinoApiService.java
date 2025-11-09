package com.devst.mimaseterointeligente.api;

import com.devst.mimaseterointeligente.models.ArduinoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;
import retrofit2.http.Query;

import java.util.Map;

/**
 * Interface de Retrofit para Arduino API
 *
 * UBICACIÓN: app/src/main/java/com/devst/mimaseterointeligente/api/ArduinoApiService.java
 * PROPÓSITO: Definir los endpoints de la API del Arduino Wemos D1 R2
 */
public interface ArduinoApiService {

    /**
     * Obtener lecturas actuales de todos los sensores
     *
     * Endpoint: GET /api/sensors
     *
     * Respuesta esperada del Arduino:
     * {
     *   "soilHumidity": 45.5,
     *   "temperature": 22.3,
     *   "ambientHumidity": 55.0,
     *   "uvLevel": 3.2,
     *   "waterLevel": 75.0,
     *   "pestCount": 0,
     *   "timestamp": 1699564800000
     * }
     */
    @GET("api/sensors")
    Call<ArduinoResponse> getSensorData();

    /**
     * Obtener status del dispositivo Arduino
     *
     * Endpoint: GET /api/status
     *
     * Respuesta esperada:
     * {
     *   "status": "online",
     *   "uptime": 123456,
     *   "wifiSignal": -45,
     *   "freeMemory": 23456
     * }
     */
    @GET("api/status")
    Call<Map<String, Object>> getDeviceStatus();

    /**
     * Activar/desactivar riego manual
     *
     * Endpoint: POST /api/control
     * Body: { "action": "water", "duration": 5000 }
     *
     * @param controlData Mapa con la acción y parámetros
     */
    @POST("api/control")
    Call<Map<String, Object>> sendControlCommand(@Body Map<String, Object> controlData);

    /**
     * Obtener historial de datos (si el Arduino lo soporta)
     *
     * Endpoint: GET /api/history?limit=10
     *
     * @param limit Número de registros históricos a obtener
     */
    @GET("api/history")
    Call<Map<String, Object>> getHistoricalData(@Query("limit") int limit);
}