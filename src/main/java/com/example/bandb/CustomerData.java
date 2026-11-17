package com.example.bandb;

import java.util.ArrayList;

public class CustomerData {

    private static final ArrayList<Customer> customers =
            new ArrayList<>();

    public static boolean register(Customer customer) {

        for (Customer existing : customers) {
            if (existing.getUsername().equalsIgnoreCase(
                    customer.getUsername())) {
                return false;
            }
        }

        customers.add(customer);
        return true;
    }

    public static Customer login(String username, String password) {

        for (Customer customer : customers) {

            if (customer.getUsername().equalsIgnoreCase(username)
                    && customer.getPassword().equals(password)) {
                return customer;
            }
        }

        return null;
    }
}