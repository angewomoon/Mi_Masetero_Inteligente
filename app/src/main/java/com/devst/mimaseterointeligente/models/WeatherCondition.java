package com.devst.mimaseterointeligente.models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo para condiciones climáticas
 *
 * UBICACIÓN: app/src/main/java/com/devst/mimaseterointeligente/models/WeatherCondition.java
 * PROPÓSITO: Representar las condiciones del clima (descripción, íconos)
 */
public class WeatherCondition {

    /**
     * ID de la condición climática
     * Ejemplos:
     * - 800: Despejado
     * - 801: Parcialmente nublado
     * - 500: Lluvia ligera
     * - 200: Tormenta eléctrica
     */
    @SerializedName("id")
    private int id;

    /**
     * Grupo de condiciones climáticas
     * Ejemplos: "Clear", "Clouds", "Rain", "Snow", "Thunderstorm"
     */
    @SerializedName("main")
    private String main;

    /**
     * Descripción detallada del clima (en el idioma configurado)
     * Ejemplos: "cielo claro", "nubes dispersas", "lluvia ligera"
     */
    @SerializedName("description")
    private String description;

    /**
     * Código del ícono del clima
     * Ejemplos: "01d", "02n", "10d"
     * URL del ícono: https://openweathermap.org/img/wn/{icon}@2x.png
     */
    @SerializedName("icon")
    private String icon;

    // ==================== CONSTRUCTORES ====================

    public WeatherCondition() {
        // Constructor vacío
    }

    public WeatherCondition(int id, String main, String description, String icon) {
        this.id = id;
        this.main = main;
        this.description = description;
        this.icon = icon;
    }

    // ==================== GETTERS Y SETTERS ====================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Obtener URL completa del ícono
     */
    public String getIconUrl() {
        if (icon != null && !icon.isEmpty()) {
            return "https://openweathermap.org/img/wn/" + icon + "@2x.png";
        }
        return null;
    }

    /**
     * Capitalizar primera letra de la descripción
     */
    public String getDescriptionCapitalized() {
        if (description != null && !description.isEmpty()) {
            return description.substring(0, 1).toUpperCase() + description.substring(1);
        }
        return "";
    }

    /**
     * Verificar si es de día o noche según el ícono
     */
    public boolean isDaytime() {
        return icon != null && icon.endsWith("d");
    }

    /**
     * Obtener emoji del clima basado en el ID
     */
    public String getWeatherEmoji() {
        // Grupo de clima basado en ID
        if (id >= 200 && id < 300) {
            return "⛈"; // Tormenta eléctrica
        } else if (id >= 300 && id < 400) {
            return "🌦"; // Llovizna
        } else if (id >= 500 && id < 600) {
            return "🌧"; // Lluvia
        } else if (id >= 600 && id < 700) {
            return "❄"; // Nieve
        } else if (id >= 700 && id < 800) {
            return "🌫"; // Niebla/Bruma
        } else if (id == 800) {
            return isDaytime() ? "☀" : "🌙"; // Despejado
        } else if (id > 800) {
            return "☁"; // Nublado
        }
        return "🌤"; // Por defecto
    }

    /**
     * Verificar si hay precipitación
     */
    public boolean hasPrecipitation() {
        return id >= 200 && id < 700;
    }

    /**
     * Verificar si está despejado
     */
    public boolean isClear() {
        return id == 800;
    }

    /**
     * Verificar si está nublado
     */
    public boolean isCloudy() {
        return id > 800 && id < 900;
    }

    /**
     * Verificar si hay tormenta
     */
    public boolean isThunderstorm() {
        return id >= 200 && id < 300;
    }

    /**
     * Verificar si está lloviendo
     */
    public boolean isRaining() {
        return (id >= 300 && id < 600);
    }

    @Override
    public String toString() {
        return "WeatherCondition{" +
                "id=" + id +
                ", main='" + main + '\'' +
                ", description='" + description + '\'' +
                ", icon='" + icon + '\'' +
                ", emoji='" + getWeatherEmoji() + '\'' +
                '}';
    }
}
