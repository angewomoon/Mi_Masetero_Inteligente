package com.devst.mimaseterointeligente.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.devst.mimaseterointeligente.models.User;
import com.devst.mimaseterointeligente.utils.PasswordUtils;
import com.devst.mimaseterointeligente.utils.SessionManager;

/**
 * Actividad para editar el perfil del usuario
 */
public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivProfileImage;
    private EditText etName;
    private EditText etEmail;
    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private Button btnSaveChanges;
    private Button btnChangePassword;
    private Button btnDeleteAccount;
    private ImageView btnBack;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        loadUserData();
    }

    /**
     * Inicializar vistas
     */
    private void initViews() {
        ivProfileImage = findViewById(R.id.ivProfileImage);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnBack = findViewById(R.id.btnBack);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Configurar listeners
        btnSaveChanges.setOnClickListener(v -> saveChanges());
        btnChangePassword.setOnClickListener(v -> changePassword());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        btnBack.setOnClickListener(v -> finish());
        ivProfileImage.setOnClickListener(v -> changeProfileImage());
    }

    /**
     * Cargar datos del usuario
     */
    private void loadUserData() {
        try {
            currentUser = dbHelper.getUserByEmail(sessionManager.getUserEmail());

            if (currentUser != null) {
                etName.setText(currentUser.getName());
                etEmail.setText(currentUser.getEmail());

                // TODO: Cargar imagen de perfil si existe
                if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
                    // Glide.with(this).load(currentUser.getProfileImage()).into(ivProfileImage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Guardar cambios de nombre y correo
     */
    private void saveChanges() {
        String newName = etName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(newName)) {
            etName.setError("El nombre es requerido");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newEmail)) {
            etEmail.setError("El correo es requerido");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            etEmail.setError("Correo inválido");
            etEmail.requestFocus();
            return;
        }

        // Verificar si el correo ya existe (si cambió)
        if (!newEmail.equals(currentUser.getEmail())) {
            User existingUser = dbHelper.getUserByEmail(newEmail);
            if (existingUser != null) {
                etEmail.setError("Este correo ya está en uso");
                etEmail.requestFocus();
                return;
            }
        }

        try {
            // Actualizar usuario
            currentUser.setName(newName);
            currentUser.setEmail(newEmail);

            int result = dbHelper.updateUser(currentUser);

            if (result > 0) {
                // Actualizar sesión
                sessionManager.saveUserEmail(newEmail);

                Toast.makeText(this, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Cambiar contraseña
     */
    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError("Ingresa tu contraseña actual");
            etCurrentPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Ingresa una nueva contraseña");
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etNewPassword.requestFocus();
            return;
        }

        try {
            // Verificar contraseña actual
            String hashedCurrentPassword = PasswordUtils.hashPassword(currentPassword);

            if (!hashedCurrentPassword.equals(currentUser.getPassword())) {
                etCurrentPassword.setError("Contraseña actual incorrecta");
                etCurrentPassword.requestFocus();
                return;
            }

            // Hash de la nueva contraseña
            String hashedNewPassword = PasswordUtils.hashPassword(newPassword);

            // Actualizar contraseña
            currentUser.setPassword(hashedNewPassword);
            int result = dbHelper.updateUser(currentUser);

            if (result > 0) {
                Toast.makeText(this, "Contraseña actualizada exitosamente", Toast.LENGTH_SHORT).show();

                // Limpiar campos
                etCurrentPassword.setText("");
                etNewPassword.setText("");
            } else {
                Toast.makeText(this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Cambiar imagen de perfil
     */
    private void changeProfileImage() {
        Toast.makeText(this, "Función de cambio de imagen próximamente", Toast.LENGTH_SHORT).show();
        // TODO: Implementar selección de imagen desde galería o cámara
    }

    /**
     * Mostrar diálogo de confirmación para eliminar cuenta
     */
    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Eliminar cuenta del usuario
     */
    private void deleteAccount() {
        try {
            // TODO: Implementar eliminación completa del usuario
            // - Eliminar plantas del usuario
            // - Eliminar alertas
            // - Eliminar datos de sensores
            // - Eliminar usuario

            Toast.makeText(this, "Función de eliminación de cuenta próximamente", Toast.LENGTH_SHORT).show();

            // Por ahora, solo cerrar sesión
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show();
        }
    }
}
