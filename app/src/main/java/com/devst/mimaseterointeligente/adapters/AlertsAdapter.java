package com.devst.mimaseterointeligente.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.models.Alert;

import java.util.List;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertViewHolder> {

    private final Context context;
    private List<Alert> alerts;

    public AlertsAdapter(Context context, List<Alert> alerts) {
        this.context = context;
        this.alerts = alerts;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        Alert alert = alerts.get(position);

        holder.tvAlertTitle.setText(alert.getTitle());
        holder.tvAlertDescription.setText(alert.getMessage());
        holder.tvAlertTimestamp.setText(alert.getRelativeTime()); // Usa el método getRelativeTime() en lugar de getTimestamp()

        // Asignar un ícono y color de fondo según el tipo de alerta
        setIconAndColor(holder, alert.getIconType());
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    public void updateAlerts(List<Alert> newAlerts) {
        this.alerts = newAlerts;
        notifyDataSetChanged();
    }

    private void setIconAndColor(AlertViewHolder holder, String iconType) {
        int iconResId = R.drawable.ic_email; // Icono por defecto
        int color = Color.parseColor("#CCCCCC"); // Color por defecto

        if (iconType != null) {
            switch (iconType.toLowerCase()) {
                case "water":
                    iconResId = R.drawable.ic_water_drop;
                    color = Color.parseColor("#2196F3"); // Azul
                    break;
                case "sun":
                    iconResId = R.drawable.ic_wb_sunny;
                    color = Color.parseColor("#FFC107"); // Amarillo
                    break;
                case "bug":
                    iconResId = R.drawable.ic_bug_report;
                    color = Color.parseColor("#F44336"); // Rojo
                    break;
                case "humidity":
                    iconResId = R.drawable.ic_water_drop;
                    color = Color.parseColor("#00BCD4"); // Cian
                    break;
                case "thermometer":
                    iconResId = R.drawable.ic_wb_sunny;
                    color = Color.parseColor("#FF5722"); // Naranja
                    break;
                case "info":
                default:
                    iconResId = R.drawable.ic_email;
                    color = Color.parseColor("#9E9E9E"); // Gris
                    break;
            }
        }

        holder.ivAlertIcon.setImageResource(iconResId);

        // Verificar que el background sea un GradientDrawable antes de hacer el cast
        if (holder.ivAlertIcon.getBackground() instanceof GradientDrawable) {
            GradientDrawable background = (GradientDrawable) holder.ivAlertIcon.getBackground();
            background.setColor(color);
        }
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAlertIcon;
        TextView tvAlertTitle;
        TextView tvAlertDescription;
        TextView tvAlertTimestamp;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAlertIcon = itemView.findViewById(R.id.ivAlertIcon);
            tvAlertTitle = itemView.findViewById(R.id.tvAlertTitle);
            tvAlertDescription = itemView.findViewById(R.id.tvAlertDescription);
            tvAlertTimestamp = itemView.findViewById(R.id.tvAlertTimestamp);
        }
    }
}
