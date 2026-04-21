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
import com.example.CafeteriaApp.Helpers.FileManager;
import com.example.CafeteriaApp.Helpers.Utils;
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

/**
 * Activity for user login.
 * Handles authentication with Firebase Auth and manages session persistence.
 */
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

        // Check internet before attempting auto-login
        if (isNetworkAvailable()) {
            attemptAutoLogin();
        } else {
            showWarning("  אין חיבור לאינטרנט. אנא התחבר ונסה שוב.");
        }
    }

    private void attemptAutoLogin() {
        // 1. Check if Firebase remembers the session
        FirebaseUser currentUser = FBRef.refAuth.getCurrentUser();
        if (currentUser != null)
        {
            // 2. Fetch stored authentication string
            String str = FileManager.getUserAuthentication(this);
            if (str != null && !str.isEmpty())
            {
                try {
                    String[] strs = str.split("#"); 
                    if (strs.length == 2 && currentUser.getUid().equals(strs[0]))
                    {
                        long expiryTime = Utils.dateStringToLong(strs[1]);
                        long currentTime = System.currentTimeMillis();

                        if (currentTime < expiryTime)
                        {
                            autoLogin(currentUser.getUid());
                        }
                    }
                } catch (Exception e) {
                    // If parsing fails, just clear and require manual login
                    FileManager.clearUserAuthentication(this);
                }
            }
        }
    }

    /**
     * Performs automatic login safely.
     */
    private void autoLogin(String uid) {
        if (isFinishing()) return;
        
        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Logging in automatically...");
        pd.setCancelable(false);
        pd.show();

        FBRef.refUsers.child(uid).get().addOnCompleteListener(task -> {
            if (!isFinishing() && pd.isShowing()) {
                pd.dismiss();
            }
            
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot != null && snapshot.exists()) {
                    User userModel = snapshot.getValue(User.class);
                    if (userModel != null) {
                        Intent mainIntent = new Intent(this, MainPage.class);
                        mainIntent.putExtra("userData", userModel);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainIntent);
                        finish();
                    }
                }
            }
        });
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
        if (tv_login_warning == null) return;
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
        pd.setCancelable(false);
        pd.show();

        FBRef.refAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            FirebaseUser firebaseUser = FBRef.refAuth.getCurrentUser();
                            if (firebaseUser != null)
                            {
                                FileManager.saveUserAuthentication(LoginPage.this);

                                FBRef.refUsers.child(firebaseUser.getUid()).get().addOnCompleteListener(
                                        taskSnapshot -> {
                                            if (!isFinishing() && pd.isShowing()) pd.dismiss();
                                            
                                            if (taskSnapshot.isSuccessful())
                                            {
                                                DataSnapshot snapshot = taskSnapshot.getResult();
                                                User userModel = snapshot.getValue(User.class);
                                                if (userModel != null)
                                                {
                                                    Intent mainIntent = new Intent(LoginPage.this, MainPage.class);
                                                    mainIntent.putExtra("userData", userModel);
                                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(mainIntent);
                                                    finish();
                                                }
                                            } else
                                            {
                                                showWarning("  שגיאה בקריאת נתוני המשתמש.");
                                            }
                                        });
                            } else
                            {
                                if (!isFinishing() && pd.isShowing()) pd.dismiss();
                            }
                        } else
                        {
                            if (!isFinishing() && pd.isShowing()) pd.dismiss();
                            handleLoginError(task.getException());
                        }
                    }
                });
    }

    private void handleLoginError(Exception exp) {
        if (exp instanceof FirebaseAuthInvalidUserException) {
            showWarning("  Invalid email address.");
        } else if (exp instanceof FirebaseAuthWeakPasswordException) {
            showWarning("  Password is too weak.");
        } else if (exp instanceof FirebaseAuthUserCollisionException) {
            showWarning("  User already exists.");
        } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
            showWarning("  Authentication failed.");
        } else if (exp instanceof FirebaseNetworkException) {
            showWarning("  Network error. Please check your connection.");
        } else {
            showWarning("  An error occurred. Please try again later.");
        }
    }

    public void Login_Click(View view)
    {
        if (checkInput())
        {
            executeFirebaseOperation(this::loginUser);
        }
    }
}
