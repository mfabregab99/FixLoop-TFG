package com.marcal.fixloop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model per a la petició d'enviar un nou missatge
 * S'utilitza a l'endpoint send_message.php
 */
public class MessageRequest {

    @SerializedName("xat_id")
    private int chatId;

    @SerializedName("emissor_id")
    private int senderId;

    @SerializedName("contingut")
    private String content;

    @SerializedName("tipus")
    private String type; // pot ser text, image, etc

    public MessageRequest(int chatId, int senderId, String content) {
        this.chatId = chatId;
        this.senderId = senderId;
        this.content = content;
        this.type = "text"; // Per defecte text
    }
}