package com.dndmaster.models;

public class ChatMessage {
    public static final String ROLE_USER = "user";
    public static final String ROLE_MASTER = "master";
    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_DICE = "dice";

    private String role;
    private String content;
    private String characterName;
    private long timestamp;
    private String messageType; // "narrative", "action", "dice_roll", "combat", "system_update"

    public ChatMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public ChatMessage(String role, String content) {
        this();
        this.role = role;
        this.content = content;
    }

    public ChatMessage(String role, String content, String characterName) {
        this(role, content);
        this.characterName = characterName;
    }

    public String getRole() { return role; }
    public void setRole(String v) { role = v; }
    public String getContent() { return content; }
    public void setContent(String v) { content = v; }
    public String getCharacterName() { return characterName; }
    public void setCharacterName(String v) { characterName = v; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long v) { timestamp = v; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String v) { messageType = v; }
}
