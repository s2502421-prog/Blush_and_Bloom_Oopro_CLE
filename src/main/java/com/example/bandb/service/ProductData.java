package com.example.bandb.service;

import com.example.bandb.model.Blush;
import com.example.bandb.model.Foundation;
import com.example.bandb.model.Lipstick;
import com.example.bandb.model.Makeup;
import com.example.bandb.model.Product;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Product inventory (CRUD).
 *
 * POLYMORPHISM: one ArrayList<Product> holds Foundation, Blush and
 * Lipstick objects together; getCategory()/getFinish() behave
 * differently for each object in the list.
 */
public final class ProductData {

    private static final ArrayList<Product> products = new ArrayList<>();

    private static boolean loaded = false;

    private ProductData() {
    }

    private static Path file() {
        return SecureFiles.dataDirectory().resolve("products.properties");
    }

    // ------------------------------------------------------------- READ

    public static synchronized ArrayList<Product> getAllProducts() {
        ensureLoaded();
        return new ArrayList<>(products);
    }

    public static synchronized Product getProductById(int id) {
        ensureLoaded();

        for (Product product : products) {
            if (product.getId() == id) {
                return product;
            }
        }

        return null;
    }

    public static synchronized int nextId() {
        ensureLoaded();

        int max = 0;

        for (Product product : products) {
            max = Math.max(max, product.getId());
        }

        return max + 1;
    }

    // ----------------------------------------------------------- CREATE

    public static synchronized void addProduct(Product product) {
        ensureLoaded();

        if (!(product instanceof Makeup)) {
            throw new IllegalArgumentException(
                    "Only makeup products are supported.");
        }

        if (getProductById(product.getId()) != null) {
            throw new IllegalArgumentException("Product ID already exists.");
        }

        rejectDuplicate((Makeup) product, -1);

        products.add(product);

        try {
            saveProducts();
        } catch (RuntimeException exception) {
            products.remove(product);
            throw exception;
        }
    }

    // ----------------------------------------------------------- UPDATE

    public static synchronized void updateMakeup(
            int id,
            String name,
            double price,
            int stock,
            String brand,
            String shade,
            String shadeColor,
            String skinTone
    ) {
        ensureLoaded();

        Product found = getProductById(id);

        if (!(found instanceof Makeup)) {
            throw new IllegalArgumentException("Product not found.");
        }

        Makeup makeup = (Makeup) found;

        String oldName = makeup.getName();
        double oldPrice = makeup.getPrice();
        int oldStock = makeup.getStock();
        String oldBrand = makeup.getBrand();
        String oldShade = makeup.getShade();
        String oldColor = makeup.getShadeColor();
        String oldTone = makeup.getRecommendedSkinTone();

        try {
            makeup.setName(name);
            makeup.setPrice(price);
            makeup.setStock(stock);
            makeup.setBrand(brand);
            makeup.setShade(shade);
            makeup.setShadeColor(shadeColor);
            makeup.setRecommendedSkinTone(skinTone);

            rejectDuplicate(makeup, id);
            saveProducts();

        } catch (RuntimeException exception) {
            makeup.setName(oldName);
            makeup.setPrice(oldPrice);
            makeup.setStock(oldStock);
            makeup.setBrand(oldBrand);
            makeup.setShade(oldShade);
            makeup.setShadeColor(oldColor);
            makeup.setRecommendedSkinTone(oldTone);
            throw exception;
        }
    }

    // ----------------------------------------------------------- DELETE

    public static synchronized void deleteProduct(int id) {
        ensureLoaded();

        Product product = getProductById(id);

        if (product == null) {
            throw new IllegalArgumentException("Product not found.");
        }

        int index = products.indexOf(product);
        products.remove(index);

        try {
            saveProducts();
        } catch (RuntimeException exception) {
            products.add(index, product);
            throw exception;
        }
    }

    // ------------------------------------------------------------ STOCK

    /** Takes stock for an order. Each line is {productId, quantity}. */
    public static synchronized void deductStock(List<int[]> lines) {
        ensureLoaded();

        for (int[] line : lines) {
            Product product = getProductById(line[0]);

            if (product == null) {
                throw new IllegalStateException(
                        "A product in your cart is no longer available. "
                                + "Please remove it and try again.");
            }

            if (product.getStock() < line[1]) {
                String label = product instanceof Makeup
                        ? product.getName() + " ("
                        + ((Makeup) product).getShade() + ")"
                        : product.getName();

                throw new IllegalStateException(
                        "Only " + product.getStock() + " left of "
                                + label + ". Please lower the quantity.");
            }
        }

        for (int[] line : lines) {
            Product product = getProductById(line[0]);
            product.setStock(product.getStock() - line[1]);
        }

        try {
            saveProducts();
        } catch (RuntimeException exception) {
            restoreStockInMemory(lines);
            throw exception;
        }
    }

    /** Puts stock back when an order could not be completed. */
    public static synchronized void restoreStock(List<int[]> lines) {
        restoreStockInMemory(lines);

        try {
            saveProducts();
        } catch (RuntimeException ignored) {
            // The in-memory stock is already correct.
        }
    }

    private static void restoreStockInMemory(List<int[]> lines) {
        for (int[] line : lines) {
            Product product = getProductById(line[0]);

            if (product != null) {
                product.setStock(product.getStock() + line[1]);
            }
        }
    }

    // ------------------------------------------------------ VALIDATION

    private static void rejectDuplicate(Makeup candidate, int ignoreId) {
        for (Product other : products) {
            if (other.getId() == ignoreId || other == candidate
                    || !(other instanceof Makeup)) {
                continue;
            }

            Makeup makeup = (Makeup) other;

            if (makeup.getCategory().equals(candidate.getCategory())
                    && makeup.getName().equalsIgnoreCase(candidate.getName())
                    && makeup.getShade()
                    .equalsIgnoreCase(candidate.getShade())) {
                throw new IllegalArgumentException(
                        "That " + candidate.getCategory().toLowerCase()
                                + " shade already exists.");
            }
        }
    }

    // ------------------------------------------------------ PERSISTENCE

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }

        products.clear();

        Path path = file();

        if (Files.exists(path)) {
            try {
                products.addAll(readFile(path));
                loaded = true;
                return;

            } catch (RuntimeException exception) {
                // Never let a damaged file break every button in the app:
                // keep a copy of it and start again from the defaults.
                try {
                    Files.move(path, path.resolveSibling(
                            "products.properties.corrupt-"
                                    + System.currentTimeMillis()));
                } catch (IOException backupFailure) {
                    throw new IllegalStateException(
                            "Inventory could not be read and its backup failed. "
                                    + "Original inventory was not overwritten.", backupFailure);
                }
            }
        }

        products.clear();
        addDefaults();
        loaded = true;

        try {
            saveProducts();
        } catch (RuntimeException ignored) {
            // The app still works from memory.
        }
    }

    private static ArrayList<Product> readFile(Path path) {
        Properties data = SecureFiles.readProperties(path);
        int count = Integer.parseInt(data.getProperty("count", "-1"));

        if (count < 0) {
            throw new IllegalArgumentException("Invalid product file.");
        }

        ArrayList<Product> loadedProducts = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String key = "product." + i + ".";

            int id = Integer.parseInt(data.getProperty(key + "id"));
            String category = data.getProperty(key + "category");
            String name = data.getProperty(key + "name");
            double price =
                    Double.parseDouble(data.getProperty(key + "price"));
            int stock = Integer.parseInt(data.getProperty(key + "stock"));
            String brand = data.getProperty(key + "brand");
            String shade = data.getProperty(key + "shade");
            String color = data.getProperty(key + "color");
            String tone = data.getProperty(key + "tone");

            Product product = create(
                    category, id, name, price, stock,
                    brand, shade, color, tone);

            for (Product existing : loadedProducts) {
                if (existing.getId() == id) {
                    throw new IllegalArgumentException("Duplicate product ID.");
                }
            }

            loadedProducts.add(product);
        }

        return loadedProducts;
    }

    /** Factory: picks the right subclass for the category name. */
    public static Makeup create(
            String category, int id, String name, double price, int stock,
            String brand, String shade, String color, String tone
    ) {
        if (category == null) {
            throw new IllegalArgumentException("Please choose a category.");
        }

        switch (category) {
            case "Foundation":
                return new Foundation(
                        id, name, price, stock, brand, shade, color, tone);
            case "Blush":
                return new Blush(
                        id, name, price, stock, brand, shade, color, tone);
            case "Lipstick":
                return new Lipstick(
                        id, name, price, stock, brand, shade, color, tone);
            default:
                throw new IllegalArgumentException(
                        "Unknown product category.");
        }
    }

    public static synchronized void saveProducts() {
        Properties data = new Properties();
        data.setProperty("count", String.valueOf(products.size()));

        for (int i = 0; i < products.size(); i++) {
            Makeup makeup = (Makeup) products.get(i);
            String key = "product." + i + ".";

            data.setProperty(key + "id", String.valueOf(makeup.getId()));
            data.setProperty(key + "category", makeup.getCategory());
            data.setProperty(key + "name", makeup.getName());
            data.setProperty(key + "price",
                    String.valueOf(makeup.getPrice()));
            data.setProperty(key + "stock",
                    String.valueOf(makeup.getStock()));
            data.setProperty(key + "brand", makeup.getBrand());
            data.setProperty(key + "shade", makeup.getShade());
            data.setProperty(key + "color", makeup.getShadeColor());
            data.setProperty(key + "tone",
                    makeup.getRecommendedSkinTone());
        }

        SecureFiles.writeProperties(
                file(), data, "Blush and Bloom Product Inventory");
    }

    private static void addDefaults() {
        String brand = "Blush & Bloom";

        String[][] foundation = {
                {"Porcelain", "#F4D9C9", "Fair"},
                {"Ivory", "#E8C3A6", "Light"},
                {"Natural Beige", "#C8956D", "Medium"},
                {"Warm Honey", "#B77747", "Tan"},
                {"Cocoa", "#805037", "Deep"},
                {"Espresso", "#4E3028", "Rich Deep"}
        };

        String[][] blush = {
                {"Baby Pink", "#F5A9C0", "Fair"},
                {"Peach Pink", "#F5A38E", "Light"},
                {"Coral", "#ED846D", "Medium"},
                {"Terracotta", "#B96750", "Tan"},
                {"Berry", "#A44266", "Deep"},
                {"Plum", "#753C65", "Rich Deep"}
        };

        String[][] lipstick = {
                {"Soft Rose", "#C97987", "Fair"},
                {"Pink Nude", "#B76E78", "Light"},
                {"Rosewood", "#96535B", "Medium"},
                {"Caramel Nude", "#8E4E3C", "Tan"},
                {"Burgundy", "#70283E", "Deep"},
                {"Deep Wine", "#512438", "Rich Deep"}
        };

        int id = 1;

        for (String[] row : foundation) {
            products.add(new Foundation(id++, "Glow Foundation", 399, 20,
                    brand, row[0], row[1], row[2]));
        }

        for (String[] row : blush) {
            products.add(new Blush(id++, "Dreamy Blush", 249, 15,
                    brand, row[0], row[1], row[2]));
        }

        for (String[] row : lipstick) {
            products.add(new Lipstick(id++, "Bloom Kiss Lipstick", 199, 25,
                    brand, row[0], row[1], row[2]));
        }
    }
}
