# 🌱 Guía de Integración Completa
## Mi Masetero Inteligente - ESP32 + Android + Firebase

---

## 📋 Tabla de Contenidos

1. [Visión General del Sistema](#visión-general-del-sistema)
2. [Configuración ESP32](#configuración-esp32)
3. [Configuración Android](#configuración-android)
4. [Configuración Firebase](#configuración-firebase)
5. [Flujo de Datos](#flujo-de-datos)
6. [Ejemplos de Uso](#ejemplos-de-uso)
7. [Troubleshooting](#troubleshooting)

---

## 🎯 Visión General del Sistema

### Componentes

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│   ESP32 Dev     │   WiFi  │  Firebase        │  WiFi   │  Android App    │
│   Kit V1        ├────────>│  Realtime DB     │<────────┤  (Kotlin/Java)  │
│                 │         │                  │         │                 │
│  - Sensores     │         │  /devices        │         │  - UI           │
│  - Actuadores   │         │  /plants         │         │  - Selección    │
│  - OLED         │         │  /sensor_data    │         │  - Dashboard    │
└─────────────────┘         │  /alerts         │         └─────────────────┘
                           └──────────────────┘
```

### Arquitectura de Datos

```
Firebase Realtime Database
├── /devices
│   └── ESP32-{CHIP_ID}
│       ├── device_id: "ESP32-A1B2C3"
│       ├── device_name: "Masetero Principal"
│       ├── is_available: true/false
│       ├── current_plant_id: "123" o ""
│       ├── last_seen: "1736956800000"
│       ├── ip_address: "192.168.1.100"
│       └── firmware_version: "1.0.0"
│
├── /plants (opcional - si usas Firebase para plantas)
│   └── {plantId}
│       ├── plant_name: "Mi Suculenta"
│       ├── device_id: "ESP32-A1B2C3"
│       └── is_connected: true
│
├── /sensor_data
│   └── {timestamp}
│       ├── plant_id: "123"
│       ├── soil_humidity: 45.5
│       ├── temperature: 25.3
│       ├── ambient_humidity: 60.0
│       ├── uv_level: 5.2
│       ├── water_level: 75.0
│       ├── pest_count: 0
│       └── timestamp: "1736956800000"
│
└── /alerts (opcional - si generas alertas desde ESP32)
```

---

## 🔧 Configuración ESP32

### 1. Hardware Requerido

- **ESP32 Dev Kit V1**
- **Sensores:**
  - Sensor de humedad del suelo (Capacitivo o resistivo)
  - DHT11 (Temperatura y humedad ambiente)
  - HC-SR04 (Ultrasónico - Nivel de agua)
  - Y401 (Ultrasónico - Detección de plagas)
  - BH1750FVI (Sensor de luz digital I2C)
  - LDR (Módulo sensor de luz analógico)
- **Actuadores:**
  - Módulo relé de 2 canales JQC-3FF-S-Z
  - 2x Electroválvula solenoide FPD-270A
- **Display:**
  - OLED 0.96" (SSD1306, I2C)

### 2. Conexiones de Pines

```cpp
// Sensores Analógicos
GPIO 34 → Sensor de humedad del suelo
GPIO 35 → Módulo LDR

// DHT11
GPIO 4  → DHT11 Data

// HC-SR04 (Nivel de agua)
GPIO 5  → TRIG
GPIO 18 → ECHO

// Y401 (Detección de plagas)
GPIO 19 → TRIG
GPIO 21 → ECHO

// BH1750FVI (I2C)
GPIO 21 → SDA
GPIO 22 → SCL

// Relés (Activo BAJO)
GPIO 25 → Relé 1 (Electroválvula 1)
GPIO 26 → Relé 2 (Electroválvula 2)

// OLED (I2C - compartido con BH1750)
GPIO 21 → SDA
GPIO 22 → SCL
```

### 3. Librerías Necesarias (Arduino IDE)

Instalar desde **Library Manager**:

```
1. Firebase ESP32 Client by Mobizt
2. DHT sensor library by Adafruit
3. Adafruit Unified Sensor
4. Adafruit GFX Library
5. Adafruit SSD1306
6. Wire (incluida)
7. WiFi (incluida)
```

### 4. Configuración del Código

Editar `ESP32_Firebase_Integration.ino`:

```cpp
// WiFi
#define WIFI_SSID "TU_RED_WIFI"
#define WIFI_PASSWORD "TU_PASSWORD"

// Firebase
#define FIREBASE_HOST "tu-proyecto.firebaseio.com"
#define FIREBASE_AUTH "TU_DATABASE_SECRET"

// Dispositivo
#define DEVICE_NAME "Masetero Principal"  // Personalizable
```

### 5. Cargar el Código

1. Conectar ESP32 por USB
2. **Tools → Board → ESP32 Dev Module**
3. **Tools → Port → COMx** (tu puerto)
4. **Upload** ✅
5. Abrir **Serial Monitor** (115200 baud) para ver logs

### 6. Verificación

En el Serial Monitor deberías ver:

```
=== ESP32 Masetero Inteligente ===

Device ID: ESP32-A1B2C3D4E5F6
Pines configurados
Conectando a WiFi........
WiFi conectado!
IP: 192.168.1.100
Configurando Firebase...
Firebase configurado
Registrando dispositivo en Firebase...
Dispositivo registrado exitosamente!
Sistema listo!
```

---

## 📱 Configuración Android

### 1. Dependencias (build.gradle.kts)

Ya están agregadas:

```kotlin
implementation("com.google.firebase:firebase-database:20.3.0")
implementation("com.google.firebase:firebase-auth:22.3.0")
```

### 2. Archivo google-services.json

**IMPORTANTE**: Debes descargar `google-services.json` desde Firebase Console:

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto
3. **Project Settings** ⚙️
4. Pestaña **General**
5. En **Your apps** → Android
6. **Download google-services.json**
7. Colocar en: `app/google-services.json`

### 3. Uso de DeviceManager

#### Asignar Planta a Dispositivo

```java
// En AddPlantActivity después de guardar la planta
DeviceManager deviceManager = DeviceManager.getInstance();

deviceManager.assignPlantToDevice(
    selectedDeviceId,  // "ESP32-A1B2C3"
    String.valueOf(plantId),  // "123"
    new DeviceManager.AssignmentCallback() {
        @Override
        public void onSuccess() {
            Toast.makeText(context, "Dispositivo conectado!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(String error) {
            Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
        }
    }
);
```

#### Desasignar Planta de Dispositivo

```java
// Al eliminar una planta o desconectar
deviceManager.unassignPlantFromDevice(
    deviceId,
    new DeviceManager.UnassignmentCallback() {
        @Override
        public void onSuccess() {
            Log.d(TAG, "Dispositivo liberado");
        }

        @Override
        public void onError(String error) {
            Log.e(TAG, "Error: " + error);
        }
    }
);
```

#### Verificar si Dispositivo está Online

```java
deviceManager.isDeviceOnline(deviceId, isOnline -> {
    if (isOnline) {
        // Mostrar icono verde
        ivStatus.setImageResource(R.drawable.ic_online);
    } else {
        // Mostrar icono gris
        ivStatus.setImageResource(R.drawable.ic_offline);
    }
});
```

### 4. Leer Datos de Sensores en Tiempo Real

```java
DatabaseReference sensorRef = FirebaseDatabase.getInstance()
    .getReference("sensor_data");

// Ordenar por timestamp, últimos 10 registros
sensorRef.orderByChild("timestamp")
    .limitToLast(10)
    .addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot data : snapshot.getChildren()) {
                Float soilHumidity = data.child("soil_humidity").getValue(Float.class);
                Float temperature = data.child("temperature").getValue(Float.class);
                // ... actualizar UI
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.e(TAG, "Error: " + error.getMessage());
        }
    });
```

---

## 🔥 Configuración Firebase

### 1. Crear Proyecto Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. **Add project** → Nombre: "MiMaseteroInteligente"
3. Habilitar Google Analytics (opcional)
4. **Create project**

### 2. Habilitar Realtime Database

1. En el menú lateral: **Build → Realtime Database**
2. **Create Database**
3. Ubicación: **United States (us-central1)** o la más cercana
4. Modo: **Start in test mode** (para desarrollo)
5. **Enable**

### 3. Reglas de Seguridad (Desarrollo)

```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

⚠️ **ADVERTENCIA**: Solo para desarrollo. Para producción, usar:

```json
{
  "rules": {
    "devices": {
      ".read": true,
      "$deviceId": {
        ".write": "auth != null"
      }
    },
    "plants": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "sensor_data": {
      ".read": "auth != null",
      ".write": true
    },
    "alerts": {
      ".read": "auth != null",
      ".write": true
    }
  }
}
```

### 4. Obtener Database Secret (para ESP32)

1. **Project Settings** ⚙️
2. Pestaña **Service accounts**
3. **Database secrets**
4. Copiar el secret (largo token)
5. Usar en `FIREBASE_AUTH` del código ESP32

---

## 🔄 Flujo de Datos

### Flujo Completo: Desde ESP32 hasta la App

```
1. ESP32 se enciende
   ↓
2. Se conecta a WiFi
   ↓
3. Se auto-registra en /devices con is_available=true
   ↓
4. App Android lee /devices y muestra lista
   ↓
5. Usuario selecciona planta + dispositivo
   ↓
6. App llama DeviceManager.assignPlantToDevice()
   ↓
7. Firebase actualiza:
   - /devices/{deviceId}/current_plant_id = "123"
   - /devices/{deviceId}/is_available = false
   ↓
8. ESP32 detecta cambio en current_plant_id
   ↓
9. ESP32 empieza a leer sensores cada 5 segundos
   ↓
10. ESP32 envía datos a /sensor_data/{timestamp}
   ↓
11. App lee /sensor_data y actualiza dashboard
```

### Diagrama de Secuencia

```
ESP32           Firebase         Android App
  |                 |                 |
  |-- register ---->|                 |
  |                 |<--- read -------|
  |                 |                 |
  |                 |<-- assign ------|
  |<-- notify -----|                 |
  |                 |                 |
  |-- send data --->|                 |
  |                 |<--- read -------|
```

---

## 💡 Ejemplos de Uso

### Ejemplo 1: Mostrar Dispositivos Disponibles

```java
// En cualquier Activity
DatabaseReference devicesRef = FirebaseDatabase.getInstance()
    .getReference("devices");

devicesRef.addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        for (DataSnapshot deviceSnap : snapshot.getChildren()) {
            Boolean isAvailable = deviceSnap.child("is_available").getValue(Boolean.class);

            if (isAvailable != null && isAvailable) {
                String deviceName = deviceSnap.child("device_name").getValue(String.class);
                Log.d(TAG, "Dispositivo disponible: " + deviceName);
            }
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        Log.e(TAG, "Error: " + error.getMessage());
    }
});
```

### Ejemplo 2: Actualizar UI con Datos de Sensor

```java
// Leer último dato de sensor para planta específica
DatabaseReference sensorRef = FirebaseDatabase.getInstance()
    .getReference("sensor_data");

Query query = sensorRef.orderByChild("plant_id")
    .equalTo(String.valueOf(plantId))
    .limitToLast(1);

query.addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        for (DataSnapshot data : snapshot.getChildren()) {
            Float soilHumidity = data.child("soil_humidity").getValue(Float.class);
            Float temperature = data.child("temperature").getValue(Float.class);

            // Actualizar UI
            tvSoilHumidity.setText(soilHumidity + "%");
            tvTemperature.setText(temperature + "°C");
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        // Handle error
    }
});
```

### Ejemplo 3: Controlar Relés desde Android

```java
// Enviar comando al ESP32 para activar riego
DatabaseReference commandRef = FirebaseDatabase.getInstance()
    .getReference("commands/" + deviceId);

Map<String, Object> command = new HashMap<>();
command.put("action", "water");
command.put("relay", 1);
command.put("duration", 5000);  // 5 segundos
command.put("timestamp", System.currentTimeMillis());

commandRef.setValue(command)
    .addOnSuccessListener(aVoid -> {
        Toast.makeText(this, "Riego activado", Toast.LENGTH_SHORT).show();
    });
```

Luego en ESP32:

```cpp
// Escuchar comandos
void checkCommands() {
  String path = "/commands/" + DEVICE_ID + "/action";

  if (Firebase.getString(firebaseData, path)) {
    String action = firebaseData.stringData();

    if (action == "water") {
      activateRelay1(5000);  // Activar por 5 segundos
      Firebase.setString(firebaseData, path, "");  // Limpiar comando
    }
  }
}
```

---

## 🐛 Troubleshooting

### ESP32 no se conecta a WiFi

**Solución:**
```cpp
// Verificar credenciales
Serial.println(WIFI_SSID);
Serial.println("Conectando...");

// Usar WiFi.scanNetworks() para ver redes disponibles
int n = WiFi.scanNetworks();
for (int i = 0; i < n; i++) {
  Serial.println(WiFi.SSID(i));
}
```

### ESP32 no aparece en la app

**Verificar:**
1. ✅ ESP32 está conectado a WiFi (ver Serial Monitor)
2. ✅ Firebase HOST y AUTH son correctos
3. ✅ Revisar Firebase Console → Realtime Database → Data
4. ✅ Verificar que `/devices/{deviceId}` existe

### Dispositivo aparece como Offline

**Causa:** `last_seen` no se actualiza

**Solución:**
```cpp
// En el loop(), verificar que se llama updateLastSeen()
void loop() {
  if (millis() - lastUpdate > 5000) {
    updateLastSeen();  // ← Debe llamarse
    lastUpdate = millis();
  }
}
```

### Datos de sensores no llegan a Firebase

**Verificar:**
1. ✅ `isConnectedToPlant` es true
2. ✅ `currentPlantId` no está vacío
3. ✅ Permisos de Firebase permiten escritura
4. ✅ Revisar Serial Monitor para errores

**Ejemplo de log correcto:**
```
Leyendo sensores...
Datos enviados correctamente
```

### App no muestra dispositivos

**Verificar:**
1. ✅ Internet en el dispositivo Android
2. ✅ Firebase rules permiten `.read: true` en `/devices`
3. ✅ `google-services.json` está en la carpeta correcta
4. ✅ Sync Gradle completado sin errores

---

## 📚 Recursos Adicionales

- **Firebase ESP32 Client:** https://github.com/mobizt/Firebase-ESP32
- **Firebase Android:** https://firebase.google.com/docs/android/setup
- **ESP32 Pinout:** https://randomnerdtutorials.com/esp32-pinout-reference-gpios/
- **OLED SSD1306:** https://randomnerdtutorials.com/esp32-ssd1306-oled-display-arduino-ide/

---

## ✅ Checklist Final

Antes de poner en producción:

- [ ] ESP32 se auto-registra correctamente
- [ ] Dispositivos aparecen en la app
- [ ] Selección de dispositivos funciona
- [ ] Datos de sensores llegan cada 5 segundos
- [ ] OLED muestra información correcta
- [ ] Relés se activan/desactivan correctamente
- [ ] Firebase rules de producción están configuradas
- [ ] Manejo de errores implementado
- [ ] Logs informativos en Serial Monitor

---

## 🎓 Próximos Pasos

1. **Implementar control de relés desde la app**
2. **Agregar gráficas de histórico de sensores**
3. **Notificaciones push cuando hay alertas**
4. **Modo automático de riego basado en umbrales**
5. **Dashboard web con Firebase Hosting**

---

**¡Tu sistema está listo! 🚀🌱**
