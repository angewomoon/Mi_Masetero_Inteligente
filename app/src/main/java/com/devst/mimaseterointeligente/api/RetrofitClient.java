package com.devst.mimaseterointeligente.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Cliente Singleton de Retrofit
 *
 * UBICACIÓN: app/src/main/java/com/devst/mimaseterointeligente/api/RetrofitClient.java
 * PROPÓSITO: Proporcionar instancias únicas de Retrofit para Arduino y Weather API
 */
public class RetrofitClient {

    // Instancias singleton
    private static Retrofit arduinoRetrofit = null;
    private static Retrofit weatherRetrofit = null;

    // Servicios
    private static ArduinoApiService arduinoService = null;
    private static WeatherApiService weatherService = null;

    /**
     * Constructor privado para evitar instanciación
     */
    private RetrofitClient() {
        // Constructor privado
    }

    // ==================== RETROFIT PARA ARDUINO ====================

    /**
     * Obtener instancia de Retrofit para Arduino
     */
    public static Retrofit getArduinoRetrofitInstance() {
        if (arduinoRetrofit == null) {
            synchronized (RetrofitClient.class) {
                if (arduinoRetrofit == null) {
                    arduinoRetrofit = new Retrofit.Builder()
                            .baseUrl(ApiConfig.ARDUINO_BASE_URL)
                            .client(createArduinoOkHttpClient())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return arduinoRetrofit;
    }

    /**
     * Crear cliente OkHttp configurado para Arduino
     */
    private static OkHttpClient createArduinoOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(ApiConfig.ARDUINO_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiConfig.ARDUINO_READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ApiConfig.ARDUINO_WRITE_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);

        // Agregar logging si está habilitado
        if (ApiConfig.ENABLE_LOGGING) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }

        return builder.build();
    }

    /**
     * Obtener servicio de API del Arduino
     */
    public static ArduinoApiService getArduinoApiService() {
        if (arduinoService == null) {
            synchronized (RetrofitClient.class) {
                if (arduinoService == null) {
                    arduinoService = getArduinoRetrofitInstance()
                            .create(ArduinoApiService.class);
                }
            }
        }
        return arduinoService;
    }

    /**
     * Reiniciar conexión con Arduino (útil si cambia la IP)
     */
    public static void resetArduinoClient() {
        synchronized (RetrofitClient.class) {
            arduinoRetrofit = null;
            arduinoService = null;
        }
    }


    // ==================== RETROFIT PARA WEATHER API ====================

    /**
     * Obtener instancia de Retrofit para Weather API
     */
    public static Retrofit getWeatherRetrofitInstance() {
        if (weatherRetrofit == null) {
            synchronized (RetrofitClient.class) {
                if (weatherRetrofit == null) {
                    weatherRetrofit = new Retrofit.Builder()
                            .baseUrl(ApiConfig.WEATHER_BASE_URL)
                            .client(createWeatherOkHttpClient())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return weatherRetrofit;
    }

    /**
     * Crear cliente OkHttp configurado para Weather API
     */
    private static OkHttpClient createWeatherOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(ApiConfig.WEATHER_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiConfig.WEATHER_READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ApiConfig.WEATHER_WRITE_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);

        // Agregar logging si está habilitado
        if (ApiConfig.ENABLE_LOGGING) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }

        return builder.build();
    }

    /**
     * Obtener servicio de API del Clima
     */
    public static WeatherApiService getWeatherApiService() {
        if (weatherService == null) {
            synchronized (RetrofitClient.class) {
                if (weatherService == null) {
                    weatherService = getWeatherRetrofitInstance()
                            .create(WeatherApiService.class);
                }
            }
        }
        return weatherService;
    }

    /**
     * Reiniciar conexión con Weather API
     */
    public static void resetWeatherClient() {
        synchronized (RetrofitClient.class) {
            weatherRetrofit = null;
            weatherService = null;
        }
    }


    // ==================== MÉTODOS GENERALES ====================

    /**
     * Reiniciar todos los clientes
     */
    public static void resetAllClients() {
        resetArduinoClient();
        resetWeatherClient();
    }

    /**
     * Verificar si los clientes están inicializados
     */
    public static boolean isArduinoClientInitialized() {
        return arduinoService != null;
    }

    public static boolean isWeatherClientInitialized() {
        return weatherService != null;
    }
}