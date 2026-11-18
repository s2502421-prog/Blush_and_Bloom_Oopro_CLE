
package com.example.bandb.service;

import com.example.bandb.model.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

public class ProductData {

    private static final ArrayList<Product> products =
            new ArrayList<>();

    private static final Path FILE = Paths.get(
            System.getProperty("user.home"),
            "BlushAndBloom",
            "products.properties"
    );

    static {
        loadProducts();
    }

    private static void addDefaults() {

        String brand = "Blush & Bloom";

        products.add(new Foundation(
                1, "Glow Foundation", 399, 20, brand,
                "Porcelain", "#F4D9C9", "Fair"
        ));
        products.add(new Foundation(
                2, "Glow Foundation", 399, 20, brand,
                "Ivory", "#E8C3A6", "Light"
        ));
        products.add(new Foundation(
                3, "Glow Foundation", 399, 20, brand,
                "Natural Beige", "#C8956D", "Medium"
        ));
        products.add(new Foundation(
                4, "Glow Foundation", 399, 20, brand,
                "Warm Honey", "#B77747", "Tan"
        ));
        products.add(new Foundation(
                5, "Glow Foundation", 399, 20, brand,
                "Cocoa", "#805037", "Deep"
        ));
        products.add(new Foundation(
                6, "Glow Foundation", 399, 20, brand,
                "Espresso", "#4E3028", "Rich Deep"
        ));

        products.add(new Blush(
                7, "Dreamy Blush", 249, 15, brand,
                "Baby Pink", "#F5A9C0", "Fair"
        ));
        products.add(new Blush(
                8, "Dreamy Blush", 249, 15, brand,
                "Peach Pink", "#F5A38E", "Light"
        ));
        products.add(new Blush(
                9, "Dreamy Blush", 249, 15, brand,
                "Coral", "#ED846D", "Medium"
        ));
        products.add(new Blush(
                10, "Dreamy Blush", 249, 15, brand,
                "Terracotta", "#B96750", "Tan"
        ));
        products.add(new Blush(
                11, "Dreamy Blush", 249, 15, brand,
                "Berry", "#A44266", "Deep"
        ));
        products.add(new Blush(
                12, "Dreamy Blush", 249, 15, brand,
                "Plum", "#753C65", "Rich Deep"
        ));

        products.add(new Lipstick(
                13, "Bloom Kiss Lipstick", 199, 25, brand,
                "Soft Rose", "#C97987", "Fair"
        ));
        products.add(new Lipstick(
                14, "Bloom Kiss Lipstick", 199, 25, brand,
                "Pink Nude", "#B76E78", "Light"
        ));
        products.add(new Lipstick(
                15, "Bloom Kiss Lipstick", 199, 25, brand,
                "Rosewood", "#96535B", "Medium"
        ));
        products.add(new Lipstick(
                16, "Bloom Kiss Lipstick", 199, 25, brand,
                "Caramel Nude", "#8E4E3C", "Tan"
        ));
        products.add(new Lipstick(
                17, "Bloom Kiss Lipstick", 199, 25, brand,
                "Burgundy", "#70283E", "Deep"
        ));
        products.add(new Lipstick(
                18, "Bloom Kiss Lipstick", 199, 25, brand,
                "Deep Wine", "#512438", "Rich Deep"
        ));
    }

    public static synchronized ArrayList<Product>
    getAllProducts() {
        return new ArrayList<>(products);
    }

    public static synchronized Product getProductById(int id) {

        for (Product product : products) {
            if (product.getId() == id) {
                return product;
            }
        }

        return null;
    }

    public static synchronized void addProduct(Product product) {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null."
            );
        }

        if (!(product instanceof Makeup)) {
            throw new IllegalArgumentException(
                    "Only makeup products are supported."
            );
        }

        if (getProductById(product.getId()) != null) {
            throw new IllegalArgumentException(
                    "Product ID already exists."
            );
        }

        products.add(product);
        saveProducts();
    }

    public static synchronized void updateProduct(
            int id,
            String name,
            double price,
            int stock
    ) {

        Product product = getProductById(id);

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product not found."
            );
        }

        if (name == null || name.isBlank()
                || !Double.isFinite(price) || price <= 0
                || stock < 0) {
            throw new IllegalArgumentException(
                    "Please enter valid product details."
            );
        }

        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);

        saveProducts();
    }

    public static synchronized void setStock(
            int id,
            int stock
    ) {

        Product product = getProductById(id);

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product not found."
            );
        }

        product.setStock(stock);
        saveProducts();
    }

    public static synchronized void deleteProduct(int id) {

        boolean removed = products.removeIf(
                product -> product.getId() == id
        );

        if (!removed) {
            throw new IllegalArgumentException(
                    "Product not found."
            );
        }

        saveProducts();
    }

    public static synchronized void saveProducts() {

        Properties data = new Properties();

        data.setProperty(
                "count",
                String.valueOf(products.size())
        );

        for (int i = 0; i < products.size(); i++) {

            Product product = products.get(i);
            Makeup makeup = (Makeup) product;

            String key = "product." + i + ".";

            data.setProperty(
                    key + "id",
                    String.valueOf(product.getId())
            );
            data.setProperty(
                    key + "category",
                    product.getCategory()
            );
            data.setProperty(
                    key + "name",
                    product.getName()
            );
            data.setProperty(
                    key + "price",
                    String.valueOf(product.getPrice())
            );
            data.setProperty(
                    key + "stock",
                    String.valueOf(product.getStock())
            );
            data.setProperty(
                    key + "brand",
                    makeup.getBrand()
            );
            data.setProperty(
                    key + "shade",
                    makeup.getShade()
            );
            data.setProperty(
                    key + "color",
                    makeup.getShadeColor()
            );
            data.setProperty(
                    key + "tone",
                    makeup.getRecommendedSkinTone()
            );
        }

        try {
            Files.createDirectories(FILE.getParent());

            try (OutputStream output =
                         Files.newOutputStream(FILE)) {

                data.store(
                        output,
                        "Blush and Bloom Product Inventory"
                );
            }

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to save product inventory.",
                    exception
            );
        }
    }

    private static synchronized void loadProducts() {

        products.clear();

        if (!Files.exists(FILE)) {
            addDefaults();
            saveProducts();
            return;
        }

        Properties data = new Properties();

        try (InputStream input =
                     Files.newInputStream(FILE)) {

            data.load(input);

            int count = Integer.parseInt(
                    data.getProperty("count", "-1")
            );

            if (count < 0) {
                throw new IllegalArgumentException(
                        "Invalid product file."
                );
            }

            ArrayList<Product> loaded = new ArrayList<>();

            for (int i = 0; i < count; i++) {

                String key = "product." + i + ".";

                int id = Integer.parseInt(
                        data.getProperty(key + "id")
                );

                String category =
                        data.getProperty(key + "category");

                String name =
                        data.getProperty(key + "name");

                double price = Double.parseDouble(
                        data.getProperty(key + "price")
                );

                int stock = Integer.parseInt(
                        data.getProperty(key + "stock")
                );

                String brand =
                        data.getProperty(key + "brand");

                String shade =
                        data.getProperty(key + "shade");

                String color =
                        data.getProperty(key + "color");

                String tone =
                        data.getProperty(key + "tone");

                Product product;

                switch (category) {
                    case "Foundation":
                        product = new Foundation(
                                id, name, price, stock,
                                brand, shade, color, tone
                        );
                        break;

                    case "Blush":
                        product = new Blush(
                                id, name, price, stock,
                                brand, shade, color, tone
                        );
                        break;

                    case "Lipstick":
                        product = new Lipstick(
                                id, name, price, stock,
                                brand, shade, color, tone
                        );
                        break;

                    default:
                        throw new IllegalArgumentException(
                                "Unknown product category."
                        );
                }

                for (Product existing : loaded) {
                    if (existing.getId() == id) {
                        throw new IllegalArgumentException(
                                "Duplicate product ID."
                        );
                    }
                }

                loaded.add(product);
            }

            products.addAll(loaded);

        } catch (IOException | RuntimeException exception) {

            throw new IllegalStateException(
                    "Unable to load saved inventory from " +
                            FILE +
                            ". Check the file before continuing.",
                    exception
            );
        }
    }
}