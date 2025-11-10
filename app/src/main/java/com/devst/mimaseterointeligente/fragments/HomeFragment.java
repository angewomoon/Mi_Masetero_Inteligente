package com.devst.mimaseterointeligente.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.activities.AddPlantActivity;
import com.devst.mimaseterointeligente.adapters.PlantAdapter;
import com.devst.mimaseterointeligente.api.RetrofitClient;
import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.devst.mimaseterointeligente.models.Plant;
import com.devst.mimaseterointeligente.models.WeatherResponse;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Fragment principal del Home
 * Muestra las plantas del usuario y la información del clima
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final int REQUEST_ADD_PLANT = 100;

    // Views
    private TextView tvTemperature;
    private TextView tvWeatherDescription;
    private ImageView ivWeatherIcon;
    private RecyclerView rvPlants;
    private LinearLayout layoutEmptyState;
    private MaterialCardView cardAddPlant;

    // Data
    private DatabaseHelper databaseHelper;
    private PlantAdapter plantAdapter;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        initViews(view);

        // Inicializar base de datos
        databaseHelper = new DatabaseHelper(requireContext());

        // Obtener userId de SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("MaseteroPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        // Configurar RecyclerView
        setupRecyclerView();

        // Cargar datos
        loadPlants();
        loadWeatherData();

        // Configurar botón de añadir planta
        cardAddPlant.setOnClickListener(v -> openAddPlantActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar plantas cada vez que el fragmento vuelve a estar visible
        loadPlants();
    }

    /**
     * Inicializar todas las vistas
     */
    private void initViews(View view) {
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvWeatherDescription = view.findViewById(R.id.tvWeatherDescription);
        ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon);
        rvPlants = view.findViewById(R.id.rvPlants);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        cardAddPlant = view.findViewById(R.id.cardAddPlant);
    }

    /**
     * Configurar RecyclerView con el adapter
     */
    private void setupRecyclerView() {
        plantAdapter = new PlantAdapter(requireContext());
        rvPlants.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPlants.setAdapter(plantAdapter);
    }

    /**
     * Cargar plantas del usuario desde la base de datos
     */
    private void loadPlants() {
        if (userId == -1) {
            Log.e(TAG, "Usuario no identificado");
            showEmptyState();
            return;
        }

        // Obtener plantas del usuario
        List<Plant> plants = databaseHelper.getPlantsByUserId(userId);

        if (plants != null && !plants.isEmpty()) {
            // Mostrar plantas en el RecyclerView
            plantAdapter.setPlants(plants);
            rvPlants.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            Log.d(TAG, "Cargadas " + plants.size() + " plantas");
        } else {
            // Mostrar mensaje de estado vacío
            showEmptyState();
            Log.d(TAG, "No hay plantas para mostrar");
        }
    }

    /**
     * Mostrar estado vacío cuando no hay plantas
     */
    private void showEmptyState() {
        rvPlants.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }

    /**
     * Cargar datos del clima desde la API
     */
    private void loadWeatherData() {
        // Coordenadas por defecto (Santiago, Chile)
        double latitude = -33.4489;
        double longitude = -70.6693;

        // Obtener ubicación desde SharedPreferences si existe
        SharedPreferences prefs = requireActivity().getSharedPreferences("MaseteroPrefs", MODE_PRIVATE);
        latitude = Double.parseDouble(prefs.getString("latitude", String.valueOf(latitude)));
        longitude = Double.parseDouble(prefs.getString("longitude", String.valueOf(longitude)));

        // **INICIO DE LA CORRECCIÓN**
        String apiKey = "TU_API_KEY_AQUI"; // ¡¡¡IMPORTANTE: Reemplaza con tu API Key real!!!
        String units = "metric"; // Para obtener la temperatura en Celsius
        String lang = "es";      // Para obtener la descripción en español

        // Llamar a la API del clima con el método y parámetros correctos
        RetrofitClient.getWeatherApiService()
                .getCurrentWeatherByCoordinates(latitude, longitude, apiKey, units, lang)
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WeatherResponse weather = response.body();
                            updateWeatherUI(weather);
                        } else {
                            Log.e(TAG, "Error al obtener clima: " + response.code());
                            setDefaultWeatherUI();
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        Log.e(TAG, "Error de conexión al obtener clima: " + t.getMessage());
                        setDefaultWeatherUI();
                    }
                });
        // **FIN DE LA CORRECCIÓN**
    }

    /**
     * Actualizar UI con datos del clima
     */
    private void updateWeatherUI(WeatherResponse weather) {
        if (getActivity() == null) return;

        requireActivity().runOnUiThread(() -> {
            // Mostrar temperatura
            double temp = weather.getMain().getTemp();
            tvTemperature.setText(String.format("%.0f°C", temp));

            // Mostrar descripción del clima
            if (weather.getWeather() != null && !weather.getWeather().isEmpty()) {
                String description = weather.getWeather().get(0).getDescription();
                tvWeatherDescription.setText(capitalizeFirst(description));
            }
        });
    }

    /**
     * Establecer UI por defecto si falla la llamada al clima
     */
    private void setDefaultWeatherUI() {
        if (getActivity() == null) return;

        requireActivity().runOnUiThread(() -> {
            tvTemperature.setText("--°C");
            tvWeatherDescription.setText("No disponible");
        });
    }

    /**
     * Capitalizar primera letra de un string
     */
    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    /**
     * Abrir actividad para añadir nueva planta
     */
    private void openAddPlantActivity() {
        Intent intent = new Intent(requireContext(), AddPlantActivity.class);
        startActivityForResult(intent, REQUEST_ADD_PLANT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_PLANT && resultCode == requireActivity().RESULT_OK) {
            // Recargar plantas después de agregar una nueva
            loadPlants();
            Toast.makeText(requireContext(), "Planta agregada exitosamente", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Método público para refrescar datos desde la actividad principal
     */
    public void refreshData() {
        loadPlants();
        loadWeatherData();
    }
}
