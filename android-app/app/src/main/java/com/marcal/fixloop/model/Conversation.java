package com.marcal.fixloop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model que representa un xat a la bústia d'entrada
 * Conté la informació bàsica per mostrar la llista de xats
 */
public class Conversation {

    @SerializedName("xat_id")
    private int id;

    @SerializedName("titol_sollicitud")
    private String requestTitle;

    @SerializedName("nom_altre_usuari")
    private String otherUserName;

    @SerializedName("foto_altre_usuari")
    private String otherUserPhoto;

    @SerializedName("data_ultima_activitat")
    private String lastActivityDate;

    // --- Getters ---

    public int getId() {
        return id;
    }

    public String getRequestTitle() {
        return requestTitle;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public String getOtherUserPhoto() {
        return otherUserPhoto;
    }

    public String getLastActivityDate() {
        return lastActivityDate;
    }
}