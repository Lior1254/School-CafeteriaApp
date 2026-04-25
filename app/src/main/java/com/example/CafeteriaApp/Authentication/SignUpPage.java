package com.example.CafeteriaApp.Authentication;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.CafeteriaApp.BaseActivity;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Models.User;
import com.example.CafeteriaApp.R;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity responsible for new user registration.
 * Collects personal details and credentials, validates input, and creates a Firebase account.
 */
public class SignUpPage extends BaseActivity implements AdapterView.OnItemSelectedListener {
    private TextView tvSignupWarning;
    private EditText etSignupPassword, etSignupUsername, etSignupEmail, etSignupName, etSignupPhoneNumber;
    private Spinner spinSignupClass, spinSignupSchool;
    private String name, username, email, password, phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);
        initializeViews();
        setupSpinners();
    }

    /**
     * Initializes UI component references.
     */
    private void initializeViews() {
        etSignupName = findViewById(R.id.ET_signup_name);
        etSignupEmail = findViewById(R.id.ET_signup_email);
        etSignupUsername = findViewById(R.id.ET_signup_username);
        etSignupPassword = findViewById(R.id.ET_signup_password);
        etSignupPhoneNumber = findViewById(R.id.ET_signup_phoneNumber);
        spinSignupClass = findViewById(R.id.Spin_signup_class);
        spinSignupSchool = findViewById(R.id.Spin_signup_School);
        tvSignupWarning = findViewById(R.id.tv_signup_warning);
    }

    /**
     * Configures the School and Class selection spinners with data from resources.
     */
    private void setupSpinners() {
        String[] schools = getResources().getStringArray(R.array.school_names);
        spinSignupSchool.setOnItemSelectedListener(this);
        ArrayAdapter<String> adpSchool = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                schools
        );
        spinSignupSchool.setAdapter(adpSchool);

        String[] classes = getResources().getStringArray(R.array.class_names);
        spinSignupClass.setOnItemSelectedListener(this);
        ArrayAdapter<String> adpClass = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                classes
        );
        spinSignupClass.setAdapter(adpClass);
    }

    /**
     * Displays a shaking warning message on the UI.
     * @param message The Hebrew message to display.
     */
    private void showWarning(String message) {
        if (tvSignupWarning == null) return;
        tvSignupWarning.setVisibility(View.VISIBLE);
        tvSignupWarning.setText(message);

        ObjectAnimator animator = ObjectAnimator.ofFloat(tvSignupWarning, "translationX", 0f, 25f,
                -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        animator.setDuration(500);
        animator.start();
    }

    /**
     * Validates user input before attempting to create an account.
     * @return true if input is valid, false otherwise.
     */
    private boolean validateInput() {
        name = etSignupName.getText().toString().trim();
        username = etSignupUsername.getText().toString().trim();
        email = etSignupEmail.getText().toString().trim();
        password = etSignupPassword.getText().toString().trim();
        phoneNumber = etSignupPhoneNumber.getText().toString().trim();

        if (name.isEmpty()) {
            showWarning(getString(R.string.val_enter_name));
            return false;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showWarning(getString(R.string.val_invalid_email));
            return false;
        }

        if (phoneNumber.length() != 10 || !phoneNumber.startsWith("05")) {
            showWarning(getString(R.string.val_invalid_phone));
            return false;
        }

        if (username.length() <= 4) {
            showWarning(getString(R.string.val_username_short));
            return false;
        }

        if (password.length() <= 6) {
            showWarning(getString(R.string.val_password_short));
            return false;
        }

        if (spinSignupSchool.getSelectedItemPosition() == 0) {
            showWarning(getString(R.string.val_select_school));
            return false;
        }

        if (spinSignupClass.getSelectedItemPosition() == 0) {
            showWarning(getString(R.string.val_select_class));
            return false;
        }

        tvSignupWarning.setVisibility(View.GONE);
        return true;
    }

    /**
     * Creates a new Firebase Auth account and stores user details in the Realtime Database.
     */
    private void performAccountCreation() {
        if (FBRef.refAuth.getCurrentUser() != null) {
            FBRef.refAuth.signOut();
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.connecting));
        progressDialog.setMessage(getString(R.string.creating_user));
        progressDialog.show();

        FBRef.refAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = FBRef.refAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToDatabase(firebaseUser.getUid(), progressDialog);
                        } else {
                            progressDialog.dismiss();
                        }
                    } else {
                        progressDialog.dismiss();
                        handleSignupError(task.getException());
                    }
                });
    }

    /**
     * Saves user profile data to the database after successful authentication.
     * @param uid The user's unique ID.
     * @param progressDialog The active progress dialog.
     */
    private void saveUserToDatabase(String uid, ProgressDialog progressDialog) {
        String school = spinSignupSchool.getSelectedItem().toString();
        String classRoom = spinSignupClass.getSelectedItem().toString();

        User newUser = new User(uid, name, email, username, phoneNumber, school, classRoom, User.ROLE_USER);

        FBRef.refUsers.child(uid).setValue(newUser)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.user_created_success), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        showWarning(getString(R.string.error_saving_user, errorMsg));
                    }
                });
    }

    /**
     * Maps Firebase registration exceptions to Hebrew error messages.
     * @param exception The exception thrown by Firebase.
     */
    private void handleSignupError(Exception exception) {
        if (exception instanceof FirebaseAuthWeakPasswordException) {
            showWarning(getString(R.string.val_weak_password));
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            showWarning(getString(R.string.val_user_collision));
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            showWarning(getString(R.string.val_invalid_email));
        } else if (exception instanceof FirebaseNetworkException) {
            showWarning(getString(R.string.val_network_error));
        } else if (exception != null) {
            showWarning(exception.getMessage());
        } else {
            showWarning(getString(R.string.val_unknown_error));
        }
    }

    /**
     * Handles the sign-up button click.
     * @param view The clicked view.
     */
    public void SignUp_Click(View view) {
        if (validateInput()) {
            executeFirebaseOperation(this::performAccountCreation);
        }
    }

    /**
     * Handles Google Sign-up click (Placeholder).
     * @param view The clicked view.
     */
    public void SignUpGoogle_Click(View view) {
        // Implementation for Google signup can be added here
    }

    /**
     * Navigates back to the login page.
     * @param view The clicked view.
     */
    public void MoveTo_Login_Click(View view) {
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // Optional logic when a spinner item is selected
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Optional logic when nothing is selected in the spinner
    }
}
