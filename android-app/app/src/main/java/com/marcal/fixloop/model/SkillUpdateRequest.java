package com.marcal.fixloop.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Model per enviar l'actualització de les categories de un reparador
 * Aquesta classe actua com un DTO per simplificar la comunicació amb l'endpoint
 * update_profile.php
 */
public class SkillUpdateRequest {
    @SerializedName("user_id")
    private int userId;

    @SerializedName("categories")
    private String categories;

    /**
     * Constructor que converteix la llista de IDs a text pla requerit pel servidor
     * @param userId identificador del usuari que actualitza el perfil
     * @param categoryIds Llista d'enters amb els IDs de les categories seleccionades
     */
    public SkillUpdateRequest(int userId, List<Integer> categoryIds) {
        this.userId = userId;

        // Convertim la llista [1, 2] en un String "1,2" que es el que necessita update_profile.php
        StringBuilder sb = new StringBuilder();
        if (categoryIds != null) {
            for (int i = 0; i < categoryIds.size(); i++) {
                sb.append(categoryIds.get(i));
                if (i < categoryIds.size() - 1) {
                    sb.append(",");
                }
            }
        }
        this.categories = sb.toString();
    }
}