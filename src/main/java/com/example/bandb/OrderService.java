
package com.example.bandb.service;

import com.example.bandb.ShoppingCart;
import com.example.bandb.model.Makeup;
import com.example.bandb.model.Product;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderService {

    public static class OrderReceipt {
        private final int totalItems;
        private final double totalAmount;

        public OrderReceipt(int totalItems, double totalAmount) {
            this.totalItems = totalItems;
            this.totalAmount = totalAmount;
        }

        public int getTotalItems() {
            return totalItems;
        }

        public double getTotalAmount() {
            return totalAmount;
        }
    }

    public static synchronized OrderReceipt placeOrder() {

        List<ShoppingCart.CartItem> cart =
                ShoppingCart.getItems();

        if (cart.isEmpty()) {
            throw new IllegalArgumentException(
                    "Your shopping cart is empty."
            );
        }

        Map<Integer, Integer> ordered =
                new LinkedHashMap<>();

        Map<Integer, Integer> previousStock =
                new LinkedHashMap<>();

        int itemCount = 0;
        double total = 0;

        for (ShoppingCart.CartItem item : cart) {

            Makeup matching = null;

            for (Product product :
                    ProductData.getAllProducts()) {

                if (product instanceof Makeup) {

                    Makeup makeup = (Makeup) product;

                    if (makeup.getCategory().equals(item.getCategory())
                            && makeup.getName().equals(item.getProductName())
                            && makeup.getShade().equals(item.getShade())) {

                        matching = makeup;
                        break;
                    }
                }
            }

            if (matching == null) {
                throw new IllegalArgumentException(
                        item.getProductName() +
                                " is no longer available."
                );
            }

            if (Double.compare(
                    matching.getPrice(),
                    item.getPrice()
            ) != 0) {
                throw new IllegalArgumentException(
                        "The price of " + item.getProductName() +
                                " has changed. Please update your cart."
                );
            }

            int id = matching.getId();

            ordered.merge(
                    id,
                    item.getQuantity(),
                    Integer::sum
            );

            previousStock.put(
                    id,
                    matching.getStock()
            );

            itemCount += item.getQuantity();
            total += item.getSubtotal();
        }

        for (Map.Entry<Integer, Integer> entry :
                ordered.entrySet()) {

            int id = entry.getKey();
            int requested = entry.getValue();
            int available = previousStock.get(id);

            if (requested > available) {

                Product product =
                        ProductData.getProductById(id);

                throw new IllegalArgumentException(
                        "Not enough stock for " +
                                product.getName() +
                                ". Available: " + available +
                                ", requested: " + requested + "."
                );
            }
        }

        try {

            for (Map.Entry<Integer, Integer> entry :
                    ordered.entrySet()) {

                int id = entry.getKey();
                int remaining =
                        previousStock.get(id) -
                                entry.getValue();

                ProductData.setStock(id, remaining);
            }

            ShoppingCart.clear();

        } catch (RuntimeException exception) {

            for (Map.Entry<Integer, Integer> entry :
                    previousStock.entrySet()) {

                try {
                    ProductData.setStock(
                            entry.getKey(),
                            entry.getValue()
                    );
                } catch (RuntimeException ignored) {
                }
            }

            throw new IllegalStateException(
                    "The order could not be saved. " +
                            "Please try again.",
                    exception
            );
        }

        return new OrderReceipt(itemCount, total);
    }
}
