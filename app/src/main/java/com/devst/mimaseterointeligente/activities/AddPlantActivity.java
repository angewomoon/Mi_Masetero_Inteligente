package com.devst.mimaseterointeligente.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.database.DatabaseHelper;
import com.devst.mimaseterointeligente.models.Plant;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AddPlantActivity extends AppCompatActivity {

    private static final String TAG = "AddPlantActivity";

    private ImageButton btnBack;
    private ImageView ivPlantImage;
    private LinearLayout layoutImageOverlay;
    private TextInputLayout tilPlantName, tilPlantType, tilSpecies, tilScientificName;
    private TextInputEditText etPlantName, etSpecies, etScientificName;
    private AutoCompleteTextView actvPlantType;
    private SwitchMaterial switchConnected;
    private MaterialButton btnSavePlant, btnSelectDevice;
    private LinearLayout layoutDeviceSelection;
    private TextView tvSelectedDevice;

    private DatabaseHelper dbHelper;
    private Uri selectedImageUri;
    private String currentPhotoPath;

    // Variables para dispositivo seleccionado
    private String selectedDeviceId;
    private String selectedDeviceName;

    // Tipos de plantas predefinidos
    private final String[] plantTypes = {
            "Ornamental", "Medicinal", "Aromática", "Suculenta",
            "Frutal", "Hortaliza", "Cactus", "Helecho", "Otro"
    };

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> deviceSelectionLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);

        initializeViews();
        setupActivityResultLaunchers();
        setupListeners();
        setupPlantTypeDropdown();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        ivPlantImage = findViewById(R.id.ivPlantImage);
        layoutImageOverlay = findViewById(R.id.layoutImageOverlay);
        tilPlantName = findViewById(R.id.tilPlantName);
        tilPlantType = findViewById(R.id.tilPlantType);
        tilSpecies = findViewById(R.id.tilSpecies);
        tilScientificName = findViewById(R.id.tilScientificName);
        etPlantName = findViewById(R.id.etPlantName);
        etSpecies = findViewById(R.id.etSpecies);
        etScientificName = findViewById(R.id.etScientificName);
        actvPlantType = findViewById(R.id.actvPlantType);
        switchConnected = findViewById(R.id.switchConnected);
        btnSavePlant = findViewById(R.id.btnSavePlant);

        // Componentes de selección de dispositivo
        layoutDeviceSelection = findViewById(R.id.layoutDeviceSelection);
        tvSelectedDevice = findViewById(R.id.tvSelectedDevice);
        btnSelectDevice = findViewById(R.id.btnSelectDevice);

        dbHelper = new DatabaseHelper(this);
    }

    private void setupActivityResultLaunchers() {
        // Launcher para la cámara
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "cameraLauncher: Resultado recibido, código: " + result.getResultCode());
                    if (result.getResultCode() == RESULT_OK) {
                        Log.d(TAG, "cameraLauncher: RESULT_OK, currentPhotoPath: " + currentPhotoPath);
                        if (currentPhotoPath != null) {
                            selectedImageUri = Uri.fromFile(new File(currentPhotoPath));
                            Log.d(TAG, "cameraLauncher: Mostrando imagen desde: " + selectedImageUri);
                            displayImage(selectedImageUri);
                        } else {
                            Log.e(TAG, "cameraLauncher: currentPhotoPath es null");
                            Toast.makeText(AddPlantActivity.this, "Error: No se guardó la ruta de la foto", Toast.LENGTH_SHORT).show();
                        }
                    } else if (result.getResultCode() == RESULT_CANCELED) {
                        Log.d(TAG, "cameraLauncher: Usuario canceló la cámara");
                    } else {
                        Log.e(TAG, "cameraLauncher: Código de resultado inesperado: " + result.getResultCode());
                    }
                }
        );

        // Launcher para la galería
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        displayImage(selectedImageUri);
                    }
                }
        );

        // Launcher para permisos
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Launcher para selección de dispositivo
        deviceSelectionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedDeviceId = result.getData().getStringExtra(DeviceSelectionActivity.EXTRA_SELECTED_DEVICE_ID);
                        selectedDeviceName = result.getData().getStringExtra(DeviceSelectionActivity.EXTRA_SELECTED_DEVICE_NAME);

                        if (selectedDeviceName != null) {
                            tvSelectedDevice.setText("Dispositivo: " + selectedDeviceName);
                            tvSelectedDevice.setTextColor(getResources().getColor(R.color.primary_green));
                            Log.d(TAG, "Dispositivo seleccionado: " + selectedDeviceName + " (ID: " + selectedDeviceId + ")");
                        }
                    }
                }
        );
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Click en la imagen para seleccionar foto
        layoutImageOverlay.setOnClickListener(v -> showImageSourceDialog());
        ivPlantImage.setOnClickListener(v -> showImageSourceDialog());

        btnSavePlant.setOnClickListener(v -> handleSavePlant());

        // Listener para el switch de conexión
        switchConnected.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutDeviceSelection.setVisibility(View.VISIBLE);
            } else {
                layoutDeviceSelection.setVisibility(View.GONE);
                // Limpiar selección de dispositivo
                selectedDeviceId = null;
                selectedDeviceName = null;
                tvSelectedDevice.setText(getString(R.string.device_none_selected));
                tvSelectedDevice.setTextColor(getResources().getColor(R.color.text_secondary));
            }
        });

        // Listener para botón de selección de dispositivo
        btnSelectDevice.setOnClickListener(v -> {
            Intent intent = new Intent(AddPlantActivity.this, DeviceSelectionActivity.class);
            deviceSelectionLauncher.launch(intent);
        });
    }

    private void setupPlantTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                plantTypes
        );
        actvPlantType.setAdapter(adapter);
    }

    private void showImageSourceDialog() {
        String[] options = {"Tomar foto", "Elegir de galería"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar imagen")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Tomar foto
                        checkCameraPermissionAndOpen();
                    } else {
                        // Elegir de galería
                        openGallery();
                    }
                })
                .show();
    }

    private void checkCameraPermissionAndOpen() {
        Log.d(TAG, "checkCameraPermissionAndOpen: Verificando permisos de cámara");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkCameraPermissionAndOpen: Permiso concedido, abriendo cámara");
            openCamera();
        } else {
            Log.d(TAG, "checkCameraPermissionAndOpen: Solicitando permiso de cámara");
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Log.d(TAG, "openCamera: Iniciando proceso de apertura de cámara");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;
        try {
            photoFile = createImageFile();
            Log.d(TAG, "openCamera: Archivo creado exitosamente: " + photoFile.getAbsolutePath());
        } catch (IOException ex) {
            Log.e(TAG, "openCamera: Error al crear archivo de imagen", ex);
            Toast.makeText(this, "Error al crear archivo de imagen: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        if (photoFile != null) {
            try {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.devst.mimaseterointeligente.fileprovider",
                        photoFile);
                Log.d(TAG, "openCamera: URI de FileProvider obtenida: " + photoURI.toString());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Intentar lanzar la cámara
                Log.d(TAG, "openCamera: Lanzando cámara...");
                cameraLauncher.launch(takePictureIntent);

            } catch (IllegalArgumentException e) {
                Log.e(TAG, "openCamera: Error al obtener URI del FileProvider", e);
                Toast.makeText(this, "Error al configurar la cámara: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(TAG, "openCamera: Error inesperado al abrir cámara", e);
                Toast.makeText(this, "No se pudo abrir la cámara. Asegúrate de tener una aplicación de cámara instalada.", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "openCamera: photoFile es null después de la creación");
            Toast.makeText(this, "Error: No se pudo crear el archivo de imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        String imageFileName = "PLANT_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void displayImage(Uri imageUri) {
        // Si la imagen viene de la galería (content://), copiarla al almacenamiento interno
        if (imageUri.getScheme() != null && imageUri.getScheme().equals("content")) {
            try {
                String savedPath = copyImageToInternalStorage(imageUri);
                if (savedPath != null) {
                    currentPhotoPath = savedPath;
                    selectedImageUri = Uri.fromFile(new File(savedPath));
                    imageUri = selectedImageUri; // Actualizar imageUri para usar en Glide
                }
            } catch (IOException e) {
                Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }
        } else {
            // Si viene de la cámara o es una ruta file://, actualizar selectedImageUri
            selectedImageUri = imageUri;
        }

        Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(ivPlantImage);

        // Ocultar el overlay una vez que se selecciona una imagen
        layoutImageOverlay.setVisibility(View.GONE);
    }

    /**
     * Copia una imagen desde una URI a un archivo en el almacenamiento interno
     * @param sourceUri URI de la imagen original
     * @return Ruta absoluta del archivo copiado
     * @throws IOException si ocurre un error al copiar
     */
    private String copyImageToInternalStorage(Uri sourceUri) throws IOException {
        // Crear archivo de destino
        String imageFileName = "PLANT_" + System.currentTimeMillis() + ".jpg";
        File storageDir = getExternalFilesDir(null);
        File destinationFile = new File(storageDir, imageFileName);

        // Copiar el contenido
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) {
                throw new IOException("No se pudo abrir la imagen de origen");
            }

            outputStream = new FileOutputStream(destinationFile);

            // Copiar bytes
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return destinationFile.getAbsolutePath();

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleSavePlant() {
        // Obtener valores de los campos
        String plantName = etPlantName.getText().toString().trim();
        String plantType = actvPlantType.getText().toString().trim();
        String species = etSpecies.getText().toString().trim();
        String scientificName = etScientificName.getText().toString().trim();
        boolean isConnected = switchConnected.isChecked();

        // Limpiar errores previos
        tilPlantName.setError(null);
        tilPlantType.setError(null);

        // Validar campos obligatorios
        boolean hasErrors = false;

        if (TextUtils.isEmpty(plantName)) {
            tilPlantName.setError("El nombre es obligatorio");
            hasErrors = true;
        }

        if (TextUtils.isEmpty(plantType)) {
            tilPlantType.setError("El tipo es obligatorio");
            hasErrors = true;
        }

        if (hasErrors) {
            return;
        }

        // Obtener el ID del usuario actual
        SharedPreferences prefs = getSharedPreferences("MaseteroPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear objeto Plant
        Plant plant = new Plant();
        plant.setUserId(userId);
        plant.setName(plantName);
        plant.setType(plantType);
        plant.setSpecies(species);
        plant.setScientificName(scientificName);
        plant.setConnected(isConnected);

        // Si está conectado, verificar que se haya seleccionado un dispositivo
        if (isConnected) {
            if (selectedDeviceId == null || selectedDeviceId.isEmpty()) {
                Toast.makeText(this, "Por favor selecciona un dispositivo", Toast.LENGTH_SHORT).show();
                return;
            }
            plant.setDeviceId(selectedDeviceId);
            Log.d(TAG, "Planta conectada a dispositivo: " + selectedDeviceId);
        } else {
            plant.setDeviceId(null);
        }

        // Guardar la ruta de la imagen local
        if (currentPhotoPath != null) {
            plant.setImageUrl(currentPhotoPath);
        }

        // Guardar en la base de datos
        long plantId = dbHelper.createPlant(plant);

        if (plantId > 0) {
            Toast.makeText(this, "Planta guardada exitosamente", Toast.LENGTH_SHORT).show();

            // Retornar a la actividad anterior
            Intent resultIntent = new Intent();
            resultIntent.putExtra("plant_id", plantId);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Error al guardar la planta", Toast.LENGTH_SHORT).show();
        }
    }
}