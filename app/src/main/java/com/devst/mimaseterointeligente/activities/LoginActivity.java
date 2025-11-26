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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordText;
    private TextView registerText;
    private ProgressBar progressBar;
    private SignInButton googleSignInButton;

    private GoogleSignInClient googleSignInClient;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth firebaseAuth; // <-- Añadido para Firebase Auth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("MaseteroPrefs", MODE_PRIVATE);
        firebaseAuth = FirebaseAuth.getInstance(); // <-- Inicializar Firebase Auth

        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            navigateToMainActivity();
            return;
        }

        initViews();
        configureGoogleSignIn();
        setupListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        registerText = findViewById(R.id.registerText);
        progressBar = findViewById(R.id.progressBar);
        googleSignInButton.setSize(SignInButton.SIZE_WIDE);
        progressBar.setVisibility(View.GONE);
    }

    private void configureGoogleSignIn() {
        // **INICIO DE LA CORRECCIÓN**
        // Configurar Google Sign-In para solicitar un idToken, que es necesario para Firebase Auth
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // <-- LA LÍNEA CLAVE
                .requestEmail()
                .build();
        // **FIN DE LA CORRECCIÓN**

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
        forgotPasswordText.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
        registerText.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim().toLowerCase();
        String password = passwordEditText.getText().toString().trim();

        if (!validateLoginForm(email, password)) return;

        showLoading(true);
        User user = databaseHelper.getUserByEmail(email);

        if (user != null && PasswordUtils.verifyPassword(password, user.getPassword())) {
            saveUserSession(user);
            navigateToMainActivity();
        } else {
            showLoading(false);
            Toast.makeText(this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateLoginForm(String email, String password) {
        // ... (sin cambios)
        return true;
    }

    private void signInWithGoogle() {
        showLoading(true);
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
                // **NUEVO:** Autenticar con Firebase
                firebaseAuthWithGoogle(account);
            }
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            showLoading(false);
            Toast.makeText(this, "Error al iniciar sesión con Google: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Inicio de sesión en Firebase exitoso, ahora manejar la base de datos local
                        String email = acct.getEmail();
                        User user = databaseHelper.getUserByEmail(email);

                        if (user == null) {
                            user = new User();
                            user.setName(acct.getDisplayName());
                            user.setEmail(email);
                            user.setGoogleId(acct.getId());
                            if (acct.getPhotoUrl() != null) {
                                user.setProfileImage(acct.getPhotoUrl().toString());
                            }
                            long newId = databaseHelper.createUser(user);
                            user.setId((int)newId);
                        }

                        saveUserSession(user);
                        navigateToMainActivity();

                    } else {
                        // Si el inicio de sesión en Firebase falla
                        Log.w(TAG, "signInWithCredential_failure", task.getException());
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, "Error de autenticación con Firebase.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
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
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        googleSignInButton.setEnabled(!show);
    }
}
