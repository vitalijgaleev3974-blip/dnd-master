package com.dndmaster.models;

public class Spell {
    private String name;
    private int level; // 0 = cantrip
    private String school;
    private String castingTime;
    private String range;
    private String components;
    private String duration;
    private boolean concentration;
    private boolean ritual;
    private String description;
    private String higherLevels;
    private String damageType;
    private String damageDice;
    private String savingThrow;

    public Spell() {}

    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public int getLevel() { return level; }
    public void setLevel(int v) { level = v; }
    public String getSchool() { return school; }
    public void setSchool(String v) { school = v; }
    public String getCastingTime() { return castingTime; }
    public void setCastingTime(String v) { castingTime = v; }
    public String getRange() { return range; }
    public void setRange(String v) { range = v; }
    public String getComponents() { return components; }
    public void setComponents(String v) { components = v; }
    public String getDuration() { return duration; }
    public void setDuration(String v) { duration = v; }
    public boolean isConcentration() { return concentration; }
    public void setConcentration(boolean v) { concentration = v; }
    public boolean isRitual() { return ritual; }
    public void setRitual(boolean v) { ritual = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public String getHigherLevels() { return higherLevels; }
    public void setHigherLevels(String v) { higherLevels = v; }
    public String getDamageType() { return damageType; }
    public void setDamageType(String v) { damageType = v; }
    public String getDamageDice() { return damageDice; }
    public void setDamageDice(String v) { damageDice = v; }
    public String getSavingThrow() { return savingThrow; }
    public void setSavingThrow(String v) { savingThrow = v; }
}
