package com.marcal.fixloop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model per a la petició d'afegir o treure un reparador de favorits
 * S'utilitza a l'endpoint toggle_favorite.php
 */
public class FavoriteRequest {

    @SerializedName("user_id")
    private int userId;

    @SerializedName("reparador_id")
    private int repairerId;

    public FavoriteRequest(int userId, int repairerId) {
        this.userId = userId;
        this.repairerId = repairerId;
    }
}