package com.marcal.fixloop.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.marcal.fixloop.R;
import com.marcal.fixloop.databinding.FragmentProfileBinding;
import com.marcal.fixloop.ui.auth.LoginActivity;
import com.marcal.fixloop.utils.SessionManager;

/**
 * Fragment de perfil d'usuari
 * Mostra la informació personal, permet tancar sessió i ofereix opcions específiques segons el rol
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SessionManager sessionManager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sessionManager = new SessionManager(requireContext());

        // Recuperar dades de l'usuari
        String userName = sessionManager.getUserName();
        String userEmail = sessionManager.getUserEmail();
        String userType = sessionManager.getUserType();
        String photoUrl = sessionManager.getUserPhoto();

        // Omplir la vista amb les dades
        binding.tvNomPerfil.setText(userName);
        binding.tvEmailPerfil.setText(userEmail);
        binding.tvRolBadge.setText(userType.toUpperCase());

        // --- Càrrega de la imatge de perfil ---
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(android.R.drawable.ic_menu_myplaces) // Placeholder mentre carrega
                    .error(android.R.drawable.ic_menu_myplaces)       // Imatge per defecte si falla
                    .circleCrop()                                     // Retallar en cercle
                    .into(binding.ivPerfilUser);
        }

        // Lògica dinàmica segons el ROL (Client vs Reparador)
        if ("reparador".equalsIgnoreCase(userType)) {
            // Vista reparador
            binding.btnGestioReparador.setVisibility(View.VISIBLE); // Botó "Habilitats"
            binding.btnGestioSolicituds.setVisibility(View.GONE);   // Amagar "Les meves sol·licituds"
            binding.tvRolBadge.setBackgroundResource(R.drawable.border_pro);
        } else {
            // Vista client
            binding.btnGestioReparador.setVisibility(View.GONE);
            binding.btnGestioSolicituds.setVisibility(View.VISIBLE);
        }

        // Botó Tancar Sessió
        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            // Neteja la pila d'activitats perquè no es pugui tornar enrere
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Listeners de botons específics
        binding.btnGestioReparador.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Aquí podràs editar les categories", Toast.LENGTH_SHORT).show();
        });

        binding.btnGestioSolicituds.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Aquí veuràs les teves sol·licituds publicades", Toast.LENGTH_SHORT).show();
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}