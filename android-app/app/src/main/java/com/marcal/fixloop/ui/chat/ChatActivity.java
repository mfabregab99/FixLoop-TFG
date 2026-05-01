package com.marcal.fixloop.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.marcal.fixloop.R;
import com.marcal.fixloop.model.AuthResponse;
import com.marcal.fixloop.model.ChatHistoryRequest;
import com.marcal.fixloop.model.Message;
import com.marcal.fixloop.model.MessageRequest;
import com.marcal.fixloop.network.RetrofitClient;
import com.marcal.fixloop.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activitat principal del xat (pantalla de conversa)
 * Gestiona la visualització de missatges, l'enviament i el refresc automàtic (polling)
 */
public class ChatActivity extends AppCompatActivity {

    // Constants per als intents
    public static final String EXTRA_CHAT_ID = "CHAT_ID";
    public static final String EXTRA_OTHER_NAME = "OTHER_USER_NAME";

    private int chatId;
    private String otherUserName;
    private int myUserId;

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private ChatAdapter adapter;
    private List<Message> messagesList = new ArrayList<>();

    // Variables per a la paginació i refresc
    private int lastMessageId = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;
    private static final int REFRESH_INTERVAL = 3000; // 3 segons

    // Semàfor per evitar Race Conditions
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Recuperar dades de l'intent
        // Utilitzem les constants definides
        chatId = getIntent().getIntExtra("CHAT_ID", -1);
        otherUserName = getIntent().getStringExtra("NOM_ALTRE");

        SessionManager session = new SessionManager(this);
        myUserId = session.getUserId();

        if (chatId == -1) {
            Toast.makeText(this, "Error: No s'ha pogut carregar el xat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar UI
        TextView tvTitle = findViewById(R.id.tvChatTitle);
        tvTitle.setText(otherUserName != null ? otherUserName : "Xat");

        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // Configuració del RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        adapter = new ChatAdapter(this, messagesList, myUserId);
        rvMessages.setAdapter(adapter);

        // Listener Enviar
        btnSend.setOnClickListener(v -> sendMessage());

        // Iniciar cicle de refresc
        setupAutoRefresh();
    }

    /**
     * Descarrega missatges del servidor
     * Si lastMessageId és 0, descarrega l'historial
     * Si és > 0, descarrega només els nous
     */
    private void loadMessages() {
        if (isLoading) return; // Si ja està treballant, sortim
        isLoading = true;

        RetrofitClient.getInstance().getMyApi().getMessages(new ChatHistoryRequest(chatId, lastMessageId))
                .enqueue(new Callback<List<Message>>() {
                    @Override
                    public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                        isLoading = false; // Alliberem semàfor

                        if (response.isSuccessful() && response.body() != null) {
                            List<Message> newMessages = response.body();

                            if (!newMessages.isEmpty()) {
                                // Filtratge extra de seguretat per evitar duplicats per error de xarxa
                                List<Message> trulyNewMessages = new ArrayList<>();
                                for (Message m : newMessages) {
                                    if (m.getId() > lastMessageId) {
                                        trulyNewMessages.add(m);
                                    }
                                }

                                if (!trulyNewMessages.isEmpty()) {
                                    messagesList.addAll(trulyNewMessages);
                                    adapter.notifyItemRangeInserted(messagesList.size() - trulyNewMessages.size(), trulyNewMessages.size());

                                    // Scroll automàtic al final
                                    rvMessages.scrollToPosition(messagesList.size() - 1);

                                    // Actualitzem el cursor
                                    lastMessageId = messagesList.get(messagesList.size() - 1).getId();
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Message>> call, Throwable t) {
                        isLoading = false;
                        // Error silenciós en el refresc automàtic
                    }
                });
    }

    /**
     * Envia el missatge escrit a l'API
     */
    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        etMessage.setText(""); // Neteja ràpida

        MessageRequest request = new MessageRequest(chatId, myUserId, text);

        RetrofitClient.getInstance().getMyApi().sendMessage(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful()) {
                    // Forcem una recàrrega immediata per veure el nostre missatge
                    loadMessages();
                } else {
                    Toast.makeText(ChatActivity.this, "Error enviant missatge", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Error de xarxa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAutoRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadMessages();
                handler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Activem el refresc quan l'usuari mira la pantalla
        handler.post(refreshRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Parem el refresc per estalviar recursos
        handler.removeCallbacks(refreshRunnable);
    }
}