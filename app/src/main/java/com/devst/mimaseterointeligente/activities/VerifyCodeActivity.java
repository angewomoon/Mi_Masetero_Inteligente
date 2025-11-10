package com.devst.mimaseterointeligente.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.devst.mimaseterointeligente.R;
import com.google.android.material.button.MaterialButton;

public class VerifyCodeActivity extends AppCompatActivity {

    private EditText etCode1, etCode2, etCode3, etCode4, etCode5, etCode6;
    private MaterialButton btnVerify;
    private ImageButton btnBack;
    private TextView tvDescription, tvResendCode;
    
    private String email;
    private String verificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        // Obtener datos del Intent
        email = getIntent().getStringExtra("email");
        verificationCode = getIntent().getStringExtra("verification_code");

        initializeViews();
        setupListeners();
        setupCodeInputs();
    }

    private void initializeViews() {
        etCode1 = findViewById(R.id.etCode1);
        etCode2 = findViewById(R.id.etCode2);
        etCode3 = findViewById(R.id.etCode3);
        etCode4 = findViewById(R.id.etCode4);
        etCode5 = findViewById(R.id.etCode5);
        etCode6 = findViewById(R.id.etCode6);
        btnVerify = findViewById(R.id.btnVerify);
        btnBack = findViewById(R.id.btnBack);
        tvDescription = findViewById(R.id.tvDescription);
        tvResendCode = findViewById(R.id.tvResendCode);

        // Actualizar descripción con el email
        if (email != null) {
            tvDescription.setText("Hemos enviado un código de 6 dígitos a\n" + email);
        }
    }

    private void setupListeners() {
        btnVerify.setOnClickListener(v -> handleVerifyCode());
        btnBack.setOnClickListener(v -> finish());
        tvResendCode.setOnClickListener(v -> handleResendCode());
    }

    private void setupCodeInputs() {
        // Array de campos para facilitar la navegación
        EditText[] codeFields = {etCode1, etCode2, etCode3, etCode4, etCode5, etCode6};

        for (int i = 0; i < codeFields.length; i++) {
            final int index = i;
            codeFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1) {
                        // Mover al siguiente campo si existe
                        if (index < codeFields.length - 1) {
                            codeFields[index + 1].requestFocus();
                        }
                    } else if (s.length() == 0) {
                        // Mover al campo anterior si se borra
                        if (index > 0) {
                            codeFields[index - 1].requestFocus();
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Auto-focus en el primer campo
        etCode1.requestFocus();
    }

    private void handleVerifyCode() {
        // Obtener el código ingresado
        String enteredCode = etCode1.getText().toString() +
                etCode2.getText().toString() +
                etCode3.getText().toString() +
                etCode4.getText().toString() +
                etCode5.getText().toString() +
                etCode6.getText().toString();

        // Validar que el código esté completo
        if (enteredCode.length() != 6) {
            Toast.makeText(this, "Por favor ingresa el código completo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar el código
        if (enteredCode.equals(verificationCode)) {
            // Código correcto, navegar a la pantalla de restablecer contraseña
            Intent intent = new Intent(VerifyCodeActivity.this, ResetPasswordActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            finish(); // Cerrar esta actividad
        } else {
            // Código incorrecto
            Toast.makeText(this, "Código incorrecto. Intenta nuevamente", Toast.LENGTH_SHORT).show();
            clearCodeFields();
        }
    }

    private void handleResendCode() {
        // En una aplicación real, aquí reenviarías el código por email
        // Para este proyecto, generamos un nuevo código
        Toast.makeText(this, "Código reenviado: " + verificationCode, Toast.LENGTH_LONG).show();
    }

    private void clearCodeFields() {
        etCode1.setText("");
        etCode2.setText("");
        etCode3.setText("");
        etCode4.setText("");
        etCode5.setText("");
        etCode6.setText("");
        etCode1.requestFocus();
    }
}
