package com.devst.mimaseterointeligente.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    /**
     * Genera un hash SHA-256 de la contraseña
     * Nota: En producción se debería usar una librería más robusta como BCrypt
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());

            // Convertir bytes a hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // Si falla el hash, retornar la contraseña sin hash (solo para desarrollo)
            return password;
        }
    }

    /**
     * Verifica si una contraseña coincide con su hash
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        String passwordHash = hashPassword(password);
        return passwordHash.equals(hashedPassword);
    }

    /**
     * Valida que la contraseña cumpla con los requisitos mínimos
     */
    public static boolean isPasswordValid(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        // Opcional: Agregar más validaciones
        // - Al menos una mayúscula
        // - Al menos un número
        // - Al menos un carácter especial

        return true;
    }

    /**
     * Genera un código de 4 dígitos para recuperación de contraseña
     */
    public static String generateRecoveryCode() {
        int code = (int) (Math.random() * 9000) + 1000;
        return String.valueOf(code);
    }
}

