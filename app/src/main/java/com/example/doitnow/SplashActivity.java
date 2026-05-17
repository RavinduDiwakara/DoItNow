package com.example.doitnow;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force Light Mode BEFORE super.onCreate to prevent theme flashing/black screens
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        // Match system bars (top and bottom) to the splash background color
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.splash_background));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.splash_background));

        // Ensure system icons are white for the dark green background
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }

        setContentView(R.layout.activity_splash);
        
        // Find the root layout and set a click listener to navigate to Login
        // this fulfills the requirement: User taps anywhere -> Login screen opens
        findViewById(R.id.main).setOnClickListener(v -> navigateToLogin());
    }

    private void navigateToLogin() {
        if (!isFinishing()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            // Transition animation for a smooth experience
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }
}