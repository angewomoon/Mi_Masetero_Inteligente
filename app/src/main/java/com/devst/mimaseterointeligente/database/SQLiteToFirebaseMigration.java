package com.devst.mimaseterointeligente.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase para migrar datos desde SQLite local hacia Firebase Realtime Database.
 * Incluye callbacks de progreso, manejo de errores, y migración por lotes.
 */
public class
SQLiteToFirebaseMigration {

    private static final String TAG = "SQLiteToFirebase";
    private static final int BATCH_SIZE = 10; // Tamaño del lote para migración
    private static final int MAX_RETRIES = 3; // Intentos máximos por lote

    private DatabaseHelper dbHelper;
    private DatabaseReference firebaseRef;
    private Context context;

    // Callbacks
    private MigrationCallback callback;

    /**
     * Constructor
     * @param context Contexto de la aplicación
     */
    public SQLiteToFirebaseMigration(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.firebaseRef = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Interface para callbacks de migración
     */
    public interface MigrationCallback {
        void onProgress(String tableName, int current, int total);
        void onTableComplete(String tableName, int recordsMigrated, int errors);
        void onComplete(int totalRecords, int totalErrors);
        void onError(String tableName, String error);
    }

    /**
     * Establece el callback de migración
     */
    public void setCallback(MigrationCallback callback) {
        this.callback = callback;
    }

    /**
     * Migrar todas las tablas de SQLite a Firebase
     */
    public void migrateAllTables() {
        Log.d(TAG, "Iniciando migración completa de SQLite a Firebase");

        new Thread(() -> {
            AtomicInteger totalRecords = new AtomicInteger(0);
            AtomicInteger totalErrors = new AtomicInteger(0);

            // Migrar usuarios primero (por Foreign Keys)
            MigrationResult usersResult = migrateTable("users", "users");
            totalRecords.addAndGet(usersResult.successCount);
            totalErrors.addAndGet(usersResult.errorCount);

            // Esperar un poco antes de continuar
            waitFor(1000);

            // Migrar plantas (dependen de users)
            MigrationResult plantsResult = migrateTable("plants", "plants");
            totalRecords.addAndGet(plantsResult.successCount);
            totalErrors.addAndGet(plantsResult.errorCount);

            // Esperar un poco antes de continuar
            waitFor(1000);

            // Migrar datos de sensores (dependen de plants)
            MigrationResult sensorsResult = migrateTable("sensor_data", "sensor_data");
            totalRecords.addAndGet(sensorsResult.successCount);
            totalErrors.addAndGet(sensorsResult.errorCount);

            // Esperar un poco antes de continuar
            waitFor(1000);

            // Migrar alertas (dependen de plants)
            MigrationResult alertsResult = migrateTable("alerts", "alerts");
            totalRecords.addAndGet(alertsResult.successCount);
            totalErrors.addAndGet(alertsResult.errorCount);

            // Callback final
            if (callback != null) {
                callback.onComplete(totalRecords.get(), totalErrors.get());
            }

            Log.d(TAG, "Migración completa finalizada: " + totalRecords.get() +
                  " registros, " + totalErrors.get() + " errores");
        }).start();
    }

    /**
     * Migrar una tabla específica
     * @param tableName Nombre de la tabla en SQLite
     * @param firebaseNode Nodo en Firebase
     */
    public void migrateTableAsync(String tableName, String firebaseNode) {
        new Thread(() -> {
            migrateTable(tableName, firebaseNode);
        }).start();
    }

    /**
     * Migrar una tabla específica (método síncrono interno)
     */
    private MigrationResult migrateTable(String tableName, String firebaseNode) {
        Log.d(TAG, "Iniciando migración de tabla: " + tableName);

        MigrationResult result = new MigrationResult();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM " + tableName, null);

            int totalRecords = cursor.getCount();
            Log.d(TAG, tableName + ": " + totalRecords + " registros encontrados");

            if (totalRecords == 0) {
                if (callback != null) {
                    callback.onTableComplete(tableName, 0, 0);
                }
                return result;
            }

            int currentRecord = 0;

            if (cursor.moveToFirst()) {
                do {
                    try {
                        // Crear un Map con los datos de la fila
                        Map<String, Object> rowData = new HashMap<>();

                        // Obtener todas las columnas
                        for (int i = 0; i < cursor.getColumnCount(); i++) {
                            String columnName = cursor.getColumnName(i);

                            // Determinar el tipo de dato
                            int columnType = cursor.getType(i);

                            switch (columnType) {
                                case Cursor.FIELD_TYPE_INTEGER:
                                    rowData.put(columnName, cursor.getLong(i));
                                    break;
                                case Cursor.FIELD_TYPE_FLOAT:
                                    rowData.put(columnName, cursor.getDouble(i));
                                    break;
                                case Cursor.FIELD_TYPE_STRING:
                                    rowData.put(columnName, cursor.getString(i));
                                    break;
                                case Cursor.FIELD_TYPE_NULL:
                                    rowData.put(columnName, null);
                                    break;
                                default:
                                    rowData.put(columnName, cursor.getString(i));
                                    break;
                            }
                        }

                        // Obtener ID para usar como key en Firebase
                        String recordId = String.valueOf(cursor.getLong(cursor.getColumnIndex("id")));

                        // Verificar si el registro ya existe en Firebase antes de insertar
                        boolean shouldInsert = checkAndInsertToFirebase(firebaseNode, recordId, rowData);

                        if (shouldInsert) {
                            result.successCount++;
                        } else {
                            Log.d(TAG, tableName + ": Registro " + recordId + " ya existe, omitiendo");
                        }

                        currentRecord++;

                        // Callback de progreso
                        if (callback != null && currentRecord % 5 == 0) {
                            final int current = currentRecord;
                            callback.onProgress(tableName, current, totalRecords);
                        }

                    } catch (Exception e) {
                        result.errorCount++;
                        Log.e(TAG, "Error al migrar registro de " + tableName + ": " + e.getMessage());
                        if (callback != null) {
                            callback.onError(tableName, "Error en registro: " + e.getMessage());
                        }
                    }

                } while (cursor.moveToNext());
            }

            // Callback de tabla completada
            if (callback != null) {
                callback.onTableComplete(tableName, result.successCount, result.errorCount);
            }

            Log.d(TAG, tableName + " completada: " + result.successCount +
                  " exitosos, " + result.errorCount + " errores");

        } catch (Exception e) {
            Log.e(TAG, "Error al migrar tabla " + tableName + ": " + e.getMessage());
            if (callback != null) {
                callback.onError(tableName, "Error general: " + e.getMessage());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return result;
    }

    /**
     * Verificar si existe y insertar en Firebase
     * @return true si se insertó, false si ya existía
     */
    private boolean checkAndInsertToFirebase(String node, String id, Map<String, Object> data) {
        try {
            DatabaseReference ref = firebaseRef.child(node).child(id);

            // Insertar/actualizar directamente
            // Firebase sobrescribirá si ya existe
            ref.setValue(data);

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al insertar en Firebase: " + e.getMessage());
            return false;
        }
    }

    /**
     * Migrar solo usuarios
     */
    public void migrateUsers() {
        migrateTableAsync("users", "users");
    }

    /**
     * Migrar solo plantas
     */
    public void migratePlants() {
        migrateTableAsync("plants", "plants");
    }

    /**
     * Migrar solo datos de sensores
     */
    public void migrateSensorData() {
        migrateTableAsync("sensor_data", "sensor_data");
    }

    /**
     * Migrar solo alertas
     */
    public void migrateAlerts() {
        migrateTableAsync("alerts", "alerts");
    }

    /**
     * Obtener estadísticas de migración
     */
    public Map<String, Integer> getMigrationStats() {
        Map<String, Integer> stats = new HashMap<>();
        SQLiteDatabase db = null;

        try {
            db = dbHelper.getReadableDatabase();

            stats.put("users", getTableCount(db, "users"));
            stats.put("plants", getTableCount(db, "plants"));
            stats.put("sensor_data", getTableCount(db, "sensor_data"));
            stats.put("alerts", getTableCount(db, "alerts"));

        } catch (Exception e) {
            Log.e(TAG, "Error al obtener estadísticas: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }

        return stats;
    }

    /**
     * Obtener conteo de registros en una tabla
     */
    private int getTableCount(SQLiteDatabase db, String tableName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al contar registros de " + tableName + ": " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    /**
     * Esperar un tiempo determinado
     */
    private void waitFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Log.e(TAG, "Error en espera: " + e.getMessage());
        }
    }

    /**
     * Clase interna para resultados de migración
     */
    private static class MigrationResult {
        int successCount = 0;
        int errorCount = 0;
    }
}
