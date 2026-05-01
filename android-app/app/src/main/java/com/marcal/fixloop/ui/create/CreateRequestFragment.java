package com.marcal.fixloop.ui.create;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.marcal.fixloop.R;
import com.marcal.fixloop.databinding.FragmentCreateRequestBinding;
import com.marcal.fixloop.model.AuthResponse;
import com.marcal.fixloop.model.Category;
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
 * Fragment per a la creació de noves sol·licituds de reparació
 * Permet a l'usuari introduir títol, descripció, categoria i una imatge
 */
public class CreateRequestFragment extends Fragment {

    private FragmentCreateRequestBinding binding;
    private Uri selectedImageUri = null;
    private List<Category> categoryList = new ArrayList<>();
    private SessionManager sessionManager;

    // Selector de fotos
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.ivPreview.setVisibility(View.VISIBLE);
                    binding.llUploadPlaceholder.setVisibility(View.GONE);
                    Glide.with(this).load(uri).centerCrop().into(binding.ivPreview);
                }
            });

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateRequestBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sessionManager = new SessionManager(requireContext());

        // Carregar Categories al Spinner
        loadCategories();

        // Click a la zona de la imatge per obrir galeria
        binding.flImageContainer.setOnClickListener(v -> {
            pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        // Botó Crear Sol·licitud
        binding.btnCrear.setOnClickListener(v -> createRepairRequest());

        return root;
    }

    /**
     * Carrega la llista de categories des del servidor per omplir el Spinner
     */
    private void loadCategories() {
        RetrofitClient.getInstance().getMyApi().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Netegem llista prèvia
                    categoryList.clear();
                    List<String> namesForSpinner = new ArrayList<>();

                    // Filtrem la categoria "Tot" (-1), perquè no pots crear un anunci de "Tot"
                    for (Category c : response.body()) {
                        if (c.getId() != -1) {
                            categoryList.add(c);
                            namesForSpinner.add(c.getName());
                        }
                    }

                    // Configurem el Spinner amb els noms
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item, namesForSpinner);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.spinnerCategories.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(getContext(), "Error categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Recull les dades del formulari, valida i envia la petició al servidor
     */
    private void createRepairRequest() {
        String title = binding.etTitol.getText().toString().trim();
        String description = binding.etDescripcio.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(getContext(), "Cal omplir Títol i Descripció", Toast.LENGTH_SHORT).show();
            return;
        }

        if (categoryList.isEmpty()) return; // Encara no han carregat

        // Obtenim l'ID de la categoria seleccionada a partir de la posició del spinner
        int position = binding.spinnerCategories.getSelectedItemPosition();
        if (position < 0 || position >= categoryList.size()) return;
        int categoryId = categoryList.get(position).getId();

        // Obtenim l'ID de l'usuari de la sessió
        int userId = sessionManager.getUserId();

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCrear.setEnabled(false);

        // Preparar Multipart (Text + Imatge)
        // Convertim a RequestBody per a Retrofit
        RequestBody rbUserId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userId));
        RequestBody rbTitle = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody rbDescription = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody rbCategoryId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(categoryId));

        MultipartBody.Part bodyImage = null;
        if (selectedImageUri != null) {
            File file = ImageUtils.getFileFromUri(requireContext(), selectedImageUri);
            if (file != null) {
                RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
                bodyImage = MultipartBody.Part.createFormData("imatge", file.getName(), reqFile);
            }
        }

        // Crida a l'API
        RetrofitClient.getInstance().getMyApi().createRepairRequest(rbUserId, rbTitle, rbDescription, rbCategoryId, bodyImage)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnCrear.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(getContext(), "Sol·licitud publicada!", Toast.LENGTH_LONG).show();
                            // Naveguem a la pantalla principal
                            Navigation.findNavController(requireView()).navigate(R.id.navigation_home);
                        } else {
                            String msg = (response.body() != null) ? response.body().getMessage() : "Error desconegut";
                            Toast.makeText(getContext(), "Error: " + msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnCrear.setEnabled(true);
                        Toast.makeText(getContext(), "Error de connexió", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}