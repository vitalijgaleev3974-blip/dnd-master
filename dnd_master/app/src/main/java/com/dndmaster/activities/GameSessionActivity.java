package com.dndmaster.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dndmaster.R;
import com.dndmaster.adapters.ChatAdapter;
import com.dndmaster.adapters.CharacterChipAdapter;
import com.dndmaster.ai.AIManager;
import com.dndmaster.ai.GameStateUpdate;
import com.dndmaster.managers.SessionManager;
import com.dndmaster.models.Character;
import com.dndmaster.models.ChatMessage;
import com.dndmaster.models.GameSession;
import com.dndmaster.utils.DiceRoller;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class GameSessionActivity extends AppCompatActivity {

    private RecyclerView recyclerChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnDice;
    private ImageButton btnCharacters;
    private ImageButton btnBack;
    private TextView tvSessionName;
    private TextView tvCombatStatus;
    private ChipGroup chipGroupCharacters;

    private ChatAdapter chatAdapter;
    private GameSession session;
    private Character activeCharacter;
    private boolean isAiThinking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_session);

        String sessionId = getIntent().getStringExtra("session_id");
        session = SessionManager.getInstance().getSession(sessionId);

        if (session == null) {
            finish();
            return;
        }

        initViews();
        loadSession();
    }

    private void initViews() {
        recyclerChat = findViewById(R.id.recyclerChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnDice = findViewById(R.id.btnDice);
        btnCharacters = findViewById(R.id.btnCharacters);
        btnBack = findViewById(R.id.btnBack);
        tvSessionName = findViewById(R.id.tvSessionName);
        tvCombatStatus = findViewById(R.id.tvCombatStatus);
        chipGroupCharacters = findViewById(R.id.chipGroupCharacters);

        tvSessionName.setText(session.getName());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerChat.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(session.getChatHistory());
        recyclerChat.setAdapter(chatAdapter);

        btnSend.setOnClickListener(v -> sendMessage());
        btnDice.setOnClickListener(v -> showDiceRoller());
        btnCharacters.setOnClickListener(v -> showCharacterPanel());
        btnBack.setOnClickListener(v -> {
            SessionManager.getInstance().saveSession(session);
            finish();
        });

        // Character selector chips
        updateCharacterChips();

        // Set first character as active
        if (!session.getCharacters().isEmpty() && activeCharacter == null) {
            activeCharacter = session.getCharacters().get(0);
        }
    }

    private void loadSession() {
        updateCombatStatus();
        scrollToBottom();

        // Send intro message if session is new
        if (session.getChatHistory().isEmpty()) {
            sendIntroMessage();
        }
    }

    private void sendIntroMessage() {
        isAiThinking = true;
        String intro = buildIntroMessage();
        AIManager.getInstance().sendMessage(session, intro, "Система", new AIManager.AICallback() {
            @Override
            public void onSuccess(String response, GameStateUpdate update) {
                isAiThinking = false;
                ChatMessage masterMsg = new ChatMessage(ChatMessage.ROLE_MASTER, response);
                masterMsg.setMessageType("narrative");
                session.addMessage(masterMsg);
                chatAdapter.addMessage(masterMsg);

                if (update.hasChanges()) {
                    List<String> notifications = SessionManager.getInstance().applyGameStateUpdate(session, update);
                    showNotifications(notifications);
                }
                updateCombatStatus();
                scrollToBottom();
            }

            @Override
            public void onError(String error) {
                isAiThinking = false;
                showError(error);
            }
        });
    }

    private String buildIntroMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Начинается новое приключение! Вот список персонажей игроков:\n\n");
        for (Character c : session.getCharacters()) {
            sb.append("- ").append(c.getCharacterName())
              .append(", ").append(c.getRace())
              .append(" ").append(c.getCharacterClass())
              .append(", уровень ").append(c.getLevel())
              .append(". Предыстория: ").append(c.getBackground() != null ? c.getBackground() : "неизвестна");
            if (c.getBackstory() != null && !c.getBackstory().isEmpty()) {
                sb.append(". ").append(c.getBackstory());
            }
            sb.append("\n");
        }
        sb.append("\nПожалуйста, начни приключение с интригующего вступления, задай атмосферу и предложи первую ситуацию или выбор для персонажей.");
        return sb.toString();
    }

    private void sendMessage() {
        if (isAiThinking) {
            Toast.makeText(this, "Мастер думает...", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;
        if (activeCharacter == null) {
            Toast.makeText(this, "Выберите персонажа", Toast.LENGTH_SHORT).show();
            return;
        }

        etMessage.setText("");

        // Add player message to chat
        ChatMessage playerMsg = new ChatMessage(ChatMessage.ROLE_USER, text, activeCharacter.getCharacterName());
        playerMsg.setMessageType("action");
        session.addMessage(playerMsg);
        chatAdapter.addMessage(playerMsg);
        scrollToBottom();

        // Show thinking indicator
        isAiThinking = true;
        ChatMessage thinkingMsg = new ChatMessage(ChatMessage.ROLE_MASTER, "...");
        thinkingMsg.setMessageType("thinking");
        chatAdapter.addMessage(thinkingMsg);
        scrollToBottom();

        // Call AI
        AIManager.getInstance().sendMessage(session, text, activeCharacter.getCharacterName(), new AIManager.AICallback() {
            @Override
            public void onSuccess(String response, GameStateUpdate update) {
                isAiThinking = false;
                chatAdapter.removeLastMessage(); // Remove thinking indicator

                ChatMessage masterMsg = new ChatMessage(ChatMessage.ROLE_MASTER, response);
                masterMsg.setMessageType("narrative");
                session.addMessage(masterMsg);
                chatAdapter.addMessage(masterMsg);

                if (update.hasChanges()) {
                    List<String> notifications = SessionManager.getInstance().applyGameStateUpdate(session, update);
                    showNotifications(notifications);
                    updateCharacterChips();
                }

                updateCombatStatus();
                scrollToBottom();
                SessionManager.getInstance().saveSession(session);
            }

            @Override
            public void onError(String error) {
                isAiThinking = false;
                chatAdapter.removeLastMessage();
                showError(error);
            }
        });
    }

    private void showDiceRoller() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_dice_roller, null);
        dialog.setContentView(view);

        int[] diceSides = {4, 6, 8, 10, 12, 20, 100};
        int[] diceButtons = {R.id.btnD4, R.id.btnD6, R.id.btnD8, R.id.btnD10, R.id.btnD12, R.id.btnD20, R.id.btnD100};

        for (int i = 0; i < diceButtons.length; i++) {
            final int sides = diceSides[i];
            view.findViewById(diceButtons[i]).setOnClickListener(v -> {
                DiceRoller.RollResult result = DiceRoller.rollWithDetails(1, sides);
                String msg = "🎲 d" + sides + ": " + result.getDescription();
                ChatMessage diceMsg = new ChatMessage(ChatMessage.ROLE_DICE, msg,
                        activeCharacter != null ? activeCharacter.getCharacterName() : "");
                diceMsg.setMessageType("dice_roll");
                session.addMessage(diceMsg);
                chatAdapter.addMessage(diceMsg);
                scrollToBottom();
                dialog.dismiss();
            });
        }

        // Custom dice notation
        EditText etCustomDice = view.findViewById(R.id.etCustomDice);
        view.findViewById(R.id.btnRollCustom).setOnClickListener(v -> {
            String notation = etCustomDice.getText().toString().trim();
            if (!notation.isEmpty()) {
                DiceRoller.RollResult result = DiceRoller.parseDiceNotation(notation);
                String msg = "🎲 " + notation + ": " + result.getDescription();
                ChatMessage diceMsg = new ChatMessage(ChatMessage.ROLE_DICE, msg,
                        activeCharacter != null ? activeCharacter.getCharacterName() : "");
                diceMsg.setMessageType("dice_roll");
                session.addMessage(diceMsg);
                chatAdapter.addMessage(diceMsg);
                scrollToBottom();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showCharacterPanel() {
        if (session.getCharacters().isEmpty()) {
            Intent intent = new Intent(this, CharacterCreationActivity.class);
            intent.putExtra("session_id", session.getId());
            startActivityForResult(intent, 100);
            return;
        }

        Intent intent = new Intent(this, CharacterSheetActivity.class);
        if (activeCharacter != null) {
            intent.putExtra("character_id", activeCharacter.getId());
        }
        startActivity(intent);
    }

    private void updateCharacterChips() {
        chipGroupCharacters.removeAllViews();
        for (Character c : session.getCharacters()) {
            Chip chip = new Chip(this);
            chip.setText(c.getCharacterName() + " " + c.getCurrentHitPoints() + "HP");
            chip.setCheckable(true);
            if (activeCharacter != null && c.getId().equals(activeCharacter.getId())) {
                chip.setChecked(true);
            }
            chip.setOnClickListener(v -> {
                activeCharacter = c;
                updateCharacterChips();
            });
            chipGroupCharacters.addView(chip);
        }

        // Add character button
        Chip addChip = new Chip(this);
        addChip.setText("+ Персонаж");
        addChip.setOnClickListener(v -> {
            Intent intent = new Intent(this, CharacterCreationActivity.class);
            intent.putExtra("session_id", session.getId());
            startActivityForResult(intent, 100);
        });
        chipGroupCharacters.addView(addChip);
    }

    private void updateCombatStatus() {
        if (session.isCombatActive()) {
            tvCombatStatus.setVisibility(View.VISIBLE);
            tvCombatStatus.setText("⚔️ БОЙ — Раунд " + session.getRound());
        } else {
            tvCombatStatus.setVisibility(View.GONE);
        }
    }

    private void showNotifications(List<String> notifications) {
        for (String notif : notifications) {
            ChatMessage sysMsg = new ChatMessage(ChatMessage.ROLE_SYSTEM, notif);
            sysMsg.setMessageType("system_update");
            chatAdapter.addMessage(sysMsg);
        }
        scrollToBottom();
    }

    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    private void scrollToBottom() {
        int count = chatAdapter.getItemCount();
        if (count > 0) recyclerChat.smoothScrollToPosition(count - 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String charId = data.getStringExtra("character_id");
            Character c = SessionManager.getInstance().getCharacter(charId);
            if (c != null) {
                session.addCharacter(c);
                SessionManager.getInstance().saveSession(session);
                updateCharacterChips();
                activeCharacter = c;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SessionManager.getInstance().saveSession(session);
    }
}
