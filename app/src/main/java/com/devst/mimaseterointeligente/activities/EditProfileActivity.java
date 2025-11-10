package com.devst.mimaseterointeligente.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.devst.mimaseterointeligente.R;

public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Configurar la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarEditProfile);
        setSupportActionBar(toolbar);

        // Habilitar el botón de regreso (flecha)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    // Manejar el clic en el botón de regreso de la toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Esto simula el comportamiento del botón "atrás" del sistema
        return true;
    }
}
