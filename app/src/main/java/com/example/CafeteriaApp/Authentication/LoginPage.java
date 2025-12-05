package com.example.CafeteriaApp.Authentication;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.MainPage;
import com.example.CafeteriaApp.Models.UserData;
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

public class LoginPage extends AppCompatActivity {
    private TextView tv_login_warning;
    private EditText ED_login_email, ED_login_password;
    private String  email, password;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        Weddings();

    }

    public void Weddings()
    {
        ED_login_email = findViewById(R.id.ED_login_email);
        ED_login_password = findViewById(R.id.ED_login_password);
        tv_login_warning = findViewById(R.id.tv_login_warning);
    }

    public void SignUp_Click(View view) {
        intent = new Intent(this, SignUpPage.class);
        startActivity(intent);
    }

    private void showWarning(String warning) {
        tv_login_warning.setVisibility(View.VISIBLE);
        tv_login_warning.setText(warning);

        // Add a shake animation to grab the user's attention
        ObjectAnimator animator = ObjectAnimator.ofFloat(tv_login_warning, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        animator.setDuration(500); // milliseconds
        animator.start();
    }

    public boolean CheckInput()
    {
        // Get texts from fields
        email = ED_login_email.getText().toString().trim();
        password = ED_login_password.getText().toString().trim();

        // Check email (not empty and valid format)
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showWarning("  נא להזין כתובת אימייל תקינה");
            return false;
        }

        // Check password (must be longer than 6 characters)
        if (password.length() <= 6) {
            showWarning("  סיסמה חייבת להכיל מעל 6 תווים");
            return false;
        }

        // If everything is valid, hide the warning message and return true
        tv_login_warning.setVisibility(View.GONE);
        return true;
    }

    public void login_User()
    {

        // As requested: Sign out the previous user before creating a new one.
        if (FBRef.refAuth.getCurrentUser() != null) {
            FBRef.refAuth.signOut();
        }
        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Logging in user...");
        pd.show();
        FBRef.refAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            FirebaseUser user = FBRef.refAuth.getCurrentUser();
                            if (user != null)
                            {
                                FBRef.refUsers.child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> taskSnapshot) {
                                        pd.dismiss();
                                        if (taskSnapshot.isSuccessful()) {
                                            DataSnapshot snapshot = taskSnapshot.getResult();
                                            UserData userData = snapshot.getValue(UserData.class);
                                            if(userData != null)
                                            {
                                                intent.putExtra("userData", userData);
                                                startActivity(intent);
                                            }
                                        } else {
                                            showWarning("  שגיאה בקריאת נתונים.");
                                        }
                                    }
                                });
                            } else {
                                pd.dismiss();
                            }
                        }
                        else
                        {
                            pd.dismiss();
                            // Handle specific exceptions
                            Exception exp = task.getException();
                            if (exp instanceof FirebaseAuthInvalidUserException) {
                                showWarning("  כתובת אימייל לא חוקית.");
                            }
                            else if (exp instanceof FirebaseAuthWeakPasswordException) {
                                showWarning("  הסיסמה חלשה מדי.");
                            }
                            else if (exp instanceof FirebaseAuthUserCollisionException) {
                                showWarning("  המשתמש כבר קיים.");
                            }
                            else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
                                showWarning("  כשל אימות כללי.");
                            }
                            else if (exp instanceof FirebaseNetworkException) {
                                showWarning("  שגיאת רשת. אנא בדוק את החיבור שלך.");
                            }
                            else {
                                showWarning("  אירעה שגיאה. אנא נסה שוב מאוחר יותר.");
                            }
                        }
                    }
                });
    }

    public void Login_Click(View view) {
        if(CheckInput())
        {
            intent = new Intent(this, MainPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            login_User();
        }
    }
}