package com.dndmaster.models;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class GameSession {
    private String id;
    private String name;
    private String aiProvider; // "claude" or "gemini"
    private String apiKey;
    private List<Character> characters;
    private List<ChatMessage> chatHistory;
    private String currentScene;
    private String worldState;
    private boolean combatActive;
    private List<CombatTurn> initiativeOrder;
    private int currentTurnIndex;
    private int round;
    private long createdAt;
    private long lastPlayedAt;

    public GameSession() {
        characters = new ArrayList<>();
        chatHistory = new ArrayList<>();
        initiativeOrder = new ArrayList<>();
        createdAt = System.currentTimeMillis();
        lastPlayedAt = createdAt;
        combatActive = false;
        round = 1;
        currentTurnIndex = 0;
    }

    public void addMessage(ChatMessage message) {
        chatHistory.add(message);
        lastPlayedAt = System.currentTimeMillis();
    }

    public void addCharacter(Character character) {
        characters.add(character);
    }

    public Character getCharacterById(String id) {
        for (Character c : characters) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    public void startCombat() {
        combatActive = true;
        round = 1;
        currentTurnIndex = 0;
    }

    public void endCombat() {
        combatActive = false;
        initiativeOrder.clear();
        round = 1;
        currentTurnIndex = 0;
    }

    public CombatTurn getCurrentTurn() {
        if (initiativeOrder.isEmpty() || currentTurnIndex >= initiativeOrder.size()) return null;
        return initiativeOrder.get(currentTurnIndex);
    }

    public void nextTurn() {
        currentTurnIndex++;
        if (currentTurnIndex >= initiativeOrder.size()) {
            currentTurnIndex = 0;
            round++;
        }
    }

    public String toJson() { return new Gson().toJson(this); }

    public static GameSession fromJson(String json) {
        return new Gson().fromJson(json, GameSession.class);
    }

    public String getId() { return id; }
    public void setId(String v) { id = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getAiProvider() { return aiProvider; }
    public void setAiProvider(String v) { aiProvider = v; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String v) { apiKey = v; }
    public List<Character> getCharacters() { return characters; }
    public void setCharacters(List<Character> v) { characters = v; }
    public List<ChatMessage> getChatHistory() { return chatHistory; }
    public void setChatHistory(List<ChatMessage> v) { chatHistory = v; }
    public String getCurrentScene() { return currentScene; }
    public void setCurrentScene(String v) { currentScene = v; }
    public String getWorldState() { return worldState; }
    public void setWorldState(String v) { worldState = v; }
    public boolean isCombatActive() { return combatActive; }
    public void setCombatActive(boolean v) { combatActive = v; }
    public List<CombatTurn> getInitiativeOrder() { return initiativeOrder; }
    public void setInitiativeOrder(List<CombatTurn> v) { initiativeOrder = v; }
    public int getCurrentTurnIndex() { return currentTurnIndex; }
    public void setCurrentTurnIndex(int v) { currentTurnIndex = v; }
    public int getRound() { return round; }
    public void setRound(int v) { round = v; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long v) { createdAt = v; }
    public long getLastPlayedAt() { return lastPlayedAt; }
    public void setLastPlayedAt(long v) { lastPlayedAt = v; }
}
