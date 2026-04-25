package com.example.CafeteriaApp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import com.example.CafeteriaApp.Authentication.LoginPage;

/**
 * Entry point activity that handles the splash/loading logic.
 * Ensures internet connectivity before allowing the user to proceed to the login screen.
 */
public class MainActivity extends BaseActivity {

    private final Handler networkHandler = new Handler(Looper.getMainLooper());
    private Runnable connectionCheckRunnable;
    private TextView tvStatusMessage;

    private static final int REFRESH_DELAY_MS = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatusMessage = findViewById(R.id.tv_no_internet);
        if (tvStatusMessage != null) {
            tvStatusMessage.setText(R.string.main_checking_connection);
        }

        connectionCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (isNetworkAvailable()) {
                    networkHandler.removeCallbacks(this);
                    navigateToLogin();
                } else {
                    if (tvStatusMessage != null) {
                        tvStatusMessage.setText(R.string.main_retrying_connection);
                    }
                    Toast.makeText(MainActivity.this, R.string.main_retrying_connection, Toast.LENGTH_SHORT).show();
                    networkHandler.postDelayed(this, REFRESH_DELAY_MS);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (connectionCheckRunnable != null) {
            networkHandler.post(connectionCheckRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (connectionCheckRunnable != null) {
            networkHandler.removeCallbacks(connectionCheckRunnable);
        }
    }

    /**
     * Redirects the user to the LoginPage and clears the current activity stack.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginPage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
