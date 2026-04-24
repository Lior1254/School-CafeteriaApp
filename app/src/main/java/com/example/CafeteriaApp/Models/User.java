package com.example.CafeteriaApp.Models;

import java.io.Serializable;

/**
 * Data model representing a user in the system.
 * Contains profile information, school details, and access roles.
 */
public class User implements Serializable
{
    /** Role constants for access control */
    public static final int ROLE_USER = 0;
    public static final int ROLE_COOK = 1;
    public static final int ROLE_MANAGER = 3;

    private String uid;
    private String name;
    private String email;
    private String username;
    private String phoneNumber;
    private String school;
    private String classRoom;
    private int role = ROLE_USER;

    /**
     * Default constructor required for Firebase Realtime Database deserialization.
     */
    public User()
    {
    }

    /**
     * Full constructor to initialize a user with all attributes.
     * 
     * @param uid Unique identifier from Firebase Auth
     * @param name Full name of the user
     * @param email User's email address
     * @param username Chosen display name
     * @param phoneNumber Contact phone number
     * @param school School institution name
     * @param classRoom Class/Grade identifier
     * @param role User's authority level (User/Cook/Manager)
     */
    public User(String uid, String name, String email, String username, String phoneNumber,
                String school, String classRoom, int role)
    {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.school = school;
        this.classRoom = classRoom;
        this.role = role;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getClassRoom() { return classRoom; }
    public void setClassRoom(String classRoom) { this.classRoom = classRoom; }

    public int getRole() { return role; }
    public void setRole(int role) { this.role = role; }
}
