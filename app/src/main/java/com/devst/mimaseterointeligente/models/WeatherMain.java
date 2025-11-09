package com.devst.mimaseterointeligente.models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo para datos principales del clima
 *
 * UBICACIÓN: app/src/main/java/com/devst/mimaseterointeligente/models/WeatherMain.java
 * PROPÓSITO: Contener los datos principales de temperatura, humedad y presión
 */
public class WeatherMain {

    /**
     * Temperatura actual (°C cuando units=metric)
     */
    @SerializedName("temp")
    private float temp;

    /**
     * Sensación térmica (°C cuando units=metric)
     */
    @SerializedName("feels_like")
    private float feelsLike;

    /**
     * Temperatura mínima en este momento
     */
    @SerializedName("temp_min")
    private float tempMin;

    /**
     * Temperatura máxima en este momento
     */
    @SerializedName("temp_max")
    private float tempMax;

    /**
     * Presión atmosférica (hPa)
     */
    @SerializedName("pressure")
    private int pressure;

    /**
     * Humedad relativa (%)
     */
    @SerializedName("humidity")
    private int humidity;

    /**
     * Presión atmosférica al nivel del mar (hPa)
     */
    @SerializedName("sea_level")
    private Integer seaLevel;

    /**
     * Presión atmosférica a nivel del suelo (hPa)
     */
    @SerializedName("grnd_level")
    private Integer grndLevel;

    // ==================== CONSTRUCTORES ====================

    public WeatherMain() {
        // Constructor vacío
    }

    public WeatherMain(float temp, float feelsLike, float tempMin, float tempMax,
                       int pressure, int humidity) {
        this.temp = temp;
        this.feelsLike = feelsLike;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.pressure = pressure;
        this.humidity = humidity;
    }

    // ==================== GETTERS Y SETTERS ====================

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public float getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(float feelsLike) {
        this.feelsLike = feelsLike;
    }

    public float getTempMin() {
        return tempMin;
    }

    public void setTempMin(float tempMin) {
        this.tempMin = tempMin;
    }

    public float getTempMax() {
        return tempMax;
    }

    public void setTempMax(float tempMax) {
        this.tempMax = tempMax;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public Integer getSeaLevel() {
        return seaLevel;
    }

    public void setSeaLevel(Integer seaLevel) {
        this.seaLevel = seaLevel;
    }

    public Integer getGrndLevel() {
        return grndLevel;
    }

    public void setGrndLevel(Integer grndLevel) {
        this.grndLevel = grndLevel;
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Obtener temperatura redondeada
     */
    public int getTempRounded() {
        return Math.round(temp);
    }

    /**
     * Obtener temperatura formateada
     */
    public String getTempFormatted() {
        return getTempRounded() + "°C";
    }

    /**
     * Obtener rango de temperatura
     */
    public String getTempRange() {
        return Math.round(tempMin) + "°C - " + Math.round(tempMax) + "°C";
    }

    /**
     * Verificar si hace calor (>28°C)
     */
    public boolean isHot() {
        return temp > 28;
    }

    /**
     * Verificar si hace frío (<15°C)
     */
    public boolean isCold() {
        return temp < 15;
    }

    /**
     * Verificar si la humedad es alta (>70%)
     */
    public boolean isHumid() {
        return humidity > 70;
    }

    /**
     * Verificar si la humedad es baja (<30%)
     */
    public boolean isDry() {
        return humidity < 30;
    }

    @Override
    public String toString() {
        return "WeatherMain{" +
                "temp=" + temp + "°C" +
                ", feelsLike=" + feelsLike + "°C" +
                ", tempMin=" + tempMin + "°C" +
                ", tempMax=" + tempMax + "°C" +
                ", pressure=" + pressure + " hPa" +
                ", humidity=" + humidity + "%" +
                '}';
    }
}