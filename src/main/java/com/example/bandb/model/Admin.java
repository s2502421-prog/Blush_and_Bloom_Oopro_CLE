package com.example.bandb.model;

/**
 * Shop staff. Can manage the product inventory but has NO method that
 * returns other customers' data, so customer records stay private.
 */
public class Admin extends Person {

    public static final String ROLE = "admin";

    public Admin(String fullName, String username, String email) {
        super(fullName, username, email);
    }

    @Override
    public String getRole() {
        return ROLE;
    }

    @Override
    public boolean canManageInventory() {
        return true;
    }
}
