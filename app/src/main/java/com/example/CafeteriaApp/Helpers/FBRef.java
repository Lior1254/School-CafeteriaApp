package com.example.CafeteriaApp.Helpers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FBRef
{
    public static FirebaseDatabase FBDB = FirebaseDatabase.getInstance();

    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();
    public static DatabaseReference refUsers = FBDB.getReference("Users");
}
