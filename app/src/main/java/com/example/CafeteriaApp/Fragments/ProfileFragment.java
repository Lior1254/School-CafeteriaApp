package com.example.CafeteriaApp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.CafeteriaApp.Authentication.LoginPage;
import com.example.CafeteriaApp.Helpers.FBRef;
import com.example.CafeteriaApp.Helpers.FileManager;
import com.example.CafeteriaApp.R;

/**
 * Fragment responsible for displaying user profile information and settings.
 * Handles user logout and navigation to account-related actions.
 */
public class ProfileFragment extends Fragment
{
    private View btnLogout;

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    /**
     * Called immediately after onCreateView has returned, but before any saved state has been restored in to the view.
     * @param view The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the logout layout view
        btnLogout = view.findViewById(R.id.btn_logout);

        // Set click listener for the logout layout
        if (btnLogout != null) {
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logoutUser();
                }
            });
        }
    }

    /**
     * Handles the logout process: clears local session, signs out from Firebase, and navigates to LoginPage.
     */
    private void logoutUser() {
        // 1. Clear stored authentication data from SharedPreferences
        if (getContext() != null) {
            FileManager.clearUserAuthentication(requireContext());
        }

        // 2. Sign out from Firebase Authentication
        if (FBRef.refAuth != null) {
            FBRef.refAuth.signOut();
        }

        // 3. Navigate back to Login Page and clear activity stack
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
