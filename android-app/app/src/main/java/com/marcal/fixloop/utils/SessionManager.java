package com.marcal.fixloop.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.marcal.fixloop.model.User;

/**
 * Gestor de sessions d'usuari basat en SharedPreferences
 * Permet guardar, recuperar i esborrar les dades de l'usuari actiu
 */
public class SessionManager {

    // Nom del fitxer de preferències
    private static final String PREF_NAME = "FixLoopSession";

    // Claus per guardar les dades
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_ID = "userId";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_TYPE = "type";
    private static final String KEY_PHOTO = "photo_url";
    private static final String KEY_CATEGORIES = "user_categories";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Crea una sessió d'usuari guardant les seves dades localment
     * @param user Objecte User rebut del servidor
     */
    public void createLoginSession(User user) {
        SharedPreferences.Editor freshEditor = pref.edit();
        freshEditor.putBoolean(KEY_IS_LOGGED_IN, true);
        freshEditor.putInt(KEY_ID, user.getId());
        freshEditor.putString(KEY_EMAIL, user.getEmail());
        freshEditor.putString(KEY_NAME, user.getFullName());
        freshEditor.putString(KEY_TYPE, user.getType());
        freshEditor.putString(KEY_PHOTO, user.getProfilePhotoUrl());

        // Guardem el string de categories
        freshEditor.putString(KEY_CATEGORIES, user.getCategoryListString());

        freshEditor.apply();
    }

    /**
     * Comprova si hi ha un usuari autenticat
     * @return true si està loguejat, false si no
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Tanca la sessió esborrant totes les dades locals
     */
    public void logout() {
        editor.clear();
        editor.commit(); // Commit síncron per assegurar que s'esborra abans de canviar de pantalla
    }

    // --- Getters per recuperar dades de l'usuari actiu ---

    public int getUserId() {
        return pref.getInt(KEY_ID, -1); // Retorna -1 si no es troba
    }

    public String getUserName() {
        return pref.getString(KEY_NAME, "Usuari");
    }

    public String getUserEmail() {
        return pref.getString(KEY_EMAIL, "email@test.com");
    }

    public String getUserType() {
        return pref.getString(KEY_TYPE, "client"); // Per defecte client
    }

    public String getUserPhoto() {
        return pref.getString(KEY_PHOTO, null);
    }

    public String getUserCategories() {
        return pref.getString(KEY_CATEGORIES, "");
    }
}