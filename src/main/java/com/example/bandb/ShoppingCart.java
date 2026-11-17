
package com.example.bandb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShoppingCart {

    public static class CartItem {
        private final String category;
        private final String productName;
        private final String shade;
        private final double price;
        private int quantity;

        public CartItem(String category, String productName,
                        String shade, double price) {
            this.category = category;
            this.productName = productName;
            this.shade = shade;
            this.price = price;
            this.quantity = 1;
        }

        public String getCategory() {
            return category;
        }

        public String getProductName() {
            return productName;
        }

        public String getShade() {
            return shade;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getSubtotal() {
            return price * quantity;
        }

        private void increaseQuantity() {
            quantity++;
        }

        public void setQuantity(int quantity) {
            if (quantity < 1) {
                throw new IllegalArgumentException(
                        "Quantity must be at least 1."
                );
            }
            this.quantity = quantity;
        }
    }

    private static final ArrayList<CartItem> items =
            new ArrayList<>();

    public static void addItem(String category,
                               String productName,
                               String shade,
                               double price) {

        for (CartItem item : items) {
            if (item.getProductName().equals(productName)
                    && item.getShade().equals(shade)) {
                item.increaseQuantity();
                return;
            }
        }

        items.add(new CartItem(
                category, productName, shade, price
        ));
    }

    public static List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public static int getTotalQuantity() {
        int total = 0;

        for (CartItem item : items) {
            total += item.getQuantity();
        }

        return total;
    }

    public static double getTotalPrice() {
        double total = 0;

        for (CartItem item : items) {
            total += item.getSubtotal();
        }

        return total;
    }

    public static void removeItem(CartItem item) {
        items.remove(item);
    }

    public static void clear() {
        items.clear();
    }
}
