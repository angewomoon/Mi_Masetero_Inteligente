package com.devst.mimaseterointeligente.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.adapters.AlertsAdapter;
import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.devst.mimaseterointeligente.models.Alert;

import java.util.ArrayList;
import java.util.List;

public class AlertsFragment extends Fragment {

    private RecyclerView rvAlerts;
    private AlertsAdapter alertsAdapter;
    private DatabaseHelper dbHelper;
    private List<Alert> alertList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inicializar el DatabaseHelper
        dbHelper = new DatabaseHelper(requireContext());
        return inflater.inflate(R.layout.fragment_alerts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Enlazar la RecyclerView
        rvAlerts = view.findViewById(R.id.rvAlerts);
        rvAlerts.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar el adaptador con una lista vacía
        alertsAdapter = new AlertsAdapter(getContext(), alertList);
        rvAlerts.setAdapter(alertsAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cargar o actualizar las alertas cada vez que el fragmento se vuelve visible
        loadAlerts();
    }

    private void loadAlerts() {
        // Obtener todas las alertas desde la base de datos
        List<Alert> newAlerts = dbHelper.getAllAlerts();
        
        // Actualizar la lista en el adaptador
        // El adaptador se encargará de notificar a la RecyclerView para que se redibuje
        alertsAdapter.updateAlerts(newAlerts);
    }
}
