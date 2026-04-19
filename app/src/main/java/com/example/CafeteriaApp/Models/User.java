package com.example.CafeteriaApp.Models;

import java.io.Serializable;

/**
 * Represents the user's profile data stored in the database.
 * Includes personal information like name, email, and school details.
 */
public class User implements Serializable
{
    public static final int ROLE_USER = 0;    // משתמש רגיל
    public static final int ROLE_COOK = 1;    // טבח
    public static final int ROLE_MANAGER = 3; // מנהל קפיטריה

    private String uid;
    private String name;
    private String email;
    private String username;
    private String phoneNumber;
    private String school;
    private String classRoom;
    private int role = ROLE_USER; // ברירת מחדל: משתמש רגיל (0)

    /**
     * Default constructor required for Firebase.
     */
    public User()
    {
    }

    /**
     * Constructs a UserData object with all details.
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
        this.role = ROLE_USER;
    }

    // Constructor with role
    public User(String uid, String name, String email, String username, String phoneNumber,
                String school, String classRoom, int role)
    {
        this(uid, name, email, username, phoneNumber, school, classRoom);
        this.role = role;
    }

    // Getters and Setters
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
