package com.marcal.fixloop.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.marcal.fixloop.MainActivity;
import com.marcal.fixloop.R;
import com.marcal.fixloop.model.AuthResponse;
import com.marcal.fixloop.model.LoginRequest;
import com.marcal.fixloop.network.RetrofitClient;
import com.marcal.fixloop.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activitat d'inici de sessió
 * Permet als usuaris existents accedir a l'aplicació
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicialitzem el gestor de sessió
        sessionManager = new SessionManager(this);

        // Si l'usuari ja està loguejat, saltem directament a la Home
        if (sessionManager.isLoggedIn()) {
            goToHome();
        }

        // Vinculem les vistes
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnContinue); // Botó principal "Inicia Sessió"
        Button btnGoToRegister = findViewById(R.id.btnGoToRegister);

        // Accions dels botons
        btnLogin.setOnClickListener(v -> performLogin());

        btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            // Tanquem Login perquè no pugui tornar enrere amb el botó back
            finish();
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validacions bàsiques
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Si us plau, omple tots els camps", Toast.LENGTH_SHORT).show();
            return;
        }

        // Creem la petició
        LoginRequest request = new LoginRequest(email, password);

        // Cridem a l'API
        RetrofitClient.getInstance().getMyApi().login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        // Login correcte -> Guardem la sessió
                        sessionManager.createLoginSession(response.body().getUser());

                        // Mostrem missatge i entrem
                        Toast.makeText(LoginActivity.this, "Benvingut/da " + response.body().getUser().getFullName(), Toast.LENGTH_SHORT).show();
                        goToHome();
                    } else {
                        // Login incorrecte (contrasenya malament o usuari no existeix)
                        Toast.makeText(LoginActivity.this, "Error: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Error del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error de connexió. Revisa internet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        // Netegem la pila de tasques perquè l'usuari no pugui tornar al Login fent "Enrere"
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}