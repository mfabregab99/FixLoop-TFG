package com.marcal.fixloop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model per a la petició de registre d'un nou usuari
 * S'utilitza a l'endpoint register.php
 */
public class RegisterRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("nom_complet")
    private String fullName;

    // "client" o "reparador"
    @SerializedName("tipus")
    private String type;

    /**
     * Constructor per crear una petició de registre
     * @param email Correu electrònic
     * @param password Contrasenya
     * @param fullName Nom complet de l'usuari
     * @param type Tipus d'usuari ("client" o "reparador")
     * @param base64Image Opcional, si s'envia com string base64 (plantejament inicial) en lloc de Multipart
     */
    public RegisterRequest(String email, String password, String fullName, String type, String base64Image) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.type = type;
    }
}