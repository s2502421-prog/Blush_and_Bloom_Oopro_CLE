
package com.example.bandb.model;

public abstract class Makeup extends Product {

    private final String brand;
    private final String shade;
    private final String shadeColor;
    private final String recommendedSkinTone;

    public Makeup(
            int id,
            String name,
            double price,
            int stock,
            String brand,
            String shade,
            String shadeColor,
            String recommendedSkinTone
    ) {
        super(id, name, price, stock);

        if (brand == null || brand.isBlank()
                || shade == null || shade.isBlank()
                || shadeColor == null || shadeColor.isBlank()
                || recommendedSkinTone == null
                || recommendedSkinTone.isBlank()) {
            throw new IllegalArgumentException(
                    "Makeup details cannot be empty."
            );
        }

        this.brand = brand;
        this.shade = shade;
        this.shadeColor = shadeColor;
        this.recommendedSkinTone = recommendedSkinTone;
    }

    public String getBrand() {
        return brand;
    }

    public String getShade() {
        return shade;
    }

    public String getShadeColor() {
        return shadeColor;
    }

    public String getRecommendedSkinTone() {
        return recommendedSkinTone;
    }

    public abstract String getFinish();
}
