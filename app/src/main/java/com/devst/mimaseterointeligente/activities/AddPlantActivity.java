package com.devst.mimaseterointeligente.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import java.io.IOException;

public class AddPlantActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView ivPlantImage;
    private LinearLayout layoutImageOverlay;
    private TextInputLayout tilPlantName, tilPlantType, tilSpecies, tilScientificName;
    private TextInputEditText etPlantName, etSpecies, etScientificName;
    private AutoCompleteTextView actvPlantType;
    private SwitchMaterial switchConnected;
    private MaterialButton btnSavePlant;

    private DatabaseHelper dbHelper;
    private Uri selectedImageUri;
    private String currentPhotoPath;

    // Tipos de plantas predefinidos
    private final String[] plantTypes = {
            "Ornamental", "Medicinal", "Aromática", "Suculenta",
            "Frutal", "Hortaliza", "Cactus", "Helecho", "Otro"
    };

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
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

        dbHelper = new DatabaseHelper(this);
    }

    private void setupActivityResultLaunchers() {
        // Launcher para la cámara
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (currentPhotoPath != null) {
                            selectedImageUri = Uri.fromFile(new File(currentPhotoPath));
                            displayImage(selectedImageUri);
                        }
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
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Click en la imagen para seleccionar foto
        layoutImageOverlay.setOnClickListener(v -> showImageSourceDialog());
        ivPlantImage.setOnClickListener(v -> showImageSourceDialog());

        btnSavePlant.setOnClickListener(v -> handleSavePlant());
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.devst.mimaseterointeligente.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            }
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
        Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(ivPlantImage);

        // Ocultar el overlay una vez que se selecciona una imagen
        layoutImageOverlay.setVisibility(View.GONE);
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

        // Guardar la URI de la imagen como String
        if (selectedImageUri != null) {
            plant.setImageUrl(selectedImageUri.toString());
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