package com.devst.mimaseterointeligente.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Clase para gestionar la sesión del usuario usando SharedPreferences
 */
public class SessionManager {
    
    private static final String PREF_NAME = "MaseteroSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;
    
    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }
    
    /**
     * Crear sesión de inicio de sesión
     */
    public void createLoginSession(int userId, String email, String name) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }
    
    /**
     * Verificar si el usuario ha iniciado sesión
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * Obtener ID del usuario
     */
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }
    
    /**
     * Obtener email del usuario
     */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }
    
    /**
     * Obtener nombre del usuario
     */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }
    
    /**
     * Actualizar email del usuario
     */
    public void saveUserEmail(String email) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }
    
    /**
     * Actualizar nombre del usuario
     */
    public void saveUserName(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }
    
    /**
     * Cerrar sesión y limpiar datos
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
