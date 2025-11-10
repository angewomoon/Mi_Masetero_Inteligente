package com.devst.mimaseterointeligente.activities;

import android.Manifest;
import android.content.Intent;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;

public class EditPlantActivity extends AppCompatActivity {

    private ImageButton btnBack, btnDelete;
    private ImageView ivPlantImage;
    private LinearLayout layoutImageOverlay;
    private FloatingActionButton fabChangeImage;
    private TextInputLayout tilPlantName, tilPlantType, tilSpecies, tilScientificName;
    private TextInputEditText etPlantName, etSpecies, etScientificName;
    private AutoCompleteTextView actvPlantType;
    private SwitchMaterial switchConnected;
    private MaterialButton btnSavePlant;
    
    private DatabaseHelper dbHelper;
    private Plant currentPlant;
    private int plantId;
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
        setContentView(R.layout.activity_edit_plant);

        // Obtener el ID de la planta del Intent
        plantId = getIntent().getIntExtra("plant_id", -1);
        
        if (plantId == -1) {
            Toast.makeText(this, "Error: ID de planta no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupActivityResultLaunchers();
        setupListeners();
        setupPlantTypeDropdown();
        loadPlantData();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnDelete = findViewById(R.id.btnDelete);
        ivPlantImage = findViewById(R.id.ivPlantImage);
        layoutImageOverlay = findViewById(R.id.layoutImageOverlay);
        fabChangeImage = findViewById(R.id.fabChangeImage);
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
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
        
        // Click para cambiar imagen
        fabChangeImage.setOnClickListener(v -> showImageSourceDialog());
        ivPlantImage.setOnClickListener(v -> showImageSourceDialog());
        
        btnSavePlant.setOnClickListener(v -> handleUpdatePlant());
    }

    private void setupPlantTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, 
            android.R.layout.simple_dropdown_item_1line, 
            plantTypes
        );
        actvPlantType.setAdapter(adapter);
    }

    private void loadPlantData() {
        currentPlant = dbHelper.getPlantById(plantId);
        
        if (currentPlant == null) {
            Toast.makeText(this, "Error: Planta no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cargar datos en los campos
        etPlantName.setText(currentPlant.getName());
        actvPlantType.setText(currentPlant.getType(), false);
        etSpecies.setText(currentPlant.getSpecies());
        etScientificName.setText(currentPlant.getScientificName());
        switchConnected.setChecked(currentPlant.isConnected());

        // Cargar imagen si existe
        if (currentPlant.getImageUrl() != null && !currentPlant.getImageUrl().isEmpty()) {
            selectedImageUri = Uri.parse(currentPlant.getImageUrl());
            Glide.with(this)
                .load(selectedImageUri)
                .centerCrop()
                .placeholder(R.drawable.ic_plant_placeholder)
                .into(ivPlantImage);
        }
    }

    private void showImageSourceDialog() {
        String[] options = {"Tomar foto", "Elegir de galería"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cambiar imagen")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    checkCameraPermissionAndOpen();
                } else {
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
    }

    private void handleUpdatePlant() {
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

        // Actualizar el objeto Plant
        currentPlant.setName(plantName);
        currentPlant.setType(plantType);
        currentPlant.setSpecies(species);
        currentPlant.setScientificName(scientificName);
        currentPlant.setConnected(isConnected);
        
        // Actualizar la URI de la imagen si se cambió
        if (selectedImageUri != null) {
            currentPlant.setImageUrl(selectedImageUri.toString());
        }

        // Actualizar en la base de datos
        int rowsAffected = dbHelper.updatePlant(currentPlant);

        if (rowsAffected > 0) {
            Toast.makeText(this, "Planta actualizada exitosamente", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error al actualizar la planta", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Eliminar planta")
            .setMessage("¿Estás seguro de que deseas eliminar esta planta? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar", (dialog, which) -> handleDeletePlant())
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void handleDeletePlant() {
        dbHelper.deletePlant(plantId);
        Toast.makeText(this, "Planta eliminada", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
