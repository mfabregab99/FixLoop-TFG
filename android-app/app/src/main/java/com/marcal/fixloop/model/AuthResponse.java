package com.marcal.fixloop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model que representa la resposta estàndard de l'API per a operacions d'autenticació
 * S'utilitza en Login, Registre, etc
 */
public class AuthResponse {

    // Estat de la petició: "success" o "error"
    @SerializedName("status")
    private String status;

    // Missatge informatiu del servidor ex: "Contrasenya incorrecta"
    @SerializedName("message")
    private String message;

    // Objecte Usuari (només ve ple si status és "success" i és un login/registre)
    @SerializedName("user")
    private User user;

    // --- Getters ---

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    /**
     * Mètode auxiliar per comprovar ràpidament si la petició ha anat bé
     * @return true si l'estat és success
     */
    public boolean isSuccess() {
        return "success".equals(status);
    }
}