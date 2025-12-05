package com.example.CafeteriaApp.Models;

public class UserData
{
    private String uid;
    private String name;
    private String email;
    private String username;
    private String phoneNumber;
    private String school;
    private String classRoom;

    public UserData()
    {

    }

    public UserData(String uid, String name, String email, String username, String phoneNumber, String school, String classRoom) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.school = school;
        this.classRoom = classRoom;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getSchool() {
        return school;
    }

    public String getClassRoom() {
        return classRoom;
    }
}
