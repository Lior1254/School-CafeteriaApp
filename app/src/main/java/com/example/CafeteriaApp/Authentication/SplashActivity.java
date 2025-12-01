package com.example.CafeteriaApp.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.CafeteriaApp.R;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1200; // 1.2 שניות

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginPage.class));
            finish(); // שלא יחזרו לספלש ב־Back
        }, SPLASH_DELAY_MS);
    }
}
