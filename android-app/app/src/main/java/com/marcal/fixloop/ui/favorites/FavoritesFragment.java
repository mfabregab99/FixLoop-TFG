package com.marcal.fixloop.ui.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.marcal.fixloop.databinding.FragmentFavoritesBinding;
import com.marcal.fixloop.model.IdRequest;
import com.marcal.fixloop.model.User;
import com.marcal.fixloop.network.RetrofitClient;
import com.marcal.fixloop.ui.home.RepairerAdapter;
import com.marcal.fixloop.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment que mostra la llista de reparadors marcats com a favorits per l'usuari
 * Gestiona la càrrega de dades, l'estat buit (empty state) i la persistència visual
 */
public class FavoritesFragment extends Fragment {

    private FragmentFavoritesBinding binding;
    private RepairerAdapter adapter;
    private List<User> favoriteUsersList;
    private SessionManager sessionManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sessionManager = new SessionManager(requireContext());

        // Configuració RecyclerView
        binding.rvFavorits.setLayoutManager(new LinearLayoutManager(getContext()));
        favoriteUsersList = new ArrayList<>();

        // Reutilitzem el mateix adaptador que a la pantalla principal (RepairerAdapter)
        adapter = new RepairerAdapter(getContext(), favoriteUsersList);
        binding.rvFavorits.setAdapter(adapter);

        return root;
    }

    /**
     * Mètode del cicle de vida que s'executa cada vegada que el fragment es fa visible
     * Utilitzem onResume per recarregar la llista, assegurant que si l'usuari ha
     * tret un favorit des d'una altra pantalla, aquí desaparegui immediatament
     */
    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    /**
     * Realitza la petició a l'API per obtenir els favorits de l'usuari actual
     */
    private void loadFavorites() {
        int myUserId = sessionManager.getUserId();

        if (myUserId == -1) {
            Toast.makeText(getContext(), "Error: Usuari no identificat", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        // Petició a l'API (getFavorites)
        Call<List<User>> call = RetrofitClient.getInstance().getMyApi().getFavorites(new IdRequest(myUserId));

        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    favoriteUsersList.clear();
                    favoriteUsersList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    // Gestió d'estat buit (Empty State)
                    if (favoriteUsersList.isEmpty()) {
                        binding.llEmptyState.setVisibility(View.VISIBLE);
                        binding.rvFavorits.setVisibility(View.GONE);
                    } else {
                        binding.llEmptyState.setVisibility(View.GONE);
                        binding.rvFavorits.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(), "Error carregant favorits", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
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