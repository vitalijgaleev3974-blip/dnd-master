package com.dndmaster.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.dndmaster.R;
import com.dndmaster.ai.AIManager;
import com.dndmaster.managers.SessionManager;
import com.dndmaster.models.GameSession;

public class SessionSetupActivity extends AppCompatActivity {

    private EditText etSessionName, etApiKey;
    private RadioGroup rgAiProvider;
    private Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_setup);

        etSessionName = findViewById(R.id.etSessionName);
        etApiKey = findViewById(R.id.etApiKey);
        rgAiProvider = findViewById(R.id.rgAiProvider);
        btnCreate = findViewById(R.id.btnCreateSession);

        // Pre-fill saved API key
        rgAiProvider.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbClaude) {
                etApiKey.setHint("Claude API ключ (начинается с sk-ant-)");
                etApiKey.setText(AIManager.getInstance().getClaudeApiKey());
            } else {
                etApiKey.setHint("Gemini API ключ");
                etApiKey.setText(AIManager.getInstance().getGeminiApiKey());
            }
        });

        // Default hint
        etApiKey.setHint("Claude API ключ (начинается с sk-ant-)");
        etApiKey.setText(AIManager.getInstance().getClaudeApiKey());

        btnCreate.setOnClickListener(v -> createSession());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void createSession() {
        String name = etSessionName.getText().toString().trim();
        String apiKey = etApiKey.getText().toString().trim();

        if (name.isEmpty()) {
            etSessionName.setError("Введите название сессии");
            return;
        }
        if (apiKey.isEmpty()) {
            etApiKey.setError("Введите API ключ");
            return;
        }

        String provider = rgAiProvider.getCheckedRadioButtonId() == R.id.rbClaude ? "claude" : "gemini";

        // Save API key
        if ("claude".equals(provider)) {
            AIManager.getInstance().saveClaudeApiKey(apiKey);
        } else {
            AIManager.getInstance().saveGeminiApiKey(apiKey);
        }

        GameSession session = SessionManager.getInstance().createSession(name, provider, apiKey);

        Toast.makeText(this, "Сессия создана!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, GameSessionActivity.class);
        intent.putExtra("session_id", session.getId());
        startActivity(intent);
        finish();
    }
}
