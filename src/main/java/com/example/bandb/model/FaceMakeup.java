
package com.example.bandb.model;

public abstract class FaceMakeup extends Makeup {

    public FaceMakeup(
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
                id,
                name,
                price,
                stock,
                brand,
                shade,
                shadeColor,
                recommendedSkinTone
        );
    }

    public String getSuitableSkinTone() {
        return getRecommendedSkinTone();
    }
}
