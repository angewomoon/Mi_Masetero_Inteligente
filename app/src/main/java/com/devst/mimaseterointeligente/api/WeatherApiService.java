package com.devst.mimaseterointeligente.api;

import com.devst.mimaseterointeligente.models.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface de Retrofit para Weather API (OpenWeatherMap)
 *
 * UBICACIÓN: app/src/main/java/com/devst/mimaseterointeligente/api/WeatherApiService.java
 * PROPÓSITO: Definir los endpoints de la API del clima de OpenWeatherMap
 */
public interface WeatherApiService {

    /**
     * Obtener clima actual por nombre de ciudad
     *
     * Endpoint: GET /weather?q={city}&appid={apiKey}&units={units}&lang={lang}
     *
     * @param city Nombre de la ciudad (ej: "Santiago")
     * @param apiKey API Key de OpenWeatherMap
     * @param units Unidades (metric, imperial, standard)
     * @param lang Idioma (es, en, etc.)
     *
     * Ejemplo de respuesta:
     * {
     *   "coord": { "lon": -70.6483, "lat": -33.4569 },
     *   "weather": [
     *     {
     *       "id": 800,
     *       "main": "Clear",
     *       "description": "cielo claro",
     *       "icon": "01d"
     *     }
     *   ],
     *   "main": {
     *     "temp": 22.5,
     *     "feels_like": 21.8,
     *     "temp_min": 20.0,
     *     "temp_max": 25.0,
     *     "pressure": 1013,
     *     "humidity": 55
     *   },
     *   "wind": { "speed": 3.5, "deg": 180 },
     *   "clouds": { "all": 0 },
     *   "dt": 1699564800,
     *   "sys": {
     *     "country": "CL",
     *     "sunrise": 1699520400,
     *     "sunset": 1699570800
     *   },
     *   "timezone": -10800,
     *   "id": 3871336,
     *   "name": "Santiago",
     *   "cod": 200
     * }
     */
    @GET("weather")
    Call<WeatherResponse> getCurrentWeatherByCity(
            @Query("q") String city,
            @Query("appid") String apiKey,
            @Query("units") String units,
            @Query("lang") String lang
    );

    /**
     * Obtener clima actual por coordenadas geográficas
     *
     * Endpoint: GET /weather?lat={lat}&lon={lon}&appid={apiKey}&units={units}&lang={lang}
     *
     * @param lat Latitud
     * @param lon Longitud
     * @param apiKey API Key de OpenWeatherMap
     * @param units Unidades (metric, imperial, standard)
     * @param lang Idioma (es, en, etc.)
     */
    @GET("weather")
    Call<WeatherResponse> getCurrentWeatherByCoordinates(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units,
            @Query("lang") String lang
    );

    /**
     * Obtener pronóstico del tiempo (5 días, cada 3 horas)
     *
     * Endpoint: GET /forecast?q={city}&appid={apiKey}&units={units}&lang={lang}
     *
     * @param city Nombre de la ciudad
     * @param apiKey API Key de OpenWeatherMap
     * @param units Unidades (metric, imperial, standard)
     * @param lang Idioma (es, en, etc.)
     */
    @GET("forecast")
    Call<WeatherResponse> getForecastByCity(
            @Query("q") String city,
            @Query("appid") String apiKey,
            @Query("units") String units,
            @Query("lang") String lang
    );
}