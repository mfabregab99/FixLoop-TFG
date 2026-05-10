package com.marcal.fixloop.ui.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.marcal.fixloop.model.AuthResponse;
import com.marcal.fixloop.model.FavoriteRequest;
import com.marcal.fixloop.model.StartChatRequest;
import com.marcal.fixloop.model.StartChatResponse;
import com.marcal.fixloop.model.User;
import com.marcal.fixloop.network.RetrofitClient;
import com.marcal.fixloop.ui.chat.ChatActivity;
import com.marcal.fixloop.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Adaptador per al RecyclerView de la pantalla d'Inici i Favorits (clients)
 * Mostra la llista de reparadors disponibles
 */
public class RepairerAdapter extends RecyclerView.Adapter<RepairerAdapter.RepairerViewHolder> {

    private Context context;
    private List<User> repairerList;
    private int currentUserId;

    public RepairerAdapter(Context context, List<User> repairerList) {
        this.context = context;
        this.repairerList = repairerList;

        SessionManager session = new SessionManager(context);
        this.currentUserId = session.getUserId();
    }

    @NonNull
    @Override
    public RepairerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_repairer, parent, false);
        return new RepairerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RepairerViewHolder holder, int position) {
        User user = repairerList.get(position);

        // --- Dades bàsiques ---
        holder.tvName.setText(user.getFullName());

        if (user.getCategoryListString() != null && !user.getCategoryListString().isEmpty()) {
            holder.tvCategory.setText(user.getCategoryListString());
        } else {
            holder.tvCategory.setText("Reparador General");
        }

        // Dades simulades per ara
        holder.tvRating.setText("4.8 (12)");
        holder.tvDistance.setText("• 2.5 km");

        // --- Lògica PRO ---
        if (user.isPro()) {
            holder.badgePro.setVisibility(View.VISIBLE);
        } else {
            holder.badgePro.setVisibility(View.GONE);
        }

        // --- Lògica Favorits ---
        updateFavoriteIcon(holder, user.isFavorite());

        holder.btnFavorite.setOnClickListener(v -> {
            boolean newState = !user.isFavorite();
            user.setFavorite(newState);
            updateFavoriteIcon(holder, newState);
            toggleFavoriteApi(user.getId(), newState, position);
        });

        // --- Carregar Imatge ---
        if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty()) {

            // Concatenem la URL base de les pujades amb el nom del fitxer de la BD
            String fullUrl = RetrofitClient.UPLOADS_URL + user.getProfilePhotoUrl();

            Glide.with(context)
                    .load(fullUrl) // carreguem la ruta completa
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .circleCrop()
                    .into(holder.ivImage);
        } else {
            // Si no té foto, posem una imatge per defecte
            holder.ivImage.setImageResource(R.drawable.ic_account);
        }

        // --- Lògica Botó Missatge (Iniciar Xat) ---
        holder.btnMessage.setOnClickListener(v -> {
            holder.btnMessage.setEnabled(false); // Evitem doble clic

            // Creem petició de xat: (sense sol·licitud)
            StartChatRequest request = new StartChatRequest(currentUserId, user.getId(), 0);

            RetrofitClient.getInstance().getMyApi().startChat(request).enqueue(new Callback<StartChatResponse>() {
                @Override
                public void onResponse(Call<StartChatResponse> call, Response<StartChatResponse> response) {
                    holder.btnMessage.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        // Obrim l'activitat de xat amb l'ID rebut
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("CHAT_ID", response.body().getChatId());
                        intent.putExtra("OTHER_USER_NAME", user.getFullName());
                        context.startActivity(intent);
                    } else {
                        String errorMsg = (response.body() != null) ? response.body().getMessage() : "Error desconegut";
                        Toast.makeText(context, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<StartChatResponse> call, Throwable t) {
                    holder.btnMessage.setEnabled(true);
                    Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateFavoriteIcon(RepairerViewHolder holder, boolean isFavorite) {
        if (isFavorite) {
            holder.btnFavorite.setImageResource(R.drawable.ic_favorite);
            holder.btnFavorite.setColorFilter(Color.parseColor("#008800"));
        } else {
            holder.btnFavorite.setImageResource(R.drawable.ic_favorite_outlined);
            holder.btnFavorite.clearColorFilter();
        }
    }

    private void toggleFavoriteApi(int repairerId, boolean isAdding, int position) {
        FavoriteRequest request = new FavoriteRequest(currentUserId, repairerId);

        RetrofitClient.getInstance().getMyApi().toggleFavorite(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (!response.isSuccessful()) {
                    revertChange(position, !isAdding);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                revertChange(position, !isAdding);
                Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void revertChange(int position, boolean originalState) {
        if (position < repairerList.size()) {
            repairerList.get(position).setFavorite(originalState);
            notifyItemChanged(position);
        }
    }

    @Override
    public int getItemCount() {
        return repairerList != null ? repairerList.size() : 0;
    }

    public static class RepairerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, btnFavorite, ivStar;
        TextView tvName, tvCategory, tvRating, tvDistance, badgePro;
        Button btnMessage;

        public RepairerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivReparadorImage);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            ivStar = itemView.findViewById(R.id.ivStar);
            tvName = itemView.findViewById(R.id.tvReparadorName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            badgePro = itemView.findViewById(R.id.badgePro);
            btnMessage = itemView.findViewById(R.id.btnMessage);
        }
    }
}