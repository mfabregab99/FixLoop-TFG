package com.marcal.fixloop.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.marcal.fixloop.R;
import com.marcal.fixloop.databinding.FragmentProfileBinding;
import com.marcal.fixloop.model.AuthResponse;
import com.marcal.fixloop.model.Category;
import com.marcal.fixloop.model.IdRequest;
import com.marcal.fixloop.model.SkillUpdateRequest;
import com.marcal.fixloop.model.User;
import com.marcal.fixloop.network.RetrofitClient;
import com.marcal.fixloop.ui.auth.LoginActivity;
import com.marcal.fixloop.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SessionManager sessionManager;
    private User currentUser; // Guardem l'usuari actual per saber les seves categories

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        sessionManager = new SessionManager(requireContext());

        carregarDadesSessio();

        // Botó Gestió Habilitats (només visible per a reparadors)
        binding.btnGestioReparador.setOnClickListener(v -> {
            mostrarSelectorHabilitats();
        });

        //Botó d'eliminar el compte
        binding.btnEliminarCompte.setOnClickListener(v -> {
            confirmarEliminacioCompte();
        });

        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.btnEditarPerfil.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        binding.btnGestioSolicituds.setOnClickListener(v -> {
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.navigation_my_requests);
        });

        return binding.getRoot();
    }

    private void mostrarSelectorHabilitats() {
        RetrofitClient.getInstance().getMyApi().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> allCategories = response.body();
                    String[] noms = new String[allCategories.size()];
                    boolean[] seleccions = new boolean[allCategories.size()];

                    // Obtenim el String amb els noms o IDs actuals
                    String habilitatsActuals = (currentUser != null) ? currentUser.getCategories() : "";
                    Log.d("FIXLOOP_DEBUG", "Habilitats actuals a la UI: " + habilitatsActuals);

                    for (int i = 0; i < allCategories.size(); i++) {
                        Category cat = allCategories.get(i);
                        noms[i] = cat.getName();
                        if (habilitatsActuals != null && habilitatsActuals.contains(cat.getName())) {
                            seleccions[i] = true;
                            cat.setSelected(true); // Marquem l'objecte original
                        }
                    }

                    new AlertDialog.Builder(requireContext())
                            .setTitle("Selecciona les teves especialitats")
                            .setMultiChoiceItems(noms, seleccions, (dialog, which, isChecked) -> {
                                // Assegurem que l'objecte de la llista canvia
                                allCategories.get(which).setSelected(isChecked);
                            })
                            .setPositiveButton("Actualitzar", (dialog, which) -> {
                                enviarHabilitats(allCategories);
                            })
                            .setNegativeButton("Cancel·lar", (dialog, which) -> Log.d("FIXLOOP_DEBUG", "Diàleg cancel·lat"))
                            .show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de xarxa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarHabilitats(List<Category> llista) {
        List<Integer> ids = new ArrayList<>();
        for (Category c : llista) {
            if (c.isSelected()) {
                ids.add(c.getId());
            }
        }

        if (ids.isEmpty()) {
            Toast.makeText(getContext(), "No has seleccionat cap categoria", Toast.LENGTH_SHORT).show();
        }

        SkillUpdateRequest request = new SkillUpdateRequest(sessionManager.getUserId(), ids);

        RetrofitClient.getInstance().getMyApi().updateRepairerCategories(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Habilitats guardades!", Toast.LENGTH_SHORT).show();
                    carregarDadesPerfil(); // Refresquem per veure els canvis
                } else {
                    Log.e("FIXLOOP_DEBUG", "Error en la resposta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.e("FIXLOOP_DEBUG", "Error de xarxa a enviarHabilitats: " + t.getMessage());
                Toast.makeText(getContext(), "Error de connexió", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarDadesPerfil();
    }

    private void carregarDadesSessio() {
        actualitzarUI(sessionManager.getUserName(), sessionManager.getUserType(), sessionManager.getUserPhoto());
    }

    private void carregarDadesPerfil() {
        IdRequest request = new IdRequest(sessionManager.getUserId());
        RetrofitClient.getInstance().getMyApi().getProfile(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    sessionManager.createLoginSession(currentUser);
                    actualitzarUI(currentUser.getFullName(), currentUser.getType(), currentUser.getProfilePhotoUrl());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("FIXLOOP_DEBUG", "Error de perfil");
            }
        });
    }

    private void actualitzarUI(String nom, String tipus, String photoUrl) {
        binding.tvNomPerfil.setText(nom);
        binding.tvRolBadge.setText(tipus != null ? tipus.toUpperCase() : "");

        // Concatena amb la carpeta d'uploads per veure la foto de perfil
        String fullUrl = RetrofitClient.UPLOADS_URL + photoUrl;

        Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.ic_account)
                .circleCrop()
                .into(binding.ivPerfilUser);

        if ("reparador".equalsIgnoreCase(tipus)) {
            binding.btnGestioReparador.setVisibility(View.VISIBLE);
            binding.btnGestioSolicituds.setVisibility(View.GONE);
        } else {
            binding.btnGestioReparador.setVisibility(View.GONE);
            binding.btnGestioSolicituds.setVisibility(View.VISIBLE);
        }
    }

    private void confirmarEliminacioCompte() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar compte")
                .setMessage("Estàs segur/a? Aquesta acció no es pot desfer i perdràs totes les teves dades.")
                .setPositiveButton("Eliminar", (dialog, which) -> executarEliminacio())
                .setNegativeButton("Cancel·lar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void executarEliminacio() {
        IdRequest request = new IdRequest(sessionManager.getUserId());

        RetrofitClient.getInstance().getMyApi().deleteAccount(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    Toast.makeText(getContext(), "Compte eliminat", Toast.LENGTH_SHORT).show();

                    // Tanquem la sessió i tornem al Login
                    sessionManager.logout();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error de xarxa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}