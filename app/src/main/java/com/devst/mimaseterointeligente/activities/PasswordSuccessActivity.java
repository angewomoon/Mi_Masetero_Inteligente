package com.devst.mimaseterointeligente.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.devst.mimaseterointeligente.R;
import com.google.android.material.button.MaterialButton;

public class PasswordSuccessActivity extends AppCompatActivity {

    private MaterialButton btnGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_success);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        btnGoToLogin = findViewById(R.id.btnGoToLogin);
    }

    private void setupListeners() {
        btnGoToLogin.setOnClickListener(v -> {
            // Navegar a LoginActivity y limpiar el stack de actividades
            Intent intent = new Intent(PasswordSuccessActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Prevenir que el usuario regrese usando el botón atrás
        // En su lugar, llevarlos al login
        Intent intent = new Intent(PasswordSuccessActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
