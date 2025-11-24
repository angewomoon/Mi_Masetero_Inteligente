package com.devst.mimaseterointeligente.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.adapters.AlertsAdapter;
import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.devst.mimaseterointeligente.models.Alert;

import java.util.List;

/**
 * Actividad para mostrar todas las alertas del masetero inteligente
 */
public class AlertsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewAlerts;
    private AlertsAdapter alertsAdapter;
    private DatabaseHelper dbHelper;
    private TextView tvEmptyState;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);

        initViews();
        loadAlerts();
    }

    /**
     * Inicializar vistas
     */
    private void initViews() {
        recyclerViewAlerts = findViewById(R.id.recyclerViewAlerts);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnBack = findViewById(R.id.btnBack);

        dbHelper = new DatabaseHelper(this);

        // Configurar RecyclerView
        recyclerViewAlerts.setLayoutManager(new LinearLayoutManager(this));

        // Configurar botón de retroceso
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Cargar alertas desde la base de datos
     */
    private void loadAlerts() {
        try {
            List<Alert> alerts = dbHelper.getAllAlerts();

            if (alerts == null || alerts.isEmpty()) {
                // Mostrar estado vacío
                recyclerViewAlerts.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                // Mostrar alertas
                recyclerViewAlerts.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);

                alertsAdapter = new AlertsAdapter(this, alerts, new AlertsAdapter.OnAlertClickListener() {
                    @Override
                    public void onAlertClick(Alert alert) {
                        // Marcar alerta como leída
                        handleAlertClick(alert);
                    }

                    @Override
                    public void onAlertDismiss(Alert alert) {
                        // Marcar como leída y eliminar de la vista
                        handleAlertDismiss(alert);
                    }
                });

                recyclerViewAlerts.setAdapter(alertsAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar alertas", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Manejar clic en una alerta
     */
    private void handleAlertClick(Alert alert) {
        if (!alert.isRead()) {
            // Marcar como leída
            dbHelper.markAlertAsRead(alert.getId());
            alert.setRead(true);

            // Actualizar el adapter
            if (alertsAdapter != null) {
                alertsAdapter.notifyDataSetChanged();
            }
        }

        Toast.makeText(this, "Alerta: " + alert.getTitle(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Manejar eliminación de alerta
     */
    private void handleAlertDismiss(Alert alert) {
        // Marcar como leída
        dbHelper.markAlertAsRead(alert.getId());

        // Recargar alertas
        loadAlerts();

        Toast.makeText(this, "Alerta marcada como leída", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar alertas al volver a la actividad
        loadAlerts();
    }
}
