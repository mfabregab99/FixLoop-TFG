package com.marcal.fixloop.ui.repairs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.marcal.fixloop.R;
import com.marcal.fixloop.model.IdRequest;
import com.marcal.fixloop.model.RepairRequest;
import com.marcal.fixloop.network.RetrofitClient;
import com.marcal.fixloop.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyRequestsFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyRepairsAdapter adapter;
    private TextView tvEmpty;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        recyclerView = view.findViewById(R.id.rvMyRequests);
        tvEmpty = view.findViewById(R.id.tvEmptyMessage);
        MaterialToolbar toolbar = view.findViewById(R.id.toolbarMyRequests);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        carregarSollicituds();
    }

    private void carregarSollicituds() {
        int userId = sessionManager.getUserId();
        Log.d("FIXLOOP_DEBUG", "Enviant ID d'usuari al PHP: " + userId);

        RetrofitClient.getInstance().getMyApi().getMyRepairRequests(new IdRequest(userId))
                .enqueue(new Callback<List<RepairRequest>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<RepairRequest>> call, @NonNull Response<List<RepairRequest>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<RepairRequest> llista = response.body();

                            if (llista.isEmpty()) {
                                tvEmpty.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                tvEmpty.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                adapter = new MyRepairsAdapter(requireContext(), llista);
                                recyclerView.setAdapter(adapter);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<RepairRequest>> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Error al connectar amb el servidor", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}