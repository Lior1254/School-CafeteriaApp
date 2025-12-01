package com.example.CafeteriaApp.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.CafeteriaApp.MainPage;
import com.example.CafeteriaApp.R;

public class LoginPage extends AppCompatActivity {
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

    }

    public void SignUp_Click(View view) {
        intent = new Intent(this, SignUpPage.class);
        startActivity(intent);
    }

    public void Login_Click(View view) {
        intent = new Intent(this, MainPage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
    }
}