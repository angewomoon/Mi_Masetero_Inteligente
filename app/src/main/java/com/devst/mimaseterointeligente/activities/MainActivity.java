package com.devst.mimaseterointeligente.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.fragments.AlertsFragment;
import com.devst.mimaseterointeligente.fragments.HomeFragment;
import com.devst.mimaseterointeligente.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private HomeFragment homeFragment;
    private AlertsFragment alertsFragment;
    private ProfileFragment profileFragment;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("MaseteroPrefs", MODE_PRIVATE);

        // Verificar si el usuario está logueado
        if (!isUserLoggedIn()) {
            navigateToLogin();
            return;
        }

        // Inicializar vistas
        initViews();

        // Inicializar fragmentos
        initFragments();

        // Configurar bottom navigation
        setupBottomNavigation();

        // Cargar fragmento inicial
        loadFragment(homeFragment, "HOME");
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);
    }

    private void initFragments() {
        fragmentManager = getSupportFragmentManager();
        homeFragment = new HomeFragment();
        alertsFragment = new AlertsFragment();
        profileFragment = new ProfileFragment();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    loadFragment(homeFragment, "HOME");
                    return true;
                } else if (itemId == R.id.navigation_add) {
                    // Abrir actividad para añadir planta
                    openAddPlantActivity();
                    return false;
                } else if (itemId == R.id.navigation_alerts) {
                    loadFragment(alertsFragment, "ALERTS");
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    loadFragment(profileFragment, "PROFILE");
                    return true;
                }

                return false;
            }
        });

        // Configurar el reseleccionar
        bottomNavigationView.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                // No hacer nada cuando se reselecciona
            }
        });
    }

    private void loadFragment(Fragment fragment, String tag) {
        // Verificar si el fragmento ya existe
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Ocultar todos los fragmentos
        for (Fragment frag : fragmentManager.getFragments()) {
            if (frag != null) {
                transaction.hide(frag);
            }
        }

        if (existingFragment != null) {
            // Si el fragmento ya existe, mostrarlo
            transaction.show(existingFragment);
        } else {
            // Si no existe, añadirlo
            transaction.add(R.id.fragmentContainer, fragment, tag);
        }

        transaction.commit();
    }

    private void openAddPlantActivity() {
        Intent intent = new Intent(MainActivity.this, AddPlantActivity.class);
        startActivity(intent);
    }

    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void logout() {
        // Limpiar sesión
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Navegar a login
        navigateToLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar el fragmento actual cuando se regresa a la actividad
        if (homeFragment != null && homeFragment.isVisible()) {
            homeFragment.refreshData();
        }
    }
}