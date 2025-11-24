package com.devst.mimaseterointeligente.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.devst.mimaseterointeligente.models.User;
import com.devst.mimaseterointeligente.utils.SessionManager;

/**
 * Actividad para mostrar y gestionar el perfil del usuario
 */
public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfileImage;
    private TextView tvUsername;
    private TextView tvPlantCount;
    private TextView tvAlertCount;
    private Button btnEditProfile;
    private Button btnLogout;
    private ImageView btnBack;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        loadUserData();
    }

    /**
     * Inicializar vistas
     */
    private void initViews() {
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvUsername = findViewById(R.id.tvUsername);
        tvPlantCount = findViewById(R.id.tvPlantCount);
        tvAlertCount = findViewById(R.id.tvAlertCount);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Obtener ID del usuario actual
        currentUserId = sessionManager.getUserId();

        // Configurar listeners
        btnEditProfile.setOnClickListener(v -> openEditProfile());
        btnLogout.setOnClickListener(v -> showLogoutDialog());
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Cargar datos del usuario
     */
    private void loadUserData() {
        try {
            // Obtener usuario de la base de datos
            User user = dbHelper.getUserByEmail(sessionManager.getUserEmail());

            if (user != null) {
                // Configurar nombre de usuario
                tvUsername.setText(user.getName());

                // Cargar imagen de perfil si existe
                if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                    // TODO: Cargar imagen desde URL o local
                    // Glide.with(this).load(user.getProfileImage()).into(ivProfileImage);
                }

                // Obtener contadores
                int plantCount = dbHelper.getPlantsCountByUserId(currentUserId);
                int alertCount = dbHelper.getUnreadAlertsCount();

                // Configurar contadores
                tvPlantCount.setText(String.valueOf(plantCount));
                tvAlertCount.setText(String.valueOf(alertCount));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar datos del perfil", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Abrir actividad de edición de perfil
     */
    private void openEditProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
    }

    /**
     * Mostrar diálogo de confirmación de cierre de sesión
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> logout())
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Cerrar sesión del usuario
     */
    private void logout() {
        // Limpiar sesión
        sessionManager.logout();

        // Redirigir al login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos al volver a la actividad
        loadUserData();
    }
}
