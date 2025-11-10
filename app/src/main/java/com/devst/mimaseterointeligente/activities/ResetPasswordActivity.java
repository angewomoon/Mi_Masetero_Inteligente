package com.devst.mimaseterointeligente.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.devst.mimaseterointeligente.utils.PasswordUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilNewPassword, tilConfirmPassword;
    private TextInputEditText etNewPassword, etConfirmPassword;
    private MaterialButton btnResetPassword;
    private ImageButton btnBack;
    private TextView tvReqLength, tvReqUppercase, tvReqNumber;
    
    private String email;
    private DatabaseHelper dbHelper;

    // Requisitos de contraseña
    private boolean hasMinLength = false;
    private boolean hasUppercase = false;
    private boolean hasNumber = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Obtener email del Intent
        email = getIntent().getStringExtra("email");

        initializeViews();
        setupListeners();
        setupPasswordValidation();
    }

    private void initializeViews() {
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBack = findViewById(R.id.btnBack);
        tvReqLength = findViewById(R.id.tvReqLength);
        tvReqUppercase = findViewById(R.id.tvReqUppercase);
        tvReqNumber = findViewById(R.id.tvReqNumber);
        
        dbHelper = new DatabaseHelper(this);
    }

    private void setupListeners() {
        btnResetPassword.setOnClickListener(v -> handleResetPassword());
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupPasswordValidation() {
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswordRequirements(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void validatePasswordRequirements(String password) {
        // Validar longitud mínima
        hasMinLength = password.length() >= 8;
        updateRequirementView(tvReqLength, hasMinLength);

        // Validar letra mayúscula
        hasUppercase = password.matches(".*[A-Z].*");
        updateRequirementView(tvReqUppercase, hasUppercase);

        // Validar número
        hasNumber = password.matches(".*\\d.*");
        updateRequirementView(tvReqNumber, hasNumber);
    }

    private void updateRequirementView(TextView textView, boolean isMet) {
        if (isMet) {
            textView.setTextColor(ContextCompat.getColor(this, R.color.success_green));
            textView.setText("✓ " + textView.getText().toString().replace("• ", ""));
        } else {
            textView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            String text = textView.getText().toString();
            if (!text.startsWith("•")) {
                textView.setText("• " + text.replace("✓ ", ""));
            }
        }
    }

    private void handleResetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Limpiar errores previos
        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);

        // Validar campos vacíos
        if (TextUtils.isEmpty(newPassword)) {
            tilNewPassword.setError("Por favor ingresa una contraseña");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Por favor confirma tu contraseña");
            return;
        }

        // Validar requisitos de contraseña
        if (!hasMinLength || !hasUppercase || !hasNumber) {
            tilNewPassword.setError("La contraseña no cumple con los requisitos");
            return;
        }

        // Validar que las contraseñas coincidan
        if (!newPassword.equals(confirmPassword)) {
            tilConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        // Actualizar la contraseña en la base de datos
        String hashedPassword = PasswordUtils.hashPassword(newPassword);
        boolean success = dbHelper.updatePassword(email, hashedPassword);

        if (success) {
            // Limpiar datos de recuperación
            clearRecoveryData();
            
            // Navegar a la pantalla de éxito
            Intent intent = new Intent(ResetPasswordActivity.this, PasswordSuccessActivity.class);
            startActivity(intent);
            
            // Cerrar todas las actividades de recuperación
            finishAffinity();
        } else {
            Toast.makeText(this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearRecoveryData() {
        // Limpiar SharedPreferences de recuperación
        getSharedPreferences("PasswordRecovery", MODE_PRIVATE)
                .edit()
                .remove("verification_code_" + email)
                .remove("code_timestamp_" + email)
                .apply();
    }
}
