package com.marcal.fixloop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model per a la resposta de l'API en iniciar un xat (start_chat.php)
 * Retorna l'ID del xat, nou o existent, per poder obrir la ChatActivity
 */
public class StartChatResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("xat_id")
    private int chatId;

    // --- Getters i Helpers ---

    /**
     * Comprova si la petició ha estat exitosa
     * @return true si l'estatus és "success"
     */
    public boolean isSuccess() {
        return "success".equals(status);
    }

    public int getChatId() {
        return chatId;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }
}