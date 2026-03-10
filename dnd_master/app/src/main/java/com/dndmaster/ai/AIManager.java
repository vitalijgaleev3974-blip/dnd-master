package com.dndmaster.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.dndmaster.models.Character;
import com.dndmaster.models.ChatMessage;
import com.dndmaster.models.GameSession;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIManager {

    private static AIManager instance;
    private Context context;
    private OkHttpClient httpClient;
    private ExecutorService executor;
    private Handler mainHandler;

    private static final String PREFS_NAME = "dnd_ai_prefs";
    private static final String KEY_CLAUDE_API = "claude_api_key";
    private static final String KEY_GEMINI_API = "gemini_api_key";

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";

    private static final String DM_SYSTEM_PROMPT = buildSystemPrompt();

    public interface AICallback {
        void onSuccess(String response, GameStateUpdate update);
        void onError(String error);
    }

    public static AIManager getInstance() {
        if (instance == null) instance = new AIManager();
        return instance;
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    private static String buildSystemPrompt() {
        return "Ты — Мастер подземелий (Dungeon Master) в игре Dungeons & Dragons 5e (редакция 2024 года, One D&D). " +
               "Ты профессиональный рассказчик и арбитр правил. Веди игру на русском языке.\n\n" +
               "ТВОИ ОБЯЗАННОСТИ:\n" +
               "1. Рассказывай увлекательную историю, описывай мир, НПС, события\n" +
               "2. Соблюдай правила D&D 5e 2024 строго и справедливо\n" +
               "3. Когда нужна проверка — укажи какой навык/характеристику, сложность (DC)\n" +
               "4. В бою управляй монстрами и НПС тактически\n" +
               "5. Награждай XP за хорошую ролевую игру, победы, решение задач\n" +
               "6. ОБНОВЛЯЙ СОСТОЯНИЕ ИГРЫ через JSON-теги\n\n" +
               "ФОРМАТ ОТВЕТА:\n" +
               "Ты можешь включать в конец ответа JSON-блок для обновления состояния игры:\n" +
               "```game_state_update\n" +
               "{\n" +
               "  \"xp_awards\": [{\"character_id\": \"id\", \"xp\": 50, \"reason\": \"причина\"}],\n" +
               "  \"hp_changes\": [{\"character_id\": \"id\", \"change\": -10, \"type\": \"damage/heal\", \"reason\": \"причина\"}],\n" +
               "  \"items_gained\": [{\"character_id\": \"id\", \"item_name\": \"название\", \"item_type\": \"тип\", \"quantity\": 1}],\n" +
               "  \"items_lost\": [{\"character_id\": \"id\", \"item_name\": \"название\"}],\n" +
               "  \"conditions_added\": [{\"character_id\": \"id\", \"condition\": \"название\"}],\n" +
               "  \"conditions_removed\": [{\"character_id\": \"id\", \"condition\": \"название\"}],\n" +
               "  \"quests\": [{\"action\": \"add/complete/fail/update_objective\", \"quest_id\": \"id\", \"title\": \"название\", \"description\": \"описание\", \"objective_index\": 0}],\n" +
               "  \"gold_changes\": [{\"character_id\": \"id\", \"amount\": 100, \"reason\": \"причина\"}],\n" +
               "  \"inspiration\": [{\"character_id\": \"id\", \"grant\": true}],\n" +
               "  \"combat_start\": false,\n" +
               "  \"combat_end\": false,\n" +
               "  \"scene_update\": \"новая сцена/локация\"\n" +
               "}\n" +
               "```\n\n" +
               "ВАЖНО: Обновляй только те поля, которые изменились. Пропускай неизменённые.\n" +
               "СТИЛЬ: Пиши ярко, атмосферно, как настоящий DM. Давай игрокам выборы и последствия.";
    }

    // ─── Main AI Query ────────────────────────────────────────────────────────

    public void sendMessage(GameSession session, String playerMessage, String characterName, AICallback callback) {
        executor.execute(() -> {
            try {
                String provider = session.getAiProvider();
                String apiKey = session.getApiKey();
                String response;

                if ("gemini".equals(provider)) {
                    response = callGemini(session, playerMessage, characterName, apiKey);
                } else {
                    response = callClaude(session, playerMessage, characterName, apiKey);
                }

                // Parse game state update from response
                GameStateUpdate update = parseGameStateUpdate(response);
                String cleanResponse = removeJsonBlock(response);

                mainHandler.post(() -> callback.onSuccess(cleanResponse, update));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Ошибка ИИ: " + e.getMessage()));
            }
        });
    }

    // ─── Claude API ───────────────────────────────────────────────────────────

    private String callClaude(GameSession session, String playerMessage, String characterName, String apiKey) throws IOException {
        JSONArray messages = new JSONArray();

        // Add recent chat history (last 20 messages)
        List<ChatMessage> history = session.getChatHistory();
        int startIndex = Math.max(0, history.size() - 20);
        for (int i = startIndex; i < history.size(); i++) {
            ChatMessage msg = history.get(i);
            JSONObject m = new JSONObject();
            try {
                String role = ChatMessage.ROLE_MASTER.equals(msg.getRole()) ? "assistant" : "user";
                m.put("role", role);
                m.put("content", msg.getContent());
                messages.put(m);
            } catch (Exception ignored) {}
        }

        // Add current message with game context
        String contextualMessage = buildContextualMessage(session, playerMessage, characterName);
        try {
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", contextualMessage);
            messages.put(userMsg);
        } catch (Exception ignored) {}

        try {
            JSONObject body = new JSONObject();
            body.put("model", "claude-opus-4-5");
            body.put("max_tokens", 2048);
            body.put("system", DM_SYSTEM_PROMPT);
            body.put("messages", messages);

            Request request = new Request.Builder()
                    .url(CLAUDE_API_URL)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", "2023-06-01")
                    .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);
                return json.getJSONArray("content").getJSONObject(0).getString("text");
            }
        } catch (Exception e) {
            throw new IOException("Claude API error: " + e.getMessage());
        }
    }

    // ─── Gemini API ───────────────────────────────────────────────────────────

    private String callGemini(GameSession session, String playerMessage, String characterName, String apiKey) throws IOException {
        try {
            String contextualMessage = buildContextualMessage(session, playerMessage, characterName);
            String fullPrompt = DM_SYSTEM_PROMPT + "\n\n" + contextualMessage;

            JSONObject body = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", fullPrompt);
            parts.put(part);
            content.put("parts", parts);
            content.put("role", "user");
            contents.put(content);
            body.put("contents", contents);

            String url = GEMINI_API_URL + "?key=" + apiKey;

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);
                return json.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            }
        } catch (Exception e) {
            throw new IOException("Gemini API error: " + e.getMessage());
        }
    }

    // ─── Context Building ─────────────────────────────────────────────────────

    private String buildContextualMessage(GameSession session, String playerMessage, String characterName) {
        StringBuilder sb = new StringBuilder();

        // Add game state context
        sb.append("[СОСТОЯНИЕ ИГРЫ]\n");
        sb.append("Текущая сцена: ").append(session.getCurrentScene() != null ? session.getCurrentScene() : "Начало приключения").append("\n");

        if (session.isCombatActive()) {
            sb.append("⚔️ АКТИВНЫЙ БОЙ — Раунд ").append(session.getRound()).append("\n");
            if (session.getCurrentTurn() != null) {
                sb.append("Ход: ").append(session.getCurrentTurn().getEntityName()).append("\n");
            }
        }

        sb.append("\n[ПЕРСОНАЖИ ИГРОКОВ]\n");
        for (Character c : session.getCharacters()) {
            sb.append("• ").append(c.getCharacterName())
              .append(" (").append(c.getRace()).append(" ").append(c.getCharacterClass()).append(", ур. ").append(c.getLevel()).append(")")
              .append(" HP: ").append(c.getCurrentHitPoints()).append("/").append(c.getMaxHitPoints());
            if (!c.getConditions().isEmpty()) {
                sb.append(" [").append(String.join(", ", c.getConditions())).append("]");
            }
            sb.append(" — ID: ").append(c.getId()).append("\n");
        }

        sb.append("\n[ДЕЙСТВИЕ ИГРОКА]\n");
        sb.append(characterName).append(": ").append(playerMessage);

        return sb.toString();
    }

    // ─── Game State Update Parser ────────────────────────────────────────────

    private GameStateUpdate parseGameStateUpdate(String response) {
        GameStateUpdate update = new GameStateUpdate();
        try {
            int start = response.indexOf("```game_state_update");
            int end = response.lastIndexOf("```");
            if (start >= 0 && end > start) {
                String jsonStr = response.substring(start + 20, end).trim();
                JSONObject json = new JSONObject(jsonStr);

                // XP awards
                if (json.has("xp_awards")) {
                    JSONArray arr = json.getJSONArray("xp_awards");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject item = arr.getJSONObject(i);
                        update.addXpAward(item.getString("character_id"),
                                item.getInt("xp"),
                                item.optString("reason", ""));
                    }
                }

                // HP changes
                if (json.has("hp_changes")) {
                    JSONArray arr = json.getJSONArray("hp_changes");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject item = arr.getJSONObject(i);
                        update.addHpChange(item.getString("character_id"),
                                item.getInt("change"),
                                item.optString("type", "damage"),
                                item.optString("reason", ""));
                    }
                }

                // Items gained
                if (json.has("items_gained")) {
                    JSONArray arr = json.getJSONArray("items_gained");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject item = arr.getJSONObject(i);
                        update.addItemGain(item.getString("character_id"),
                                item.getString("item_name"),
                                item.optString("item_type", "misc"),
                                item.optInt("quantity", 1));
                    }
                }

                // Conditions
                if (json.has("conditions_added")) {
                    JSONArray arr = json.getJSONArray("conditions_added");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject item = arr.getJSONObject(i);
                        update.addCondition(item.getString("character_id"), item.getString("condition"));
                    }
                }
                if (json.has("conditions_removed")) {
                    JSONArray arr = json.getJSONArray("conditions_removed");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject item = arr.getJSONObject(i);
                        update.removeCondition(item.getString("character_id"), item.getString("condition"));
                    }
                }

                // Quests
                if (json.has("quests")) {
                    JSONArray arr = json.getJSONArray("quests");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject q = arr.getJSONObject(i);
                        update.addQuestUpdate(q.getString("action"),
                                q.optString("quest_id", ""),
                                q.optString("title", ""),
                                q.optString("description", ""),
                                q.optInt("objective_index", -1));
                    }
                }

                // Gold
                if (json.has("gold_changes")) {
                    JSONArray arr = json.getJSONArray("gold_changes");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject item = arr.getJSONObject(i);
                        update.addGoldChange(item.getString("character_id"), item.getInt("amount"));
                    }
                }

                // Combat flags
                update.combatStart = json.optBoolean("combat_start", false);
                update.combatEnd = json.optBoolean("combat_end", false);
                update.sceneUpdate = json.optString("scene_update", null);

                // Inspiration
                if (json.has("inspiration")) {
                    JSONArray arr = json.getJSONArray("inspiration");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject item = arr.getJSONObject(i);
                        if (item.optBoolean("grant", false)) {
                            update.addInspiration(item.getString("character_id"));
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return update;
    }

    private String removeJsonBlock(String response) {
        int start = response.indexOf("```game_state_update");
        if (start >= 0) {
            int end = response.lastIndexOf("```");
            if (end > start) {
                return (response.substring(0, start) + response.substring(end + 3)).trim();
            }
        }
        return response;
    }

    // ─── API Key Management ───────────────────────────────────────────────────

    public void saveClaudeApiKey(String key) {
        getPrefs().edit().putString(KEY_CLAUDE_API, key).apply();
    }

    public void saveGeminiApiKey(String key) {
        getPrefs().edit().putString(KEY_GEMINI_API, key).apply();
    }

    public String getClaudeApiKey() {
        return getPrefs().getString(KEY_CLAUDE_API, "");
    }

    public String getGeminiApiKey() {
        return getPrefs().getString(KEY_GEMINI_API, "");
    }

    private SharedPreferences getPrefs() {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
