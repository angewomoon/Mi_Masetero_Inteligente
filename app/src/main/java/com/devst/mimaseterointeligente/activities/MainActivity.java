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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private HomeFragment homeFragment;
    private AlertsFragment alertsFragment;
    private ProfileFragment profileFragment;
    private SharedPreferences sharedPreferences;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("MaseteroPrefs", MODE_PRIVATE);

        if (!isUserLoggedIn()) {
            navigateToLogin();
            return;
        }

        initViews();
        initFragments();

        // Configurar Google Sign-In Client para poder cerrar la sesión
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        setupBottomNavigation();
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
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                loadFragment(homeFragment, "HOME");
                return true;
            } else if (itemId == R.id.navigation_add) {
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
        });

        bottomNavigationView.setOnItemReselectedListener(item -> {
            // No hacer nada cuando se reselecciona
        });
    }

    private void loadFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (Fragment frag : fragmentManager.getFragments()) {
            if (frag != null) {
                transaction.hide(frag);
            }
        }

        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);
        if (existingFragment != null) {
            transaction.show(existingFragment);
        } else {
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

    /**
     * Método público y centralizado para el cierre de sesión.
     * Cierra la sesión de Google, y al completarse, limpia los datos locales y navega a Login.
     */
    public void logout() {
        // Primero, cerrar la sesión de Google. El listener se ejecutará cuando termine.
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Ahora que Google ha cerrado sesión, limpiamos nuestros datos locales.
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear(); // Limpia todos los datos de SharedPreferences.
            editor.putBoolean("isLoggedIn", false); // Por seguridad, lo ponemos explícitamente a false.
            editor.commit(); // Usar commit para asegurar el guardado inmediato.

            // Por último, navegar a la pantalla de Login.
            navigateToLogin();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (homeFragment != null && homeFragment.isVisible()) {
            homeFragment.refreshData();
        }
    }
}
