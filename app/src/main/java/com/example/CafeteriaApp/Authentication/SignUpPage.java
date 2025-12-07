package com.example.CafeteriaApp.Authentication;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Models.UserData;
import com.example.CafeteriaApp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class SignUpPage extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private TextView  tv_signup_warning;
    private EditText ET_signup_password, ET_signup_username, ET_signup_email, ET_signup_name, ET_signup_phoneNumber;
    private Spinner Spin_signup_class, Spin_signup_School;
    private String name, username, email, password, phoneNumber;
    private String[] classes,schools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);
        Weddings();
        setAdapter();


    }


    public void Weddings()
    {
        ET_signup_name = findViewById(R.id.ET_signup_name);
        ET_signup_email = findViewById(R.id.ET_signup_email);
        ET_signup_username = findViewById(R.id.ET_signup_username);
        ET_signup_password = findViewById(R.id.ET_signup_password);
        ET_signup_phoneNumber = findViewById(R.id.ET_signup_phoneNumber);
        Spin_signup_class = findViewById(R.id.Spin_signup_class);
        Spin_signup_School = findViewById(R.id.Spin_signup_School);
        tv_signup_warning = findViewById(R.id.tv_signup_warning);

    }

    public void setAdapter()
    {
        //Spin_signup_School
        schools = getResources().getStringArray(R.array.school_names);
        Spin_signup_School.setOnItemSelectedListener(this);
        ArrayAdapter<String> adpSchool = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                schools
        );
        Spin_signup_School.setAdapter(adpSchool);

        classes = getResources().getStringArray(R.array.class_names);
        Spin_signup_class.setOnItemSelectedListener(this);
        ArrayAdapter<String> adpClass = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                classes
        );
        Spin_signup_class.setAdapter(adpClass);
    }


    private void showWarning(String warning) {
        tv_signup_warning.setVisibility(View.VISIBLE);
        tv_signup_warning.setText(warning);

        // Add a shake animation to grab the user's attention
        ObjectAnimator animator = ObjectAnimator.ofFloat(tv_signup_warning, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        animator.setDuration(500); // milliseconds
        animator.start();
    }

    public boolean CheckInput()
    {
        // Get texts from fields
        name = ET_signup_name.getText().toString().trim();
        username = ET_signup_username.getText().toString().trim();
        email = ET_signup_email.getText().toString().trim();
        password = ET_signup_password.getText().toString().trim();
        phoneNumber = ET_signup_phoneNumber.getText().toString().trim();

        // Check name
        if (name.isEmpty()) {
            showWarning("  נא להזין שם");
            return false;
        }

        // Check email (not empty and valid format)
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showWarning("  נא להזין כתובת אימייל תקינה");
            return false;
        }

        // Check phone number
        if (phoneNumber.length() != 10 || !phoneNumber.startsWith("05")) {
            showWarning("  נא להזין מספר טלפון תקין\n  (10 ספרות, מתחיל ב-05)");
            return false;
        }

        // Check username (must be longer than 4 characters)
        if (username.length() <= 4) {
            showWarning("  שם משתמש חייב להכיל מעל 4 תווים");
            return false;
        }

        // Check password (must be longer than 6 characters)
        if (password.length() <= 6) {
            showWarning("  סיסמה חייבת להכיל מעל 6 תווים");
            return false;
        }

        // Check school spinner (the first option is usually a title like "Select school")
        if (Spin_signup_School.getSelectedItemPosition() == 0) {
            showWarning("   נא לבחור בית ספר");
            return false;
        }

        // Check class spinner
        if (Spin_signup_class.getSelectedItemPosition() == 0) {
            showWarning("  נא לבחור כיתה");
            return false;
        }

        // If everything is valid, hide the warning message and return true
        tv_signup_warning.setVisibility(View.GONE);
        return true;
    }

    public void CreateAccount()
    {

        // As requested: Sign out the previous user before creating a new one.
        if (FBRef.refAuth.getCurrentUser() != null) {
            FBRef.refAuth.signOut();
        }
        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Create user...");
        pd.show();
        FBRef.refAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful())
                        {
                            FirebaseUser user = FBRef.refAuth.getCurrentUser();
                            if (user != null)
                            {
                                String school = Spin_signup_School.getSelectedItem().toString();
                                String classRoom = Spin_signup_class.getSelectedItem().toString();
                                UserData userData = new UserData(user.getUid(), name, email, username, phoneNumber, school, classRoom);

                                // Save to Realtime Database with listener
                                FBRef.refUsers.child(user.getUid()).setValue(userData)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task2) {
                                                pd.dismiss();
                                                if (task2.isSuccessful())
                                                {
                                                    Toast.makeText(SignUpPage.this, "User created successfully.", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                } else {
                                                    showWarning("Failed to save user data: " + task.getException().getMessage());
                                                }
                                            }
                                        });
                            }
                            else
                            {
                                pd.dismiss();
                            }
                        }
                        else
                        {
                            pd.dismiss();
                            // Handle specific exceptions
                            Exception exp = task.getException();
                            if (exp instanceof FirebaseAuthWeakPasswordException) {
                                showWarning("  הסיסמה חלשה מדי.");
                            } else if (exp instanceof FirebaseAuthUserCollisionException) {
                                showWarning("  האימייל הזה כבר רשום במערכת.");
                            } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
                                showWarning("  פורמט אימייל לא תקין.");
                            } else if (exp instanceof FirebaseNetworkException) {
                                showWarning("  שגיאת רשת, בדוק את החיבור שלך.");
                            } else {
                                if (exp != null) {
                                    showWarning("  שגיאה: " + exp.getMessage());
                                } else {
                                    showWarning("  אירעה שגיאה לא ידועה.");
                                }
                            }
                        }
                    }
                });
    }
    public void SignUp_Click(View view) {
        if(CheckInput())
        {
            CreateAccount();
        }
    }

    public void SignUpGoogle_Click(View view) {

    }
    public void MoveTo_Login_Click(View view)
    {
        finish();
    }


    //Spinner input
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
