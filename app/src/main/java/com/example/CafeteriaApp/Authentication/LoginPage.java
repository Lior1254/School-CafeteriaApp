package com.example.CafeteriaApp.Authentication;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.CafeteriaApp.BaseActivity;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.MainPage;
import com.example.CafeteriaApp.Models.User;
import com.example.CafeteriaApp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

public class LoginPage extends BaseActivity
{
    private TextView tv_login_warning;
    private EditText ED_login_email, ED_login_password;
    private String email, password;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        initializeViews();
    }

    public void initializeViews()
    {
        ED_login_email = findViewById(R.id.ED_login_email);
        ED_login_password = findViewById(R.id.ED_login_password);
        tv_login_warning = findViewById(R.id.tv_login_warning);
    }

    public void SignUp_Click(View view)
    {
        intent = new Intent(this, SignUpPage.class);
        startActivity(intent);
    }

    private void showWarning(String warning)
    {
        tv_login_warning.setVisibility(View.VISIBLE);
        tv_login_warning.setText(warning);

        ObjectAnimator animator = ObjectAnimator.ofFloat(tv_login_warning, "translationX", 0f, 25f,
                                                         -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        animator.setDuration(500); 
        animator.start();
    }

    public boolean checkInput()
    {
        email = ED_login_email.getText().toString().trim();
        password = ED_login_password.getText().toString().trim();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            showWarning("  Please enter a valid email address");
            return false;
        }

        if (password.length() <= 6)
        {
            showWarning("  Password must be longer than 6 characters");
            return false;
        }

        tv_login_warning.setVisibility(View.GONE);
        return true;
    }

    public void loginUser()
    {
        if (FBRef.refAuth.getCurrentUser() != null)
        {
            FBRef.refAuth.signOut();
        }
        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Logging in user...");
        pd.show();

        FBRef.refAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            FirebaseUser user = FBRef.refAuth.getCurrentUser();
                            if (user != null)
                            {
                                FBRef.refUsers.child(user.getUid()).get().addOnCompleteListener(
                                        new OnCompleteListener<DataSnapshot>()
                                        {
                                            @Override
                                            public void onComplete(
                                                    @NonNull Task<DataSnapshot> taskSnapshot)
                                            {
                                                pd.dismiss();
                                                if (taskSnapshot.isSuccessful())
                                                {
                                                    DataSnapshot snapshot = taskSnapshot.getResult();
                                                    User user = snapshot.getValue(
                                                            User.class);
                                                    if (user != null)
                                                    {
                                                        intent.putExtra("userData", user);
                                                        startActivity(intent);
                                                    }
                                                } else
                                                {
                                                    showWarning("  Error reading user data.");
                                                }
                                            }
                                        });
                            } else
                            {
                                pd.dismiss();
                            }
                        } else
                        {
                            pd.dismiss();
                            Exception exp = task.getException();
                            if (exp instanceof FirebaseAuthInvalidUserException)
                            {
                                showWarning("  Invalid email address.");
                            } else if (exp instanceof FirebaseAuthWeakPasswordException)
                            {
                                showWarning("  Password is too weak.");
                            } else if (exp instanceof FirebaseAuthUserCollisionException)
                            {
                                showWarning("  User already exists.");
                            } else if (exp instanceof FirebaseAuthInvalidCredentialsException)
                            {
                                showWarning("  Authentication failed.");
                            } else if (exp instanceof FirebaseNetworkException)
                            {
                                showWarning("  Network error. Please check your connection.");
                            } else
                            {
                                showWarning("  An error occurred. Please try again later.");
                            }
                        }                    }
                });
    }

    public void Login_Click(View view)
    {
        if (checkInput())
        {
            executeFirebaseOperation(() -> {
                intent = new Intent(this, MainPage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                loginUser();
            });
        }
    }
}
