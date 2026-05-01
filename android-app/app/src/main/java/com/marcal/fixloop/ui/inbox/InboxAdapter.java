package com.marcal.fixloop.ui.inbox;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.marcal.fixloop.R;
import com.marcal.fixloop.model.Conversation;

import java.util.List;

/**
 * Adaptador per al RecyclerView de la pantalla de Bústia d'entrada (InboxFragment)
 * Mostra la llista de converses actives de l'usuari
 */
public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ConversationViewHolder> {

    private Context context;
    private List<Conversation> conversationList;
    private OnConversationClickListener listener;

    /**
     * Interfície per gestionar els clics en una conversa
     */
    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public InboxAdapter(Context context, List<Conversation> conversationList, OnConversationClickListener listener) {
        this.context = context;
        this.conversationList = conversationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);

        holder.tvName.setText(conversation.getOtherUserName());
        holder.tvRequestTitle.setText(conversation.getRequestTitle());

        // Formatem la data
        holder.tvDate.setText(conversation.getLastActivityDate());

        // Carregar imatge de l'altre usuari
        if (conversation.getOtherUserPhoto() != null && !conversation.getOtherUserPhoto().isEmpty()) {
            Glide.with(context).load(conversation.getOtherUserPhoto()).circleCrop().into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
        }

        // Listener de clic a l'element
        holder.itemView.setOnClickListener(v -> listener.onConversationClick(conversation));
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    /**
     * ViewHolder per a les files de la llista de converses
     */
    public static class ConversationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvRequestTitle, tvDate;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvRequestTitle = itemView.findViewById(R.id.tvSolicitudTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}