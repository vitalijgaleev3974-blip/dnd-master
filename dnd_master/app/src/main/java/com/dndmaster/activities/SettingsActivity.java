package com.dndmaster.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.dndmaster.R;
import com.dndmaster.ai.AIManager;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        EditText etClaude = findViewById(R.id.etClaudeKey);
        EditText etGemini = findViewById(R.id.etGeminiKey);

        etClaude.setText(AIManager.getInstance().getClaudeApiKey());
        etGemini.setText(AIManager.getInstance().getGeminiApiKey());

        Button btnSave = findViewById(R.id.btnSaveSettings);
        btnSave.setOnClickListener(v -> {
            AIManager.getInstance().saveClaudeApiKey(etClaude.getText().toString().trim());
            AIManager.getInstance().saveGeminiApiKey(etGemini.getText().toString().trim());
            Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show();
            finish();
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
