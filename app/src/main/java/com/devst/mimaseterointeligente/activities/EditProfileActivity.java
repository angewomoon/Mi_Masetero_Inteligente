package com.devst.mimaseterointeligente.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.devst.mimaseterointeligente.models.User;
import com.devst.mimaseterointeligente.utils.PasswordUtils;
import com.devst.mimaseterointeligente.utils.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Actividad para editar el perfil del usuario
 */
public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private FrameLayout layoutProfileImage;
    private ImageView ivProfileImage;
    private EditText etName;
    private EditText etEmail;
    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmNewPassword;
    private Button btnSaveChanges;
    private Button btnChangePassword;
    private Button btnDeleteAccount;
    private ImageView btnBack;
    private TextView tvReqLength, tvReqUppercase, tvReqNumber;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private User currentUser;

    // Para manejar la selección de imagen
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private boolean deleteProfileImage = false; // Flag para eliminar imagen

    // Requisitos de contraseña
    private boolean hasMinLength = false;
    private boolean hasUppercase = false;
    private boolean hasNumber = false;

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
        layoutProfileImage = findViewById(R.id.layoutProfileImage);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnBack = findViewById(R.id.btnBack);
        tvReqLength = findViewById(R.id.tvReqLength);
        tvReqUppercase = findViewById(R.id.tvReqUppercase);
        tvReqNumber = findViewById(R.id.tvReqNumber);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Inicializar el launcher para seleccionar imagen
        initializeImagePicker();

        // Configurar listeners
        btnSaveChanges.setOnClickListener(v -> saveChanges());
        btnChangePassword.setOnClickListener(v -> changePassword());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        btnBack.setOnClickListener(v -> finish());
        layoutProfileImage.setOnClickListener(v -> showImageSourceDialog());

        // Validación en tiempo real de la nueva contraseña
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

    /**
     * Inicializa el launcher para seleccionar imágenes de la galería
     */
    private void initializeImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            try {
                                // Cargar la imagen
                                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                                // Corregir orientación según EXIF
                                Bitmap orientedBitmap = fixImageOrientation(imageUri, bitmap);

                                // Comprimir imagen
                                Bitmap compressedBitmap = compressImage(orientedBitmap);

                                // Convertir a Base64
                                String base64Image = bitmapToBase64(compressedBitmap);

                                // Guardar INMEDIATAMENTE en la base de datos (igual que ProfileFragment)
                                saveProfileImage(base64Image);

                                // Mostrar preview
                                ivProfileImage.setImageBitmap(compressedBitmap);

                                // Resetear flag de eliminar si había uno
                                deleteProfileImage = false;

                                Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();

                            } catch (Exception e) {
                                Log.e(TAG, "Error al cargar imagen: " + e.getMessage());
                                Toast.makeText(this, "Error al cargar imagen", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    /**
     * Cargar datos del usuario
     */
    private void loadUserData() {
        try {
            // Usar MaseteroPrefs que es donde realmente se guardan los datos (igual que ProfileFragment)
            SharedPreferences prefs = getSharedPreferences("MaseteroPrefs", Context.MODE_PRIVATE);
            String email = prefs.getString("userEmail", "");

            Log.d(TAG, "Cargando usuario con email: " + email);

            if (email.isEmpty()) {
                // Si no hay email en MaseteroPrefs, intentar con SessionManager
                email = sessionManager.getUserEmail();
                Log.d(TAG, "Email desde SessionManager: " + email);
            }

            if (!email.isEmpty()) {
                currentUser = dbHelper.getUserByEmail(email);

                if (currentUser != null) {
                    Log.d(TAG, "Usuario cargado: " + currentUser.getName());
                    etName.setText(currentUser.getName());
                    etEmail.setText(currentUser.getEmail());

                    // Cargar imagen de perfil si existe
                    if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
                        try {
                            Bitmap bitmap = base64ToBitmap(currentUser.getProfileImage());
                            ivProfileImage.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            Log.e(TAG, "Error al cargar foto de perfil: " + e.getMessage());
                        }
                    }
                } else {
                    Log.e(TAG, "No se encontró usuario con email: " + email);
                }
            } else {
                Log.e(TAG, "No hay email guardado en SharedPreferences");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al cargar datos del usuario: " + e.getMessage(), e);
            Toast.makeText(this, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Guardar cambios de nombre y correo
     */
    private void saveChanges() {
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no cargado", Toast.LENGTH_SHORT).show();
            return;
        }

        String newName = etName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();

        // Validación del nombre
        // Si el campo está vacío, mantener el actual
        if (TextUtils.isEmpty(newName)) {
            newName = currentUser.getName();
        }

        // Validación del correo
        // Si el campo está vacío, mantener el actual
        if (TextUtils.isEmpty(newEmail)) {
            newEmail = currentUser.getEmail();
        } else {
            // Si ingresó un correo, validar formato
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                etEmail.setError("Correo inválido");
                etEmail.requestFocus();
                return;
            }

            // Verificar si el correo ya existe (solo si cambió)
            if (!newEmail.equals(currentUser.getEmail())) {
                User existingUser = dbHelper.getUserByEmail(newEmail);
                if (existingUser != null) {
                    etEmail.setError("Este correo ya está en uso");
                    etEmail.requestFocus();
                    return;
                }
            }
        }

        try {
            // Actualizar nombre y correo
            currentUser.setName(newName);
            currentUser.setEmail(newEmail);

            // Manejar foto de perfil solo si se marcó para eliminar
            if (deleteProfileImage) {
                currentUser.setProfileImage(null);
            }
            // La imagen ya se guarda inmediatamente cuando se selecciona, no aquí

            int result = dbHelper.updateUser(currentUser);

            if (result > 0) {
                // Actualizar SessionManager (MaseteroSession)
                sessionManager.saveUserEmail(newEmail);
                sessionManager.saveUserName(newName);

                // IMPORTANTE: También actualizar MaseteroPrefs para compatibilidad con ProfileFragment
                SharedPreferences maseteroPrefs = getSharedPreferences("MaseteroPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = maseteroPrefs.edit();
                editor.putString("userName", newName);
                editor.putString("userEmail", newEmail);
                editor.apply();

                // Enviar broadcast para notificar a ProfileFragment que se actualizó el perfil
                Intent broadcastIntent = new Intent("com.devst.mimaseterointeligente.PROFILE_UPDATED");
                sendBroadcast(broadcastIntent);

                Toast.makeText(this, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar perfil: " + e.getMessage(), e);
            Toast.makeText(this, "Error al actualizar el perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Cambiar contraseña
     */
    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmNewPassword = etConfirmNewPassword.getText().toString().trim();

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

        // Validar requisitos de contraseña
        if (!hasMinLength || !hasUppercase || !hasNumber) {
            etNewPassword.setError("La contraseña no cumple con todos los requisitos");
            etNewPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmNewPassword)) {
            etConfirmNewPassword.setError("Confirma tu nueva contraseña");
            etConfirmNewPassword.requestFocus();
            return;
        }

        // Validar que las contraseñas coincidan
        if (!newPassword.equals(confirmNewPassword)) {
            etConfirmNewPassword.setError("Las contraseñas no coinciden");
            etConfirmNewPassword.requestFocus();
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
                etConfirmNewPassword.setText("");
            } else {
                Toast.makeText(this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Mostrar diálogo para seleccionar fuente de imagen
     */
    private void showImageSourceDialog() {
        // Determinar opciones según si hay foto actual
        String[] options;
        boolean hasProfileImage = currentUser != null
                && currentUser.getProfileImage() != null
                && !currentUser.getProfileImage().isEmpty();

        if (hasProfileImage) {
            options = new String[]{"Seleccionar desde Galería", "Eliminar Foto Actual", "Cancelar"};
        } else {
            options = new String[]{"Seleccionar desde Galería", "Cancelar"};
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Foto de Perfil");
        builder.setItems(options, (dialog, which) -> {
            if (hasProfileImage) {
                // Con foto: Galería (0), Eliminar (1), Cancelar (2)
                switch (which) {
                    case 0:
                        openGallery();
                        break;
                    case 1:
                        confirmDeleteProfileImage();
                        break;
                    case 2:
                        dialog.dismiss();
                        break;
                }
            } else {
                // Sin foto: Galería (0), Cancelar (1)
                switch (which) {
                    case 0:
                        openGallery();
                        break;
                    case 1:
                        dialog.dismiss();
                        break;
                }
            }
        });
        builder.show();
    }

    /**
     * Abrir galería para seleccionar imagen
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /**
     * Corregir la orientación de la imagen según metadatos EXIF
     */
    private Bitmap fixImageOrientation(Uri imageUri, Bitmap bitmap) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            ExifInterface exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setScale(1, -1);
                    break;
                default:
                    return bitmap;
            }

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (rotatedBitmap != bitmap) {
                bitmap.recycle();
            }
            return rotatedBitmap;

        } catch (Exception e) {
            Log.e(TAG, "Error al corregir orientación: " + e.getMessage());
            return bitmap;
        }
    }

    /**
     * Comprimir imagen para optimizar almacenamiento
     */
    private Bitmap compressImage(Bitmap bitmap) {
        int maxWidth = 400;
        int maxHeight = 400;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
    }

    /**
     * Convertir Bitmap a Base64
     */
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * Convertir Base64 a Bitmap
     */
    private Bitmap base64ToBitmap(String base64Str) {
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    /**
     * Confirmar eliminación de foto de perfil
     */
    private void confirmDeleteProfileImage() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Foto de Perfil")
                .setMessage("¿Estás seguro de que deseas eliminar tu foto de perfil?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteProfileImageAction())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Acción para eliminar la foto de perfil
     */
    private void deleteProfileImageAction() {
        // Marcar flag para eliminar al guardar
        deleteProfileImage = true;

        // Restaurar imagen por defecto
        ivProfileImage.setImageResource(R.drawable.ic_email);

        Toast.makeText(this, "Foto marcada para eliminar. Guarda los cambios para aplicar.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Guarda la foto de perfil en la base de datos inmediatamente (igual que ProfileFragment)
     */
    private void saveProfileImage(String base64Image) {
        // Usar MaseteroPrefs para obtener el email (igual que ProfileFragment)
        SharedPreferences prefs = getSharedPreferences("MaseteroPrefs", Context.MODE_PRIVATE);
        String email = prefs.getString("userEmail", "");

        if (!email.isEmpty()) {
            User user = dbHelper.getUserByEmail(email);
            if (user != null) {
                user.setProfileImage(base64Image);
                int result = dbHelper.updateUser(user);
                if (result > 0) {
                    // Actualizar currentUser también
                    currentUser = user;
                    Log.d(TAG, "Foto de perfil guardada correctamente");

                    // Enviar broadcast para notificar cambios en el perfil
                    Intent broadcastIntent = new Intent("com.devst.mimaseterointeligente.PROFILE_UPDATED");
                    sendBroadcast(broadcastIntent);
                } else {
                    Log.e(TAG, "Error al guardar foto de perfil");
                }
            }
        }
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
