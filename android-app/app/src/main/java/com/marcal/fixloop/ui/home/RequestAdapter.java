package com.marcal.fixloop.ui.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.marcal.fixloop.R;
import com.marcal.fixloop.model.RepairRequest;
import com.marcal.fixloop.model.StartChatRequest;
import com.marcal.fixloop.model.StartChatResponse;
import com.marcal.fixloop.network.RetrofitClient;
import com.marcal.fixloop.ui.chat.ChatActivity;
import com.marcal.fixloop.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Adaptador per al RecyclerView de la pantalla d'Inici (per a Reparadors)
 * Mostra la llista de sol·licituds de reparació (RepairRequest) publicades pels clients
 */
public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private Context context;
    private List<RepairRequest> requestList;
    private int currentUserId;

    public RequestAdapter(Context context, List<RepairRequest> requestList) {
        this.context = context;
        this.requestList = requestList;

        // Obtenim l'ID de l'usuari actual (el reparador que està mirant la llista)
        SessionManager session = new SessionManager(context);
        this.currentUserId = session.getUserId();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request_card, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        RepairRequest request = requestList.get(position);

        // Assignació de textos
        holder.tvTitle.setText(request.getTitle());
        holder.tvDescription.setText(request.getDescription());
        holder.tvCategory.setText(request.getCategoryName());
        holder.tvUserName.setText(request.getUserName());

        // --- Gestió foto de l'objecte trencat ---
        if (request.getPhotoUrl() != null && !request.getPhotoUrl().isEmpty()) {
            holder.ivRequestImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(request.getPhotoUrl())
                    .centerCrop()
                    .into(holder.ivRequestImage);
        } else {
            holder.ivRequestImage.setVisibility(View.GONE);
        }

        // --- Gestió foto de perfil del client ---
        if (request.getUserPhoto() != null && !request.getUserPhoto().isEmpty()) {
            Glide.with(context)
                    .load(request.getUserPhoto())
                    .circleCrop()
                    .into(holder.ivUserAvatar);
        } else {
            holder.ivUserAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
        }

        // --- accio del boto contactar ---
        holder.btnContact.setOnClickListener(v -> {
            // Desactivem el botó per evitar dobles clics
            holder.btnContact.setEnabled(false);

            // Preparem la petició per iniciar el xat:
            // Sender: Jo (Reparador, currentUserId)
            // Receiver: El client (request.getUserId())
            // Request ID: ID de la sol·licitud (request.getId())

            StartChatRequest startChatRequest = new StartChatRequest(currentUserId, request.getUserId(), request.getId());

            RetrofitClient.getInstance().getMyApi().startChat(startChatRequest).enqueue(new Callback<StartChatResponse>() {
                @Override
                public void onResponse(Call<StartChatResponse> call, Response<StartChatResponse> response) {
                    holder.btnContact.setEnabled(true); // Reactivem el botó

                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        // Obrim la pantalla de xat
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("CHAT_ID", response.body().getChatId());
                        intent.putExtra("OTHER_USER_NAME", request.getUserName());
                        context.startActivity(intent);
                    } else {
                        String errorMsg = response.body() != null ? response.body().getMessage() : "Error desconegut";
                        Toast.makeText(context, "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<StartChatResponse> call, Throwable t) {
                    holder.btnContact.setEnabled(true);
                    Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    /**
     * ViewHolder per a les targetes de sol·licitud
     * Vincula les vistes del layout item_request_card.xml
     */
    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvCategory, tvUserName;
        ImageView ivRequestImage, ivUserAvatar;
        Button btnContact;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitol);
            tvDescription = itemView.findViewById(R.id.tvDescripcio);
            tvCategory = itemView.findViewById(R.id.tvCategoryBadge);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            ivRequestImage = itemView.findViewById(R.id.ivSolicitudImage);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            btnContact = itemView.findViewById(R.id.btnContactar);
        }
    }
}