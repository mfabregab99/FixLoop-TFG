package com.marcal.fixloop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model genèric per enviar peticions que només requereixen l'ID de l'usuari
 * S'utilitza a get_users.php, get_favorites.php, get_conversations.php, etc
 */
public class IdRequest {

    @SerializedName("user_id")
    private int userId;

    public IdRequest(int userId) {
        this.userId = userId;
    }
}