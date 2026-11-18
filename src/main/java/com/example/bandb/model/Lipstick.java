
package com.example.bandb.model;

public class Lipstick extends Makeup {

    public Lipstick(
            int id,
            String name,
            double price,
            int stock,
            String brand,
            String shade,
            String shadeColor,
            String recommendedSkinTone
    ) {
        super(
                id, name, price, stock, brand,
                shade, shadeColor, recommendedSkinTone
        );
    }

    @Override
    public String getCategory() {
        return "Lipstick";
    }

    @Override
    public String getFinish() {
        return "Cream";
    }
}
