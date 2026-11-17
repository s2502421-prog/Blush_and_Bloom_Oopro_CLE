
package com.example.bandb.service;

import com.example.bandb.model.*;
import java.util.ArrayList;

public class ProductData {

    private static final ArrayList<Product> products =
            new ArrayList<>();

    static {
        products.add(new Foundation(
                1, "Glow Foundation", 399, 20,
                "Blush & Bloom", "Ivory", "Fair"
        ));

        products.add(new Blush(
                2, "Soft Pink Blush", 249, 15,
                "Blush & Bloom", "Rose", "Light"
        ));

        products.add(new Lipstick(
                3, "Velvet Lipstick", 199, 25,
                "Blush & Bloom", "Cherry"
        ));
    }

    // READ - Get all products
    public static ArrayList<Product> getProducts() {
        return new ArrayList<>(products);
    }

    // CREATE - Add a new product
    public static void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null."
            );
        }

        for (Product existing : products) {
            if (existing.getId() == product.getId()) {
                throw new IllegalArgumentException(
                        "Product ID already exists."
                );
            }
        }

        products.add(product);
    }

    // UPDATE - Replace an existing product
    public static boolean updateProduct(Product updatedProduct) {
        if (updatedProduct == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null."
            );
        }

        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() ==
                    updatedProduct.getId()) {

                products.set(i, updatedProduct);
                return true;
            }
        }

        return false;
    }

    // DELETE - Remove a product
    public static boolean deleteProduct(int id) {
        return products.removeIf(
                product -> product.getId() == id
        );
    }
}
