package com.marcal.fixloop.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.marcal.fixloop.R;
import com.marcal.fixloop.model.Message;

import java.util.List;

/**
 * Adaptador per al RecyclerView de la pantalla de Xat
 * Gestiona la visualització dels missatges, diferenciant entre els enviats (Outgoing)
 * i els rebuts (Incoming) utilitzant diferents layouts
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Constants per identificar el tipus de missatge
    private static final int VIEW_TYPE_OUTGOING = 1;
    private static final int VIEW_TYPE_INCOMING = 2;

    private Context context;
    private List<Message> messages;
    private int currentUserId;

    public ChatAdapter(Context context, List<Message> messages, int currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    /**
     * Determina quin tipus de vista s'ha d'utilitzar per a la posició donada
     * @param position Posició de l'element a la llista
     * @return VIEW_TYPE_OUTGOING si l'emissor és l'usuari actual, VIEW_TYPE_INCOMING cas contrari
     */
    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getSenderId() == currentUserId) {
            return VIEW_TYPE_OUTGOING;
        } else {
            return VIEW_TYPE_INCOMING;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_OUTGOING) {
            // Layout per als missatges propis (alineats a la dreta)
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_outgoing, parent, false);
            return new OutgoingViewHolder(view);
        } else {
            // Layout per als missatges de l'altre (alineats a l'esquerra)
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_incoming, parent, false);
            return new IncomingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof OutgoingViewHolder) {
            ((OutgoingViewHolder) holder).bind(message);
        } else if (holder instanceof IncomingViewHolder) {
            ((IncomingViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // --- ViewHolders ---

    /**
     * ViewHolder per als missatges enviats
     */
    static class OutgoingViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        public OutgoingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(Message message) {
            tvMessage.setText(message.getContent());
            tvTime.setText(message.getSentDate());
        }
    }

    /**
     * ViewHolder per als missatges rebuts
     */
    static class IncomingViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        public IncomingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(Message message) {
            tvMessage.setText(message.getContent());
            tvTime.setText(message.getSentDate());
        }
    }
}