package com.dndmaster.models;

import java.util.ArrayList;
import java.util.List;

public class Quest {
    private String id;
    private String title;
    private String description;
    private String status; // active, completed, failed
    private List<String> objectives;
    private List<Boolean> objectivesCompleted;
    private int xpReward;
    private String goldReward;
    private String itemReward;
    private String giver;
    private String location;
    private boolean isMainQuest;

    public Quest() {
        objectives = new ArrayList<>();
        objectivesCompleted = new ArrayList<>();
        status = "active";
    }

    public Quest(String id, String title, String description) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public void addObjective(String objective) {
        objectives.add(objective);
        objectivesCompleted.add(false);
    }

    public void completeObjective(int index) {
        if (index >= 0 && index < objectivesCompleted.size()) {
            objectivesCompleted.set(index, true);
        }
    }

    public boolean allObjectivesComplete() {
        for (Boolean b : objectivesCompleted) {
            if (!b) return false;
        }
        return !objectivesCompleted.isEmpty();
    }

    public String getId() { return id; }
    public void setId(String v) { id = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { title = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public List<String> getObjectives() { return objectives; }
    public void setObjectives(List<String> v) { objectives = v; }
    public List<Boolean> getObjectivesCompleted() { return objectivesCompleted; }
    public void setObjectivesCompleted(List<Boolean> v) { objectivesCompleted = v; }
    public int getXpReward() { return xpReward; }
    public void setXpReward(int v) { xpReward = v; }
    public String getGoldReward() { return goldReward; }
    public void setGoldReward(String v) { goldReward = v; }
    public String getItemReward() { return itemReward; }
    public void setItemReward(String v) { itemReward = v; }
    public String getGiver() { return giver; }
    public void setGiver(String v) { giver = v; }
    public String getLocation() { return location; }
    public void setLocation(String v) { location = v; }
    public boolean isMainQuest() { return isMainQuest; }
    public void setMainQuest(boolean v) { isMainQuest = v; }
}
