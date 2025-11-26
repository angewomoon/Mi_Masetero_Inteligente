package com.devst.mimaseterointeligente.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.activities.EditProfileActivity;
import com.devst.mimaseterointeligente.activities.MainActivity;
import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.devst.mimaseterointeligente.models.User;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private Button btnCerrarSesion;
    private Button btnEditarPerfil;
    private ImageView ivProfileAvatar;
    private TextView tvUsername;
    private TextView tvUserEmail;
    private TextView tvPlantsCount;
    private TextView tvAlertsCount;

    private DatabaseHelper dbHelper;
    private int userId;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // BroadcastReceiver para escuchar cambios en tiempo real
    private BroadcastReceiver statsUpdateReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dbHelper = new DatabaseHelper(requireContext());
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar el launcher para seleccionar imagen
        initializeImagePicker();

        // Enlazar vistas
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);
        ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvPlantsCount = view.findViewById(R.id.tvPlantsCount);
        tvAlertsCount = view.findViewById(R.id.tvAlertsCount);

        SharedPreferences prefs = requireContext().getSharedPreferences("MaseteroPrefs", Context.MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        // Configurar listeners
        btnCerrarSesion.setOnClickListener(v -> showLogoutDialog());

        // Listener para abrir la actividad de editar perfil
        btnEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        // Listener para cambiar foto de perfil
        ivProfileAvatar.setOnClickListener(v -> openImagePicker());

        // Inicializar BroadcastReceiver para actualizaciones en tiempo real
        setupBroadcastReceiver();
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
                                InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                                // Corregir orientación según EXIF
                                Bitmap orientedBitmap = fixImageOrientation(imageUri, bitmap);

                                // Comprimir imagen para no ocupar mucho espacio
                                Bitmap compressedBitmap = compressImage(orientedBitmap);

                                // Convertir a Base64 para guardar en la BD
                                String base64Image = bitmapToBase64(compressedBitmap);

                                // Guardar en la base de datos
                                saveProfileImage(base64Image);

                                // Mostrar la imagen en el ImageView
                                ivProfileAvatar.setImageBitmap(compressedBitmap);

                                Toast.makeText(requireContext(), "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();

                            } catch (Exception e) {
                                Log.e(TAG, "Error al cargar imagen: " + e.getMessage());
                                Toast.makeText(requireContext(), "Error al cargar imagen", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    /**
     * Abre el selector de imágenes
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /**
     * Configura el BroadcastReceiver para escuchar cambios en plantas, alertas y perfil
     */
    private void setupBroadcastReceiver() {
        statsUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    Log.d(TAG, "Broadcast recibido: " + action);

                    // Si es actualización de perfil, recargar todo
                    if ("com.devst.mimaseterointeligente.PROFILE_UPDATED".equals(action)) {
                        loadUserProfile();
                        loadStats();
                    } else {
                        // Para otros eventos, solo actualizar estadísticas
                        loadStats();
                    }
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
        loadStats();

        // Registrar el BroadcastReceiver para escuchar cambios en tiempo real
        if (statsUpdateReceiver != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.devst.mimaseterointeligente.PLANT_ADDED");
            filter.addAction("com.devst.mimaseterointeligente.PLANT_DELETED");
            filter.addAction("com.devst.mimaseterointeligente.ALERT_CREATED");
            filter.addAction("com.devst.mimaseterointeligente.ALERT_READ");
            filter.addAction("com.devst.mimaseterointeligente.PROFILE_UPDATED"); // NUEVO: Escuchar cambios de perfil

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireContext().registerReceiver(statsUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                requireContext().registerReceiver(statsUpdateReceiver, filter);
            }
            Log.d(TAG, "BroadcastReceiver registrado");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Desregistrar el BroadcastReceiver para evitar fugas de memoria
        if (statsUpdateReceiver != null) {
            try {
                requireContext().unregisterReceiver(statsUpdateReceiver);
                Log.d(TAG, "BroadcastReceiver desregistrado");
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error al desregistrar receiver: " + e.getMessage());
            }
        }
    }

    private void loadUserProfile() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MaseteroPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("userName", "Usuario");
        String email = prefs.getString("userEmail", "usuario@email.com");
        tvUsername.setText(username);
        tvUserEmail.setText(email);

        // Cargar foto de perfil desde la base de datos
        if (userId != -1) {
            User user = dbHelper.getUserByEmail(email);
            if (user != null && user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                try {
                    Bitmap bitmap = base64ToBitmap(user.getProfileImage());
                    ivProfileAvatar.setImageBitmap(bitmap);
                    Log.d(TAG, "Foto de perfil cargada correctamente");
                } catch (Exception e) {
                    Log.e(TAG, "Error al cargar foto de perfil: " + e.getMessage());
                    // Si hay error, usar imagen por defecto
                    ivProfileAvatar.setImageResource(R.drawable.ic_email);
                }
            } else {
                // Si no hay foto de perfil, usar imagen por defecto
                Log.d(TAG, "No hay foto de perfil, usando imagen por defecto");
                ivProfileAvatar.setImageResource(R.drawable.ic_email);
            }
        } else {
            // Si no hay userId, usar imagen por defecto
            ivProfileAvatar.setImageResource(R.drawable.ic_email);
        }
    }

    private void loadStats() {
        if (userId != -1) {
            // Cargar conteo de plantas
            int plantsCount = dbHelper.getPlantsCountByUserId(userId);
            tvPlantsCount.setText(String.valueOf(plantsCount));

            // Cargar conteo de alertas no leídas
            int alertsCount = dbHelper.getUnreadAlertsCount();
            tvAlertsCount.setText(String.valueOf(alertsCount));
        } else {
            tvPlantsCount.setText("0");
            tvAlertsCount.setText("0");
        }
    }

    /**
     * Corregir la orientación de la imagen según metadatos EXIF
     */
    private Bitmap fixImageOrientation(Uri imageUri, Bitmap bitmap) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
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
     * Comprime una imagen Bitmap para reducir su tamaño
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
     * Convierte un Bitmap a String Base64
     */
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * Convierte un String Base64 a Bitmap
     */
    private Bitmap base64ToBitmap(String base64Str) {
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    /**
     * Guarda la foto de perfil en la base de datos
     */
    private void saveProfileImage(String base64Image) {
        SharedPreferences prefs = requireContext().getSharedPreferences("MaseteroPrefs", Context.MODE_PRIVATE);
        String email = prefs.getString("userEmail", "");

        if (!email.isEmpty()) {
            User user = dbHelper.getUserByEmail(email);
            if (user != null) {
                user.setProfileImage(base64Image);
                int result = dbHelper.updateUser(user);
                if (result > 0) {
                    Log.d(TAG, "Foto de perfil guardada correctamente");

                    // Enviar broadcast para notificar cambios en el perfil
                    Intent broadcastIntent = new Intent("com.devst.mimaseterointeligente.PROFILE_UPDATED");
                    requireContext().sendBroadcast(broadcastIntent);
                } else {
                    Log.e(TAG, "Error al guardar foto de perfil");
                }
            }
        }
    }

    /**
     * Muestra un diálogo de confirmación para cerrar sesión
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).logout();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
