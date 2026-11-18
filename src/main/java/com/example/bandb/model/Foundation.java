
package com.example.bandb.model;

public class Foundation extends FaceMakeup {

    public Foundation(
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
        return "Foundation";
    }

    @Override
    public String getFinish() {
        return "Natural";
    }
}
