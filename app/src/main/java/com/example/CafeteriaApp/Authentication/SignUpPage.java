package com.example.CafeteriaApp.Authentication;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class SignUpPage extends BaseActivity implements AdapterView.OnItemSelectedListener
{
    private TextView tv_signup_warning;
    private EditText ET_signup_password, ET_signup_username, ET_signup_email, ET_signup_name,
            ET_signup_phoneNumber;
    private Spinner Spin_signup_class, Spin_signup_School;
    private String name, username, email, password, phoneNumber;
    private String[] classes, schools;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);
        initializeViews();
        setupSpinners();
    }

    public void initializeViews()
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

    public void setupSpinners()
    {
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

    private void showWarning(String warning)
    {
        tv_signup_warning.setVisibility(View.VISIBLE);
        tv_signup_warning.setText(warning);

        ObjectAnimator animator = ObjectAnimator.ofFloat(tv_signup_warning, "translationX", 0f, 25f,
                                                         -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        animator.setDuration(500); 
        animator.start();
    }

    public boolean checkInput()
    {
        name = ET_signup_name.getText().toString().trim();
        username = ET_signup_username.getText().toString().trim();
        email = ET_signup_email.getText().toString().trim();
        password = ET_signup_password.getText().toString().trim();
        phoneNumber = ET_signup_phoneNumber.getText().toString().trim();

        if (name.isEmpty())
        {
            showWarning("  Please enter your name");
            return false;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            showWarning("  Please enter a valid email address");
            return false;
        }

        if (phoneNumber.length() != 10 || !phoneNumber.startsWith("05"))
        {
            showWarning("  Please enter a valid phone number\n  (10 digits, starts with 05)");
            return false;
        }

        if (username.length() <= 4)
        {
            showWarning("  Username must be longer than 4 characters");
            return false;
        }

        if (password.length() <= 6)
        {
            showWarning("  Password must be longer than 6 characters");
            return false;
        }

        if (Spin_signup_School.getSelectedItemPosition() == 0)
        {
            showWarning("   Please select a school");
            return false;
        }

        if (Spin_signup_class.getSelectedItemPosition() == 0)
        {
            showWarning("  Please select a class");
            return false;
        }

        tv_signup_warning.setVisibility(View.GONE);
        return true;
    }

    public void createAccount()
    {
        if (FBRef.refAuth.getCurrentUser() != null)
        {
            FBRef.refAuth.signOut();
        }
        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Creating user...");
        pd.show();

        FBRef.refAuth.createUserWithEmailAndPassword(email, password)
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
                                String school = Spin_signup_School.getSelectedItem().toString();
                                String classRoom = Spin_signup_class.getSelectedItem().toString();

                                User user1 = new User(user.getUid(), name, email,
                                                      username, phoneNumber, school,
                                                      classRoom);

                                FBRef.refUsers.child(user.getUid()).setValue(user1)
                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task2)
                                            {
                                                pd.dismiss();
                                                if (task2.isSuccessful())
                                                {
                                                    Toast.makeText(SignUpPage.this,
                                                                   "User created successfully.",
                                                                   Toast.LENGTH_SHORT).show();
                                                    finish();
                                                } else
                                                {
                                                    showWarning(
                                                            "Failed to save user data: " + task2.getException().getMessage());
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
                            if (exp instanceof FirebaseAuthWeakPasswordException)
                            {
                                showWarning("  Password is too weak.");
                            } else if (exp instanceof FirebaseAuthUserCollisionException)
                            {
                                showWarning("  This email is already registered.");
                            } else if (exp instanceof FirebaseAuthInvalidCredentialsException)
                            {
                                showWarning("  Invalid email format.");
                            } else if (exp instanceof FirebaseNetworkException)
                            {
                                showWarning("  Network error, please check your connection.");
                            } else
                            {
                                if (exp != null)
                                {
                                    showWarning("  Error: " + exp.getMessage());
                                } else
                                {
                                    showWarning("  An unknown error occurred.");
                                }
                            }
                        }
                    }
                });
    }

    public void SignUp_Click(View view)
    {
        if (checkInput())
        {
            executeFirebaseOperation(this::createAccount);
        }
    }

    public void SignUpGoogle_Click(View view)
    {
    }

    public void Login_Click(View view)
    {
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {
    }
}
