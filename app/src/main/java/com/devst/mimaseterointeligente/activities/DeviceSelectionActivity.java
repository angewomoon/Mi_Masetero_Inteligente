package com.devst.mimaseterointeligente.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.adapters.DeviceAdapter;
import com.devst.mimaseterointeligente.models.Device;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity para seleccionar un dispositivo ESP32 disponible desde Firebase.
 * Los dispositivos se auto-registran en Firebase cuando se encienden.
 */
public class DeviceSelectionActivity extends AppCompatActivity implements DeviceAdapter.OnDeviceClickListener {

    private static final String TAG = "DeviceSelection";
    public static final String EXTRA_SELECTED_DEVICE_ID = "selected_device_id";
    public static final String EXTRA_SELECTED_DEVICE_NAME = "selected_device_name";

    private RecyclerView recyclerView;
    private DeviceAdapter deviceAdapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private DatabaseReference devicesRef;
    private List<Device> deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_selection);

        // Inicializar vistas
        initializeViews();

        // Configurar Firebase
        devicesRef = FirebaseDatabase.getInstance().getReference("devices");

        // Cargar dispositivos
        loadDevices();
    }

    /**
     * Inicializar vistas
     */
    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewDevices);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Configurar RecyclerView
        deviceList = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(deviceList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(deviceAdapter);
    }

    /**
     * Cargar dispositivos desde Firebase
     */
    private void loadDevices() {
        showLoading();

        devicesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                deviceList.clear();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    showEmpty();
                    return;
                }

                for (DataSnapshot deviceSnapshot : snapshot.getChildren()) {
                    try {
                        Device device = deviceSnapshot.getValue(Device.class);
                        if (device != null) {
                            deviceList.add(device);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al parsear dispositivo: " + e.getMessage());
                    }
                }

                if (deviceList.isEmpty()) {
                    showEmpty();
                } else {
                    showDevices();
                }

                deviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoading();
                Log.e(TAG, "Error al cargar dispositivos: " + error.getMessage());
                Toast.makeText(DeviceSelectionActivity.this,
                    "Error al cargar dispositivos: " + error.getMessage(),
                    Toast.LENGTH_SHORT).show();
                showEmpty();
            }
        });
    }

    /**
     * Callback cuando se hace clic en un dispositivo
     */
    @Override
    public void onDeviceClick(Device device) {
        // Verificar que el dispositivo esté disponible
        if (!device.isAvailable()) {
            Toast.makeText(this, "Este dispositivo ya está en uso", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar que esté online
        if (!device.isOnline()) {
            Toast.makeText(this,
                "Este dispositivo está offline. Enciéndelo y vuelve a intentar.",
                Toast.LENGTH_LONG).show();
            return;
        }

        // Devolver dispositivo seleccionado
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SELECTED_DEVICE_ID, device.getDeviceId());
        resultIntent.putExtra(EXTRA_SELECTED_DEVICE_NAME, device.getDeviceName());
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Mostrar loading
     */
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
    }

    /**
     * Ocultar loading
     */
    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    /**
     * Mostrar lista de dispositivos
     */
    private void showDevices() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }

    /**
     * Mostrar mensaje vacío
     */
    private void showEmpty() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
    }
}
