package com.marcal.fixloop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model que representa una sol·licitud de reparació.
 * S'utilitza per mapejar la resposta JSON de l'API
 * Inclou la info de la reparació i de l'usuari que ho publica
 */

public class RepairRequest {

    // id de la sol·licitud
    private int id;

    @SerializedName("usuari_id")
    private int userId;

    @SerializedName("categoria_id")
    private int categoryId;

    // Dades de la sol·licitud
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

    //Da des adicionals
    @SerializedName("categoria_nom")
    private String categoryName;

    @SerializedName("usuari_nom")
    private String userName;

    @SerializedName("usuari_foto")
    private String userPhoto;

    //Constructor buit
    public RepairRequest() {}

    // --- GETTERS ---
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getCategoryId() { return categoryId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPhotoUrl() { return photoUrl; }
    public String getStatus() { return status; }
    public String getCreatedDate() { return createdDate; }
    public String getCategoryName() { return categoryName; }
    public String getUserName() { return userName; }
    public String getUserPhoto() { return userPhoto; }
}