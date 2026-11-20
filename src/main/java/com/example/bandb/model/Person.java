package com.example.bandb.model;

import java.util.Locale;

/**
 * ABSTRACTION + ENCAPSULATION
 * Base class of every account holder. It never stores a password:
 * passwords only exist (salted + hashed) inside the private
 * CustomerData service, so a Person object is safe to pass around.
 */
public abstract class Person {

    private String fullName;
    private final String username;
    private String email;

    protected Person(String fullName, String username, String email) {
        if (username == null
                || !username.trim().matches("[A-Za-z0-9_]{3,30}")) {
            throw new IllegalArgumentException(
                    "Username must contain 3 to 30 letters, "
                            + "numbers, or underscores.");
        }
        this.username = username.trim();
        setFullName(fullName);
        setEmail(email);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter your full name.");
        }
        this.fullName = fullName.trim();
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.trim().matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException(
                    "Please enter a valid email address.");
        }
        this.email = email.trim().toLowerCase(Locale.ROOT);
    }

    /** Each subclass states its own role (polymorphism). */
    public abstract String getRole();


    @Override
    public String toString() {
        // Deliberately leaves out the e-mail so it never leaks in logs.
        return getRole() + "[" + username + "]";
    }
}
