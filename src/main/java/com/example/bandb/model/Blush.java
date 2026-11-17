
package com.example.bandb.model;

public class Blush extends FaceMakeup {

    public Blush(int id, String name,
                 double price, int stock,
                 String brand, String shade,
                 String suitableSkinTone) {

        super(id, name, price, stock,
                brand, shade, suitableSkinTone);
    }

    @Override
    public String getCategory() {
        return "Blush";
    }

    @Override
    public String getFinish() {
        return "Matte";
    }
}
