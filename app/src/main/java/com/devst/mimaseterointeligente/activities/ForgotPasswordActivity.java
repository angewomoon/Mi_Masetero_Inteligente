package com.devst.mimaseterointeligente.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private MaterialButton btnSendCode;
    private ImageButton btnBack;
    private TextView tvBackToLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnBack = findViewById(R.id.btnBack);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        dbHelper = new DatabaseHelper(this);
    }

    private void setupListeners() {
        btnSendCode.setOnClickListener(v -> handleSendCode());
        btnBack.setOnClickListener(v -> finish());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void handleSendCode() {
        String email = etEmail.getText().toString().trim();

        // Limpiar errores previos
        tilEmail.setError(null);

        // Validar email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Por favor ingresa tu correo electrónico");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Por favor ingresa un correo válido");
            return;
        }

        // Verificar si el email existe en la base de datos
        if (!dbHelper.emailExists(email)) {
            tilEmail.setError("Este correo no está registrado");
            return;
        }

        // Generar código de verificación de 6 dígitos
        String verificationCode = generateVerificationCode();

        // En una aplicación real, aquí enviarías el código por email
        // Para este proyecto, lo almacenaremos temporalmente
        saveVerificationCode(email, verificationCode);

        // Mostrar el código en un Toast para propósitos de desarrollo
        Toast.makeText(this, "Código de verificación: " + verificationCode, 
                Toast.LENGTH_LONG).show();

        // Navegar a la pantalla de verificación de código
        Intent intent = new Intent(ForgotPasswordActivity.this, VerifyCodeActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("verification_code", verificationCode);
        startActivity(intent);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Genera un número de 6 dígitos
        return String.valueOf(code);
    }

    private void saveVerificationCode(String email, String code) {
        // Guardar el código en SharedPreferences temporalmente
        getSharedPreferences("PasswordRecovery", MODE_PRIVATE)
                .edit()
                .putString("verification_code_" + email, code)
                .putLong("code_timestamp_" + email, System.currentTimeMillis())
                .apply();
    }
}
