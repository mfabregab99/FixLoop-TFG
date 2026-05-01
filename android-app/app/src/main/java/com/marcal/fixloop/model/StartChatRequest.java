package com.marcal.fixloop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model per a la petició d'iniciar un xat nou o recuperar-ne un d'existent
 * S'utilitza a l'endpoint start_chat.php
 */
public class StartChatRequest {

    @SerializedName("emissor_id")
    private int senderId;

    @SerializedName("receptor_id")
    private int receiverId;

    // ID de la sol·licitud relacionada
    // Si és 0 (o null al backend), es considera un xat "general" de perfil
    @SerializedName("sollicitud_id")
    private int requestId;

    /**
     * Constructor per iniciar un xat
     * @param senderId ID de l'usuari que inicia el xat
     * @param receiverId ID de l'usuari amb qui es vol parlar
     * @param requestId ID de la feina (0 si és contacte directe)
     */
    public StartChatRequest(int senderId, int receiverId, int requestId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.requestId = requestId;
    }
}