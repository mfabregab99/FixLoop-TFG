package com.marcal.fixloop.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.marcal.fixloop.MainActivity;
import com.marcal.fixloop.R;
import com.marcal.fixloop.model.AuthResponse;
import com.marcal.fixloop.model.Category;
import com.marcal.fixloop.model.LoginRequest;
import com.marcal.fixloop.network.RetrofitClient;
import com.marcal.fixloop.utils.ImageUtils;
import com.marcal.fixloop.utils.SessionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activitat de registre d'usuaris
 * Implementa un procés en 3 passos utilitzant un ViewFlipper:
 * 1- Email
 * 2- Dades bàsiques (Nom, Password, Tipus)
 * 3- Dades extra per a reparadors (Descripció, Categories)
 */
public class RegisterActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;

    // Pas 1
    private EditText etEmail;

    // Pas 2
    private EditText etName, etPassword, etRepeatPassword;
    private RadioGroup rgType;
    private ImageView ivProfilePic;
    private Button btnRegisterStep2;

    // Pas 3 (Reparador)
    private EditText etDescription;
    private LinearLayout llCategoriesCheckboxes;
    private Button btnFinalizeRepairer;

    private Uri selectedImageUri = null;
    private List<CheckBox> categoryCheckBoxes = new ArrayList<>();

    // Selector d'imatges
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).circleCrop().into(ivProfilePic);
                    ivProfilePic.setPadding(0, 0, 0, 0);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Vinculació de vistes
        viewFlipper = findViewById(R.id.viewFlipper);
        etEmail = findViewById(R.id.etEmail);

        etName = findViewById(R.id.etNom);
        etPassword = findViewById(R.id.etPassword);
        etRepeatPassword = findViewById(R.id.etRepeatPassword);
        rgType = findViewById(R.id.rgTipus);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        btnRegisterStep2 = findViewById(R.id.btnRegisterFinal);

        // Pas 3
        etDescription = findViewById(R.id.etDescripcio);
        llCategoriesCheckboxes = findViewById(R.id.llCategoriesCheckboxes);
        btnFinalizeRepairer = findViewById(R.id.btnFinalitzarReparador);

        // --- LISTENERS ---

        // Botó Pas 1: Continuar
        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (!email.isEmpty()) checkEmailAvailability(email);
            else Toast.makeText(this, "Correu invàlid", Toast.LENGTH_SHORT).show();
        });

        // Botó Pas 2: Següent o Finalitzar (segons rol)
        btnRegisterStep2.setOnClickListener(v -> processSecondStep());

        // Botó Pas 3: Finalitzar Reparador
        btnFinalizeRepairer.setOnClickListener(v -> performRegistration(true));

        findViewById(R.id.btnGoToLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        ivProfilePic.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        // Canvi dinàmic del text del botó segons el tipus d'usuari
        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbReparador) {
                btnRegisterStep2.setText("Següent (Configurar Perfil)");
            } else {
                btnRegisterStep2.setText("Crear Compte");
            }
        });

        // Pre-carregar categories per al pas 3
        loadCategoriesForCheckbox();
    }

    private void loadCategoriesForCheckbox() {
        RetrofitClient.getInstance().getMyApi().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Category cat : response.body()) {
                        if (cat.getId() == -1) continue; // Saltem la categoria "Tot"

                        CheckBox cb = new CheckBox(RegisterActivity.this);
                        cb.setText(cat.getName());
                        cb.setTag(cat.getId());
                        llCategoriesCheckboxes.addView(cb);
                        categoryCheckBoxes.add(cb);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {}
        });
    }

    private void checkEmailAvailability(String email) {
        // Comprovem si el correu està lliure abans de deixar avançar
        RetrofitClient.getInstance().getMyApi().checkEmail(new LoginRequest(email, "")).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                // El backend retorna "available" si no existeix
                if (response.isSuccessful() && response.body() != null && "available".equals(response.body().getStatus())) {
                    // Animació cap a la dreta
                    viewFlipper.setInAnimation(RegisterActivity.this, R.anim.slide_in_right);
                    viewFlipper.setOutAnimation(RegisterActivity.this, R.anim.slide_out_left);
                    viewFlipper.showNext();
                } else {
                    Toast.makeText(RegisterActivity.this, "Correu no disponible", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                android.util.Log.e("API_ERROR", "Error a checkEmail: " + t.getMessage(), t);
                Toast.makeText(RegisterActivity.this, "Error de connexió", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processSecondStep() {
        // Validem camps bàsics
        String name = etName.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String passRepeat = etRepeatPassword.getText().toString().trim();

        if (name.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Omple tots els camps", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pass.equals(passRepeat)) {
            etRepeatPassword.setError("No coincideixen");
            return;
        }

        // Decidim camí segons el rol
        int selectedId = rgType.getCheckedRadioButtonId();
        if (selectedId == R.id.rbReparador) {
            // És reparador -> Anem al PAS 3 (Categories i descripció)
            viewFlipper.showNext();
        } else {
            // És client -> Registre directe
            performRegistration(false);
        }
    }

    private void performRegistration(boolean isReparadorStep3) {
        String email = etEmail.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        // Si venim del pas 3, agafem la descripció, si no buida
        String description = isReparadorStep3 ? etDescription.getText().toString().trim() : "";
        String type = isReparadorStep3 ? "reparador" : "client";

        // Recollim categories seleccionades (IDs separats per comes)
        StringBuilder categoriesIds = new StringBuilder();
        if (isReparadorStep3) {
            for (CheckBox cb : categoryCheckBoxes) {
                if (cb.isChecked()) {
                    if (categoriesIds.length() > 0) categoriesIds.append(",");
                    categoriesIds.append(cb.getTag().toString());
                }
            }
        }

        // --- PREPARAR PETICIÓ MULTIPART ---
        // Convertim strings a RequestBody per a Retrofit
        RequestBody rbEmail = RequestBody.create(MediaType.parse("text/plain"), email);
        RequestBody rbPass = RequestBody.create(MediaType.parse("text/plain"), pass);
        RequestBody rbName = RequestBody.create(MediaType.parse("text/plain"), name);
        RequestBody rbType = RequestBody.create(MediaType.parse("text/plain"), type);
        RequestBody rbDesc = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody rbCats = RequestBody.create(MediaType.parse("text/plain"), categoriesIds.toString());

        MultipartBody.Part bodyFoto = null;
        if (selectedImageUri != null) {
            File file = ImageUtils.getFileFromUri(this, selectedImageUri);
            if (file != null) {
                RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
                bodyFoto = MultipartBody.Part.createFormData("foto", file.getName(), reqFile);
            }
        }

        // CRIDA A L'API
        RetrofitClient.getInstance().getMyApi().register(rbEmail, rbPass, rbName, rbType, rbDesc, rbCats, bodyFoto)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            performAutoLogin(email, pass);
                        } else {
                            // Si falla, mostrem el missatge del servidor
                            String errorMsg = response.body() != null ? response.body().getMessage() : "Error desconegut";
                            Toast.makeText(RegisterActivity.this, "Error registre: " + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        Toast.makeText(RegisterActivity.this, "Error de xarxa", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void performAutoLogin(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);

        RetrofitClient.getInstance().getMyApi().login(loginRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Guardem sessió i anem a la pantalla principal
                    SessionManager sessionManager = new SessionManager(RegisterActivity.this);
                    sessionManager.createLoginSession(response.body().getUser());

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Si el login automàtic falla (no hauria), anem al login manual
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                }
            }
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}