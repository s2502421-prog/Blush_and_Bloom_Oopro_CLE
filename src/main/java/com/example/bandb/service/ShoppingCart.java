
package com.example.bandb.service;

import com.example.bandb.model.Makeup;
import com.example.bandb.model.Product;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class ShoppingCart {

    public static final int MAX_QUANTITY = 20;

    private static final ArrayList<CartItem> items = new ArrayList<>();
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy  •  h:mm a", Locale.ENGLISH);

    private static String currentUsername;

    public static class CartItem {
        private final int productId;
        private final String category;
        private final String productName;
        private final String shade;
        private final double price;
        private int quantity;

        private CartItem(Makeup product, int quantity) {
            if (product == null) {
                throw new IllegalArgumentException("Product not found.");
            }
            if (quantity < 1 || quantity > MAX_QUANTITY) {
                throw new IllegalArgumentException("Quantity must be between 1 and 20.");
            }
            this.productId = product.getId();
            this.category = product.getCategory();
            this.productName = product.getName();
            this.shade = product.getShade();
            this.price = product.getPrice();
            this.quantity = quantity;
        }

        public int getProductId() { return productId; }
        public String getCategory() { return category; }
        public String getProductName() { return productName; }
        public String getShade() { return shade; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public double getSubtotal() { return price * quantity; }
        public int getAvailableStock() {
            Product product = ProductData.getProductById(productId);
            return product == null ? 0 : product.getStock();
        }
        /** Highest quantity allowed: 20 per order, or what is in stock. */
        public int getMaxAllowed() {
            return Math.min(MAX_QUANTITY, getAvailableStock());
        }

        public void setQuantity(int quantity) {
            if (!ShoppingCart.updateQuantity(this, quantity)) {
                throw new IllegalArgumentException("That is the most you can order of this shade.");
            }
        }

        public boolean increaseQuantity() {
            return ShoppingCart.updateQuantity(this, quantity + 1);
        }

        public boolean decreaseQuantity() {
            return ShoppingCart.updateQuantity(this, quantity - 1);
        }
    }

    public static synchronized void activateAccount(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("A valid customer account is required.");
        }
        String normalized = username.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals(currentUsername)) {
            return;
        }
        ArrayList<CartItem> loaded = loadCartFor(normalized);
        items.clear();
        items.addAll(loaded);
        currentUsername = normalized;
    }

    public static synchronized void logout() {
        items.clear();
        currentUsername = null;
    }

    public static synchronized boolean isLoggedIn() {
        return currentUsername != null;
    }

    public static synchronized String getCurrentUsername() {
        return currentUsername;
    }

    private static void requireLogin() {
        if (currentUsername == null) {
            throw new IllegalStateException("Please log in to access your shopping cart.");
        }
    }

    private static String accountKey(String username) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(username.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte value : hash) {
                result.append(String.format("%02x", value & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Unable to identify customer data.", exception);
        }
    }

    private static Path cartFile(String username) {
        return SecureFiles.dataDirectory().resolve("carts")
                .resolve(accountKey(username) + ".properties");
    }

    private static Path historyFile(String username) {
        return SecureFiles.dataDirectory().resolve("orders")
                .resolve(accountKey(username) + ".properties");
    }

    private static Makeup findProduct(String category, String name, String shade) {
        if (category == null || name == null || shade == null) {
            return null;
        }
        for (Product product : ProductData.getAllProducts()) {
            if (product instanceof Makeup) {
                Makeup makeup = (Makeup) product;
                if (makeup.getCategory().equals(category)
                        && makeup.getName().equals(name)
                        && makeup.getShade().equals(shade)) {
                    return makeup;
                }
            }
        }
        return null;
    }

    public static synchronized boolean addItem(
            String category, String productName, String shade, double price
    ) {
        requireLogin();
        if (category == null || category.isBlank()
                || productName == null || productName.isBlank()
                || shade == null || shade.isBlank()
                || !Double.isFinite(price) || price <= 0) {
            throw new IllegalArgumentException("Invalid product information.");
        }
        Makeup product = findProduct(category, productName, shade);
        if (product == null) {
            throw new IllegalArgumentException("Product not found.");
        }
        if (Double.compare(product.getPrice(), price) != 0) {
            throw new IllegalArgumentException("Product price has changed.");
        }
        for (CartItem item : items) {
            if (item.getProductId() == product.getId()) {
                return updateQuantity(item, item.getQuantity() + 1);
            }
        }
        if (product.getStock() < 1) {
            return false;
        }
        CartItem newItem = new CartItem(product, 1);
        items.add(newItem);
        try {
            saveCart();
        } catch (RuntimeException exception) {
            items.remove(newItem);
            throw exception;
        }
        return true;
    }

    public static synchronized List<CartItem> getItems() {
        requireLogin();
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public static synchronized boolean updateQuantity(CartItem item, int quantity) {
        requireLogin();
        if (item == null || !items.contains(item)
                || quantity < 1 || quantity > MAX_QUANTITY) {
            return false;
        }
        if (quantity > item.quantity && quantity > item.getAvailableStock()) {
            return false;
        }
        int old = item.quantity;
        item.quantity = quantity;
        try {
            saveCart();
        } catch (RuntimeException exception) {
            item.quantity = old;
            throw exception;
        }
        return true;
    }

    public static synchronized void removeItem(CartItem item) {
        requireLogin();
        int index = items.indexOf(item);
        if (index < 0) {
            return;
        }
        items.remove(index);
        try {
            saveCart();
        } catch (RuntimeException exception) {
            items.add(index, item);
            throw exception;
        }
    }

    public static synchronized int getTotalQuantity() {
        requireLogin();
        int total = 0;
        for (CartItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    public static synchronized double getTotalPrice() {
        requireLogin();
        double total = 0;
        for (CartItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    public static synchronized boolean isEmpty() {
        requireLogin();
        return items.isEmpty();
    }

    public static synchronized void clear() {
        requireLogin();
        ArrayList<CartItem> backup = new ArrayList<>(items);
        items.clear();
        try {
            saveCart();
        } catch (RuntimeException exception) {
            items.addAll(backup);
            throw exception;
        }
    }

    private static void writeProperties(Path file, Properties data, String description) {
        SecureFiles.writeProperties(file, data, description);
    }

    private static Properties readProperties(Path file) {
        return SecureFiles.readProperties(file);
    }

    public static synchronized void saveCart() {
        requireLogin();
        Properties data = new Properties();
        data.setProperty("username", currentUsername);
        data.setProperty("count", String.valueOf(items.size()));
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            String key = "item." + i + ".";
            data.setProperty(key + "id", String.valueOf(item.getProductId()));
            data.setProperty(key + "quantity", String.valueOf(item.getQuantity()));
        }
        writeProperties(cartFile(currentUsername), data,
                "Blush and Bloom Customer Shopping Cart");
    }

    private static ArrayList<CartItem> loadCartFor(String username) {
        ArrayList<CartItem> loaded = new ArrayList<>();
        Path file = cartFile(username);
        if (Files.notExists(file)) {
            return loaded;
        }
        Properties data = readProperties(file);
        if (!username.equals(data.getProperty("username"))) {
            throw new IllegalStateException("The saved cart belongs to another account.");
        }
        try {
            int count = Integer.parseInt(data.getProperty("count", "-1"));
            if (count < 0 || count > 10000) {
                throw new IllegalArgumentException("Invalid saved cart.");
            }
            for (int i = 0; i < count; i++) {
                String key = "item." + i + ".";
                int id = Integer.parseInt(data.getProperty(key + "id"));
                int quantity = Integer.parseInt(data.getProperty(key + "quantity"));
                Product product = ProductData.getProductById(id);
                if (!(product instanceof Makeup) || quantity < 1) {
                    continue;
                }
                Makeup makeup = (Makeup) product;
                int safeQuantity = Math.min(quantity, MAX_QUANTITY);
                CartItem existing = null;
                for (CartItem item : loaded) {
                    if (item.getProductId() == id) {
                        existing = item;
                        break;
                    }
                }
                if (existing == null) {
                    loaded.add(new CartItem(makeup, safeQuantity));
                } else {
                    existing.quantity = Math.min(
                            MAX_QUANTITY, existing.quantity + safeQuantity);
                }
            }
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Unable to load the saved cart for "
                    + username + ".", exception);
        }
        return loaded;
    }

    private static Properties readHistory() {
        requireLogin();
        Path file = historyFile(currentUsername);
        Properties data = readProperties(file);
        if (Files.exists(file) && !currentUsername.equals(data.getProperty("username"))) {
            throw new IllegalStateException("Order history belongs to another customer.");
        }
        return data;
    }

    public static synchronized void placeOrder() {
        requireLogin();
        if (items.isEmpty()) {
            throw new IllegalStateException("Please add products before checkout.");
        }

        Properties history = readHistory();
        Properties original = new Properties();
        original.putAll(history);
        int orderCount;
        try {
            orderCount = Integer.parseInt(history.getProperty("count", "0"));
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Saved order history is invalid.", exception);
        }
        if (orderCount < 0 || orderCount == Integer.MAX_VALUE) {
            throw new IllegalStateException("Saved order history is invalid.");
        }

        String prefix = "order." + orderCount + ".";
        history.setProperty("username", currentUsername);
        history.setProperty("count", String.valueOf(orderCount + 1));
        history.setProperty(prefix + "number", String.valueOf(orderCount + 1));
        history.setProperty(prefix + "date", LocalDateTime.now().toString());
        history.setProperty(prefix + "total", Double.toString(getTotalPrice()));
        history.setProperty(prefix + "itemCount", String.valueOf(items.size()));
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            String key = prefix + "item." + i + ".";
            history.setProperty(key + "name", item.getProductName());
            history.setProperty(key + "category", item.getCategory());
            history.setProperty(key + "shade", item.getShade());
            history.setProperty(key + "quantity", String.valueOf(item.getQuantity()));
            history.setProperty(key + "price", Double.toString(item.getPrice()));
        }

        // Take the stock first so two orders can never sell the same item.
        ArrayList<int[]> lines = new ArrayList<>();
        for (CartItem item : items) {
            lines.add(new int[]{item.getProductId(), item.getQuantity()});
        }
        ProductData.deductStock(lines);

        Path historyPath = historyFile(currentUsername);
        try {
            writeProperties(historyPath, history, "Blush and Bloom Order History");
            try {
                clear();
            } catch (RuntimeException exception) {
                try {
                    if (orderCount == 0 && original.isEmpty()) {
                        Files.deleteIfExists(historyPath);
                    } else {
                        writeProperties(historyPath, original, "Blush and Bloom Order History");
                    }
                } catch (IOException | RuntimeException rollbackFailure) {
                    exception.addSuppressed(rollbackFailure);
                }
                throw exception;
            }
        } catch (RuntimeException exception) {
            ProductData.restoreStock(lines);
            throw exception;
        }
    }

    public static synchronized List<String> getOrderHistory() {
        Properties data = readHistory();
        int count;
        try {
            count = Integer.parseInt(data.getProperty("count", "0"));
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Saved order history is invalid.", exception);
        }
        if (count < 0 || count > 100000) {
            throw new IllegalStateException("Saved order history is invalid.");
        }
        List<String> orders = new ArrayList<>();
        for (int i = count - 1; i >= 0; i--) {
            String prefix = "order." + i + ".";
            int number = Integer.parseInt(data.getProperty(prefix + "number"));
            String date = LocalDateTime.parse(data.getProperty(prefix + "date"))
                    .format(DATE_FORMAT);
            double total = Double.parseDouble(data.getProperty(prefix + "total"));
            int itemCount = Integer.parseInt(data.getProperty(prefix + "itemCount"));
            StringBuilder details = new StringBuilder();
            details.append(String.format("ORDER #%03d", number)).append("\n");
            details.append(date).append("\n\n");
            for (int j = 0; j < itemCount; j++) {
                String key = prefix + "item." + j + ".";
                String name = data.getProperty(key + "name", "Product");
                String shade = data.getProperty(key + "shade", "");
                int quantity = Integer.parseInt(data.getProperty(key + "quantity"));
                double price = Double.parseDouble(data.getProperty(key + "price"));
                details.append(name).append(" — ").append(shade)
                        .append("  × ").append(quantity).append("\n")
                        .append(String.format("₱%,.2f", price * quantity))
                        .append("\n");
            }
            details.append("\n").append(String.format("TOTAL  ₱%,.2f", total));
            orders.add(details.toString());
        }
        return Collections.unmodifiableList(orders);
    }
}
