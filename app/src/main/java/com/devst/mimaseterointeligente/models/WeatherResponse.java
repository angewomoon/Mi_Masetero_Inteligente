package com.devst.mimaseterointeligente.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Modelo de respuesta de OpenWeatherMap API
 *
 * UBICACIÓN: app/src/main/java/com/devst/mimaseterointeligente/models/WeatherResponse.java
 * PROPÓSITO: Mapear la respuesta JSON de la API del clima a un objeto Java
 */
public class WeatherResponse {

    /**
     * Datos principales del clima (temperatura, humedad, presión)
     */
    @SerializedName("main")
    private WeatherMain main;

    /**
     * Lista de condiciones climáticas (puede tener múltiples)
     */
    @SerializedName("weather")
    private List<WeatherCondition> weather;

    /**
     * Nombre de la ciudad
     */
    @SerializedName("name")
    private String cityName;

    /**
     * Código de respuesta HTTP
     * 200 = OK
     */
    @SerializedName("cod")
    private int cod;

    /**
     * Timestamp de la lectura
     */
    @SerializedName("dt")
    private long dt;

    /**
     * Zona horaria en segundos desde UTC
     */
    @SerializedName("timezone")
    private int timezone;

    /**
     * Datos de viento (opcional)
     */
    @SerializedName("wind")
    private Wind wind;

    /**
     * Datos de nubes (opcional)
     */
    @SerializedName("clouds")
    private Clouds clouds;

    /**
     * Datos del sistema (país, amanecer, atardecer)
     */
    @SerializedName("sys")
    private Sys sys;

    // ==================== CLASES INTERNAS ====================

    /**
     * Datos de viento
     */
    public static class Wind {
        @SerializedName("speed")
        private float speed; // velocidad en m/s

        @SerializedName("deg")
        private int deg; // dirección en grados

        public float getSpeed() {
            return speed;
        }

        public int getDeg() {
            return deg;
        }
    }

    /**
     * Datos de nubes
     */
    public static class Clouds {
        @SerializedName("all")
        private int all; // porcentaje de nubosidad (0-100)

        public int getAll() {
            return all;
        }
    }

    /**
     * Datos del sistema
     */
    public static class Sys {
        @SerializedName("country")
        private String country; // código del país (ej: "CL")

        @SerializedName("sunrise")
        private long sunrise; // timestamp del amanecer

        @SerializedName("sunset")
        private long sunset; // timestamp del atardecer

        public String getCountry() {
            return country;
        }

        public long getSunrise() {
            return sunrise;
        }

        public long getSunset() {
            return sunset;
        }
    }

    // ==================== CONSTRUCTORES ====================

    public WeatherResponse() {
        // Constructor vacío
    }

    // ==================== GETTERS Y SETTERS ====================

    public WeatherMain getMain() {
        return main;
    }

    public void setMain(WeatherMain main) {
        this.main = main;
    }

    public List<WeatherCondition> getWeather() {
        return weather;
    }

    public void setWeather(List<WeatherCondition> weather) {
        this.weather = weather;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCod() {
        return cod;
    }

    public void setCod(int cod) {
        this.cod = cod;
    }

    public long getDt() {
        return dt;
    }

    public void setDt(long dt) {
        this.dt = dt;
    }

    public int getTimezone() {
        return timezone;
    }

    public void setTimezone(int timezone) {
        this.timezone = timezone;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public void setClouds(Clouds clouds) {
        this.clouds = clouds;
    }

    public Sys getSys() {
        return sys;
    }

    public void setSys(Sys sys) {
        this.sys = sys;
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Verificar si la respuesta es exitosa
     */
    public boolean isSuccessful() {
        return cod == 200;
    }

    /**
     * Obtener la primera condición climática (la principal)
     */
    public WeatherCondition getFirstWeatherCondition() {
        if (weather != null && !weather.isEmpty()) {
            return weather.get(0);
        }
        return null;
    }

    /**
     * Obtener temperatura en formato legible
     */
    public String getTemperatureFormatted() {
        if (main != null) {
            return Math.round(main.getTemp()) + "°C";
        }
        return "N/A";
    }

    /**
     * Obtener descripción del clima
     */
    public String getWeatherDescription() {
        WeatherCondition condition = getFirstWeatherCondition();
        if (condition != null) {
            return condition.getDescription();
        }
        return "No disponible";
    }

    /**
     * Obtener ícono del clima
     */
    public String getWeatherIcon() {
        WeatherCondition condition = getFirstWeatherCondition();
        if (condition != null) {
            return condition.getIcon();
        }
        return "";
    }

    @Override
    public String toString() {
        return "WeatherResponse{" +
                "cityName='" + cityName + '\'' +
                ", temperature=" + (main != null ? main.getTemp() : "N/A") + "°C" +
                ", humidity=" + (main != null ? main.getHumidity() : "N/A") + "%" +
                ", description='" + getWeatherDescription() + '\'' +
                '}';
    }
}