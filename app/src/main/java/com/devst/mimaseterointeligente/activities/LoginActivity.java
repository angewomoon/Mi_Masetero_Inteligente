package com.devst.mimaseterointeligente.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.devst.mimaseterointeligente.models.User;
import com.devst.mimaseterointeligente.utils.PasswordUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordText;
    private TextView registerText;
    private ProgressBar progressBar;

    private SignInButton googleSignInButton;           // ✅ botón oficial
    private GoogleSignInClient googleSignInClient;     // ✅ faltaba este campo

    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("MaseteroPrefs", MODE_PRIVATE);

        // (opcional) si ya hay sesión, saltar directo
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            navigateToMainActivity();
            return;
        }

        initViews();
        googleSignInButton.setSize(com.google.android.gms.common.SignInButton.SIZE_WIDE);
        configureGoogleSignIn();
        setupListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        googleSignInButton = findViewById(R.id.googleSignInButton); // ✅ solo aquí
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        registerText = findViewById(R.id.registerText);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()

                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso); // ✅ usa el campo
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        forgotPasswordText.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        registerText.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim().toLowerCase(); // 👍 normaliza
        String password = passwordEditText.getText().toString().trim();

        if (!validateLoginForm(email, password)) return;

        showLoading(true);
        User user = databaseHelper.getUserByEmail(email);

        if (user != null) {
            String hashedPassword = PasswordUtils.hashPassword(password);
            if (hashedPassword.equals(user.getPassword())) {
                saveUserSession(user);
                navigateToMainActivity();
            } else {
                showLoading(false);
                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                passwordEditText.setError("Contraseña incorrecta");
            }
        } else {
            showLoading(false);
            Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
            emailEditText.setError("Email no registrado");
        }
    }

    private boolean validateLoginForm(String email, String password) {
        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("El email es requerido");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email inválido");
            valid = false;
        } else {
            emailEditText.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("La contraseña es requerida");
            valid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener al menos 6 caracteres");
            valid = false;
        } else {
            passwordEditText.setError(null);
        }
        return valid;
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String email = account.getEmail();
                if (email == null) { // 👀 a veces puede venir null
                    Toast.makeText(this, "Tu cuenta de Google no tiene email asociado", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    return;
                }
                email = email.trim().toLowerCase();

                String name = account.getDisplayName();
                String googleId = account.getId();
                String photoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;

                User existingUser = databaseHelper.getUserByEmail(email);
                if (existingUser != null) {
                    existingUser.setGoogleId(googleId);
                    if (photoUrl != null) existingUser.setProfileImage(photoUrl);
                    databaseHelper.updateUser(existingUser);
                    saveUserSession(existingUser);
                } else {
                    User newUser = new User();
                    newUser.setName(name);
                    newUser.setEmail(email);
                    newUser.setGoogleId(googleId);
                    newUser.setProfileImage(photoUrl);
                    long userId = databaseHelper.createUser(newUser);
                    newUser.setId((int) userId);
                    saveUserSession(newUser);
                }
                navigateToMainActivity();
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
            showLoading(false);
        }
    }

    private void saveUserSession(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putInt("userId", user.getId());
        editor.putString("userEmail", user.getEmail());
        editor.putString("userName", user.getName());
        editor.putString("userProfileImage", user.getProfileImage());
        editor.apply();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
            googleSignInButton.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            googleSignInButton.setEnabled(true);
        }
    }
}
