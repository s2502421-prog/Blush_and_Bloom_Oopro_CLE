
package com.example.bandb.model;

public abstract class FaceMakeup extends Makeup {

    private String suitableSkinTone;

    public FaceMakeup(int id, String name,
                      double price, int stock,
                      String brand, String shade,
                      String suitableSkinTone) {

        super(id, name, price, stock, brand, shade);

        setSuitableSkinTone(suitableSkinTone);
    }

    public String getSuitableSkinTone() {
        return suitableSkinTone;
    }

    public void setSuitableSkinTone(String suitableSkinTone) {
        if (suitableSkinTone == null ||
                suitableSkinTone.isBlank()) {
            throw new IllegalArgumentException(
                    "Suitable skin tone cannot be empty."
            );
        }

        this.suitableSkinTone = suitableSkinTone.trim();
    }

    public boolean matchesSkinTone(String skinTone) {
        return suitableSkinTone.equalsIgnoreCase(skinTone);
    }
}
