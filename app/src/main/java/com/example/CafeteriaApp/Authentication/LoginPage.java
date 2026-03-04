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

        // 1. Check if Firebase remembers the session
        FirebaseUser currentUser = FBRef.refAuth.getCurrentUser();
        if (currentUser != null)
        {
            // 2. Fetch stored authentication string (UID#ExpiryTime)
            String str = FileManager.getUserAuthentication(this);
            if (!str.isEmpty())
            {
                String[] strs = str.split("#"); // Expected format: uid#expiry_time
                if (strs.length == 2 && currentUser.getUid().equals(strs[0]))
                {
                    // 3. Check if current time is before the saved expiry time
                    long expiryTime = Utils.dateStringToLong(strs[1]);
                    long currentTime = System.currentTimeMillis();

                    if (currentTime < expiryTime)
                    {
                        // 4. Session is still valid - proceed to automatic login
                        autoLogin(currentUser.getUid());
                    }
                }
            }
        }
    }

    /**
     * Performs automatic login by fetching user data and navigating to the main page.
     * @param uid The authenticated user's ID.
     */
    private void autoLogin(String uid) {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Logging in automatically...");
        pd.show();

        FBRef.refUsers.child(uid).get().addOnCompleteListener(task -> {
            pd.dismiss();
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                User userModel = snapshot.getValue(User.class);
                if (userModel != null) {
                    Intent mainIntent = new Intent(this, MainPage.class);
                    mainIntent.putExtra("userData", userModel);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                }
            }
        });
    }

    /**
     * Initializes UI components from layout.
     */
    public void initializeViews()
    {
        ED_login_email = findViewById(R.id.ED_login_email);
        ED_login_password = findViewById(R.id.ED_login_password);
        tv_login_warning = findViewById(R.id.tv_login_warning);
    }

    /**
     * Navigates to the Sign Up page.
     */
    public void SignUp_Click(View view)
    {
        intent = new Intent(this, SignUpPage.class);
        startActivity(intent);
    }

    /**
     * Displays a warning message with a visual shake animation.
     * @param warning Text message to be displayed.
     */
    private void showWarning(String warning)
    {
        tv_login_warning.setVisibility(View.VISIBLE);
        tv_login_warning.setText(warning);

        ObjectAnimator animator = ObjectAnimator.ofFloat(tv_login_warning, "translationX", 0f, 25f,
                                                         -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        animator.setDuration(500); 
        animator.start();
    }

    /**
     * Validates user input for email and password.
     * @return true if input is valid, false otherwise.
     */
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

    /**
     * Performs standard login with Firebase Authentication.
     */
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
                            FirebaseUser firebaseUser = FBRef.refAuth.getCurrentUser();
                            if (firebaseUser != null)
                            {
                                // Store current UID and login expiry time (7 days from now)
                                FileManager.saveUserAuthentication(LoginPage.this);

                                FBRef.refUsers.child(firebaseUser.getUid()).get().addOnCompleteListener(
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

    /**
     * Handles the login button click event.
     */
    public void Login_Click(View view)
    {
        if (checkInput())
        {
            // Executes login with network validation from BaseActivity
            executeFirebaseOperation(this::loginUser);
        }
    }
}
