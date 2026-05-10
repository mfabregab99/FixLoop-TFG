package com.marcal.fixloop.ui.repairs;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.marcal.fixloop.R;
import com.marcal.fixloop.model.RepairRequest;
import com.marcal.fixloop.network.RetrofitClient;
import com.marcal.fixloop.ui.profile.EditProfileActivity;

import java.util.List;

public class MyRepairsAdapter extends RecyclerView.Adapter<MyRepairsAdapter.ViewHolder> {

    private List<RepairRequest> repairs;
    private Context context;

    public MyRepairsAdapter(Context context, List<RepairRequest> repairs) {
        this.context = context;
        this.repairs = repairs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_repair, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RepairRequest item = repairs.get(position);

        holder.tvTitol.setText(item.getTitle());
        holder.tvCategoria.setText(item.getCategoryName());

        // Ajuntem la ruta del servidor amb el nom del fitxer
        String fullImageUrl = RetrofitClient.UPLOADS_URL + item.getPhotoUrl();

        if (item.getPhotoUrl() != null && !item.getPhotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(fullImageUrl) // Carreguem la URL construïda
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(holder.ivFoto);
        }

        // Cic per anar a editar
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditRepairActivity.class);
            intent.putExtra("sollicitud_id", item.getId());
            intent.putExtra("titol", item.getTitle());
            intent.putExtra("descripcio", item.getDescription());
            //Passem la URL completa perquè Glide la carregui a la següent pantalla
            intent.putExtra("foto_url", fullImageUrl);
            intent.putExtra("categoria_id", item.getCategoryId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return repairs != null ? repairs.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        TextView tvTitol, tvCategoria;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.imgRepair);
            tvTitol = itemView.findViewById(R.id.txtTitolRepair);
            tvCategoria = itemView.findViewById(R.id.txtCategoriaRepair);
        }
    }
}