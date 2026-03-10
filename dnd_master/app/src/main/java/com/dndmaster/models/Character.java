package com.dndmaster.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Character {

    // Basic Info
    private String id;
    private String playerName;
    private String characterName;
    private String race;
    private String subrace;
    private String characterClass;
    private String subclass;
    private String background;
    private String alignment;
    private int level;
    private int experiencePoints;

    // Ability Scores
    private int strength;
    private int dexterity;
    private int constitution;
    private int intelligence;
    private int wisdom;
    private int charisma;

    // Combat Stats
    private int maxHitPoints;
    private int currentHitPoints;
    private int temporaryHitPoints;
    private int armorClass;
    private int speed;
    private int initiative;
    private int proficiencyBonus;

    // Hit Dice
    private String hitDice;
    private int hitDiceTotal;
    private int hitDiceUsed;

    // Death Saves
    private int deathSaveSuccesses;
    private int deathSaveFailures;

    // Skills & Proficiencies
    private List<String> skillProficiencies;
    private List<String> savingThrowProficiencies;
    private List<String> weaponProficiencies;
    private List<String> armorProficiencies;
    private List<String> toolProficiencies;
    private List<String> languages;

    // Features & Traits
    private List<String> features;
    private List<String> traits;
    private List<String> ideals;
    private List<String> bonds;
    private List<String> flaws;

    // Spellcasting
    private boolean isSpellcaster;
    private String spellcastingAbility;
    private int spellSaveDC;
    private int spellAttackBonus;
    private List<Spell> preparedSpells;
    private List<Spell> knownSpells;
    private Map<Integer, Integer> spellSlots; // level -> total
    private Map<Integer, Integer> usedSpellSlots; // level -> used

    // Inventory & Equipment
    private List<Item> inventory;
    private int goldPieces;
    private int silverPieces;
    private int copperPieces;
    private int electrumPieces;
    private int platinumPieces;

    // Conditions & Status
    private List<String> conditions;
    private List<String> statusEffects;
    private boolean isConcentrating;
    private String concentrationSpell;

    // Inspiration
    private boolean hasInspiration;

    // Notes
    private String backstory;
    private String notes;
    private String appearance;

    // Quests & Progress
    private List<Quest> activeQuests;
    private List<Quest> completedQuests;

    // Class resources (Ki, Rage, Sorcery Points, etc.)
    private Map<String, Integer> classResourcesMax;
    private Map<String, Integer> classResourcesUsed;

    // Exhaustion level (0-6)
    private int exhaustionLevel;

    public Character() {
        this.skillProficiencies = new ArrayList<>();
        this.savingThrowProficiencies = new ArrayList<>();
        this.weaponProficiencies = new ArrayList<>();
        this.armorProficiencies = new ArrayList<>();
        this.toolProficiencies = new ArrayList<>();
        this.languages = new ArrayList<>();
        this.features = new ArrayList<>();
        this.traits = new ArrayList<>();
        this.ideals = new ArrayList<>();
        this.bonds = new ArrayList<>();
        this.flaws = new ArrayList<>();
        this.preparedSpells = new ArrayList<>();
        this.knownSpells = new ArrayList<>();
        this.spellSlots = new HashMap<>();
        this.usedSpellSlots = new HashMap<>();
        this.inventory = new ArrayList<>();
        this.conditions = new ArrayList<>();
        this.statusEffects = new ArrayList<>();
        this.activeQuests = new ArrayList<>();
        this.completedQuests = new ArrayList<>();
        this.classResourcesMax = new HashMap<>();
        this.classResourcesUsed = new HashMap<>();
        this.level = 1;
        this.experiencePoints = 0;
        this.proficiencyBonus = 2;
    }

    // ─── Ability Modifiers ───────────────────────────────────────────────────

    public int getAbilityModifier(int score) {
        return (int) Math.floor((score - 10) / 2.0);
    }

    public int getStrengthModifier()     { return getAbilityModifier(strength); }
    public int getDexterityModifier()    { return getAbilityModifier(dexterity); }
    public int getConstitutionModifier() { return getAbilityModifier(constitution); }
    public int getIntelligenceModifier() { return getAbilityModifier(intelligence); }
    public int getWisdomModifier()       { return getAbilityModifier(wisdom); }
    public int getCharismaModifier()     { return getAbilityModifier(charisma); }

    // ─── Skill Checks ────────────────────────────────────────────────────────

    public int getSkillBonus(String skill) {
        int base = getSkillBaseModifier(skill);
        if (skillProficiencies.contains(skill)) {
            return base + proficiencyBonus;
        }
        return base;
    }

    private int getSkillBaseModifier(String skill) {
        switch (skill) {
            case "Athletics": return getStrengthModifier();
            case "Acrobatics": case "Sleight of Hand": case "Stealth": return getDexterityModifier();
            case "Arcana": case "History": case "Investigation": case "Nature": case "Religion": return getIntelligenceModifier();
            case "Animal Handling": case "Insight": case "Medicine": case "Perception": case "Survival": return getWisdomModifier();
            case "Deception": case "Intimidation": case "Performance": case "Persuasion": return getCharismaModifier();
            default: return 0;
        }
    }

    // ─── Saving Throws ───────────────────────────────────────────────────────

    public int getSavingThrow(String ability) {
        int base;
        switch (ability) {
            case "Strength": base = getStrengthModifier(); break;
            case "Dexterity": base = getDexterityModifier(); break;
            case "Constitution": base = getConstitutionModifier(); break;
            case "Intelligence": base = getIntelligenceModifier(); break;
            case "Wisdom": base = getWisdomModifier(); break;
            case "Charisma": base = getCharismaModifier(); break;
            default: base = 0;
        }
        if (savingThrowProficiencies.contains(ability)) {
            return base + proficiencyBonus;
        }
        return base;
    }

    // ─── Level & XP ──────────────────────────────────────────────────────────

    public void addExperience(int xp) {
        this.experiencePoints += xp;
        updateLevel();
        updateProficiencyBonus();
    }

    private void updateLevel() {
        int[] xpThresholds = {0, 300, 900, 2700, 6500, 14000, 23000, 34000,
                               48000, 64000, 85000, 100000, 120000, 140000,
                               165000, 195000, 225000, 265000, 305000, 355000};
        for (int i = xpThresholds.length - 1; i >= 0; i--) {
            if (experiencePoints >= xpThresholds[i]) {
                level = i + 1;
                break;
            }
        }
    }

    private void updateProficiencyBonus() {
        if (level <= 4) proficiencyBonus = 2;
        else if (level <= 8) proficiencyBonus = 3;
        else if (level <= 12) proficiencyBonus = 4;
        else if (level <= 16) proficiencyBonus = 5;
        else proficiencyBonus = 6;
    }

    // ─── HP Management ───────────────────────────────────────────────────────

    public void takeDamage(int damage) {
        if (temporaryHitPoints > 0) {
            if (temporaryHitPoints >= damage) {
                temporaryHitPoints -= damage;
                return;
            } else {
                damage -= temporaryHitPoints;
                temporaryHitPoints = 0;
            }
        }
        currentHitPoints = Math.max(0, currentHitPoints - damage);
    }

    public void heal(int amount) {
        currentHitPoints = Math.min(maxHitPoints, currentHitPoints + amount);
    }

    public boolean isUnconscious() { return currentHitPoints == 0; }
    public boolean isDead() { return deathSaveFailures >= 3; }

    // ─── Condition Management ────────────────────────────────────────────────

    public void addCondition(String condition) {
        if (!conditions.contains(condition)) conditions.add(condition);
    }

    public void removeCondition(String condition) {
        conditions.remove(condition);
    }

    public boolean hasCondition(String condition) {
        return conditions.contains(condition);
    }

    // ─── Spell Slots ─────────────────────────────────────────────────────────

    public boolean useSpellSlot(int level) {
        int used = usedSpellSlots.getOrDefault(level, 0);
        int max = spellSlots.getOrDefault(level, 0);
        if (used < max) {
            usedSpellSlots.put(level, used + 1);
            return true;
        }
        return false;
    }

    public void regainSpellSlots() {
        usedSpellSlots.clear();
    }

    // ─── Long/Short Rest ─────────────────────────────────────────────────────

    public void takeLongRest() {
        currentHitPoints = maxHitPoints;
        temporaryHitPoints = 0;
        hitDiceUsed = Math.max(0, hitDiceUsed - (hitDiceTotal / 2));
        regainSpellSlots();
        classResourcesUsed.clear();
        deathSaveSuccesses = 0;
        deathSaveFailures = 0;
        if (exhaustionLevel > 0) exhaustionLevel--;
        isConcentrating = false;
        concentrationSpell = null;
    }

    public void takeShortRest(int hitDiceToSpend) {
        for (int i = 0; i < hitDiceToSpend && hitDiceUsed < hitDiceTotal; i++) {
            hitDiceUsed++;
            // Healing is calculated by caller based on hit die type
        }
    }

    // ─── Serialization ───────────────────────────────────────────────────────

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static Character fromJson(String json) {
        return new Gson().fromJson(json, Character.class);
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }
    public String getRace() { return race; }
    public void setRace(String race) { this.race = race; }
    public String getSubrace() { return subrace; }
    public void setSubrace(String subrace) { this.subrace = subrace; }
    public String getCharacterClass() { return characterClass; }
    public void setCharacterClass(String characterClass) { this.characterClass = characterClass; }
    public String getSubclass() { return subclass; }
    public void setSubclass(String subclass) { this.subclass = subclass; }
    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }
    public String getAlignment() { return alignment; }
    public void setAlignment(String alignment) { this.alignment = alignment; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; updateProficiencyBonus(); }
    public int getExperiencePoints() { return experiencePoints; }
    public void setExperiencePoints(int xp) { this.experiencePoints = xp; }
    public int getStrength() { return strength; }
    public void setStrength(int v) { this.strength = v; }
    public int getDexterity() { return dexterity; }
    public void setDexterity(int v) { this.dexterity = v; }
    public int getConstitution() { return constitution; }
    public void setConstitution(int v) { this.constitution = v; }
    public int getIntelligence() { return intelligence; }
    public void setIntelligence(int v) { this.intelligence = v; }
    public int getWisdom() { return wisdom; }
    public void setWisdom(int v) { this.wisdom = v; }
    public int getCharisma() { return charisma; }
    public void setCharisma(int v) { this.charisma = v; }
    public int getMaxHitPoints() { return maxHitPoints; }
    public void setMaxHitPoints(int v) { this.maxHitPoints = v; }
    public int getCurrentHitPoints() { return currentHitPoints; }
    public void setCurrentHitPoints(int v) { this.currentHitPoints = v; }
    public int getTemporaryHitPoints() { return temporaryHitPoints; }
    public void setTemporaryHitPoints(int v) { this.temporaryHitPoints = v; }
    public int getArmorClass() { return armorClass; }
    public void setArmorClass(int v) { this.armorClass = v; }
    public int getSpeed() { return speed; }
    public void setSpeed(int v) { this.speed = v; }
    public int getInitiative() { return initiative; }
    public void setInitiative(int v) { this.initiative = v; }
    public int getProficiencyBonus() { return proficiencyBonus; }
    public void setProficiencyBonus(int v) { this.proficiencyBonus = v; }
    public String getHitDice() { return hitDice; }
    public void setHitDice(String v) { this.hitDice = v; }
    public int getHitDiceTotal() { return hitDiceTotal; }
    public void setHitDiceTotal(int v) { this.hitDiceTotal = v; }
    public int getHitDiceUsed() { return hitDiceUsed; }
    public void setHitDiceUsed(int v) { this.hitDiceUsed = v; }
    public int getDeathSaveSuccesses() { return deathSaveSuccesses; }
    public void setDeathSaveSuccesses(int v) { this.deathSaveSuccesses = v; }
    public int getDeathSaveFailures() { return deathSaveFailures; }
    public void setDeathSaveFailures(int v) { this.deathSaveFailures = v; }
    public List<String> getSkillProficiencies() { return skillProficiencies; }
    public void setSkillProficiencies(List<String> v) { this.skillProficiencies = v; }
    public List<String> getSavingThrowProficiencies() { return savingThrowProficiencies; }
    public void setSavingThrowProficiencies(List<String> v) { this.savingThrowProficiencies = v; }
    public List<String> getWeaponProficiencies() { return weaponProficiencies; }
    public void setWeaponProficiencies(List<String> v) { this.weaponProficiencies = v; }
    public List<String> getArmorProficiencies() { return armorProficiencies; }
    public void setArmorProficiencies(List<String> v) { this.armorProficiencies = v; }
    public List<String> getToolProficiencies() { return toolProficiencies; }
    public void setToolProficiencies(List<String> v) { this.toolProficiencies = v; }
    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> v) { this.languages = v; }
    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> v) { this.features = v; }
    public List<String> getConditions() { return conditions; }
    public void setConditions(List<String> v) { this.conditions = v; }
    public List<Item> getInventory() { return inventory; }
    public void setInventory(List<Item> v) { this.inventory = v; }
    public int getGoldPieces() { return goldPieces; }
    public void setGoldPieces(int v) { this.goldPieces = v; }
    public int getSilverPieces() { return silverPieces; }
    public void setSilverPieces(int v) { this.silverPieces = v; }
    public int getCopperPieces() { return copperPieces; }
    public void setCopperPieces(int v) { this.copperPieces = v; }
    public int getPlatinumPieces() { return platinumPieces; }
    public void setPlatinumPieces(int v) { this.platinumPieces = v; }
    public List<String> getStatusEffects() { return statusEffects; }
    public void setStatusEffects(List<String> v) { this.statusEffects = v; }
    public boolean isConcentrating() { return isConcentrating; }
    public void setConcentrating(boolean v) { this.isConcentrating = v; }
    public String getConcentrationSpell() { return concentrationSpell; }
    public void setConcentrationSpell(String v) { this.concentrationSpell = v; }
    public boolean isHasInspiration() { return hasInspiration; }
    public void setHasInspiration(boolean v) { this.hasInspiration = v; }
    public String getBackstory() { return backstory; }
    public void setBackstory(String v) { this.backstory = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { this.notes = v; }
    public String getAppearance() { return appearance; }
    public void setAppearance(String v) { this.appearance = v; }
    public List<Quest> getActiveQuests() { return activeQuests; }
    public void setActiveQuests(List<Quest> v) { this.activeQuests = v; }
    public List<Quest> getCompletedQuests() { return completedQuests; }
    public void setCompletedQuests(List<Quest> v) { this.completedQuests = v; }
    public List<String> getTraits() { return traits; }
    public void setTraits(List<String> v) { this.traits = v; }
    public List<String> getIdeals() { return ideals; }
    public void setIdeals(List<String> v) { this.ideals = v; }
    public List<String> getBonds() { return bonds; }
    public void setBonds(List<String> v) { this.bonds = v; }
    public List<String> getFlaws() { return flaws; }
    public void setFlaws(List<String> v) { this.flaws = v; }
    public boolean isSpellcaster() { return isSpellcaster; }
    public void setSpellcaster(boolean v) { this.isSpellcaster = v; }
    public String getSpellcastingAbility() { return spellcastingAbility; }
    public void setSpellcastingAbility(String v) { this.spellcastingAbility = v; }
    public int getSpellSaveDC() { return spellSaveDC; }
    public void setSpellSaveDC(int v) { this.spellSaveDC = v; }
    public int getSpellAttackBonus() { return spellAttackBonus; }
    public void setSpellAttackBonus(int v) { this.spellAttackBonus = v; }
    public List<Spell> getPreparedSpells() { return preparedSpells; }
    public void setPreparedSpells(List<Spell> v) { this.preparedSpells = v; }
    public List<Spell> getKnownSpells() { return knownSpells; }
    public void setKnownSpells(List<Spell> v) { this.knownSpells = v; }
    public Map<Integer, Integer> getSpellSlots() { return spellSlots; }
    public void setSpellSlots(Map<Integer, Integer> v) { this.spellSlots = v; }
    public Map<Integer, Integer> getUsedSpellSlots() { return usedSpellSlots; }
    public void setUsedSpellSlots(Map<Integer, Integer> v) { this.usedSpellSlots = v; }
    public Map<String, Integer> getClassResourcesMax() { return classResourcesMax; }
    public void setClassResourcesMax(Map<String, Integer> v) { this.classResourcesMax = v; }
    public Map<String, Integer> getClassResourcesUsed() { return classResourcesUsed; }
    public void setClassResourcesUsed(Map<String, Integer> v) { this.classResourcesUsed = v; }
    public int getExhaustionLevel() { return exhaustionLevel; }
    public void setExhaustionLevel(int v) { this.exhaustionLevel = Math.min(6, Math.max(0, v)); }
}
