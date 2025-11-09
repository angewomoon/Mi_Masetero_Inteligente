package com.devst.mimaseterointeligente.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.devst.mimaseterointeligente.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500; // ms
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPreferences = getSharedPreferences("MaseteroPrefs", MODE_PRIVATE);

        ImageView logoImage = findViewById(R.id.logoImage);
        TextView appName = findViewById(R.id.appName);
        TextView appTagline = findViewById(R.id.appTagline);

        // Animaciones (asegúrate de tener ambos XML en res/anim)
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        logoImage.startAnimation(fadeIn);
        appName.startAnimation(slideUp);
        appTagline.startAnimation(slideUp);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserSession, SPLASH_DURATION);
    }

    private void checkUserSession() {
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        Intent intent = new Intent(this, isLoggedIn ? MainActivity.class : LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

