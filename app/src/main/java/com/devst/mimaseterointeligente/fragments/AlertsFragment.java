package com.devst.mimaseterointeligente.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.adapters.AlertsAdapter;
import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.devst.mimaseterointeligente.models.Alert;
import com.devst.mimaseterointeligente.models.Plant;
import com.devst.mimaseterointeligente.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragmento para mostrar alertas del usuario
 * Filtra alertas solo de las plantas del usuario actual
 */
public class AlertsFragment extends Fragment {

    private static final String TAG = "AlertsFragment";

    private RecyclerView rvAlerts;
    private LinearLayout layoutEmptyState;
    private TextView tvEmptyMessage;
    private AlertsAdapter alertsAdapter;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private List<Alert> alertList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inicializar el DatabaseHelper y SessionManager
        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());
        return inflater.inflate(R.layout.fragment_alerts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Enlazar vistas
        rvAlerts = view.findViewById(R.id.rvAlerts);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        // Configurar RecyclerView
        rvAlerts.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar el adaptador con una lista vacía
        alertsAdapter = new AlertsAdapter(getContext(), alertList);
        rvAlerts.setAdapter(alertsAdapter);

        // Cargar alertas
        loadAlerts();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cargar o actualizar las alertas cada vez que el fragmento se vuelve visible
        loadAlerts();
    }

    /**
     * Cargar alertas solo de las plantas del usuario actual
     */
    private void loadAlerts() {
        try {
            int userId = sessionManager.getUserId();

            if (userId == -1) {
                Log.e(TAG, "Usuario no identificado");
                showEmptyState("No se pudo identificar el usuario");
                return;
            }

            // Obtener todas las plantas del usuario
            List<Plant> userPlants = dbHelper.getUserPlants(userId);

            if (userPlants == null || userPlants.isEmpty()) {
                showEmptyState("No tienes plantas registradas");
                return;
            }

            // Obtener todas las alertas
            List<Alert> allAlerts = dbHelper.getAllAlerts();

            // Filtrar alertas que pertenecen a plantas del usuario
            List<Alert> userAlerts = new ArrayList<>();
            List<Integer> plantIds = new ArrayList<>();

            // Extraer IDs de plantas del usuario
            for (Plant plant : userPlants) {
                plantIds.add(plant.getId());
            }

            // Filtrar alertas
            if (allAlerts != null) {
                for (Alert alert : allAlerts) {
                    if (plantIds.contains(alert.getPlantId())) {
                        userAlerts.add(alert);
                    }
                }
            }

            Log.d(TAG, "Alertas del usuario: " + userAlerts.size());

            // Actualizar el adaptador
            if (userAlerts.isEmpty()) {
                showEmptyState("No tienes alertas");
            } else {
                showAlerts(userAlerts);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error al cargar alertas: " + e.getMessage(), e);
            showEmptyState("Error al cargar alertas");
        }
    }

    /**
     * Mostrar alertas en la lista
     */
    private void showAlerts(List<Alert> alerts) {
        rvAlerts.setVisibility(View.VISIBLE);
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(View.GONE);
        }
        alertsAdapter.updateAlerts(alerts);
    }

    /**
     * Mostrar estado vacío
     */
    private void showEmptyState(String message) {
        rvAlerts.setVisibility(View.GONE);
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            if (tvEmptyMessage != null) {
                tvEmptyMessage.setText(message);
            }
        }
    }
}
