package com.devst.mimaseterointeligente.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.devst.mimaseterointeligente.models.User;
import com.devst.mimaseterointeligente.utils.PasswordUtils;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button registerButton;
    private ImageButton backButton;
    private ProgressBar progressBar;

    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar base de datos y SharedPreferences
        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("MaseteroPrefs", MODE_PRIVATE);

        // Inicializar vistas
        initViews();

        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        backButton = findViewById(R.id.backButton);
        progressBar = findViewById(R.id.progressBar);

        // Ocultar progress bar inicialmente
        progressBar.setVisibility(View.GONE);
    }

    private void setupListeners() {
        // Botón de registro
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });

        // Botón de volver
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void attemptRegister() {
        // Obtener valores
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim().toLowerCase();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validar formulario
        if (!validateRegisterForm(name, email, password, confirmPassword)) {
            return;
        }

        // Mostrar progress bar
        showLoading(true);

        // Verificar si el email ya está registrado
        User existingUser = databaseHelper.getUserByEmail(email);

        if (existingUser != null) {
            // Email ya registrado
            showLoading(false);
            Toast.makeText(this, "Este email ya está registrado", Toast.LENGTH_SHORT).show();
            emailEditText.setError("Email ya registrado");
            return;
        }

        // Crear nuevo usuario
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(PasswordUtils.hashPassword(password));

        // Guardar en base de datos
        long userId = databaseHelper.createUser(newUser);

        if (userId != -1) {
            // Registro exitoso
            newUser.setId((int) userId);

            // Guardar sesión
            saveUserSession(newUser);

            // Navegar a MainActivity
            navigateToMainActivity();

            Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show();
        } else {
            // Error al crear usuario
            showLoading(false);
            Toast.makeText(this, "Error al crear la cuenta", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateRegisterForm(String name, String email, String password, String confirmPassword) {
        boolean valid = true;

        // Validar nombre
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("El nombre es requerido");
            valid = false;
        } else if (name.length() < 2) {
            nameEditText.setError("El nombre debe tener al menos 2 caracteres");
            valid = false;
        } else {
            nameEditText.setError(null);
        }

        // Validar email
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("El email es requerido");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email inválido");
            valid = false;
        } else {
            emailEditText.setError(null);
        }

        // Validar contraseña
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("La contraseña es requerida");
            valid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener al menos 6 caracteres");
            valid = false;
        } else {
            passwordEditText.setError(null);
        }

        // Validar confirmación de contraseña
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Confirma tu contraseña");
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Las contraseñas no coinciden");
            valid = false;
        } else {
            confirmPasswordEditText.setError(null);
        }

        return valid;
    }

    private void saveUserSession(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putInt("userId", user.getId());
        editor.putString("userEmail", user.getEmail());
        editor.putString("userName", user.getName());
        editor.apply();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            registerButton.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            registerButton.setEnabled(true);
        }
    }
}
