package com.devst.mimaseterointeligente.adapters;

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

    /**
     * Actualizar la lista de plantas
     */
    public void setPlants(List<Plant> plants) {
        this.plantList.clear();
        if (plants != null) {
            this.plantList.addAll(plants);
        }
        notifyDataSetChanged();
    }

    /**
     * Obtener la lista actual de plantas
     */
    public List<Plant> getPlants() {
        return plantList;
    }

    /**
     * ViewHolder para cada item de planta
     */
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
            // Nombre de la planta
            tvPlantName.setText(plant.getName());

            // Tipo de planta
            if (plant.getType() != null && !plant.getType().isEmpty()) {
                tvPlantType.setText(plant.getType());
                tvPlantType.setVisibility(View.VISIBLE);
            } else {
                tvPlantType.setVisibility(View.GONE);
            }

            // Estado de conexión
            if (plant.isConnected()) {
                viewConnectionIndicator.setBackgroundTintList(
                    context.getResources().getColorStateList(R.color.green_primary, null));
                tvConnectionStatus.setText("Conectada");
            } else {
                viewConnectionIndicator.setBackgroundTintList(
                    context.getResources().getColorStateList(R.color.text_disabled, null));
                tvConnectionStatus.setText("Sin conexión");
            }

            // Cargar imagen de la planta (si existe)
            if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
                Glide.with(context)
                    .load(plant.getImageUrl())
                    .placeholder(R.drawable.ic_plant)
                    .error(R.drawable.ic_plant)
                    .circleCrop()
                    .into(ivPlantIcon);
            } else {
                ivPlantIcon.setImageResource(R.drawable.ic_plant);
            }

            // Click listener para abrir el dashboard de la planta
            cardPlant.setOnClickListener(v -> {
                Intent intent = new Intent(context, PlantDashboardActivity.class);
                intent.putExtra("plant_id", plant.getId());
                context.startActivity(intent);
            });
        }
    }
}
