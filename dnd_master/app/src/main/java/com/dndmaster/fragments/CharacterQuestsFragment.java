package com.dndmaster.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.dndmaster.R;
import com.dndmaster.managers.SessionManager;
import com.dndmaster.models.Character;
import com.dndmaster.models.Quest;

public class CharacterQuestsFragment extends Fragment {
    private static final String ARG_CHAR_ID = "char_id";

    public static CharacterQuestsFragment newInstance(String charId) {
        CharacterQuestsFragment f = new CharacterQuestsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHAR_ID, charId);
        f.setArguments(args);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_character_quests, container, false);
        String charId = getArguments().getString(ARG_CHAR_ID);
        Character character = SessionManager.getInstance().getCharacter(charId);
        if (character != null) {
            StringBuilder sb = new StringBuilder();

            if (!character.getActiveQuests().isEmpty()) {
                sb.append("━━ АКТИВНЫЕ КВЕСТЫ ━━\n\n");
                for (Quest q : character.getActiveQuests()) {
                    sb.append(q.isMainQuest() ? "⭐ " : "📜 ").append(q.getTitle()).append("\n");
                    sb.append(q.getDescription()).append("\n");
                    if (q.getObjectives() != null && !q.getObjectives().isEmpty()) {
                        for (int i = 0; i < q.getObjectives().size(); i++) {
                            boolean done = q.getObjectivesCompleted() != null &&
                                    i < q.getObjectivesCompleted().size() &&
                                    q.getObjectivesCompleted().get(i);
                            sb.append("  ").append(done ? "✅ " : "○ ").append(q.getObjectives().get(i)).append("\n");
                        }
                    }
                    sb.append("\n");
                }
            } else {
                sb.append("Активных квестов нет.\n\n");
            }

            if (!character.getCompletedQuests().isEmpty()) {
                sb.append("━━ ВЫПОЛНЕННЫЕ КВЕСТЫ ━━\n\n");
                for (Quest q : character.getCompletedQuests()) {
                    sb.append("✅ ").append(q.getTitle()).append("\n");
                }
            }

            ((TextView) view.findViewById(R.id.tvQuestsList)).setText(sb.toString());
        }
        return view;
    }
}
