package com.example.CafeteriaApp.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.CafeteriaApp.R;
import com.google.android.material.button.MaterialButton;

public class SignUpPage extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private View vfSteps; // ViewFlipper
    private MaterialButton btnPrev, btnNextOrCreate;
    private TextView loginRedirectText;
    private EditText ET_signup_password, ET_signup_username, ET_signup_email, ET_signup_name;
    private Spinner Spin_signup_class;
    private AutoCompleteTextView actClass;

    private Intent intent;
    private boolean isNextButtonEnabled = false;

    private String username, password;
    private String[] classes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);
        Weddings();
        setAdapter();
        getIntent();


    }


    public void Weddings()
    {


        ET_signup_name = findViewById(R.id.ET_signup_name);
        ET_signup_email = findViewById(R.id.ET_signup_email);
        ET_signup_username = findViewById(R.id.ET_signup_username);
        ET_signup_password = findViewById(R.id.ET_signup_password);
        Spin_signup_class = findViewById(R.id.Spin_signup_class);



    }

    public void setAdapter()
    {
        classes = getResources().getStringArray(R.array.class_names);
        Spin_signup_class.setOnItemSelectedListener(this);
        ArrayAdapter<String> adp = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                classes
        );
        Spin_signup_class.setAdapter(adp);
    }




    /*
    public void Next_Click(View view) {

        if (!isNextButtonEnabled) { // --- STAGE 1: username + password ---
            String user = etUsername.getText().toString().trim();
            String pass = etPasswordSignup.getText().toString();

            // validate username + password
            if (!isLegally(user, "username")) {
                etUsername.setError("שם משתמש חייב להיות רק עברית או רק אנגלית (6–15), ללא רווחים.");
                etUsername.requestFocus();
                return;
            } else {
                etUsername.setError(null);
            }

            if (!isLegally(pass, "password")) {
                etPasswordSignup.setError("סיסמה חייבת להיות בין 6–15 תווים.");
                etPasswordSignup.requestFocus();
                return;
            } else {
                etPasswordSignup.setError(null);
            }

            // save stage-1 values
            username = user;
            password = pass;

            // move to stage 2 (ViewFlipper child #1)
            isNextButtonEnabled = true;                       // we are now in stage 2
            tvStepIndicator.setText(R.string.step_2_of_2);
            ViewFlipper flipper = findViewById(R.id.vfSteps);
            if (flipper.getDisplayedChild() == 0) {
                flipper.setDisplayedChild(1);
                btnPrev.setVisibility(View.VISIBLE);
                btnNextOrCreate.setText(R.string.create_account_button); // change button text
            }

        } else { // --- STAGE 2: class + phone + email ---
            String klass = actClass.getText().toString().trim(); // AutoCompleteTextView/EditText
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            // very basic checks; adjust to your rules
            if (klass.isEmpty()) {
                actClass.setError("נא להזין כיתה");
                actClass.requestFocus();
                return;
            } else actClass.setError(null);

            if (!phone.matches("^[0-9]{9,}$")) {
                etPhone.setError("מספר טלפון לא תקין");
                etPhone.requestFocus();
                return;
            } else etPhone.setError(null);

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("אימייל לא תקין");
                etEmail.requestFocus();
                return;
            } else etEmail.setError(null);

            // TODO: create account / move forward
            // startActivity(new Intent(this, MainActivity.class));
            // finish();
        }
    }

    public void Prev_Click(View v) {
        // go back to stage 1
        ViewFlipper flipper = findViewById(R.id.vfSteps);
        if (flipper.getDisplayedChild() == 1) {
            flipper.setDisplayedChild(0);
        }
        isNextButtonEnabled = false;                           // we are back to stage 1
        btnPrev.setVisibility(View.GONE);
        btnNextOrCreate.setText(R.string.next_button);
        tvStepIndicator.setText(R.string.step_1_of_2);
    }

    public boolean isLegally(String str, String witch_input) {
        if (str == null) str = "";
        str = str.trim();

        // *** IMPORTANT: never compare strings with '==' in Java. Use equals(). ***
        if ("username".equals(witch_input)) {
            // length 6..15
            if (str.length() < 6) {
                Toast.makeText(this, "שם המשתמש צריך להיות עם 6 תווים לפחות.", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (str.length() > 15) {
                Toast.makeText(this, "שם המשתמש יכול להכיל עד 15 תווים.", Toast.LENGTH_SHORT).show();
                return false;
            }

            // only Hebrew OR only English letters, no spaces (no mix)
            // עברית: \u05D0-\u05EA ; אנגלית: A-Z a-z
            boolean ok = str.matches("^(?:[A-Za-z]{6,15}|[\\u05D0-\\u05EA]{6,15})$");
            if (!ok) {
                Toast.makeText(this, "שם משתמש חייב להיות רק אותיות עברית או רק אותיות אנגלית, ללא רווחים.", Toast.LENGTH_SHORT).show();
                return false;
            }

        } else if ("password".equals(witch_input)) {
            if (str.length() < 6) {
                Toast.makeText(this, "הסיסמה צריכה להיות עם 6 תווים לפחות.", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (str.length() > 15) {
                Toast.makeText(this, "הסיסמה יכולה להכיל עד 15 תווים.", Toast.LENGTH_SHORT).show();
                return false;
            }
            // you can add more password rules if you want (digits, symbols, etc.)
        }

        return true;
    }


    */
    public void SignUpGoogle_Click(View view) {

    }
    public void Login_Click(View view) {
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