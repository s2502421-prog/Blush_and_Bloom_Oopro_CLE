package com.example.bandb.service;

import com.example.bandb.model.Person;

/** Who is logged in right now. Only this class holds the current user. */
public final class Session {

    private static Person current;

    private Session() {
    }

    public static synchronized void start(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("A valid account is required.");
        }
        ShoppingCart.activateAccount(person.getUsername());
        current = person;
    }

    public static synchronized void end() {
        current = null;
        ShoppingCart.logout();
    }

    public static synchronized boolean isLoggedIn() {
        return current != null;
    }


    public static synchronized String getDisplayName() {
        return current == null ? "" : current.getFullName();
    }
}
