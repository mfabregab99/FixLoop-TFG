package com.marcal.fixloop.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.marcal.fixloop.R;
import com.marcal.fixloop.model.AuthResponse;
import com.marcal.fixloop.model.IdRequest;
import com.marcal.fixloop.model.User;
import com.marcal.fixloop.network.RetrofitClient;
import com.marcal.fixloop.utils.ImageUtils;
import com.marcal.fixloop.utils.SessionManager;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activitat que permet modificar les dades personals de l'usuari
 */

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etNom, etDescripcio, etPassword;
    private TextInputLayout layoutDescripcio;
    private Button btnGuardar, btnCanviarFoto;
    private ImageView ivFoto;
    private SessionManager sessionManager;
    private Uri imatgeSeleccionadaUri = null;

    // Selector d'imatges de la galeria
    private final ActivityResultLauncher<String> galeriaLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imatgeSeleccionadaUri = uri;
                    Glide.with(this).load(uri).circleCrop().into(ivFoto);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sessionManager = new SessionManager(this);

        // Vincular vistes
        etEmail = findViewById(R.id.etEditEmail);
        etNom = findViewById(R.id.etEditNom);
        etDescripcio = findViewById(R.id.etEditDescripcio);
        etPassword = findViewById(R.id.etEditPassword);
        layoutDescripcio = findViewById(R.id.layoutEditDescripcio);
        btnGuardar = findViewById(R.id.btnGuardarPerfil);
        btnCanviarFoto = findViewById(R.id.btnCanviarFoto);
        ivFoto = findViewById(R.id.ivEditFoto);

        // L'email es mostra però no es pot editar
        etEmail.setText(sessionManager.getUserEmail());

        // Gestió de rol, amaguem descripció si l'usuari és un client
        if ("client".equalsIgnoreCase(sessionManager.getUserType())) {
            layoutDescripcio.setVisibility(View.GONE);
        }

        // Listeners
        btnCanviarFoto.setOnClickListener(v -> galeriaLauncher.launch("image/*"));
        btnGuardar.setOnClickListener(v -> actualitzarDades());

        // Carreguem les dades actuals des del servidor per omplir el formulari
        carregarDadesActuals();

        MaterialToolbar toolbar = findViewById(R.id.toolbarEditProfile);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Carreguem les dades des de l'API per assegurar que estan actualitzades
     */
    private void carregarDadesActuals() {
        int userId = sessionManager.getUserId();

        RetrofitClient.getInstance().getMyApi().getProfile(new IdRequest(userId)).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User usuari = response.body();
                    etNom.setText(usuari.getFullName());

                    if ("reparador".equalsIgnoreCase(usuari.getType())) {
                        etDescripcio.setText(usuari.getDescription());
                    }

                    if (usuari.getProfilePhotoUrl() != null && !usuari.getProfilePhotoUrl().isEmpty()) {
                        Glide.with(EditProfileActivity.this)
                                .load(usuari.getProfilePhotoUrl())
                                .circleCrop()
                                .into(ivFoto);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Error de xarxa al carregar perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Recull les dades del formulari i les envia al servidor
     */
    private void actualitzarDades() {
        String nouNom = etNom.getText().toString().trim();
        String novaDesc = etDescripcio.getText().toString().trim();
        String novaPass = etPassword.getText().toString().trim();

        // Recuperem els IDs guardats (ex: "1,2,5")
        String categoriesAEnviar = sessionManager.getUserCategories();

        if (nouNom.isEmpty()) {
            etNom.setError("El nom és obligatori");
            return;
        }


        // Preparació dels RequestBody en text pla
        RequestBody userIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(sessionManager.getUserId()));
        RequestBody nomBody = RequestBody.create(MediaType.parse("text/plain"), nouNom);
        RequestBody descBody = RequestBody.create(MediaType.parse("text/plain"), novaDesc);
        RequestBody passBody = RequestBody.create(MediaType.parse("text/plain"), novaPass);
        RequestBody categoriesBody = RequestBody.create(MediaType.parse("text/plain"), categoriesAEnviar);

        //processem la imatge
        MultipartBody.Part fotoPart = null;
        if (imatgeSeleccionadaUri != null) {
            File imatgeFile = ImageUtils.getFileFromUri(this, imatgeSeleccionadaUri);
            if (imatgeFile != null) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imatgeFile);
                fotoPart = MultipartBody.Part.createFormData("photo", imatgeFile.getName(), requestFile);
            }
        }

        RetrofitClient.getInstance().getMyApi().updateProfile(userIdBody, nomBody, passBody, descBody, categoriesBody, fotoPart)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if ("success".equals(response.body().getStatus())) {
                                Toast.makeText(EditProfileActivity.this, "Canvis guardats!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(EditProfileActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            android.util.Log.e("FIXLOOP_DEBUG", "Error del servidor: " + response.code());
                            Toast.makeText(EditProfileActivity.this, "Error del servidor", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        android.util.Log.e("FIXLOOP_DEBUG", "Error CRÍTIC a Retrofit: " + t.getMessage(), t);
                        Toast.makeText(EditProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}