package com.devst.mimaseterointeligente.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.fragments.AlertsFragment;

/**
 * Actividad que actúa como un simple contenedor para AlertsFragment.
 * Toda la lógica de negocio y la UI se gestionan dentro del fragmento.
 */
public class AlertsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. Establecer el layout que contiene el FrameLayout
        setContentView(R.layout.activity_alerts);

        // 2. Cargar el AlertsFragment en el contenedor si es la primera vez que se crea la actividad
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AlertsFragment())
                    .commit();
        }
    }
}
