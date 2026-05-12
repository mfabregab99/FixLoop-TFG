package com.marcal.fixloop.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.marcal.fixloop.R;
import com.marcal.fixloop.databinding.FragmentHomeBinding;
import com.marcal.fixloop.model.Category;
import com.marcal.fixloop.model.IdRequest;
import com.marcal.fixloop.model.RepairRequest;
import com.marcal.fixloop.model.User;
import com.marcal.fixloop.network.RetrofitClient;
import com.marcal.fixloop.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment principal de l'aplicació
 * Gestiona dues vistes diferents segons el rol de l'usuari:
 * 1- Clients: Veuen llista de reparadors (RepairerAdapter)
 * 2- Reparadors: Veuen llista de sol·licituds de feina (RequestAdapter).
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient;
    private android.location.Location myLocation; // Per guardar la posicio

    // Adaptadors
    private RepairerAdapter repairerAdapter;
    private RequestAdapter requestAdapter;

    // Llistes per a clients (reparadors)
    private List<User> allRepairersList = new ArrayList<>();
    private List<User> displayRepairersList = new ArrayList<>();

    // Llistes per a reparadors (sol·licituds de feines)
    private List<RepairRequest> allRequestsList = new ArrayList<>();
    private List<RepairRequest> displayRequestsList = new ArrayList<>();

    private SessionManager sessionManager;
    private boolean isRepairer = false;

    // Control de Categories (Filtres)
    private List<MaterialButton> categoryButtons = new ArrayList<>();
    private int selectedCategoryId = -1; // -1 = Tot

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sessionManager = new SessionManager(requireContext());

        // Recuperem el tipus d'usuari ("client" o "reparador")
        String type = sessionManager.getUserType();
        isRepairer = "reparador".equalsIgnoreCase(type);

        binding.rvReparadors.setLayoutManager(new LinearLayoutManager(getContext()));

        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireContext());
        obtenirMevaUbicacio();

        // --- CONFIGURACIÓ SEGONS ROL ---
        if (isRepairer) {
            // vista reparador: Veu sol·licituds de feina
            binding.etSearch.setHint("Busca feines (títol, descripció...)");

            // Inicialitzem l'adaptador amb la llista de visualització
            requestAdapter = new RequestAdapter(getContext(), displayRequestsList);
            binding.rvReparadors.setAdapter(requestAdapter);

            loadRepairRequests();

        } else {
            // vista client: Veu reparadors
            binding.etSearch.setHint("Busca un reparador per nom");

            repairerAdapter = new RepairerAdapter(getContext(), displayRepairersList);
            binding.rvReparadors.setAdapter(repairerAdapter);

            loadRepairers();
        }

        // Listener de cerca unificat
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filtrem la llista corresponent segons qui som
                if (isRepairer) {
                    filterRequests();
                } else {
                    filterRepairers();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Les categories es carreguen sempre perquè serveixen per filtrar les dues llistes
        loadCategories();

        return root;
    }
    private void obtenirMevaUbicacio() {
        if (androidx.core.app.ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                myLocation = location;
                // Si ja teníem reparadors carregats, recalculem
                if (!allRepairersList.isEmpty()) {
                    recalcularDistancies();
                }
            }
        });
    }
    private void recalcularDistancies() {
        if (myLocation == null || allRepairersList.isEmpty()) return;

        for (User user : allRepairersList) {
            // Només calculem si el reparador té coordenades vàlides
            if (user.getLatitud() != 0 && user.getLongitud() != 0) {
                float[] results = new float[1];
                android.location.Location.distanceBetween(
                        myLocation.getLatitude(), myLocation.getLongitude(),
                        user.getLatitud(), user.getLongitud(),
                        results
                );

                float metres = results[0];
                String textDistancia;
                if (metres < 1000) {
                    textDistancia = Math.round(metres) + " m";
                } else {
                    textDistancia = String.format("%.1f km", metres / 1000);
                }
                user.setDistance(textDistancia);
            }
        }
        repairerAdapter.notifyDataSetChanged();
    }

    // --- CÀRREGA DE CATEGORIES ---
    private void loadCategories() {
        RetrofitClient.getInstance().getMyApi().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    createCategoryButtons(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                // Gestió d'error silenciosa
            }
        });
    }

    private void createCategoryButtons(List<Category> categories) {
        binding.llCategoriesContainer.removeAllViews();
        categoryButtons.clear();

        for (Category cat : categories) {
            MaterialButton btn = new MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 16, 0);
            btn.setLayoutParams(params);

            btn.setText(cat.getName());
            btn.setCornerRadius(20);
            btn.setTag(cat.getId()); // Guardem l'ID per filtrar

            btn.setOnClickListener(v -> {
                selectedCategoryId = (int) v.getTag();
                updateButtonStyles();

                // Refiltrem segons el rol actiu
                if (isRepairer) {
                    filterRequests();
                } else {
                    filterRepairers();
                }
            });
            binding.llCategoriesContainer.addView(btn);
            categoryButtons.add(btn);
        }
        updateButtonStyles();
    }

    private void updateButtonStyles() {
        for (MaterialButton btn : categoryButtons) {
            int id = (int) btn.getTag();
            if (id == selectedCategoryId) {
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.fixloop_green));
                btn.setTextColor(Color.WHITE);
                btn.setStrokeWidth(0);
            } else {
                btn.setBackgroundColor(Color.TRANSPARENT);
                btn.setTextColor(Color.parseColor("#666666"));
                btn.setStrokeColor(ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray));
                btn.setStrokeWidth(2);
            }
        }
    }

    // --- Lògica clients ---

    private void loadRepairers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        int myId = sessionManager.getUserId();

        RetrofitClient.getInstance().getMyApi().getRepairers(new IdRequest(myId)).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allRepairersList.clear();
                    allRepairersList.addAll(response.body());
                    recalcularDistancies();
                    filterRepairers(); // Apliquem filtre inicial
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error carregant reparadors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterRepairers() {
        displayRepairersList.clear();
        String searchText = binding.etSearch.getText().toString().toLowerCase().trim();

        // Obtenim el nom de la categoria seleccionada
        String selectedCategoryName = "";
        if (selectedCategoryId != -1) {
            for (MaterialButton btn : categoryButtons) {
                if ((int)btn.getTag() == selectedCategoryId) {
                    selectedCategoryName = btn.getText().toString();
                    break;
                }
            }
        }

        for (User user : allRepairersList) {
            // Filtre Categoria
            boolean matchesCategory = (selectedCategoryId == -1) ||
                    (user.getCategoryListString() != null && user.getCategoryListString().contains(selectedCategoryName));

            // Filtre Text
            boolean matchesSearch = searchText.isEmpty() || user.getFullName().toLowerCase().contains(searchText);

            if (matchesCategory && matchesSearch) {
                displayRepairersList.add(user);
            }
        }
        repairerAdapter.notifyDataSetChanged();
    }

    // --- lògica reparadors ---

    private void loadRepairRequests() {
        binding.progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getInstance().getMyApi().getRepairRequests().enqueue(new Callback<List<RepairRequest>>() {
            @Override
            public void onResponse(Call<List<RepairRequest>> call, Response<List<RepairRequest>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allRequestsList.clear();
                    allRequestsList.addAll(response.body());

                    filterRequests(); // Apliquem filtre inicial

                    if (allRequestsList.isEmpty()) {
                        Toast.makeText(getContext(), "No hi ha feines disponibles", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<RepairRequest>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error de xarxa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterRequests() {
        displayRequestsList.clear();
        String searchText = binding.etSearch.getText().toString().toLowerCase().trim();

        for (RepairRequest request : allRequestsList) {

            // Filtre Categoria per ID
            boolean matchesCategory = false;
            if (selectedCategoryId == -1) {
                matchesCategory = true;
            } else {
                if (request.getCategoryId() == selectedCategoryId) {
                    matchesCategory = true;
                }
            }

            // filtre Text
            boolean matchesSearch = false;
            if (searchText.isEmpty()) {
                matchesSearch = true;
            } else {
                // Busquem al títol
                if (request.getTitle() != null && request.getTitle().toLowerCase().contains(searchText)) {
                    matchesSearch = true;
                }
                // O a la descripció
                else if (request.getDescription() != null && request.getDescription().toLowerCase().contains(searchText)) {
                    matchesSearch = true;
                }
            }

            if (matchesCategory && matchesSearch) {
                displayRequestsList.add(request);
            }
        }
        // Actualitzem la llista visible
        requestAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}