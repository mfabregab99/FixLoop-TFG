package com.marcal.fixloop.network;

import com.marcal.fixloop.model.AuthResponse;
import com.marcal.fixloop.model.Category;
import com.marcal.fixloop.model.ChatHistoryRequest;
import com.marcal.fixloop.model.Conversation;
import com.marcal.fixloop.model.FavoriteRequest;
import com.marcal.fixloop.model.IdRequest;
import com.marcal.fixloop.model.LoginRequest;
import com.marcal.fixloop.model.Message;
import com.marcal.fixloop.model.MessageRequest;
import com.marcal.fixloop.model.RepairRequest;
import com.marcal.fixloop.model.StartChatRequest;
import com.marcal.fixloop.model.StartChatResponse;
import com.marcal.fixloop.model.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Interfície de Retrofit que defineix tots els punts d'accés (endpoints) a l'API del backend
 * Gestiona l'autenticació, dades d'usuaris, sol·licituds de reparació i missatgeria
 */
public interface ApiService {

    // --- AUTENTICACIÓ ---

    /**
     * Autentica un usuari existent
     * @param request Objecte amb email i contrasenya
     * @return Resposta amb l'estat i les dades de l'usuari si és correcte
     */
    @POST("login.php")
    Call<AuthResponse> login(@Body LoginRequest request);

    /**
     * Comprova si un correu electrònic ja està registrat a la base de dades
     * @param request Objecte amb l'email a comprovar
     */
    @POST("check_email.php")
    Call<AuthResponse> checkEmail(@Body LoginRequest request);

    /**
     * Registra un nou usuari amb foto de perfil
     * Utilitza Multipart per permetre la pujada de foto
     */
    @Multipart
    @POST("register.php")
    Call<AuthResponse> register(
            @Part("email") RequestBody email,
            @Part("password") RequestBody password,
            @Part("nom_complet") RequestBody fullName,
            @Part("tipus") RequestBody type,
            @Part("descripcio") RequestBody description,
            @Part("categories") RequestBody categories,
            @Part MultipartBody.Part photo
    );

    // --- DADES PRINCIPALS ---

    /**
     * Obté les dades completes del perfil de l'usuari actual
     */
    @POST("get_profile.php")
    Call<User> getProfile(@Body IdRequest request);

    /**
     * Actualitza les dades del perfil d'un ausuari
     * Utilitza multipart per si l'usuari canvia la foto (pot ser null si no canvia la foto)
     */
    @Multipart
    @POST("update_profile.php")
    Call<AuthResponse> updateProfile(
            @Part("user_id") RequestBody userId,
            @Part("nom_complet") RequestBody fullName,
            @Part("password") RequestBody password,
            @Part("descripcio") RequestBody description,
            @Part("categories") RequestBody categories,
            @Part MultipartBody.Part photo
    );
    /**
     * Elimina el compte d'un usuari i totes les seves dades associades
     */
    @POST("delete_account.php")
    Call<AuthResponse> deleteAccount(@Body IdRequest request);

    /**
     * Obté la llista d'usuaris (filtra per rol 'reparador' al backend)
     * @param request Petició amb l'ID de l'usuari actual (per saber si són favorits)
     */
    @POST("get_users.php")
    Call<List<User>> getRepairers(@Body IdRequest request);

    /**
     * Obté la llista de categories de reparació disponibles
     */
    @POST("get_categories.php")
    Call<List<Category>> getCategories();

    // --- FAVORITS ---

    /**
     * Obté la llista de reparadors preferits d'un usuari específic
     */
    @POST("get_favorites.php")
    Call<List<User>> getFavorites(@Body IdRequest request);

    /**
     * Afegeix o elimina un reparador de la llista de favorits
     */
    @POST("toggle_favorite.php")
    Call<AuthResponse> toggleFavorite(@Body FavoriteRequest request);

    // --- SOL·LICITUDS DE REPARACIÓ ---

    /**
     * Crea una nova sol·licitud de reparació publicada per un client
     */
    @Multipart
    @POST("create_sollicitud.php")
    Call<AuthResponse> createRepairRequest(
            @Part("user_id") RequestBody userId,
            @Part("titol") RequestBody title,
            @Part("descripcio") RequestBody description,
            @Part("categoria_id") RequestBody categoryId,
            @Part MultipartBody.Part image
    );

    /**
     * Obté la llista de sol·licituds de reparació actives (visible per als reparadors)
     */
    @POST("get_sollicituds.php")
    Call<List<RepairRequest>> getRepairRequests();

    /**
     * Obté la llista d'anuncis de l'usuari acual
     */
    @POST("get_my_sollicituds.php")
    Call<List<RepairRequest>> getMyRepairRequests(@Body IdRequest request);

    /**
     * Actualitza un anunci existent
     */
    @Multipart
    @POST("update_sollicitud.php")
    Call<AuthResponse> updateRepairRequest(
            @Part("sollicitud_id") RequestBody sollicitudId,
            @Part("titol") RequestBody title,
            @Part("descripcio") RequestBody description,
            @Part("categoria_id") RequestBody categoryId,
            @Part MultipartBody.Part image
    );
    /**
     * Elimina un anunci específic
     * (Reaprofitant el model IdRequest passant-li l'ID de la sol·licitud en lloc de id de l'usuari)
     */
    @POST("delete_sollicitud.php")
    Call<AuthResponse> deleteRepairRequest(@Body IdRequest request);

    @POST("update_profile.php")
    Call<AuthResponse> updateRepairerCategories(@Body com.marcal.fixloop.model.SkillUpdateRequest request);

    // --- MISSATGERIA ---

    /**
     * Obté la llista de converses actives (Bústia d'entrada) per a l'usuari
     */
    @POST("get_xats.php")
    Call<List<Conversation>> getConversations(@Body IdRequest request);

    /**
     * Obté els missatges d'un xat específic
     * Suporta paginació per cursor utilitzant 'last_id'
     */
    @POST("get_missatges.php")
    Call<List<Message>> getMessages(@Body ChatHistoryRequest request);

    /**
     * Envia un nou missatge de text
     */
    @POST("send_message.php")
    Call<AuthResponse> sendMessage(@Body MessageRequest request);

    /**
     * Inicia un xat nou o en recupera un d'existent
     */
    @POST("start_chat.php")
    Call<StartChatResponse> startChat(@Body StartChatRequest request);
}