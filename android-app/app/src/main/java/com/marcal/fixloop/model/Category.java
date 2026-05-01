package com.marcal.fixloop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model que representa una categoria de reparació (Ex: Electrònica, Roba...)
 */
public class Category {

    private int id;


    @SerializedName("nom")
    private String name;

    // --- Getters ---

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // --- Mètodes auxiliars ---

    /**
     * Retorna el nom de la categoria quan es converteix a text
     * Útil per a Spinners o Logs
     */
    @Override
    public String toString() {
        return name;
    }
}