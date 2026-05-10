package com.marcal.fixloop.ui.repairs;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.marcal.fixloop.R;
import com.marcal.fixloop.model.AuthResponse;
import com.marcal.fixloop.model.IdRequest;
import com.marcal.fixloop.network.RetrofitClient;
import com.marcal.fixloop.utils.ImageUtils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditRepairActivity extends AppCompatActivity {

    /**
     * Activitat que permet modificar les dades de les sol·licituds de reparacio de l'usuari
     * aixi com eliminar-les
     */
    private TextInputEditText etTitol, etDescripcio;
    private ImageView ivReparacio;
    private int sollicitudId;
    private int categoriaId;
    private Uri novaImatgeUri = null;

    // Per seleccionar una imatge nova de la galeria
    private final ActivityResultLauncher<String> galeriaLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    novaImatgeUri = uri;
                    ivReparacio.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_repair);

        // Vincular vistes
        etTitol = findViewById(R.id.etEditTitol);
        etDescripcio = findViewById(R.id.etEditDescripcioReparacio);
        ivReparacio = findViewById(R.id.ivEditFotoReparacio);
        Button btnGuardar = findViewById(R.id.btnGuardarReparacio);
        Button btnCanviarFoto = findViewById(R.id.btnCanviarFotoReparacio);
        Button btnEliminar = findViewById(R.id.btnEliminarReparacio);
        MaterialToolbar toolbar = findViewById(R.id.toolbarEditRepair);

        // Configuració Toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        // Rebre dades de l'intent
        rebreDadesIntent();

        // Listeners
        btnCanviarFoto.setOnClickListener(v -> galeriaLauncher.launch("image/*"));
        btnGuardar.setOnClickListener(v -> actualitzarSollicitud());
        btnEliminar.setOnClickListener(v -> mostrarDialegConfirmacio());
    }

    private void rebreDadesIntent() {
        if (getIntent().hasExtra("sollicitud_id")) {
            sollicitudId = getIntent().getIntExtra("sollicitud_id", -1);
            categoriaId = getIntent().getIntExtra("categoria_id", 1);
            etTitol.setText(getIntent().getStringExtra("titol"));
            etDescripcio.setText(getIntent().getStringExtra("descripcio"));

            String fotoUrl = getIntent().getStringExtra("foto_url");
            if (fotoUrl != null && !fotoUrl.isEmpty()) {
                Glide.with(this).load(fotoUrl).into(ivReparacio);
            }
        }
    }

    private void actualitzarSollicitud() {
        String nouTitol = etTitol.getText().toString().trim();
        String novaDesc = etDescripcio.getText().toString().trim();

        if (nouTitol.isEmpty()) {
            etTitol.setError("El títol és obligatori");
            return;
        }

        //cos de la peticio multipart
        RequestBody idBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(sollicitudId));
        RequestBody titolBody = RequestBody.create(MediaType.parse("text/plain"), nouTitol);
        RequestBody descBody = RequestBody.create(MediaType.parse("text/plain"), novaDesc);
        RequestBody catBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(categoriaId));

        MultipartBody.Part fotoPart = null;
        if (novaImatgeUri != null) {
            File file = ImageUtils.getFileFromUri(this, novaImatgeUri);
            if (file != null) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                fotoPart = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
            }
        }

        RetrofitClient.getInstance().getMyApi().updateRepairRequest(idBody, titolBody, descBody, catBody, fotoPart)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                            Toast.makeText(EditRepairActivity.this, "Canvis guardats correctament", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        Toast.makeText(EditRepairActivity.this, "Error de connexió", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarDialegConfirmacio() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar sol·licitud")
                .setMessage("Estàs segur que vols eliminar aquesta sol·licitud definitivament?")
                .setPositiveButton("Eliminar", (dialog, which) -> executarEliminacio())
                .setNegativeButton("Cancel·lar", null)
                .show();
    }

    private void executarEliminacio() {
        RetrofitClient.getInstance().getMyApi().deleteRepairRequest(new IdRequest(sollicitudId))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                            Toast.makeText(EditRepairActivity.this, "Sol·licitud eliminada", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        Toast.makeText(EditRepairActivity.this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}