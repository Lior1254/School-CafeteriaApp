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
import com.google.android.material.card.MaterialCardView;

public class ProfileFragment extends Fragment
{
    private MaterialCardView btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i,
                             @Nullable ViewGroup c,
                             @Nullable Bundle b)
    {
        return i.inflate(R.layout.fragment_profile, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the logout card view
        btnLogout = view.findViewById(R.id.btn_logout);

        // Set click listener for the entire card
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
        Intent intent = new Intent(getActivity(), LoginPage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
