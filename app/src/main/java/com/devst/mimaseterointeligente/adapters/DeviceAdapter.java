package com.devst.mimaseterointeligente.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.models.Device;

import java.util.List;

/**
 * Adapter para mostrar lista de dispositivos ESP32 disponibles
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private List<Device> deviceList;
    private OnDeviceClickListener listener;

    public interface OnDeviceClickListener {
        void onDeviceClick(Device device);
    }

    public DeviceAdapter(List<Device> deviceList, OnDeviceClickListener listener) {
        this.deviceList = deviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.bind(device, listener);
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    /**
     * ViewHolder para dispositivos
     */
    static class DeviceViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private TextView tvDeviceName;
        private TextView tvDeviceId;
        private TextView tvStatus;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewDevice);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvDeviceId = itemView.findViewById(R.id.tvDeviceId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(Device device, OnDeviceClickListener listener) {
            tvDeviceName.setText(device.getDeviceName());
            tvDeviceId.setText("ID: " + device.getDeviceId());

            // Configurar estado
            String statusText = device.getStatusText();
            tvStatus.setText(statusText);

            // Color según estado
            if (device.isOnline() && device.isAvailable()) {
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.white));
            } else if (!device.isOnline()) {
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.background_light));
            } else {
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
                cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.background_light));
            }

            // Click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeviceClick(device);
                }
            });
        }
    }
}
