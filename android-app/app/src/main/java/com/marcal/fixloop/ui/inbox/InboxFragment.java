package com.marcal.fixloop.ui.inbox;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.marcal.fixloop.databinding.FragmentInboxBinding;
import com.marcal.fixloop.model.Conversation;
import com.marcal.fixloop.model.IdRequest;
import com.marcal.fixloop.network.RetrofitClient;
import com.marcal.fixloop.ui.chat.ChatActivity;
import com.marcal.fixloop.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment que mostra la Bústia d'entrada (llista de converses actives)
 * Gestiona la càrrega de xats des del servidor i la navegació cap a la pantalla de conversa
 */
public class InboxFragment extends Fragment {

    private FragmentInboxBinding binding;
    private InboxAdapter adapter;
    private List<Conversation> conversationList = new ArrayList<>();
    private SessionManager sessionManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentInboxBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sessionManager = new SessionManager(requireContext());

        // Configuració RecyclerView
        binding.rvXats.setLayoutManager(new LinearLayoutManager(getContext()));

        // Listener: Quan cliquem una conversa, obrim la ChatActivity
        adapter = new InboxAdapter(getContext(), conversationList, conversation -> {
            // Obrir la pantalla de conversa
            Intent intent = new Intent(getContext(), ChatActivity.class);

            // Utilitzem les constants de ChatActivity per evitar errors
            intent.putExtra(ChatActivity.EXTRA_CHAT_ID, conversation.getId());
            intent.putExtra(ChatActivity.EXTRA_OTHER_NAME, conversation.getOtherUserName());

            startActivity(intent);
        });

        binding.rvXats.setAdapter(adapter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recarreguem la llista cada cop que la pantalla es fa visible
        loadConversations();
    }

    /**
     * Carrega la llista de converses de l'usuari des del servidor
     */
    private void loadConversations() {
        int userId = sessionManager.getUserId();
        binding.progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getInstance().getMyApi().getConversations(new IdRequest(userId))
                .enqueue(new Callback<List<Conversation>>() {
                    @Override
                    public void onResponse(Call<List<Conversation>> call, Response<List<Conversation>> response) {
                        binding.progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            conversationList.clear();
                            conversationList.addAll(response.body());
                            adapter.notifyDataSetChanged();

                            // Gestió d'estat buit (Empty State)
                            if (conversationList.isEmpty()) {
                                binding.tvEmpty.setVisibility(View.VISIBLE);
                                binding.rvXats.setVisibility(View.GONE);
                            } else {
                                binding.tvEmpty.setVisibility(View.GONE);
                                binding.rvXats.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Conversation>> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error carregant converses", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}