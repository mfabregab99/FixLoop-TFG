package com.marcal.fixloop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model per sol·licitar l'historial de missatges d'un xat
 * S'utilitza per a la paginació per demanar missatges posteriors a l'últim conegut
 */
public class ChatHistoryRequest {

    @SerializedName("xat_id")
    private int xatId;

    /** L'ID de l'últim missatge que tenim al mòbil
     * Si és 0, el servidor ens tornarà els últims missatges
     * Si és > 0, el servidor ens tornarà només els missatges més nous a aquell ID
     */
    @SerializedName("last_id")
    private int lastId;

    public ChatHistoryRequest(int xatId, int lastId) {
        this.xatId = xatId;
        this.lastId = lastId;
    }
}