package com.dndmaster.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.dndmaster.ai.GameStateUpdate;
import com.dndmaster.models.Character;
import com.dndmaster.models.ChatMessage;
import com.dndmaster.models.GameSession;
import com.dndmaster.models.Item;
import com.dndmaster.models.Quest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SessionManager {

    private static SessionManager instance;
    private Context context;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "dnd_sessions";
    private static final String KEY_SESSIONS = "sessions_list";
    private static final String KEY_CHARACTERS = "characters_list";
    private static final String KEY_ACTIVE_SESSION = "active_session_id";

    private GameSession activeSession;

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ─── Session Management ───────────────────────────────────────────────────

    public GameSession createSession(String name, String aiProvider, String apiKey) {
        GameSession session = new GameSession();
        session.setId(UUID.randomUUID().toString());
        session.setName(name);
        session.setAiProvider(aiProvider);
        session.setApiKey(apiKey);
        saveSession(session);
        return session;
    }

    public void saveSession(GameSession session) {
        List<GameSession> sessions = getAllSessions();
        boolean found = false;
        for (int i = 0; i < sessions.size(); i++) {
            if (sessions.get(i).getId().equals(session.getId())) {
                sessions.set(i, session);
                found = true;
                break;
            }
        }
        if (!found) sessions.add(session);
        prefs.edit().putString(KEY_SESSIONS, new Gson().toJson(sessions)).apply();
    }

    public List<GameSession> getAllSessions() {
        String json = prefs.getString(KEY_SESSIONS, "[]");
        Type type = new TypeToken<List<GameSession>>() {}.getType();
        List<GameSession> list = new Gson().fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    public GameSession getSession(String id) {
        for (GameSession s : getAllSessions()) {
            if (s.getId().equals(id)) return s;
        }
        return null;
    }

    public void deleteSession(String id) {
        List<GameSession> sessions = getAllSessions();
        sessions.removeIf(s -> s.getId().equals(id));
        prefs.edit().putString(KEY_SESSIONS, new Gson().toJson(sessions)).apply();
    }

    public void setActiveSession(GameSession session) {
        this.activeSession = session;
        if (session != null) {
            prefs.edit().putString(KEY_ACTIVE_SESSION, session.getId()).apply();
        }
    }

    public GameSession getActiveSession() {
        if (activeSession == null) {
            String id = prefs.getString(KEY_ACTIVE_SESSION, null);
            if (id != null) activeSession = getSession(id);
        }
        return activeSession;
    }

    // ─── Character Management ─────────────────────────────────────────────────

    public Character createCharacter(String playerName, String characterName) {
        Character c = new Character();
        c.setId(UUID.randomUUID().toString());
        c.setPlayerName(playerName);
        c.setCharacterName(characterName);
        return c;
    }

    public void saveCharacter(Character character) {
        List<Character> characters = getAllCharacters();
        boolean found = false;
        for (int i = 0; i < characters.size(); i++) {
            if (characters.get(i).getId().equals(character.getId())) {
                characters.set(i, character);
                found = true;
                break;
            }
        }
        if (!found) characters.add(character);
        prefs.edit().putString(KEY_CHARACTERS, new Gson().toJson(characters)).apply();
    }

    public List<Character> getAllCharacters() {
        String json = prefs.getString(KEY_CHARACTERS, "[]");
        Type type = new TypeToken<List<Character>>() {}.getType();
        List<Character> list = new Gson().fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    public Character getCharacter(String id) {
        for (Character c : getAllCharacters()) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    // ─── Apply AI Game State Update ───────────────────────────────────────────

    public List<String> applyGameStateUpdate(GameSession session, GameStateUpdate update) {
        List<String> notifications = new ArrayList<>();

        // XP Awards
        for (GameStateUpdate.XpAward award : update.xpAwards) {
            Character c = session.getCharacterById(award.characterId);
            if (c != null) {
                int oldLevel = c.getLevel();
                c.addExperience(award.xp);
                String msg = "⭐ " + c.getCharacterName() + " получает " + award.xp + " XP";
                if (!award.reason.isEmpty()) msg += " (" + award.reason + ")";
                notifications.add(msg);
                if (c.getLevel() > oldLevel) {
                    notifications.add("🎉 " + c.getCharacterName() + " достигает уровня " + c.getLevel() + "!");
                }
            }
        }

        // HP Changes
        for (GameStateUpdate.HpChange change : update.hpChanges) {
            Character c = session.getCharacterById(change.characterId);
            if (c != null) {
                if ("heal".equals(change.type)) {
                    c.heal(Math.abs(change.change));
                    notifications.add("💚 " + c.getCharacterName() + " восстанавливает " + Math.abs(change.change) + " HP");
                } else {
                    c.takeDamage(Math.abs(change.change));
                    notifications.add("💔 " + c.getCharacterName() + " получает " + Math.abs(change.change) + " урона (" + change.reason + ")");
                    if (c.isUnconscious()) {
                        notifications.add("😵 " + c.getCharacterName() + " без сознания! Броски спасения от смерти!");
                    }
                }
            }
        }

        // Items gained
        for (GameStateUpdate.ItemChange itemChange : update.itemsGained) {
            Character c = session.getCharacterById(itemChange.characterId);
            if (c != null) {
                Item item = new Item(itemChange.itemName, itemChange.itemType, itemChange.quantity);
                c.getInventory().add(item);
                notifications.add("🎒 " + c.getCharacterName() + " получает: " + itemChange.itemName);
            }
        }

        // Conditions
        for (GameStateUpdate.ConditionChange cc : update.conditionsAdded) {
            Character c = session.getCharacterById(cc.characterId);
            if (c != null) {
                c.addCondition(cc.condition);
                notifications.add("⚠️ " + c.getCharacterName() + " получает состояние: " + cc.condition);
            }
        }
        for (GameStateUpdate.ConditionChange cc : update.conditionsRemoved) {
            Character c = session.getCharacterById(cc.characterId);
            if (c != null) {
                c.removeCondition(cc.condition);
                notifications.add("✅ " + c.getCharacterName() + " избавляется от: " + cc.condition);
            }
        }

        // Quests
        for (GameStateUpdate.QuestUpdate qu : update.questUpdates) {
            if ("add".equals(qu.action)) {
                Quest quest = new Quest(qu.questId.isEmpty() ? UUID.randomUUID().toString() : qu.questId,
                        qu.title, qu.description);
                for (Character c : session.getCharacters()) {
                    c.getActiveQuests().add(quest);
                }
                notifications.add("📜 Новый квест: " + qu.title);
            } else if ("complete".equals(qu.action)) {
                for (Character c : session.getCharacters()) {
                    c.getActiveQuests().removeIf(q -> {
                        if (q.getId().equals(qu.questId) || q.getTitle().equals(qu.title)) {
                            q.setStatus("completed");
                            c.getCompletedQuests().add(q);
                            return true;
                        }
                        return false;
                    });
                }
                notifications.add("✅ Квест выполнен: " + qu.title);
            }
        }

        // Gold
        for (GameStateUpdate.GoldChange gc : update.goldChanges) {
            Character c = session.getCharacterById(gc.characterId);
            if (c != null) {
                c.setGoldPieces(Math.max(0, c.getGoldPieces() + gc.amount));
                String dir = gc.amount >= 0 ? "получает" : "теряет";
                notifications.add("💰 " + c.getCharacterName() + " " + dir + " " + Math.abs(gc.amount) + " зм");
            }
        }

        // Inspiration
        for (String charId : update.inspirationGrants) {
            Character c = session.getCharacterById(charId);
            if (c != null) {
                c.setHasInspiration(true);
                notifications.add("✨ " + c.getCharacterName() + " получает Вдохновение!");
            }
        }

        // Combat
        if (update.combatStart && !session.isCombatActive()) {
            session.startCombat();
            notifications.add("⚔️ Начинается бой!");
        }
        if (update.combatEnd && session.isCombatActive()) {
            session.endCombat();
            notifications.add("🕊️ Бой закончен.");
        }

        // Scene
        if (update.sceneUpdate != null) {
            session.setCurrentScene(update.sceneUpdate);
        }

        // Save everything
        saveSession(session);
        for (Character c : session.getCharacters()) {
            saveCharacter(c);
        }

        return notifications;
    }
}
