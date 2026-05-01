package com.marcal.fixloop.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Client de xarxa singleton que gestiona la connexió amb l'API
 * Utilitza el patró Singleton per garantir que només existeixi una instància de Retrofit
 */
public class RetrofitClient {

    // URL base de l'API
    private static final String BASE_URL = "https://eimtcms.eimt.uoc.edu/~mfabregab99/api/";

    private static RetrofitClient instance = null;
    private ApiService myApi;

    // Constructor privat per evitar instanciació directa
    private RetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        myApi = retrofit.create(ApiService.class);
    }

    /**
     * Obté la instància única del client Retrofit
     * Si no existeix, la crea
     */
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    /**
     * Retorna la interfície de l'API per fer les crides
     */
    public ApiService getMyApi() {
        return myApi;
    }
}