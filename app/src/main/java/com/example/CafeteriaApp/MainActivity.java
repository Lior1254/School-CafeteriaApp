package com.example.CafeteriaApp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.CafeteriaApp.Authentication.LoginPage;

public class MainActivity extends BaseActivity
{
    private Handler handler = new Handler();
    private Runnable networkCheckRunnable;
    private static final int CHECK_INTERVAL = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        
        networkCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (isNetworkAvailable()) {
                    handler.removeCallbacks(this); // Stop the loop
                    proceedToLogin();
                } else {
                    Toast.makeText(MainActivity.this, "No internet connection. Retrying...", Toast.LENGTH_SHORT).show();
                    handler.postDelayed(this, CHECK_INTERVAL); // Check again after the interval
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(networkCheckRunnable); // Start the check when the activity is resumed
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(networkCheckRunnable); 
    }

    private void proceedToLogin() {
        Intent intent = new Intent(this, LoginPage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
