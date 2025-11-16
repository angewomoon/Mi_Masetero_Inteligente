/*
 * ========================================
 * ESP32 - Firebase Auto-Registration
 * Proyecto: Mi Masetero Inteligente
 * ========================================
 *
 * Este código permite que el ESP32 Dev Kit V1 se auto-registre
 * en Firebase Realtime Database para ser visible en la app Android.
 *
 * LIBRERÍAS NECESARIAS:
 * - Firebase ESP32 Client by Mobizt
 * - WiFi (incluida en ESP32)
 * - Wire (para I2C)
 * - Adafruit_GFX + Adafruit_SSD1306 (para OLED)
 *
 * Instalar desde Library Manager de Arduino IDE:
 * - "Firebase ESP32 Client" by Mobizt
 */

#include <WiFi.h>
#include <FirebaseESP32.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

// ============================================
// CONFIGURACIÓN - MODIFICAR SEGÚN TU PROYECTO
// ============================================

// WiFi Credentials
#define WIFI_SSID "TU_RED_WIFI"
#define WIFI_PASSWORD "TU_PASSWORD_WIFI"

// Firebase Credentials
#define FIREBASE_HOST "mimaseterointeligente-default-rtdb.firebaseio.com/"  // Sin https:// ni /
#define FIREBASE_AUTH "wZgnWs0aVWsR556EOBTWakbfZatT798E7CWTBYZ3"  // Database Secret o Auth Token

// Configuración del dispositivo
#define DEVICE_NAME "Masetero Principal"  // Nombre amigable del dispositivo
String DEVICE_ID = "";  // Se generará automáticamente basado en MAC

// Pines de sensores (ajustar según tu conexión)
#define SOIL_HUMIDITY_PIN 34     // Sensor de humedad del suelo (analógico)
#define DHT_PIN 4               // DHT11 temperatura y humedad
#define TRIG_PIN_HC 5           // HC-SR04 TRIG (nivel de agua)
#define ECHO_PIN_HC 18          // HC-SR04 ECHO
#define TRIG_PIN_Y401 19        // Y401 TRIG (detección de plagas/distancia)
#define ECHO_PIN_Y401 21        // Y401 ECHO
#define RELAY_1_PIN 25          // Relé 1 (electroválvula 1)
#define RELAY_2_PIN 26          // Relé 2 (electroválvula 2)
#define LDR_PIN 35              // Sensor LDR (luz analógico)

// OLED Display (0.96")
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

// Objetos Firebase
FirebaseData firebaseData;
FirebaseAuth auth;
FirebaseConfig config;

// Variables de estado
bool isConnectedToPlant = false;
String currentPlantId = "";
unsigned long lastUpdate = 0;
const unsigned long UPDATE_INTERVAL = 5000;  // Actualizar cada 5 segundos

// ============================================
// SETUP
// ============================================

void setup() {
  Serial.begin(115200);
  Serial.println("\n=== ESP32 Masetero Inteligente ===\n");

  // Generar DEVICE_ID único basado en MAC
  DEVICE_ID = "ESP32-" + getChipID();
  Serial.println("Device ID: " + DEVICE_ID);

  // Inicializar pines
  setupPins();

  // Inicializar OLED
  if(!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
    Serial.println("Error al inicializar OLED");
  } else {
    displayMessage("Iniciando...", DEVICE_ID);
  }

  // Conectar a WiFi
  connectToWiFi();

  // Configurar Firebase
  setupFirebase();

  // Registrar dispositivo en Firebase
  registerDeviceInFirebase();

  Serial.println("Sistema listo!");
  displayMessage("Sistema OK", "Buscando plantas...");
}

// ============================================
// LOOP PRINCIPAL
// ============================================

void loop() {
  // Actualizar timestamp cada segundo
  if (millis() - lastUpdate > UPDATE_INTERVAL) {
    updateLastSeen();

    // Si está conectado a una planta, leer y enviar datos de sensores
    if (isConnectedToPlant) {
      readAndSendSensorData();
    }

    lastUpdate = millis();
  }

  // Verificar si se asignó una planta
  checkPlantAssignment();

  delay(100);
}

// ============================================
// FUNCIONES DE CONFIGURACIÓN
// ============================================

void setupPins() {
  // Sensores
  pinMode(SOIL_HUMIDITY_PIN, INPUT);
  pinMode(LDR_PIN, INPUT);
  pinMode(TRIG_PIN_HC, OUTPUT);
  pinMode(ECHO_PIN_HC, INPUT);
  pinMode(TRIG_PIN_Y401, OUTPUT);
  pinMode(ECHO_PIN_Y401, INPUT);

  // Relés (activo bajo)
  pinMode(RELAY_1_PIN, OUTPUT);
  pinMode(RELAY_2_PIN, OUTPUT);
  digitalWrite(RELAY_1_PIN, HIGH);  // Apagado inicialmente
  digitalWrite(RELAY_2_PIN, HIGH);  // Apagado inicialmente

  Serial.println("Pines configurados");
}

void connectToWiFi() {
  Serial.print("Conectando a WiFi");
  displayMessage("WiFi", "Conectando...");

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 20) {
    delay(500);
    Serial.print(".");
    attempts++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWiFi conectado!");
    Serial.println("IP: " + WiFi.localIP().toString());
    displayMessage("WiFi OK", WiFi.localIP().toString());
    delay(2000);
  } else {
    Serial.println("\nError al conectar WiFi");
    displayMessage("WiFi Error", "Reiniciando...");
    delay(3000);
    ESP.restart();
  }
}

void setupFirebase() {
  Serial.println("Configurando Firebase...");

  config.host = FIREBASE_HOST;
  config.signer.tokens.legacy_token = FIREBASE_AUTH;

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  Serial.println("Firebase configurado");
}

// ============================================
// FUNCIONES DE FIREBASE
// ============================================

void registerDeviceInFirebase() {
  Serial.println("Registrando dispositivo en Firebase...");
  displayMessage("Firebase", "Registrando...");

  String path = "/devices/" + DEVICE_ID;

  // Crear objeto JSON con datos del dispositivo
  FirebaseJson json;
  json.set("device_id", DEVICE_ID);
  json.set("device_name", DEVICE_NAME);
  json.set("is_available", true);
  json.set("current_plant_id", "");
  json.set("last_seen", String(millis()));
  json.set("ip_address", WiFi.localIP().toString());
  json.set("firmware_version", "1.0.0");

  if (Firebase.setJSON(firebaseData, path, json)) {
    Serial.println("Dispositivo registrado exitosamente!");
    displayMessage("Registrado", DEVICE_ID);
  } else {
    Serial.println("Error al registrar: " + firebaseData.errorReason());
    displayMessage("Error", "Registro fallido");
  }

  delay(2000);
}

void updateLastSeen() {
  String path = "/devices/" + DEVICE_ID + "/last_seen";
  Firebase.setString(firebaseData, path, String(millis()));
}

void checkPlantAssignment() {
  // Verificar si se asignó una planta a este dispositivo
  String path = "/devices/" + DEVICE_ID + "/current_plant_id";

  if (Firebase.getString(firebaseData, path)) {
    String plantId = firebaseData.stringData();

    if (plantId != "" && plantId != currentPlantId) {
      // Nueva planta asignada
      currentPlantId = plantId;
      isConnectedToPlant = true;

      // Actualizar is_available a false
      Firebase.setBool(firebaseData, "/devices/" + DEVICE_ID + "/is_available", false);

      Serial.println("Planta asignada: " + plantId);
      displayMessage("Planta", "Conectada!");

    } else if (plantId == "" && isConnectedToPlant) {
      // Planta desconectada
      currentPlantId = "";
      isConnectedToPlant = false;

      // Actualizar is_available a true
      Firebase.setBool(firebaseData, "/devices/" + DEVICE_ID + "/is_available", true);

      Serial.println("Planta desconectada");
      displayMessage("Disponible", "Sin planta");
    }
  }
}

void readAndSendSensorData() {
  if (!isConnectedToPlant || currentPlantId == "") return;

  Serial.println("Leyendo sensores...");

  // Leer sensores
  float soilHumidity = readSoilHumidity();
  float temperature = readTemperature();
  float ambientHumidity = readAmbientHumidity();
  float uvLevel = readLightLevel();  // Convertir LDR a UV simulado
  float waterLevel = readWaterLevel();
  int pestCount = detectPests();

  // Crear JSON con datos
  FirebaseJson sensorJson;
  sensorJson.set("plant_id", currentPlantId);
  sensorJson.set("soil_humidity", soilHumidity);
  sensorJson.set("temperature", temperature);
  sensorJson.set("ambient_humidity", ambientHumidity);
  sensorJson.set("uv_level", uvLevel);
  sensorJson.set("water_level", waterLevel);
  sensorJson.set("pest_count", pestCount);
  sensorJson.set("timestamp", String(millis()));

  // Enviar a Firebase
  String path = "/sensor_data/" + String(millis());  // Usar timestamp como ID

  if (Firebase.setJSON(firebaseData, path, sensorJson)) {
    Serial.println("Datos enviados correctamente");
    displaySensorData(temperature, soilHumidity, waterLevel);
  } else {
    Serial.println("Error al enviar datos: " + firebaseData.errorReason());
  }
}

// ============================================
// FUNCIONES DE LECTURA DE SENSORES
// ============================================

float readSoilHumidity() {
  int analogValue = analogRead(SOIL_HUMIDITY_PIN);
  // Convertir de 0-4095 a 0-100% (ajustar según calibración)
  float humidity = map(analogValue, 0, 4095, 0, 100);
  return constrain(humidity, 0, 100);
}

float readTemperature() {
  // Placeholder - Implementar lectura DHT11
  // Requiere librería DHT
  return 25.0;  // Valor de ejemplo
}

float readAmbientHumidity() {
  // Placeholder - Implementar lectura DHT11
  return 60.0;  // Valor de ejemplo
}

float readLightLevel() {
  int ldrValue = analogRead(LDR_PIN);
  // Convertir LDR a índice UV simulado (0-11)
  float uvIndex = map(ldrValue, 0, 4095, 0, 11);
  return constrain(uvIndex, 0, 11);
}

float readWaterLevel() {
  // Leer distancia con HC-SR04
  float distance = readUltrasonic(TRIG_PIN_HC, ECHO_PIN_HC);

  // Convertir distancia a porcentaje de nivel de agua
  // Ejemplo: tanque de 20cm de altura
  float level = map(distance, 2, 20, 100, 0);  // 2cm = lleno, 20cm = vacío
  return constrain(level, 0, 100);
}

int detectPests() {
  // Usar sensor Y401 para detección de movimiento/presencia
  float distance = readUltrasonic(TRIG_PIN_Y401, ECHO_PIN_Y401);

  // Si hay algo muy cerca, podría ser una plaga
  if (distance < 5.0 && distance > 0) {
    return 1;  // Plaga detectada
  }
  return 0;
}

float readUltrasonic(int trigPin, int echoPin) {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  long duration = pulseIn(echoPin, HIGH, 30000);  // Timeout 30ms
  float distance = duration * 0.034 / 2;  // Convertir a cm

  return distance;
}

// ============================================
// FUNCIONES DE DISPLAY OLED
// ============================================

void displayMessage(String line1, String line2) {
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);

  display.setCursor(0, 20);
  display.println(line1);

  display.setCursor(0, 40);
  display.println(line2);

  display.display();
}

void displaySensorData(float temp, float soil, float water) {
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);

  display.setCursor(0, 0);
  display.println("MASETERO ACTIVO");

  display.setCursor(0, 20);
  display.print("Temp: ");
  display.print(temp, 1);
  display.println("C");

  display.setCursor(0, 35);
  display.print("Suelo: ");
  display.print(soil, 0);
  display.println("%");

  display.setCursor(0, 50);
  display.print("Agua: ");
  display.print(water, 0);
  display.println("%");

  display.display();
}

// ============================================
// FUNCIONES AUXILIARES
// ============================================

String getChipID() {
  uint64_t chipid = ESP.getEfuseMac();
  char buffer[13];
  sprintf(buffer, "%04X%08X", (uint16_t)(chipid >> 32), (uint32_t)chipid);
  return String(buffer);
}
