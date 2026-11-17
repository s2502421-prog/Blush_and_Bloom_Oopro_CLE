package com.example.bandb;

public class Customer {

    private String fullName;
    private String username;
    private String email;
    private String password;

    public Customer(String fullName, String username,
                    String email, String password) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
