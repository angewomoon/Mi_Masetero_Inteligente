package com.devst.mimaseterointeligente.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.devst.mimaseterointeligente.R;
import com.devst.mimaseterointeligente.activities.EditProfileActivity;
import com.devst.mimaseterointeligente.activities.MainActivity;
import com.devst.mimaseterointeligente.database.DatabaseHelper;

public class ProfileFragment extends Fragment {

    private Button btnCerrarSesion;
    private Button btnEditarPerfil; // <-- Añadido
    private TextView tvUsername;
    private TextView tvUserEmail;
    private TextView tvPlantsCount;

    private DatabaseHelper dbHelper;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dbHelper = new DatabaseHelper(requireContext());
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Enlazar vistas
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil); // <-- Añadido
        tvUsername = view.findViewById(R.id.tvUsername);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvPlantsCount = view.findViewById(R.id.tvPlantsCount);

        SharedPreferences prefs = requireContext().getSharedPreferences("MaseteroPrefs", Context.MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        // Configurar listeners
        btnCerrarSesion.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).logout();
            }
        });

        // Listener para abrir la actividad de editar perfil
        btnEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
        loadStats();
    }

    private void loadUserProfile() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MaseteroPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("userName", "Usuario");
        String email = prefs.getString("userEmail", "usuario@email.com");
        tvUsername.setText(username);
        tvUserEmail.setText(email);
    }

    private void loadStats() {
        if (userId != -1) {
            int plantsCount = dbHelper.getPlantsCountByUserId(userId);
            tvPlantsCount.setText(String.valueOf(plantsCount));
        } else {
            tvPlantsCount.setText("0");
        }
    }
}
