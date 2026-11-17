
package com.example.bandb.model;

public abstract class Makeup extends Product {

    private String brand;
    private String shade;

    public Makeup(int id, String name,
                  double price, int stock,
                  String brand, String shade) {

        super(id, name, price, stock);

        setBrand(brand);
        setShade(shade);
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException(
                    "Brand cannot be empty."
            );
        }
        this.brand = brand.trim();
    }

    public String getShade() {
        return shade;
    }

    public void setShade(String shade) {
        if (shade == null || shade.isBlank()) {
            throw new IllegalArgumentException(
                    "Shade cannot be empty."
            );
        }
        this.shade = shade.trim();
    }

    public abstract String getFinish();
}
