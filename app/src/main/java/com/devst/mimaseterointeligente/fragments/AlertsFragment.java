package com.devst.mimaseterointeligente.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager; // Added for context
import androidx.recyclerview.widget.RecyclerView;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.adapters.AlertsAdapter;
import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.devst.mimaseterointeligente.models.Alert;
import com.devst.mimaseterointeligente.models.Plant;
import com.devst.mimaseterointeligente.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fragmento que muestra y gestiona las alertas del usuario.
 * Toda la lógica de negocio se encuentra aquí.
 */
public class AlertsFragment extends Fragment implements AlertsAdapter.OnAlertClickListener {

    private static final String TAG = "AlertsFragment";

    private RecyclerView rvAlerts;
    private LinearLayout layoutEmptyState;
    private TextView tvEmptyMessage;
    private AlertsAdapter alertsAdapter;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());
        return inflater.inflate(R.layout.fragment_alerts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvAlerts = view.findViewById(R.id.rvAlerts);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        setupRecyclerView();
        loadAlerts();

        // Manejar el botón de retroceso si existe en el layout
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().finish());
        }
    }

    private void setupRecyclerView() {
        alertsAdapter = new AlertsAdapter(requireContext(), new ArrayList<>(), this);
        rvAlerts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAlerts.setAdapter(alertsAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAlerts();
    }

    private void loadAlerts() {
        try {
            int userId = sessionManager.getUserId();
            if (userId == -1) {
                showEmptyState("No se pudo identificar al usuario.");
                return;
            }

            List<Plant> userPlants = dbHelper.getUserPlants(userId);
            if (userPlants == null || userPlants.isEmpty()) {
                showEmptyState("Aún no tienes plantas registradas.");
                return;
            }

            // Optimización: Obtener IDs de plantas en un Set para búsquedas rápidas
            List<Integer> userPlantIds = userPlants.stream().map(Plant::getId).collect(Collectors.toList());
            List<Alert> allAlerts = dbHelper.getAllAlerts(); // Asumiendo que esto es necesario

            if (allAlerts == null || allAlerts.isEmpty()) {
                showEmptyState("¡Felicidades! No tienes alertas.");
                return;
            }

            // Filtrar alertas del usuario
            List<Alert> userAlerts = allAlerts.stream()
                    .filter(alert -> userPlantIds.contains(alert.getPlantId()))
                    .collect(Collectors.toList());

            if (userAlerts.isEmpty()) {
                showEmptyState("¡Todo en orden! No hay alertas para tus plantas.");
            } else {
                showAlerts(userAlerts);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error al cargar alertas", e);
            showEmptyState("Ocurrió un error al cargar las alertas.");
        }
    }

    private void showAlerts(List<Alert> alerts) {
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        rvAlerts.setVisibility(View.VISIBLE);
        alertsAdapter.updateAlerts(alerts);
    }

    private void showEmptyState(String message) {
        rvAlerts.setVisibility(View.GONE);
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            if (tvEmptyMessage != null) {
                tvEmptyMessage.setText(message);
            }
        }
    }

    // --- Implementación de OnAlertClickListener ---

    @Override
    public void onAlertClick(Alert alert) {
        handleAlertClick(alert);
    }

    // --- FIX IS HERE ---
    // Renamed this method from onDismissClick to onAlertDismiss
    @Override
    public void onAlertDismiss(Alert alert) {
        handleAlertDismiss(alert);
    }

    // --- Lógica de negocio movida desde la Activity ---

    private void handleAlertClick(Alert alert) {
        Toast.makeText(getContext(), "Abriendo detalles de la alerta: " + alert.getTitle(), Toast.LENGTH_SHORT).show();
        // Aquí podrías navegar a un dashboard de planta o una pantalla de detalle
        // Por ahora, solo la marcamos como leída
        markAlertAsRead(alert);
    }

    private void handleAlertDismiss(Alert alert) {
        Toast.makeText(getContext(), "Alerta '" + alert.getTitle() + "' descartada.", Toast.LENGTH_SHORT).show();
        markAlertAsRead(alert);
    }

    private void markAlertAsRead(Alert alert) {
        try {
            dbHelper.markAlertAsRead(alert.getId());
            Log.d(TAG, "Alerta marcada como leída: " + alert.getId());
            // Recargar las alertas para que desaparezca de la lista (si el filtro es por no leídas)
            loadAlerts();
        } catch (Exception e) {
            Log.e(TAG, "Error al marcar alerta como leída", e);
            Toast.makeText(getContext(), "Error al actualizar la alerta", Toast.LENGTH_SHORT).show();
        }
    }
}
