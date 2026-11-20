package com.example.bandb.model;

/** A shopper. Inherits the validated profile data from Person. */
public class Customer extends Person {

    public static final String ROLE = "customer";

    public Customer(String fullName, String username, String email) {
        super(fullName, username, email);
    }

    @Override
    public String getRole() {
        return ROLE;
    }

}
