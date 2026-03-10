package com.dndmaster.models;

public class Item {
    private String name;
    private String description;
    private String type; // weapon, armor, potion, misc, quest
    private int quantity;
    private double weight;
    private int goldValue;
    private boolean equipped;
    private boolean magical;
    private String magicEffect;
    private String damage;
    private String damageType;
    private int acBonus;
    private String properties; // finesse, thrown, versatile, etc.

    public Item() {}

    public Item(String name, String type, int quantity) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public String getType() { return type; }
    public void setType(String v) { type = v; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int v) { quantity = v; }
    public double getWeight() { return weight; }
    public void setWeight(double v) { weight = v; }
    public int getGoldValue() { return goldValue; }
    public void setGoldValue(int v) { goldValue = v; }
    public boolean isEquipped() { return equipped; }
    public void setEquipped(boolean v) { equipped = v; }
    public boolean isMagical() { return magical; }
    public void setMagical(boolean v) { magical = v; }
    public String getMagicEffect() { return magicEffect; }
    public void setMagicEffect(String v) { magicEffect = v; }
    public String getDamage() { return damage; }
    public void setDamage(String v) { damage = v; }
    public String getDamageType() { return damageType; }
    public void setDamageType(String v) { damageType = v; }
    public int getAcBonus() { return acBonus; }
    public void setAcBonus(int v) { acBonus = v; }
    public String getProperties() { return properties; }
    public void setProperties(String v) { properties = v; }
}
