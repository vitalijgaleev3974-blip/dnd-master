package com.dndmaster.models;

public class CombatTurn {
    private String entityId;
    private String entityName;
    private int initiative;
    private boolean isPlayer;
    private boolean hasActed;
    private boolean hasBonusActed;
    private boolean hasReacted;
    private int remainingMovement;

    public CombatTurn() {}

    public CombatTurn(String id, String name, int initiative, boolean isPlayer) {
        this.entityId = id;
        this.entityName = name;
        this.initiative = initiative;
        this.isPlayer = isPlayer;
    }

    public void resetTurn(int speed) {
        hasActed = false;
        hasBonusActed = false;
        hasReacted = false;
        remainingMovement = speed;
    }

    public String getEntityId() { return entityId; }
    public void setEntityId(String v) { entityId = v; }
    public String getEntityName() { return entityName; }
    public void setEntityName(String v) { entityName = v; }
    public int getInitiative() { return initiative; }
    public void setInitiative(int v) { initiative = v; }
    public boolean isPlayer() { return isPlayer; }
    public void setPlayer(boolean v) { isPlayer = v; }
    public boolean isHasActed() { return hasActed; }
    public void setHasActed(boolean v) { hasActed = v; }
    public boolean isHasBonusActed() { return hasBonusActed; }
    public void setHasBonusActed(boolean v) { hasBonusActed = v; }
    public boolean isHasReacted() { return hasReacted; }
    public void setHasReacted(boolean v) { hasReacted = v; }
    public int getRemainingMovement() { return remainingMovement; }
    public void setRemainingMovement(int v) { remainingMovement = v; }
}
