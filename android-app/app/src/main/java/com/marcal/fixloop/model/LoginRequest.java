package com.marcal.fixloop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model per a la petició d'inici de sessió
 * S'utilitza a l'endpoint login.php
 */
public class LoginRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}