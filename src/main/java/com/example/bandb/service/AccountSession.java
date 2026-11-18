
package com.example.bandb.service;

import java.util.Locale;

public class AccountSession {

    private static String currentUsername;

    public static void login(String username) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter a valid username."
            );
        }

        if (!AccountStore.usernameExists(username)) {
            throw new IllegalArgumentException(
                    "This account does not exist."
            );
        }

        String registeredUsername =
                AccountStore.getUsername(username);

        if (registeredUsername == null) {
            throw new IllegalArgumentException(
                    "Unable to find your account."
            );
        }

        currentUsername = registeredUsername
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static boolean isLoggedIn() {
        return currentUsername != null;
    }

    public static void logout() {
        currentUsername = null;
    }
}