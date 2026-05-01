package com.marcal.fixloop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model que representa un missatge individual dins d'una conversa
 * S'utilitza tant per rebre l'historial (get_messages.php) com per enviar (send_message.php)
 */
public class Message {

    private int id;

    @SerializedName("xat_id")
    private int chatId;

    @SerializedName("emissor_id")
    private int senderId;

    @SerializedName("contingut")
    private String content;

    @SerializedName("tipus")
    private String type; // "text", "image", etc

    @SerializedName("data_enviament")
    private String sentDate;

    // --- Constructor ---

    /**
     * Constructor per crear un missatge localment abans d'enviar-lo
     * @param senderId ID de l'usuari que envia
     * @param content Text del missatge
     */
    public Message(int senderId, String content) {
        this.senderId = senderId;
        this.content = content;
        this.type = "text"; // Tipus per defecte
    }

    // --- Getters ---

    public int getId() {
        return id;
    }

    public int getChatId() {
        return chatId;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public String getSentDate() {
        return sentDate;
    }
}