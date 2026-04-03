package com.example.CafeteriaApp.Models;

import java.io.Serializable;

/**
 * Represents the user's profile data stored in the database.
 * Includes personal information like name, email, and school details.
 */
public class User implements Serializable
{
    private String uid;
    private String name;
    private String email;
    private String username;
    private String phoneNumber;
    private String school;
    private String classRoom;

    /**
     * Default constructor required for Firebase.
     */
    public User()
    {
    }

    /**
     * Constructs a UserData object with all details.
     *
     * @param uid         Unique user ID from authentication.
     * @param name        Full name of the user.
     * @param email       Email address.
     * @param username    Chosen username.
     * @param phoneNumber Contact phone number.
     * @param school      Name of the school.
     * @param classRoom   Classroom identifier (e.g., "12-A").
     */
    public User(String uid, String name, String email, String username, String phoneNumber,
                String school, String classRoom)
    {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.school = school;
        this.classRoom = classRoom;
    }

    // Getters
    public String getUid()
    {
        return uid;
    }

    public String getName()
    {
        return name;
    }

    /**
     * Returns the first name by splitting the full name.
     * @return The first part of the name string.
     */
    public String getFirstName()
    {
        if (name == null || name.isEmpty()) return "";
        return name.split(" ")[0];
    }

    public String getEmail()
    {
        return email;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public String getSchool()
    {
        return school;
    }

    public String getClassRoom()
    {
        return classRoom;
    }
}
