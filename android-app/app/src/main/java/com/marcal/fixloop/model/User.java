package com.marcal.fixloop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model que representa un usuari de l'aplicació (tant client com reparador)
 * S'utilitza per guardar la sessió, llistar reparadors i gestionar perfils
 */
public class User {

    private int id;
    private String distance;


    @SerializedName("nom_complet")
    private String fullName;

    @SerializedName("email")
    private String email;

    // client o reparador
    @SerializedName("tipus")
    private String type;

    // Indica si és un reparador professional (true) o amateur (false)
    @SerializedName("es_pro")
    private boolean isPro;

    @SerializedName("foto_perfil")
    private String profilePhotoUrl;

    // Indica si l'usuari actual té aquest reparador a la seva llista de favorits
    // Aquest camp l'omple l'API (get_users.php o get_favorites.php) fent una subconsulta
    @SerializedName("is_favorite")
    private boolean isFavorite;

    // Llista de noms de categories separats per comes
    // Només s'utilitza per mostrar informació a la targeta del reparador
    @SerializedName("llista_categories")
    private String categoryListString;

    @SerializedName("descripcio")
    private String description;

    @SerializedName("categories_ids")
    private String categoriesIds;

    // Latitud i Longitud
    private double latitud;
    private double longitud;

    // --- Getters i Setters ---

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getType() {
        return type;
    }

    public boolean isPro() {
        return isPro;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public String getCategoryListString() {
        return categoryListString;
    }

    public String getCategories() {
        return categoryListString;
    }

    public String getCategoriesIds() {
        return categoriesIds;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}