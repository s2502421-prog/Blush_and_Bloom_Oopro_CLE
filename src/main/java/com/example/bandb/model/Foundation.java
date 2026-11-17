
package com.example.bandb.model;

public class Foundation extends FaceMakeup {

    public Foundation(int id, String name,
                      double price, int stock,
                      String brand, String shade,
                      String suitableSkinTone) {

        super(id, name, price, stock,
                brand, shade, suitableSkinTone);
    }

    @Override
    public String getCategory() {
        return "Foundation";
    }

    @Override
    public String getFinish() {
        return "Dewy";
    }
}
