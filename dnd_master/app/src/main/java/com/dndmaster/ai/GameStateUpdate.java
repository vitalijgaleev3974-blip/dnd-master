package com.dndmaster.ai;

import java.util.ArrayList;
import java.util.List;

public class GameStateUpdate {

    public boolean combatStart = false;
    public boolean combatEnd = false;
    public String sceneUpdate = null;

    public List<XpAward> xpAwards = new ArrayList<>();
    public List<HpChange> hpChanges = new ArrayList<>();
    public List<ItemChange> itemsGained = new ArrayList<>();
    public List<ItemChange> itemsLost = new ArrayList<>();
    public List<ConditionChange> conditionsAdded = new ArrayList<>();
    public List<ConditionChange> conditionsRemoved = new ArrayList<>();
    public List<QuestUpdate> questUpdates = new ArrayList<>();
    public List<GoldChange> goldChanges = new ArrayList<>();
    public List<String> inspirationGrants = new ArrayList<>();

    public boolean hasChanges() {
        return !xpAwards.isEmpty() || !hpChanges.isEmpty() || !itemsGained.isEmpty()
                || !itemsLost.isEmpty() || !conditionsAdded.isEmpty() || !conditionsRemoved.isEmpty()
                || !questUpdates.isEmpty() || !goldChanges.isEmpty() || !inspirationGrants.isEmpty()
                || combatStart || combatEnd || sceneUpdate != null;
    }

    public void addXpAward(String charId, int xp, String reason) {
        xpAwards.add(new XpAward(charId, xp, reason));
    }

    public void addHpChange(String charId, int change, String type, String reason) {
        hpChanges.add(new HpChange(charId, change, type, reason));
    }

    public void addItemGain(String charId, String itemName, String itemType, int quantity) {
        itemsGained.add(new ItemChange(charId, itemName, itemType, quantity));
    }

    public void addCondition(String charId, String condition) {
        conditionsAdded.add(new ConditionChange(charId, condition));
    }

    public void removeCondition(String charId, String condition) {
        conditionsRemoved.add(new ConditionChange(charId, condition));
    }

    public void addQuestUpdate(String action, String questId, String title, String description, int objectiveIndex) {
        questUpdates.add(new QuestUpdate(action, questId, title, description, objectiveIndex));
    }

    public void addGoldChange(String charId, int amount) {
        goldChanges.add(new GoldChange(charId, amount));
    }

    public void addInspiration(String charId) {
        inspirationGrants.add(charId);
    }

    // ─── Inner Classes ────────────────────────────────────────────────────────

    public static class XpAward {
        public String characterId;
        public int xp;
        public String reason;
        public XpAward(String id, int xp, String reason) { characterId = id; this.xp = xp; this.reason = reason; }
    }

    public static class HpChange {
        public String characterId;
        public int change;
        public String type;
        public String reason;
        public HpChange(String id, int change, String type, String reason) {
            characterId = id; this.change = change; this.type = type; this.reason = reason;
        }
    }

    public static class ItemChange {
        public String characterId;
        public String itemName;
        public String itemType;
        public int quantity;
        public ItemChange(String id, String name, String type, int qty) {
            characterId = id; itemName = name; itemType = type; quantity = qty;
        }
    }

    public static class ConditionChange {
        public String characterId;
        public String condition;
        public ConditionChange(String id, String cond) { characterId = id; condition = cond; }
    }

    public static class QuestUpdate {
        public String action;
        public String questId;
        public String title;
        public String description;
        public int objectiveIndex;
        public QuestUpdate(String action, String id, String title, String desc, int idx) {
            this.action = action; questId = id; this.title = title; description = desc; objectiveIndex = idx;
        }
    }

    public static class GoldChange {
        public String characterId;
        public int amount;
        public GoldChange(String id, int amt) { characterId = id; amount = amt; }
    }
}
