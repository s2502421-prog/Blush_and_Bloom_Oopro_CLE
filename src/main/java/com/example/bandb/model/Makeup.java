package com.example.bandb.model;

/** Second level of the hierarchy: adds shade information. */
public abstract class Makeup extends Product {

    private String brand;
    private String shade;
    private String shadeColor;
    private String recommendedSkinTone;

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
        setBrand(brand);
        setShade(shade);
        setShadeColor(shadeColor);
        setRecommendedSkinTone(recommendedSkinTone);
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = requireText(brand, "Brand");
    }

    public String getShade() {
        return shade;
    }

    public void setShade(String shade) {
        this.shade = requireText(shade, "Shade");
    }

    public String getShadeColor() {
        return shadeColor;
    }

    public void setShadeColor(String shadeColor) {
        if (shadeColor == null
                || !shadeColor.trim().matches("#[0-9A-Fa-f]{6}")) {
            throw new IllegalArgumentException(
                    "Shade color must be a hex code such as #F4D9C9.");
        }
        this.shadeColor = shadeColor.trim().toUpperCase();
    }

    public String getRecommendedSkinTone() {
        return recommendedSkinTone;
    }

    public void setRecommendedSkinTone(String tone) {
        this.recommendedSkinTone = requireText(tone, "Skin tone");
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    label + " cannot be empty.");
        }
        return value.trim();
    }

    /** Each concrete product reports its own finish (polymorphism). */
    public abstract String getFinish();
}
