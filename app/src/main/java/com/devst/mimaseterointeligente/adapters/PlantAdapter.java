package com.devst.mimaseterointeligente.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.activities.PlantDashboardActivity;
import com.devst.mimaseterointeligente.models.Plant;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para mostrar plantas en RecyclerView
 */
public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    private Context context;
    private List<Plant> plantList;

    public PlantAdapter(Context context) {
        this.context = context;
        this.plantList = new ArrayList<>();
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_plant_card, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plantList.get(position);
        holder.bind(plant);
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public void setPlants(List<Plant> plants) {
        this.plantList.clear();
        if (plants != null) {
            this.plantList.addAll(plants);
        }
        notifyDataSetChanged();
    }

    public List<Plant> getPlants() {
        return plantList;
    }

    class PlantViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView cardPlant;
        ImageView ivPlantIcon;
        TextView tvPlantName;
        TextView tvPlantType;
        View viewConnectionIndicator;
        TextView tvConnectionStatus;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            cardPlant = itemView.findViewById(R.id.cardPlant);
            ivPlantIcon = itemView.findViewById(R.id.ivPlantIcon);
            tvPlantName = itemView.findViewById(R.id.tvPlantName);
            tvPlantType = itemView.findViewById(R.id.tvPlantType);
            viewConnectionIndicator = itemView.findViewById(R.id.viewConnectionIndicator);
            tvConnectionStatus = itemView.findViewById(R.id.tvConnectionStatus);
        }

        public void bind(Plant plant) {
            tvPlantName.setText(plant.getName());

            if (plant.getType() != null && !plant.getType().isEmpty()) {
                tvPlantType.setText(plant.getType());
                tvPlantType.setVisibility(View.VISIBLE);
            } else {
                tvPlantType.setVisibility(View.GONE);
            }

            // Usar ContextCompat para obtener los colores de forma segura y compatible
            if (plant.isConnected()) {
                ColorStateList colorConnected = ContextCompat.getColorStateList(context, R.color.green_primary);
                viewConnectionIndicator.setBackgroundTintList(colorConnected);
                tvConnectionStatus.setText("Conectada");
            } else {
                ColorStateList colorDisconnected = ContextCompat.getColorStateList(context, R.color.text_disabled);
                viewConnectionIndicator.setBackgroundTintList(colorDisconnected);
                tvConnectionStatus.setText("Sin conexión");
            }

            // Cargar imagen con manejo robusto de errores
            try {
                if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
                    // Intentar cargar la imagen (puede ser una ruta local o una URI)
                    Object imageSource;
                    if (plant.getImageUrl().startsWith("/")) {
                        // Es una ruta de archivo local
                        imageSource = new java.io.File(plant.getImageUrl());
                    } else if (plant.getImageUrl().startsWith("file://")) {
                        // Es una URI de archivo
                        imageSource = android.net.Uri.parse(plant.getImageUrl());
                    } else if (plant.getImageUrl().startsWith("content://")) {
                        // Es una URI de contenido (puede no funcionar si no hay permisos)
                        imageSource = android.net.Uri.parse(plant.getImageUrl());
                    } else {
                        // Formato desconocido, usar imagen por defecto
                        ivPlantIcon.setImageResource(R.drawable.ic_plant);
                        return;
                    }

                    Glide.with(context)
                        .load(imageSource)
                        .placeholder(R.drawable.ic_plant)
                        .error(R.drawable.ic_plant)
                        .circleCrop()
                        .into(ivPlantIcon);
                } else {
                    // Sin imagen, usar imagen por defecto
                    ivPlantIcon.setImageResource(R.drawable.ic_plant);
                }
            } catch (Exception e) {
                // Si hay cualquier error, usar imagen por defecto
                ivPlantIcon.setImageResource(R.drawable.ic_plant);
            }

            cardPlant.setOnClickListener(v -> {
                Intent intent = new Intent(context, PlantDashboardActivity.class);
                intent.putExtra("plant_id", plant.getId());
                context.startActivity(intent);
            });
        }
    }
}
