package com.example.CafeteriaApp.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.CafeteriaApp.Authentication.LoginPage;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Helpers.FileManager;
import com.example.CafeteriaApp.Models.User;
import com.example.CafeteriaApp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Fragment responsible for displaying and editing user profile information and settings.
 * Handles user logout and real-time profile updates.
 */
public class ProfileFragment extends Fragment
{
    private TextView tvName, tvUsername;
    private View btnLogout, btnEditProfile, btnMyOrders;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        loadUserData();
        setupClickListeners();
    }

    private void initializeViews(View view) {
        tvName = view.findViewById(R.id.tvProfileName);
        tvUsername = view.findViewById(R.id.tvProfileUsername);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnMyOrders = view.findViewById(R.id.btn_my_orders);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    private void setupClickListeners() {
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logoutUser());
        }

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        }

        if (btnMyOrders != null) {
            btnMyOrders.setOnClickListener(v -> {
                // Navigate to Orders Tab via activity method
                if (getActivity() instanceof com.example.CafeteriaApp.MainPage) {
                    ((com.example.CafeteriaApp.MainPage) getActivity()).switchToOrdersTab();
                }
            });
        }
    }

    /**
     * Loads the current user data from Firebase to display on the profile.
     */
    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FBRef.refUsers.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                if (currentUser != null && isAdded()) {
                    tvName.setText(currentUser.getName());
                    tvUsername.setText("@" + currentUser.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Displays an AlertDialog to edit user profile details.
     * Validates input before updating Firebase.
     */
    private void showEditProfileDialog() {
        if (currentUser == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("עריכת פרופיל");

        // Dynamic view creation for the dialog
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etName = new EditText(requireContext());
        etName.setHint("שם מלא");
        etName.setText(currentUser.getName());
        layout.addView(etName);

        final EditText etPhone = new EditText(requireContext());
        etPhone.setHint("מספר טלפון");
        etPhone.setText(currentUser.getPhoneNumber());
        layout.addView(etPhone);

        builder.setView(layout);

        builder.setPositiveButton("שמור", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newPhone = etPhone.getText().toString().trim();

            if (validateUpdate(newName, newPhone)) {
                updateProfileInFirebase(newName, newPhone);
            }
        });

        builder.setNegativeButton("ביטול", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private boolean validateUpdate(String name, String phone) {
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "השם לא יכול להיות ריק", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (phone.length() != 10 || !phone.startsWith("05")) {
            Toast.makeText(getContext(), "מספר טלפון לא תקין", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateProfileInFirebase(String name, String phone) {
        if (currentUser == null) return;

        currentUser.setName(name);
        currentUser.setPhoneNumber(phone);

        FBRef.refUsers.child(currentUser.getUid()).setValue(currentUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "הפרופיל עודכן בהצלחה!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "עדכון נכשל", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logoutUser() {
        if (getContext() != null) {
            FileManager.clearUserAuthentication(requireContext());
        }
        if (FBRef.refAuth != null) {
            FBRef.refAuth.signOut();
        }
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
