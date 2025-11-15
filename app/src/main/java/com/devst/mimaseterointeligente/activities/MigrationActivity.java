package com.devst.mimaseterointeligente.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.devst.mimaseterointeligente.database.FirebaseToSQLiteMigration;
import com.devst.mimaseterointeligente.database.SQLiteToFirebaseMigration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Activity para gestionar la migración bidireccional entre SQLite y Firebase.
 * Permite exportar datos locales a Firebase e importar datos de Firebase a SQLite.
 */
public class MigrationActivity extends AppCompatActivity {

    private static final String TAG = "MigrationActivity";

    // UI Components
    private ProgressBar progressBar;
    private TextView tvStatus;
    private TextView tvLogs;
    private TextView tvSqliteUsers, tvSqlitePlants, tvSqliteSensors, tvSqliteAlerts;
    private TextView tvFirebaseUsers, tvFirebasePlants, tvFirebaseSensors, tvFirebaseAlerts;

    // Buttons - Export
    private Button btnExportAll, btnExportUsers, btnExportPlants, btnExportSensors, btnExportAlerts;

    // Buttons - Import
    private Button btnImportAll, btnImportUsers, btnImportPlants, btnImportSensors, btnImportAlerts;

    // Buttons - Advanced
    private Button btnRefreshStats, btnClearDatabase, btnClearLogs;

    // Migration helpers
    private SQLiteToFirebaseMigration sqliteToFirebase;
    private FirebaseToSQLiteMigration firebaseToSqlite;
    private DatabaseHelper dbHelper;

    // Handler para UI thread
    private Handler uiHandler;

    // StringBuilder para logs
    private StringBuilder logs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_migration);

        // Inicializar componentes
        initializeViews();
        initializeMigrationHelpers();
        setupClickListeners();

        // Cargar estadísticas iniciales
        refreshStats();
    }

    /**
     * Inicializar vistas
     */
    private void initializeViews() {
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        tvLogs = findViewById(R.id.tvLogs);

        // Estadísticas SQLite
        tvSqliteUsers = findViewById(R.id.tvSqliteUsers);
        tvSqlitePlants = findViewById(R.id.tvSqlitePlants);
        tvSqliteSensors = findViewById(R.id.tvSqliteSensors);
        tvSqliteAlerts = findViewById(R.id.tvSqliteAlerts);

        // Estadísticas Firebase
        tvFirebaseUsers = findViewById(R.id.tvFirebaseUsers);
        tvFirebasePlants = findViewById(R.id.tvFirebasePlants);
        tvFirebaseSensors = findViewById(R.id.tvFirebaseSensors);
        tvFirebaseAlerts = findViewById(R.id.tvFirebaseAlerts);

        // Botones Export
        btnExportAll = findViewById(R.id.btnExportAll);
        btnExportUsers = findViewById(R.id.btnExportUsers);
        btnExportPlants = findViewById(R.id.btnExportPlants);
        btnExportSensors = findViewById(R.id.btnExportSensors);
        btnExportAlerts = findViewById(R.id.btnExportAlerts);

        // Botones Import
        btnImportAll = findViewById(R.id.btnImportAll);
        btnImportUsers = findViewById(R.id.btnImportUsers);
        btnImportPlants = findViewById(R.id.btnImportPlants);
        btnImportSensors = findViewById(R.id.btnImportSensors);
        btnImportAlerts = findViewById(R.id.btnImportAlerts);

        // Botones Advanced
        btnRefreshStats = findViewById(R.id.btnRefreshStats);
        btnClearDatabase = findViewById(R.id.btnClearDatabase);
        btnClearLogs = findViewById(R.id.btnClearLogs);

        // Handler para UI
        uiHandler = new Handler(Looper.getMainLooper());

        // Logs
        logs = new StringBuilder();
    }

    /**
     * Inicializar helpers de migración
     */
    private void initializeMigrationHelpers() {
        dbHelper = new DatabaseHelper(this);
        sqliteToFirebase = new SQLiteToFirebaseMigration(this);
        firebaseToSqlite = new FirebaseToSQLiteMigration(this);

        // Configurar callbacks para SQLite -> Firebase
        sqliteToFirebase.setCallback(new SQLiteToFirebaseMigration.MigrationCallback() {
            @Override
            public void onProgress(String tableName, int current, int total) {
                uiHandler.post(() -> {
                    int progress = (int) ((current * 100.0) / total);
                    progressBar.setProgress(progress);
                    updateStatus("Exportando " + tableName + ": " + current + "/" + total);
                });
            }

            @Override
            public void onTableComplete(String tableName, int recordsMigrated, int errors) {
                uiHandler.post(() -> {
                    String message = "✓ " + tableName + ": " + recordsMigrated + " registros, " + errors + " errores";
                    addLog(message);
                    refreshStats();
                });
            }

            @Override
            public void onComplete(int totalRecords, int totalErrors) {
                uiHandler.post(() -> {
                    hideProgress();
                    updateStatus("✓ Exportación completada: " + totalRecords + " registros");
                    addLog("=== EXPORTACIÓN COMPLETADA ===");
                    addLog("Total: " + totalRecords + " registros, " + totalErrors + " errores");
                    Toast.makeText(MigrationActivity.this,
                        "Exportación completada: " + totalRecords + " registros",
                        Toast.LENGTH_LONG).show();
                    enableAllButtons();
                });
            }

            @Override
            public void onError(String tableName, String error) {
                uiHandler.post(() -> {
                    addLog("✗ Error en " + tableName + ": " + error);
                });
            }
        });

        // Configurar callbacks para Firebase -> SQLite
        firebaseToSqlite.setCallback(new FirebaseToSQLiteMigration.MigrationCallback() {
            @Override
            public void onProgress(String tableName, int current, int total) {
                uiHandler.post(() -> {
                    int progress = (int) ((current * 100.0) / total);
                    progressBar.setProgress(progress);
                    updateStatus("Importando " + tableName + ": " + current + "/" + total);
                });
            }

            @Override
            public void onTableComplete(String tableName, int recordsMigrated, int errors) {
                uiHandler.post(() -> {
                    String message = "✓ " + tableName + ": " + recordsMigrated + " registros, " + errors + " errores";
                    addLog(message);
                    refreshStats();
                });
            }

            @Override
            public void onComplete(int totalRecords, int totalErrors) {
                uiHandler.post(() -> {
                    hideProgress();
                    updateStatus("✓ Importación completada: " + totalRecords + " registros");
                    addLog("=== IMPORTACIÓN COMPLETADA ===");
                    addLog("Total: " + totalRecords + " registros, " + totalErrors + " errores");
                    Toast.makeText(MigrationActivity.this,
                        "Importación completada: " + totalRecords + " registros",
                        Toast.LENGTH_LONG).show();
                    enableAllButtons();
                });
            }

            @Override
            public void onError(String tableName, String error) {
                uiHandler.post(() -> {
                    addLog("✗ Error en " + tableName + ": " + error);
                });
            }
        });
    }

    /**
     * Configurar listeners de botones
     */
    private void setupClickListeners() {
        // Export buttons
        btnExportAll.setOnClickListener(v -> exportAllTables());
        btnExportUsers.setOnClickListener(v -> exportTable("users"));
        btnExportPlants.setOnClickListener(v -> exportTable("plants"));
        btnExportSensors.setOnClickListener(v -> exportTable("sensor_data"));
        btnExportAlerts.setOnClickListener(v -> exportTable("alerts"));

        // Import buttons
        btnImportAll.setOnClickListener(v -> importAllTables());
        btnImportUsers.setOnClickListener(v -> importTable("users"));
        btnImportPlants.setOnClickListener(v -> importTable("plants"));
        btnImportSensors.setOnClickListener(v -> importTable("sensor_data"));
        btnImportAlerts.setOnClickListener(v -> importTable("alerts"));

        // Advanced buttons
        btnRefreshStats.setOnClickListener(v -> refreshStats());
        btnClearDatabase.setOnClickListener(v -> showClearDatabaseDialog());
        btnClearLogs.setOnClickListener(v -> clearLogs());
    }

    /**
     * Exportar todas las tablas
     */
    private void exportAllTables() {
        showProgress();
        disableAllButtons();
        addLog("=== INICIANDO EXPORTACIÓN COMPLETA ===");
        sqliteToFirebase.migrateAllTables();
    }

    /**
     * Exportar una tabla específica
     */
    private void exportTable(String tableName) {
        showProgress();
        disableAllButtons();
        addLog("=== EXPORTANDO " + tableName.toUpperCase() + " ===");

        switch (tableName) {
            case "users":
                sqliteToFirebase.migrateUsers();
                break;
            case "plants":
                sqliteToFirebase.migratePlants();
                break;
            case "sensor_data":
                sqliteToFirebase.migrateSensorData();
                break;
            case "alerts":
                sqliteToFirebase.migrateAlerts();
                break;
        }
    }

    /**
     * Importar todas las tablas
     */
    private void importAllTables() {
        showProgress();
        disableAllButtons();
        addLog("=== INICIANDO IMPORTACIÓN COMPLETA ===");
        firebaseToSqlite.importAllTables();
    }

    /**
     * Importar una tabla específica
     */
    private void importTable(String tableName) {
        showProgress();
        disableAllButtons();
        addLog("=== IMPORTANDO " + tableName.toUpperCase() + " ===");

        new Thread(() -> {
            FirebaseToSQLiteMigration.MigrationResult result;

            switch (tableName) {
                case "users":
                    result = firebaseToSqlite.importUsers();
                    break;
                case "plants":
                    result = firebaseToSqlite.importPlants();
                    break;
                case "sensor_data":
                    result = firebaseToSqlite.importSensorData();
                    break;
                case "alerts":
                    result = firebaseToSqlite.importAlerts();
                    break;
                default:
                    return;
            }

            // Actualizar UI
            uiHandler.post(() -> {
                hideProgress();
                addLog("✓ " + tableName + " importada: " + result.successCount + " registros, " +
                       result.errorCount + " errores");
                refreshStats();
                enableAllButtons();
                Toast.makeText(this, tableName + " importada correctamente", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    /**
     * Refrescar estadísticas
     */
    private void refreshStats() {
        // Estadísticas SQLite
        Map<String, Integer> sqliteStats = sqliteToFirebase.getMigrationStats();
        tvSqliteUsers.setText("Usuarios: " + sqliteStats.getOrDefault("users", 0));
        tvSqlitePlants.setText("Plantas: " + sqliteStats.getOrDefault("plants", 0));
        tvSqliteSensors.setText("Sensores: " + sqliteStats.getOrDefault("sensor_data", 0));
        tvSqliteAlerts.setText("Alertas: " + sqliteStats.getOrDefault("alerts", 0));

        // Estadísticas Firebase
        firebaseToSqlite.getFirebaseStats(stats -> {
            uiHandler.post(() -> {
                tvFirebaseUsers.setText("Usuarios: " + stats.getOrDefault("users", 0));
                tvFirebasePlants.setText("Plantas: " + stats.getOrDefault("plants", 0));
                tvFirebaseSensors.setText("Sensores: " + stats.getOrDefault("sensor_data", 0));
                tvFirebaseAlerts.setText("Alertas: " + stats.getOrDefault("alerts", 0));
            });
        });

        addLog("Estadísticas actualizadas");
    }

    /**
     * Mostrar diálogo de confirmación para limpiar base de datos
     */
    private void showClearDatabaseDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Limpiar Base de Datos")
            .setMessage("¿Estás seguro de que deseas eliminar TODOS los datos locales? Esta acción no se puede deshacer.")
            .setPositiveButton("SÍ, ELIMINAR", (dialog, which) -> clearDatabase())
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    /**
     * Limpiar base de datos local
     */
    private void clearDatabase() {
        addLog("=== LIMPIANDO BASE DE DATOS LOCAL ===");

        new Thread(() -> {
            try {
                dbHelper.clearAllTables();

                uiHandler.post(() -> {
                    addLog("✓ Base de datos limpiada correctamente");
                    refreshStats();
                    Toast.makeText(this, "Base de datos limpiada", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                uiHandler.post(() -> {
                    addLog("✗ Error al limpiar base de datos: " + e.getMessage());
                    Toast.makeText(this, "Error al limpiar base de datos", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * Limpiar logs
     */
    private void clearLogs() {
        logs.setLength(0);
        tvLogs.setText(getString(R.string.migration_logs_empty));
    }

    /**
     * Agregar log con timestamp
     */
    private void addLog(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String logEntry = "[" + timestamp + "] " + message + "\n";
        logs.append(logEntry);
        tvLogs.setText(logs.toString());
    }

    /**
     * Actualizar estado
     */
    private void updateStatus(String message) {
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText(message);
    }

    /**
     * Mostrar barra de progreso
     */
    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        tvStatus.setVisibility(View.VISIBLE);
    }

    /**
     * Ocultar barra de progreso
     */
    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
        tvStatus.setVisibility(View.GONE);
    }

    /**
     * Deshabilitar todos los botones durante operaciones
     */
    private void disableAllButtons() {
        btnExportAll.setEnabled(false);
        btnExportUsers.setEnabled(false);
        btnExportPlants.setEnabled(false);
        btnExportSensors.setEnabled(false);
        btnExportAlerts.setEnabled(false);
        btnImportAll.setEnabled(false);
        btnImportUsers.setEnabled(false);
        btnImportPlants.setEnabled(false);
        btnImportSensors.setEnabled(false);
        btnImportAlerts.setEnabled(false);
        btnClearDatabase.setEnabled(false);
    }

    /**
     * Habilitar todos los botones
     */
    private void enableAllButtons() {
        btnExportAll.setEnabled(true);
        btnExportUsers.setEnabled(true);
        btnExportPlants.setEnabled(true);
        btnExportSensors.setEnabled(true);
        btnExportAlerts.setEnabled(true);
        btnImportAll.setEnabled(true);
        btnImportUsers.setEnabled(true);
        btnImportPlants.setEnabled(true);
        btnImportSensors.setEnabled(true);
        btnImportAlerts.setEnabled(true);
        btnClearDatabase.setEnabled(true);
    }
}
