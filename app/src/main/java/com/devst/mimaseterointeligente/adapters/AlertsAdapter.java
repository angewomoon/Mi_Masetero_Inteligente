package com.devst.mimaseterointeligente.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.models.Alert;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter para mostrar alertas en un RecyclerView
 */
public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertViewHolder> {

    private Context context;
    private List<Alert> alerts;
    private OnAlertClickListener listener;

    public interface OnAlertClickListener {
        void onAlertClick(Alert alert);
        void onAlertDismiss(Alert alert);
    }

    public AlertsAdapter(Context context, List<Alert> alerts, OnAlertClickListener listener) {
        this.context = context;
        this.alerts = alerts;
        this.listener = listener;
    }

    /**
     * Constructor sin listener (para fragmentos que no necesitan manejar clicks)
     */
    public AlertsAdapter(Context context, List<Alert> alerts) {
        this.context = context;
        this.alerts = alerts;
        this.listener = null;
    }

    /**
     * Actualizar la lista de alertas
     */
    public void updateAlerts(List<Alert> newAlerts) {
        if (newAlerts != null) {
            this.alerts = newAlerts;
            notifyDataSetChanged();
        }
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

        // Configurar título y mensaje
        holder.tvTitle.setText(alert.getTitle());
        holder.tvMessage.setText(alert.getMessage());

        // Configurar tiempo relativo
        holder.tvTime.setText(getRelativeTime(alert.getTimestamp()));

        // Configurar ícono según el tipo de alerta
        configureIcon(holder, alert);

        // Configurar estado de lectura
        if (alert.isRead()) {
            holder.cardView.setAlpha(0.6f);
            holder.ivIndicator.setVisibility(View.GONE);
        } else {
            holder.cardView.setAlpha(1.0f);
            holder.ivIndicator.setVisibility(View.VISIBLE);
        }

        // Click en la tarjeta
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAlertClick(alert);
            }
        });
    }

    @Override
    public int getItemCount() {
        return alerts != null ? alerts.size() : 0;
    }

    /**
     * Configurar ícono según el tipo de alerta
     */
    private void configureIcon(AlertViewHolder holder, Alert alert) {
        String iconType = alert.getIconType();
        String severity = alert.getSeverity();

        // Configurar color de fondo del ícono según severidad
        int backgroundColor;
        if (Alert.SEVERITY_CRITICAL.equals(severity)) {
            backgroundColor = ContextCompat.getColor(context, R.color.alert_critical_bg);
        } else if (Alert.SEVERITY_WARNING.equals(severity)) {
            backgroundColor = ContextCompat.getColor(context, R.color.alert_warning_bg);
        } else {
            backgroundColor = ContextCompat.getColor(context, R.color.alert_info_bg);
        }

        holder.ivIcon.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(backgroundColor)
        );

        // Configurar drawable del ícono
        int iconResource;
        switch (iconType != null ? iconType : "") {
            case "water":
                iconResource = R.drawable.ic_water_drop;
                break;
            case "sun":
            case "light":
                iconResource = R.drawable.ic_sun;
                break;
            case "bug":
            case "pest":
                iconResource = R.drawable.ic_bug;
                break;
            case "temperature":
                iconResource = R.drawable.ic_temperature;
                break;
            default:
                iconResource = R.drawable.ic_alert;
                break;
        }

        holder.ivIcon.setImageResource(iconResource);
    }

    /**
     * Obtener tiempo relativo desde el timestamp
     */
    private String getRelativeTime(String timestamp) {
        try {
            long alertTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis();
            long diffInMillis = currentTime - alertTime;

            long minutes = diffInMillis / (60 * 1000);
            long hours = diffInMillis / (60 * 60 * 1000);
            long days = diffInMillis / (24 * 60 * 60 * 1000);

            if (minutes < 1) {
                return "Ahora";
            } else if (minutes < 60) {
                return "Hace " + minutes + " min";
            } else if (hours < 24) {
                return "Hace " + hours + "h";
            } else if (days < 7) {
                return "Hace " + days + "d";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return sdf.format(new Date(alertTime));
            }
        } catch (Exception e) {
            return "Hace un momento";
        }
    }

    /**
     * ViewHolder para las alertas
     */
    static class AlertViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivIcon;
        ImageView ivIndicator;
        TextView tvTitle;
        TextView tvMessage;
        TextView tvTime;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            ivIcon = itemView.findViewById(R.id.ivAlertIcon);
            ivIndicator = itemView.findViewById(R.id.ivUnreadIndicator);
            tvTitle = itemView.findViewById(R.id.tvAlertTitle);
            tvMessage = itemView.findViewById(R.id.tvAlertMessage);
            tvTime = itemView.findViewById(R.id.tvAlertTime);
        }
    }
}
