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
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.CafeteriaApp.Authentication.LoginPage;
import com.example.CafeteriaApp.Helpers.BaseActivity;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Helpers.FileManager;
import com.example.CafeteriaApp.MainPage;
import com.example.CafeteriaApp.Models.User;
import com.example.CafeteriaApp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Fragment responsible for displaying and editing user profile information and settings.
 * Handles user logout, profile updates, and navigation to orders.
 */
public class ProfileFragment extends Fragment {

    private TextView profileNameTextView, profileUsernameTextView;
    private View editProfileButton, myOrdersButton, logoutButton;
    private SwitchCompat nightModeSwitch;
    private User currentUser;
    private ValueEventListener userProfileListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupClickListeners();
        startListeningToUserData();
    }

    /**
     * Initializes all UI components from the layout.
     *
     * @param view The root view of the fragment.
     */
    private void initializeViews(View view) {
        profileNameTextView = view.findViewById(R.id.tvProfileName);
        profileUsernameTextView = view.findViewById(R.id.tvProfileUsername);
        editProfileButton = view.findViewById(R.id.clEditProfile);
        myOrdersButton = view.findViewById(R.id.clMyOrders);
        logoutButton = view.findViewById(R.id.cvLogout);
        nightModeSwitch = view.findViewById(R.id.switchNightMode);
    }

    /**
     * Sets up click interactions for the profile options and settings.
     */
    private void setupClickListeners() {
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> logoutUser());
        }

        if (editProfileButton != null) {
            editProfileButton.setOnClickListener(v -> showEditProfileDialog());
        }

        if (myOrdersButton != null) {
            myOrdersButton.setOnClickListener(v -> {
                if (getActivity() instanceof MainPage) {
                    ((MainPage) getActivity()).switchToOrdersTab();
                }
            });
        }

        if (nightModeSwitch != null) {
            // Placeholder for night mode logic - can be implemented with SharedPreferences
            nightModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Implementation for night mode toggle
            });
        }
    }

    /**
     * Starts a real-time listener for current user data from Firebase.
     */
    private void startListeningToUserData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        userProfileListener = FBRef.refUsers.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                if (currentUser != null && isAdded()) {
                    profileNameTextView.setText(currentUser.getName());
                    String formattedUsername = "@" + currentUser.getUsername();
                    profileUsernameTextView.setText(formattedUsername);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), getString(R.string.error_user_data_read), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Displays an AlertDialog to allow editing profile details (Name and Phone).
     * Validates input locally before updating the remote database.
     */
    private void showEditProfileDialog() {
        if (currentUser == null || !isAdded()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.profile_edit_title);

        // Build the dialog layout programmatically for simplicity or use a custom layout file
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        final EditText nameEditText = new EditText(requireContext());
        nameEditText.setHint(R.string.profile_edit_hint_name);
        nameEditText.setText(currentUser.getName());
        layout.addView(nameEditText);

        final EditText phoneEditText = new EditText(requireContext());
        phoneEditText.setHint(R.string.profile_edit_hint_phone);
        phoneEditText.setText(currentUser.getPhoneNumber());
        layout.addView(phoneEditText);

        builder.setView(layout);

        builder.setPositiveButton(R.string.profile_save, (dialog, which) -> {
            String newName = nameEditText.getText().toString().trim();
            String newPhone = phoneEditText.getText().toString().trim();

            if (validateUpdateInput(newName, newPhone)) {
                performProfileUpdate(newName, newPhone);
            }
        });

        builder.setNegativeButton(R.string.profile_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Validates the input for profile updates.
     *
     * @param name  The new name.
     * @param phone The new phone number.
     * @return True if valid, false otherwise with visual feedback.
     */
    private boolean validateUpdateInput(String name, String phone) {
        if (name.isEmpty()) {
            Toast.makeText(getContext(), R.string.profile_error_name_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        // Basic phone validation for 10 digits starting with 05
        if (phone.length() != 10 || !phone.startsWith("05")) {
            Toast.makeText(getContext(), R.string.profile_error_phone_invalid, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Updates the user profile in Firebase.
     *
     * @param name  The validated new name.
     * @param phone The validated new phone number.
     */
    private void performProfileUpdate(String name, String phone) {
        if (currentUser == null) return;

        // Check for network before performing operation
        if (getActivity() instanceof BaseActivity) {
            if (!((BaseActivity) getActivity()).isNetworkAvailable()) {
                Toast.makeText(getContext(), R.string.error_no_internet, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        currentUser.setName(name);
        currentUser.setPhoneNumber(phone);

        FBRef.refUsers.child(currentUser.getUid()).setValue(currentUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), R.string.profile_update_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), R.string.profile_update_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Signs out the user, clears local auth state, and returns to the login screen.
     */
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove Firebase listener to prevent memory leaks or crashes when fragment is destroyed
        if (userProfileListener != null && currentUser != null) {
            FBRef.refUsers.child(currentUser.getUid()).removeEventListener(userProfileListener);
        }
    }
}
