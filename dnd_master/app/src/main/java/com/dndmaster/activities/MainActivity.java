package com.dndmaster.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dndmaster.R;
import com.dndmaster.adapters.SessionAdapter;
import com.dndmaster.managers.SessionManager;
import com.dndmaster.models.GameSession;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SessionAdapter.SessionClickListener {

    private RecyclerView recyclerSessions;
    private TextView tvEmpty;
    private FloatingActionButton fabNewSession;
    private SessionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerSessions = findViewById(R.id.recyclerSessions);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabNewSession = findViewById(R.id.fabNewSession);

        recyclerSessions.setLayoutManager(new LinearLayoutManager(this));

        fabNewSession.setOnClickListener(v -> {
            startActivity(new Intent(this, SessionSetupActivity.class));
        });

        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSessions();
    }

    private void loadSessions() {
        List<GameSession> sessions = SessionManager.getInstance().getAllSessions();
        if (sessions.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerSessions.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerSessions.setVisibility(View.VISIBLE);
            adapter = new SessionAdapter(sessions, this);
            recyclerSessions.setAdapter(adapter);
        }
    }

    @Override
    public void onSessionClick(GameSession session) {
        SessionManager.getInstance().setActiveSession(session);
        Intent intent = new Intent(this, GameSessionActivity.class);
        intent.putExtra("session_id", session.getId());
        startActivity(intent);
    }

    @Override
    public void onSessionDelete(GameSession session) {
        SessionManager.getInstance().deleteSession(session.getId());
        loadSessions();
    }
}
