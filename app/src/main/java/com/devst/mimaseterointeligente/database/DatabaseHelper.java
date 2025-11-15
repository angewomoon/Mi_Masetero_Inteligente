package com.devst.mimaseterointeligente.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.devst.mimaseterointeligente.models.Alert;
import com.devst.mimaseterointeligente.models.Plant;
import com.devst.mimaseterointeligente.models.SensorData;
import com.devst.mimaseterointeligente.models.User;
import com.devst.mimaseterointeligente.utils.PasswordUtils;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Información de la base de datos
    private static final String DATABASE_NAME = "MaseteroInteligente.db";
    private static final int DATABASE_VERSION = 2;

    // Tablas
    private static final String TABLE_USERS = "users";
    private static final String TABLE_PLANTS = "plants";
    private static final String TABLE_SENSOR_DATA = "sensor_data";
    private static final String TABLE_ALERTS = "alerts";

    // Columnas comunes
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_UPDATED_AT = "updated_at";

    // Columnas de la tabla users
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_PROFILE_IMAGE = "profile_image";
    private static final String KEY_GOOGLE_ID = "google_id";

    // Columnas de la tabla plants
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_PLANT_NAME = "plant_name";
    private static final String KEY_TYPE = "type";
    private static final String KEY_SPECIES = "species";
    private static final String KEY_SCIENTIFIC_NAME = "scientific_name";
    private static final String KEY_IMAGE_URL = "image_url";
    private static final String KEY_IS_CONNECTED = "is_connected";
    private static final String KEY_OPTIMAL_SOIL_HUM_MIN = "optimal_soil_hum_min";
    private static final String KEY_OPTIMAL_SOIL_HUM_MAX = "optimal_soil_hum_max";
    private static final String KEY_OPTIMAL_TEMP_MIN = "optimal_temp_min";
    private static final String KEY_OPTIMAL_TEMP_MAX = "optimal_temp_max";
    private static final String KEY_OPTIMAL_AMB_HUM_MIN = "optimal_amb_hum_min";
    private static final String KEY_OPTIMAL_AMB_HUM_MAX = "optimal_amb_hum_max";
    private static final String KEY_OPTIMAL_LIGHT = "optimal_light";

    // Columnas de la tabla sensor_data
    private static final String KEY_PLANT_ID = "plant_id";
    private static final String KEY_SOIL_HUMIDITY = "soil_humidity";
    private static final String KEY_TEMPERATURE = "temperature";
    private static final String KEY_AMBIENT_HUMIDITY = "ambient_humidity";
    private static final String KEY_UV_LEVEL = "uv_level";
    private static final String KEY_WATER_LEVEL = "water_level";
    private static final String KEY_PEST_COUNT = "pest_count";
    private static final String KEY_TIMESTAMP = "timestamp";

    // Columnas de la tabla alerts
    private static final String KEY_ALERT_TYPE = "alert_type";
    private static final String KEY_TITLE = "title";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_SEVERITY = "severity";
    private static final String KEY_IS_READ = "is_read";
    private static final String KEY_ICON_TYPE = "icon_type";

    // Creación de tablas SQL
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_NAME + " TEXT NOT NULL, "
            + KEY_EMAIL + " TEXT UNIQUE NOT NULL, "
            + KEY_PASSWORD + " TEXT NOT NULL, "
            + KEY_PROFILE_IMAGE + " TEXT, "
            + KEY_GOOGLE_ID + " TEXT, "
            + KEY_CREATED_AT + " TEXT, "
            + KEY_UPDATED_AT + " TEXT"
            + ");";
    private static final String CREATE_TABLE_PLANTS = "CREATE TABLE " + TABLE_PLANTS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USER_ID + " INTEGER,"
            + KEY_PLANT_NAME + " TEXT NOT NULL,"
            + KEY_TYPE + " TEXT,"
            + KEY_SPECIES + " TEXT,"
            + KEY_SCIENTIFIC_NAME + " TEXT,"
            + KEY_IMAGE_URL + " TEXT,"
            + KEY_IS_CONNECTED + " INTEGER DEFAULT 0,"
            + KEY_OPTIMAL_SOIL_HUM_MIN + " REAL,"
            + KEY_OPTIMAL_SOIL_HUM_MAX + " REAL,"
            + KEY_OPTIMAL_TEMP_MIN + " REAL,"
            + KEY_OPTIMAL_TEMP_MAX + " REAL,"
            + KEY_OPTIMAL_AMB_HUM_MIN + " REAL,"
            + KEY_OPTIMAL_AMB_HUM_MAX + " REAL,"
            + KEY_OPTIMAL_LIGHT + " TEXT,"
            + KEY_CREATED_AT + " TEXT,"
            + KEY_UPDATED_AT + " TEXT,"
            + "FOREIGN KEY(" + KEY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + ")"
            + ")";

    private static final String CREATE_TABLE_SENSOR_DATA = "CREATE TABLE " + TABLE_SENSOR_DATA + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_PLANT_ID + " INTEGER,"
            + KEY_SOIL_HUMIDITY + " REAL,"
            + KEY_TEMPERATURE + " REAL,"
            + KEY_AMBIENT_HUMIDITY + " REAL,"
            + KEY_UV_LEVEL + " REAL,"
            + KEY_WATER_LEVEL + " REAL,"
            + KEY_PEST_COUNT + " INTEGER,"
            + KEY_TIMESTAMP + " TEXT,"
            + "FOREIGN KEY(" + KEY_PLANT_ID + ") REFERENCES " + TABLE_PLANTS + "(" + KEY_ID + ")"
            + ")";

    private static final String CREATE_TABLE_ALERTS = "CREATE TABLE " + TABLE_ALERTS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_PLANT_ID + " INTEGER,"
            + KEY_ALERT_TYPE + " TEXT,"
            + KEY_TITLE + " TEXT,"
            + KEY_MESSAGE + " TEXT,"
            + KEY_SEVERITY + " TEXT,"
            + KEY_IS_READ + " INTEGER DEFAULT 0,"
            + KEY_ICON_TYPE + " TEXT,"
            + KEY_TIMESTAMP + " TEXT,"
            + "FOREIGN KEY(" + KEY_PLANT_ID + ") REFERENCES " + TABLE_PLANTS + "(" + KEY_ID + ")"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tablas
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_PLANTS);
        db.execSQL(CREATE_TABLE_SENSOR_DATA);
        db.execSQL(CREATE_TABLE_ALERTS);
        Log.d(TAG, "Database tables created");

        // ✨ AÑADIR DATOS DE PRUEBA INICIALES ✨
        addInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Eliminar tablas antiguas si existen
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALERTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSOR_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Crear tablas nuevas
        onCreate(db);
    }

    /**
     * Añade datos iniciales a la base de datos.
     * Este método es llamado solo cuando la base de datos se crea por primera vez.
     */
    private void addInitialData(SQLiteDatabase db) {
        ContentValues userValues = new ContentValues();
        userValues.put(KEY_NAME, "Usuario de Prueba");
        userValues.put(KEY_EMAIL, "usuario@gmail.com");
        // **CORREGIDO:** Usar el hash SHA-256 correcto para "123456"
        userValues.put(KEY_PASSWORD, "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92");
        long userId1 = db.insert(TABLE_USERS, null, userValues);

        ContentValues plant1Values = new ContentValues();
        plant1Values.put(KEY_USER_ID, userId1);
        plant1Values.put(KEY_PLANT_NAME, "Mi Suculenta");
        plant1Values.put(KEY_TYPE, "Suculenta");
        long plantId1 = db.insert(TABLE_PLANTS, null, plant1Values);

        ContentValues plant2Values = new ContentValues();
        plant2Values.put(KEY_USER_ID, userId1);
        plant2Values.put(KEY_PLANT_NAME, "Helecho del balcón");
        plant2Values.put(KEY_TYPE, "Planta de interior");
        db.insert(TABLE_PLANTS, null, plant2Values);

        long currentTime = System.currentTimeMillis();

        ContentValues alert1 = new ContentValues();
        alert1.put(KEY_PLANT_ID, plantId1);
        alert1.put(KEY_ALERT_TYPE, Alert.TYPE_LOW_SOIL_HUMIDITY);
        alert1.put(KEY_TITLE, "Humedad Baja");
        alert1.put(KEY_MESSAGE, "La humedad de tu planta está por debajo del nivel óptimo. Considera regar.");
        alert1.put(KEY_SEVERITY, Alert.SEVERITY_WARNING);
        alert1.put(KEY_ICON_TYPE, "water");
        alert1.put(KEY_IS_READ, 0);
        alert1.put(KEY_TIMESTAMP, String.valueOf(currentTime - 10 * 60 * 1000)); // Hace 10 minutos
        db.insert(TABLE_ALERTS, null, alert1);

        ContentValues alert2 = new ContentValues();
        alert2.put(KEY_PLANT_ID, plantId1);
        alert2.put(KEY_ALERT_TYPE, Alert.TYPE_LOW_LIGHT);
        alert2.put(KEY_TITLE, "Poca luz");
        alert2.put(KEY_MESSAGE, "Tu planta necesita más luz solar. Muévela a un lugar más iluminado.");
        alert2.put(KEY_SEVERITY, Alert.SEVERITY_WARNING);
        alert2.put(KEY_ICON_TYPE, "sun");
        alert2.put(KEY_IS_READ, 0);
        alert2.put(KEY_TIMESTAMP, String.valueOf(currentTime - 15 * 60 * 1000)); // Hace 15 minutos
        db.insert(TABLE_ALERTS, null, alert2);

        ContentValues alert3 = new ContentValues();
        alert3.put(KEY_PLANT_ID, plantId1);
        alert3.put(KEY_ALERT_TYPE, Alert.TYPE_PEST_DETECTED);
        alert3.put(KEY_TITLE, "¡Plaga Detectada!");
        alert3.put(KEY_MESSAGE, "Se ha detectado una posible plaga. Revisa tu planta.");
        alert3.put(KEY_SEVERITY, Alert.SEVERITY_CRITICAL);
        alert3.put(KEY_ICON_TYPE, "bug");
        alert3.put(KEY_IS_READ, 0);
        alert3.put(KEY_TIMESTAMP, String.valueOf(currentTime - 30 * 60 * 1000)); // Hace 30 minutos
        db.insert(TABLE_ALERTS, null, alert3);

        ContentValues alert4 = new ContentValues();
        alert4.put(KEY_PLANT_ID, plantId1);
        alert4.put(KEY_ALERT_TYPE, Alert.TYPE_LOW_WATER);
        alert4.put(KEY_TITLE, "Nivel de Agua bajo");
        alert4.put(KEY_MESSAGE, "El nivel de agua actualmente es bajo. Considere rellenar.");
        alert4.put(KEY_SEVERITY, Alert.SEVERITY_WARNING);
        alert4.put(KEY_ICON_TYPE, "water");
        alert4.put(KEY_IS_READ, 0);
        alert4.put(KEY_TIMESTAMP, String.valueOf(currentTime - 5 * 60 * 60 * 1000)); // Hace 5 horas
        db.insert(TABLE_ALERTS, null, alert4);

        Log.d(TAG, "Datos de prueba iniciales insertados correctamente.");
    }

    // ... (resto de tus métodos)

    // ========== OPERACIONES DE USUARIOS ==========

    // Crear usuario
    public long createUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, user.getName());
        values.put(KEY_EMAIL, user.getEmail());
        values.put(KEY_PASSWORD, user.getPassword());
        values.put(KEY_PROFILE_IMAGE, user.getProfileImage());
        values.put(KEY_GOOGLE_ID, user.getGoogleId());
        values.put(KEY_CREATED_AT, System.currentTimeMillis());
        values.put(KEY_UPDATED_AT, System.currentTimeMillis());

        long userId = db.insert(TABLE_USERS, null, values);
        db.close();

        return userId;
    }

    // Obtener usuario por email
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS,
                new String[]{KEY_ID, KEY_NAME, KEY_EMAIL, KEY_PASSWORD, KEY_PROFILE_IMAGE, KEY_GOOGLE_ID},
                KEY_EMAIL + "=?",
                new String[]{email},
                null, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
            user.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndex(KEY_EMAIL)));
            user.setPassword(cursor.getString(cursor.getColumnIndex(KEY_PASSWORD)));
            user.setProfileImage(cursor.getString(cursor.getColumnIndex(KEY_PROFILE_IMAGE)));
            user.setGoogleId(cursor.getString(cursor.getColumnIndex(KEY_GOOGLE_ID)));
            cursor.close();
        }

        db.close();
        return user;
    }

    // Actualizar usuario
    public int updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, user.getName());
        values.put(KEY_EMAIL, user.getEmail());
        if (user.getPassword() != null) {
            values.put(KEY_PASSWORD, user.getPassword());
        }
        values.put(KEY_PROFILE_IMAGE, user.getProfileImage());
        values.put(KEY_UPDATED_AT, System.currentTimeMillis());

        int result = db.update(TABLE_USERS, values, KEY_ID + "=?",
                new String[]{String.valueOf(user.getId())});
        db.close();

        return result;
    }

    // ========== OPERACIONES DE PLANTAS ==========

    // Crear planta
    public long createPlant(Plant plant) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, plant.getUserId());
        values.put(KEY_PLANT_NAME, plant.getName());
        values.put(KEY_TYPE, plant.getType());
        values.put(KEY_SPECIES, plant.getSpecies());
        values.put(KEY_SCIENTIFIC_NAME, plant.getScientificName());
        values.put(KEY_IMAGE_URL, plant.getImageUrl());
        values.put(KEY_IS_CONNECTED, plant.isConnected() ? 1 : 0);
        values.put(KEY_OPTIMAL_SOIL_HUM_MIN, plant.getOptimalSoilHumidityMin());
        values.put(KEY_OPTIMAL_SOIL_HUM_MAX, plant.getOptimalSoilHumidityMax());
        values.put(KEY_OPTIMAL_TEMP_MIN, plant.getOptimalTempMin());
        values.put(KEY_OPTIMAL_TEMP_MAX, plant.getOptimalTempMax());
        values.put(KEY_OPTIMAL_AMB_HUM_MIN, plant.getOptimalAmbientHumidityMin());
        values.put(KEY_OPTIMAL_AMB_HUM_MAX, plant.getOptimalAmbientHumidityMax());
        values.put(KEY_OPTIMAL_LIGHT, plant.getOptimalLightLevel());
        values.put(KEY_CREATED_AT, System.currentTimeMillis());
        values.put(KEY_UPDATED_AT, System.currentTimeMillis());

        long plantId = db.insert(TABLE_PLANTS, null, values);
        db.close();

        return plantId;
    }

    // Obtener todas las plantas de un usuario
    public List<Plant> getUserPlants(int userId) {
        List<Plant> plants = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PLANTS + " WHERE " + KEY_USER_ID + " = " + userId;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Plant plant = new Plant();
                plant.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                plant.setUserId(cursor.getInt(cursor.getColumnIndex(KEY_USER_ID)));
                plant.setName(cursor.getString(cursor.getColumnIndex(KEY_PLANT_NAME)));
                plant.setType(cursor.getString(cursor.getColumnIndex(KEY_TYPE)));
                plant.setSpecies(cursor.getString(cursor.getColumnIndex(KEY_SPECIES)));
                plant.setScientificName(cursor.getString(cursor.getColumnIndex(KEY_SCIENTIFIC_NAME)));
                plant.setImageUrl(cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL)));
                plant.setConnected(cursor.getInt(cursor.getColumnIndex(KEY_IS_CONNECTED)) == 1);

                plants.add(plant);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return plants;
    }

    /**
     * Contar el número de plantas de un usuario.
     * @param userId ID del usuario.
     * @return El número total de plantas del usuario.
     */
    public int getPlantsCountByUserId(int userId) {
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_PLANTS + " WHERE " + KEY_USER_ID + " = " + userId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = 0;
        if(cursor.moveToFirst()){
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    // Obtener planta por ID
    public Plant getPlantById(int plantId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PLANTS, null, KEY_ID + "=?",
                new String[]{String.valueOf(plantId)}, null, null, null, null);

        Plant plant = null;
        if (cursor != null && cursor.moveToFirst()) {
            plant = new Plant();
            plant.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
            plant.setUserId(cursor.getInt(cursor.getColumnIndex(KEY_USER_ID)));
            plant.setName(cursor.getString(cursor.getColumnIndex(KEY_PLANT_NAME)));
            plant.setType(cursor.getString(cursor.getColumnIndex(KEY_TYPE)));
            plant.setSpecies(cursor.getString(cursor.getColumnIndex(KEY_SPECIES)));
            plant.setScientificName(cursor.getString(cursor.getColumnIndex(KEY_SCIENTIFIC_NAME)));
            plant.setImageUrl(cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL)));
            plant.setConnected(cursor.getInt(cursor.getColumnIndex(KEY_IS_CONNECTED)) == 1);
            cursor.close();
        }

        db.close();
        return plant;
    }

    // Eliminar planta
    public void deletePlant(int plantId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Eliminar alertas asociadas
        db.delete(TABLE_ALERTS, KEY_PLANT_ID + "=?", new String[]{String.valueOf(plantId)});
        // Eliminar datos de sensores asociados
        db.delete(TABLE_SENSOR_DATA, KEY_PLANT_ID + "=?", new String[]{String.valueOf(plantId)});
        // Eliminar planta
        db.delete(TABLE_PLANTS, KEY_ID + "=?", new String[]{String.valueOf(plantId)});

        db.close();
    }

    // ========== OPERACIONES DE DATOS DE SENSORES ==========

    // Insertar datos de sensores
    public long insertSensorData(SensorData data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PLANT_ID, data.getPlantId());
        values.put(KEY_SOIL_HUMIDITY, data.getSoilHumidity());
        values.put(KEY_TEMPERATURE, data.getTemperature());
        values.put(KEY_AMBIENT_HUMIDITY, data.getAmbientHumidity());
        values.put(KEY_UV_LEVEL, data.getUvLevel());
        values.put(KEY_WATER_LEVEL, data.getWaterLevel());
        values.put(KEY_PEST_COUNT, data.getPestCount());
        values.put(KEY_TIMESTAMP, data.getTimestamp());

        long id = db.insert(TABLE_SENSOR_DATA, null, values);
        db.close();

        return id;
    }

    // Obtener últimos datos de sensores para una planta
    public SensorData getLatestSensorData(int plantId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_SENSOR_DATA, null, KEY_PLANT_ID + "=?",
                new String[]{String.valueOf(plantId)}, null, null,
                KEY_TIMESTAMP + " DESC", "1");

        SensorData data = null;
        if (cursor != null && cursor.moveToFirst()) {
            data = new SensorData();
            data.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
            data.setPlantId(cursor.getInt(cursor.getColumnIndex(KEY_PLANT_ID)));
            data.setSoilHumidity(cursor.getFloat(cursor.getColumnIndex(KEY_SOIL_HUMIDITY)));
            data.setTemperature(cursor.getFloat(cursor.getColumnIndex(KEY_TEMPERATURE)));
            data.setAmbientHumidity(cursor.getFloat(cursor.getColumnIndex(KEY_AMBIENT_HUMIDITY)));
            data.setUvLevel(cursor.getFloat(cursor.getColumnIndex(KEY_UV_LEVEL)));
            data.setWaterLevel(cursor.getFloat(cursor.getColumnIndex(KEY_WATER_LEVEL)));
            data.setPestCount(cursor.getInt(cursor.getColumnIndex(KEY_PEST_COUNT)));
            data.setTimestamp(cursor.getString(cursor.getColumnIndex(KEY_TIMESTAMP)));
            cursor.close();
        }

        db.close();
        return data;
    }

    // ========== OPERACIONES DE ALERTAS ==========

    // Crear alerta
    public long createAlert(Alert alert) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PLANT_ID, alert.getPlantId());
        values.put(KEY_ALERT_TYPE, alert.getAlertType());
        values.put(KEY_TITLE, alert.getTitle());
        values.put(KEY_MESSAGE, alert.getMessage());
        values.put(KEY_SEVERITY, alert.getSeverity());
        values.put(KEY_IS_READ, alert.isRead() ? 1 : 0);
        values.put(KEY_ICON_TYPE, alert.getIconType());
        values.put(KEY_TIMESTAMP, alert.getTimestamp());

        long alertId = db.insert(TABLE_ALERTS, null, values);
        db.close();
        return alertId;
    }

    // Obtener todas las alertas
    public List<Alert> getAllAlerts() {
        List<Alert> alerts = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ALERTS + " ORDER BY " + KEY_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Alert alert = new Alert();
                alert.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
                alert.setPlantId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PLANT_ID)));
                alert.setType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ALERT_TYPE)));
                alert.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)));
                alert.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(KEY_MESSAGE)));
                alert.setSeverity(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SEVERITY)));
                alert.setRead(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_READ)) == 1);
                alert.setIconType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ICON_TYPE)));
                alert.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIMESTAMP)));

                alerts.add(alert);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return alerts;
    }

    // Marcar alerta como leída
    public void markAlertAsRead(int alertId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_IS_READ, 1);

        db.update(TABLE_ALERTS, values, KEY_ID + "=?",
                new String[]{String.valueOf(alertId)});
        db.close();
    }

    // Obtener número de alertas no leídas
    public int getUnreadAlertsCount() {
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_ALERTS + " WHERE " + KEY_IS_READ + " = 0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();

        return count;
    }

    /**
     * Obtener plantas conectadas al masetero
     */
    public List<Plant> getConnectedPlants() {
        List<Plant> plants = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_PLANTS,
                null,
                KEY_IS_CONNECTED + "=1",
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Plant plant = cursorToPlant(cursor);
                plants.add(plant);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return plants;
    }

    // Convierte el cursor de PLANTS a un objeto Plant
    private Plant cursorToPlant(Cursor cursor) {
        Plant plant = new Plant();

        plant.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
        plant.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ID)));
        plant.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PLANT_NAME)));
        plant.setType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TYPE)));
        plant.setSpecies(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SPECIES)));
        plant.setScientificName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SCIENTIFIC_NAME)));
        plant.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE_URL)));
        plant.setConnected(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_CONNECTED)) == 1);

        // Campos opcionales — solo si existen en tu modelo
        try { plant.setOptimalSoilHumidityMin(cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_OPTIMAL_SOIL_HUM_MIN))); } catch (Exception ignored) {}
        try { plant.setOptimalSoilHumidityMax(cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_OPTIMAL_SOIL_HUM_MAX))); } catch (Exception ignored) {}
        try { plant.setOptimalTempMin(cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_OPTIMAL_TEMP_MIN))); } catch (Exception ignored) {}
        try { plant.setOptimalTempMax(cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_OPTIMAL_TEMP_MAX))); } catch (Exception ignored) {}
        try { plant.setOptimalAmbientHumidityMin(cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_OPTIMAL_AMB_HUM_MIN))); } catch (Exception ignored) {}
        try { plant.setOptimalAmbientHumidityMax(cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_OPTIMAL_AMB_HUM_MAX))); } catch (Exception ignored) {}
        try { plant.setOptimalLightLevel(cursor.getString(cursor.getColumnIndexOrThrow(KEY_OPTIMAL_LIGHT))); } catch (Exception ignored) {}
        try { plant.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREATED_AT))); } catch (Exception ignored) {}
        try { plant.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_UPDATED_AT))); } catch (Exception ignored) {}

        return plant;
    }

    /**
     * Obtener alertas no leídas
     */
    public List<Alert> getUnreadAlerts() {
        List<Alert> alerts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_ALERTS,
                null,
                KEY_IS_READ + "=0",
                null,
                null,
                null,
                KEY_TIMESTAMP + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Alert alert = cursorToAlert(cursor);
                alerts.add(alert);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return alerts;
    }

    /**
     * Convertir cursor a Alert
     */
    private Alert cursorToAlert(Cursor cursor) {
        Alert alert = new Alert();
        alert.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
        alert.setPlantId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PLANT_ID)));
        alert.setType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ALERT_TYPE)));
        alert.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)));
        alert.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(KEY_MESSAGE)));
        alert.setSeverity(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SEVERITY)));
        alert.setRead(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_READ)) == 1);
        alert.setIconType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ICON_TYPE)));
        alert.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIMESTAMP)));
        return alert;
    }

    // ========== MÉTODOS PARA RECUPERACIÓN DE CONTRASEÑA ==========
    // ✨ NUEVOS MÉTODOS AGREGADOS ✨

    /**
     * Verificar si un email existe en la base de datos
     * @param email Email del usuario a verificar
     * @return true si el email existe, false si no existe
     */
    public boolean emailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{KEY_EMAIL},
                KEY_EMAIL + "=?",
                new String[]{email},
                null, null, null
        );

        boolean exists = cursor != null && cursor.getCount() > 0;

        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return exists;
    }

    /**
     * Actualizar la contraseña de un usuario por email
     * @param email Email del usuario
     * @param hashedPassword Contraseña hasheada (SHA-256)
     * @return true si se actualizó correctamente, false si falló
     */
    public boolean updatePassword(String email, String hashedPassword) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PASSWORD, hashedPassword);
        values.put(KEY_UPDATED_AT, System.currentTimeMillis());

        int rowsAffected = db.update(
                TABLE_USERS,
                values,
                KEY_EMAIL + "=?",
                new String[]{email}
        );

        db.close();
        return rowsAffected > 0;
    }
    public int updatePlant(Plant plant) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PLANT_NAME, plant.getName());
        values.put(KEY_TYPE, plant.getType());
        values.put(KEY_SPECIES, plant.getSpecies());
        values.put(KEY_SCIENTIFIC_NAME, plant.getScientificName());
        values.put(KEY_IMAGE_URL, plant.getImageUrl());        // null si no cambió
        values.put(KEY_IS_CONNECTED, plant.isConnected() ? 1 : 0);
        values.put(KEY_UPDATED_AT, String.valueOf(System.currentTimeMillis()));

        int rows = db.update(
                TABLE_PLANTS,
                values,
                KEY_ID + "=?",
                new String[]{ String.valueOf(plant.getId()) }
        );

        db.close();
        return rows;
    }

    /**
     * Obtener plantas por userId (alias de getUserPlants)
     * @param userId ID del usuario
     * @return Lista de plantas del usuario
     */
    public List<Plant> getPlantsByUserId(int userId) {
        return getUserPlants(userId);
    }

    // ========== MÉTODOS AUXILIARES PARA MIGRACIÓN ==========

    /**
     * Limpiar todas las tablas de la base de datos
     * PRECAUCIÓN: Esta operación elimina TODOS los datos
     */
    public void clearAllTables() {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.beginTransaction();

            // Eliminar en orden inverso por Foreign Keys
            db.delete(TABLE_ALERTS, null, null);
            db.delete(TABLE_SENSOR_DATA, null, null);
            db.delete(TABLE_PLANTS, null, null);
            db.delete(TABLE_USERS, null, null);

            db.setTransactionSuccessful();
            Log.d(TAG, "Todas las tablas han sido limpiadas");

        } catch (Exception e) {
            Log.e(TAG, "Error al limpiar tablas: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
     * Obtener estadísticas de la base de datos
     * @return Map con el conteo de registros por tabla
     */
    public java.util.Map<String, Integer> getTableStats() {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            // Contar usuarios
            Cursor cursorUsers = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
            if (cursorUsers.moveToFirst()) {
                stats.put("users", cursorUsers.getInt(0));
            }
            cursorUsers.close();

            // Contar plantas
            Cursor cursorPlants = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PLANTS, null);
            if (cursorPlants.moveToFirst()) {
                stats.put("plants", cursorPlants.getInt(0));
            }
            cursorPlants.close();

            // Contar datos de sensores
            Cursor cursorSensors = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SENSOR_DATA, null);
            if (cursorSensors.moveToFirst()) {
                stats.put("sensor_data", cursorSensors.getInt(0));
            }
            cursorSensors.close();

            // Contar alertas
            Cursor cursorAlerts = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ALERTS, null);
            if (cursorAlerts.moveToFirst()) {
                stats.put("alerts", cursorAlerts.getInt(0));
            }
            cursorAlerts.close();

        } catch (Exception e) {
            Log.e(TAG, "Error al obtener estadísticas: " + e.getMessage());
        } finally {
            db.close();
        }

        return stats;
    }

    /**
     * Exportar base de datos a JSON (opcional)
     * Útil para backup manual
     */
    public String exportToJSON() {
        StringBuilder json = new StringBuilder();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            json.append("{\n");

            // Exportar cada tabla
            String[] tables = {TABLE_USERS, TABLE_PLANTS, TABLE_SENSOR_DATA, TABLE_ALERTS};

            for (int i = 0; i < tables.length; i++) {
                String tableName = tables[i];
                json.append("  \"").append(tableName).append("\": [\n");

                Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);

                if (cursor.moveToFirst()) {
                    do {
                        json.append("    {");

                        for (int j = 0; j < cursor.getColumnCount(); j++) {
                            String columnName = cursor.getColumnName(j);
                            String value = cursor.getString(j);

                            json.append("\"").append(columnName).append("\": ");

                            if (value == null) {
                                json.append("null");
                            } else {
                                json.append("\"").append(value.replace("\"", "\\\"")).append("\"");
                            }

                            if (j < cursor.getColumnCount() - 1) {
                                json.append(", ");
                            }
                        }

                        json.append("}");

                        if (!cursor.isLast()) {
                            json.append(",");
                        }
                        json.append("\n");

                    } while (cursor.moveToNext());
                }

                cursor.close();
                json.append("  ]");

                if (i < tables.length - 1) {
                    json.append(",");
                }
                json.append("\n");
            }

            json.append("}\n");

        } catch (Exception e) {
            Log.e(TAG, "Error al exportar a JSON: " + e.getMessage());
            return null;
        } finally {
            db.close();
        }

        return json.toString();
    }

    public class SQLiteToFirebaseMigration {

        private DatabaseReference firebaseRef;
        private SQLiteDatabase sqliteDb;

        public SQLiteToFirebaseMigration(Context context) {
            // Inicializar Firebase
            firebaseRef = FirebaseDatabase.getInstance().getReference();

            // Abrir tu base de datos SQLite
            MiDatabaseHelper dbHelper = new MiDatabaseHelper(context);
            sqliteDb = dbHelper.getReadableDatabase();
        }

        // Migrar una tabla específica
        public void migrarTabla(String nombreTabla, String nodoFirebase) {
            Cursor cursor = sqliteDb.rawQuery("SELECT * FROM " + nombreTabla, null);

            if (cursor.moveToFirst()) {
                do {
                    // Crear un HashMap con los datos
                    Map<String, Object> datos = new HashMap<>();

                    // Obtener todas las columnas
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        String columnName = cursor.getColumnName(i);
                        String value = cursor.getString(i);
                        datos.put(columnName, value);
                    }

                    // Subir a Firebase (usar un ID único)
                    String id = cursor.getString(cursor.getColumnIndex("id"));
                    firebaseRef.child(nodoFirebase).child(id).setValue(datos)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Migration", "Registro migrado: " + id);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Migration", "Error: " + e.getMessage());
                            });

                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        // Migrar toda la base de datos
        public void migrarTodasLasTablas() {
            Cursor cursor = sqliteDb.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table'", null);

            if (cursor.moveToFirst()) {
                do {
                    String tableName = cursor.getString(0);
                    if (!tableName.equals("android_metadata") &&
                            !tableName.equals("sqlite_sequence")) {
                        migrarTabla(tableName, tableName);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }
}
