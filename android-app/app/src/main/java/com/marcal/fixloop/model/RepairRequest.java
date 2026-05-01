package com.marcal.fixloop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model que representa una sol·licitud de reparació
 * Utilitzat per crear noves sol·licituds de reparacio (create_request.php) i per llistar-les (get_requests.php)
 */
public class RepairRequest {

    private int id;

    // Camps de la base de dades
    @SerializedName("usuari_id")
    private int userId;

    @SerializedName("categoria_id")
    private int categoryId;

    @SerializedName("titol")
    private String title;

    @SerializedName("descripcio")
    private String description;

    @SerializedName("foto_url")
    private String photoUrl;

    @SerializedName("estat")
    private String status;

    @SerializedName("data_creacio")
    private String createdDate;

    // --- Camps visuals (JOINs de l'API) ---
    @SerializedName("categoria_nom")
    private String categoryName;

    @SerializedName("usuari_nom")
    private String userName;

    @SerializedName("usuari_foto")
    private String userPhoto;

    // --- Constructors ---

    public RepairRequest() {}

    public RepairRequest(int userId, int categoryId, String title, String description) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
    }

    // --- Getters ---

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPhoto() {
        return userPhoto;
    }
}