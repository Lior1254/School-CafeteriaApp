package com.example.CafeteriaApp.Authentication;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
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
 * Activity responsible for user authentication.
 * Manages manual login, automatic session restoration, and "Remember Me" functionality.
 */
public class LoginPage extends BaseActivity {
    private TextView tvLoginWarning;
    private EditText etLoginEmail, etLoginPassword;
    private CheckBox cbRememberMe;
    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        initializeViews();

        if (isNetworkAvailable()) {
            attemptAutoLogin();
        } else {
            showWarning(getString(R.string.error_no_internet));
        }
    }

    /**
     * Attempts to automatically log in the user based on stored session data and Firebase state.
     */
    private void attemptAutoLogin() {
        FirebaseUser currentUser = FBRef.refAuth.getCurrentUser();
        if (currentUser != null) {
            String authData = FileManager.getUserAuthentication(this);

            if (authData != null && !authData.isEmpty()) {
                try {
                    String[] parts = authData.split("#");
                    if (parts.length == 2 && currentUser.getUid().equals(parts[0])) {
                        long expiryTime = Utils.dateStringToLong(parts[1]);
                        if (System.currentTimeMillis() < expiryTime) {
                            performAutoLogin(currentUser.getUid());
                            return;
                        }
                    }
                } catch (Exception e) {
                    FileManager.clearUserAuthentication(this);
                }
            }
            FBRef.refAuth.signOut();
        }
    }

    /**
     * Fetches user profile data from Firebase and redirects to the main page.
     * @param uid The unique identifier of the user.
     */
    private void performAutoLogin(String uid) {
        if (isFinishing()) return;

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.connecting));
        progressDialog.setMessage(getString(R.string.logging_in_auto));
        progressDialog.setCancelable(false);
        progressDialog.show();

        FBRef.refUsers.child(uid).get().addOnCompleteListener(task -> {
            if (!isFinishing() && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot != null && snapshot.exists()) {
                    User userModel = snapshot.getValue(User.class);
                    if (userModel != null) {
                        navigateToMain(userModel);
                    }
                }
            }
        });
    }

    /**
     * Initializes UI component references.
     */
    private void initializeViews() {
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        tvLoginWarning = findViewById(R.id.tvLoginWarning);
        cbRememberMe = findViewById(R.id.cbRememberMe);
    }

    /**
     * Navigates to the sign-up page.
     * @param view The clicked view.
     */
    public void SignUp_Click(View view) {
        startActivity(new Intent(this, SignUpPage.class));
    }

    /**
     * Displays a shaking warning message on the UI.
     * @param message The Hebrew message to display.
     */
    private void showWarning(String message) {
        if (tvLoginWarning == null) return;
        tvLoginWarning.setVisibility(View.VISIBLE);
        tvLoginWarning.setText(message);

        ObjectAnimator animator = ObjectAnimator.ofFloat(tvLoginWarning, "translationX", 0f, 25f,
                -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        animator.setDuration(500);
        animator.start();
    }

    /**
     * Validates user input before attempting login.
     * @return true if input is valid, false otherwise.
     */
    private boolean validateInput() {
        email = etLoginEmail.getText().toString().trim();
        password = etLoginPassword.getText().toString().trim();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showWarning(getString(R.string.val_invalid_email));
            return false;
        }

        if (password.length() <= 6) {
            showWarning(getString(R.string.val_password_short));
            return false;
        }

        tvLoginWarning.setVisibility(View.GONE);
        return true;
    }

    /**
     * Authenticates the user with Firebase Email/Password.
     */
    private void performManualLogin() {
        if (FBRef.refAuth.getCurrentUser() != null) {
            FBRef.refAuth.signOut();
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.connecting));
        progressDialog.setMessage(getString(R.string.logging_in_manual));
        progressDialog.setCancelable(false);
        progressDialog.show();

        FBRef.refAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = FBRef.refAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            FileManager.saveUserAuthentication(this, cbRememberMe.isChecked());
                            fetchUserData(firebaseUser.getUid(), progressDialog);
                        } else if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    } else {
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        handleLoginError(task.getException());
                    }
                });
    }

    /**
     * Fetches user data from Realtime Database after successful authentication.
     * @param uid The user's UID.
     * @param progressDialog The active progress dialog.
     */
    private void fetchUserData(String uid, ProgressDialog progressDialog) {
        FBRef.refUsers.child(uid).get().addOnCompleteListener(task -> {
            if (!isFinishing() && progressDialog.isShowing()) progressDialog.dismiss();

            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                User userModel = snapshot.getValue(User.class);
                if (userModel != null) {
                    navigateToMain(userModel);
                }
            } else {
                showWarning(getString(R.string.error_user_data_read));
            }
        });
    }

    /**
     * Redirects to MainPage and clears the activity stack.
     * @param user The logged-in user object.
     */
    private void navigateToMain(User user) {
        Intent mainIntent = new Intent(this, MainPage.class);
        mainIntent.putExtra("userData", user);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    /**
     * Maps Firebase Auth exceptions to Hebrew error messages.
     * @param exception The exception thrown by Firebase.
     */
    private void handleLoginError(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            showWarning(getString(R.string.val_user_not_found));
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            showWarning(getString(R.string.val_weak_password));
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            showWarning(getString(R.string.val_user_collision));
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            showWarning(getString(R.string.val_auth_failed));
        } else if (exception instanceof FirebaseNetworkException) {
            showWarning(getString(R.string.val_network_error));
        } else {
            showWarning(getString(R.string.val_unknown_error));
        }
    }

    /**
     * Handles the login button click.
     * @param view The clicked view.
     */
    public void Login_Click(View view) {
        if (validateInput()) {
            executeFirebaseOperation(this::performManualLogin);
        }
    }
}
