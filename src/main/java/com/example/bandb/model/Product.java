package com.example.bandb.model;

/**
 * ABSTRACTION - top of the product hierarchy
 * (Product -> Makeup -> FaceMakeup -> Foundation / Blush).
 * ENCAPSULATION - private fields, validation inside the setters.
 */
public abstract class Product {

    private final int id;
    private String name;
    private double price;
    private int stock;

    public Product(int id, String name, double price, int stock) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid product ID.");
        }
        this.id = id;
        setName(name);
        setPrice(price);
        setStock(stock);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Product name cannot be empty.");
        }
        this.name = name.trim();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (!Double.isFinite(price) || price <= 0) {
            throw new IllegalArgumentException(
                    "Price must be greater than zero.");
        }
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException(
                    "Stock cannot be negative.");
        }
        this.stock = stock;
    }

    /** Overridden by every concrete subclass (polymorphism). */
    public abstract String getCategory();
}
