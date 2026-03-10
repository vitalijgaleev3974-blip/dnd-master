package com.dndmaster.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.dndmaster.R;
import com.dndmaster.managers.SessionManager;
import com.dndmaster.models.Character;
import com.dndmaster.models.Spell;
import java.util.Map;

public class CharacterSpellsFragment extends Fragment {
    private static final String ARG_CHAR_ID = "char_id";

    public static CharacterSpellsFragment newInstance(String charId) {
        CharacterSpellsFragment f = new CharacterSpellsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHAR_ID, charId);
        f.setArguments(args);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_character_spells, container, false);
        String charId = getArguments().getString(ARG_CHAR_ID);
        Character character = SessionManager.getInstance().getCharacter(charId);
        if (character != null) {
            TextView tvSpells = view.findViewById(R.id.tvSpellsList);
            if (!character.isSpellcaster()) {
                tvSpells.setText("Этот персонаж не является заклинателем.");
                return view;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Атрибут заклинания: ").append(character.getSpellcastingAbility()).append("\n");
            sb.append("СЗ заклинаний: ").append(character.getSpellSaveDC()).append("\n");
            sb.append("Бонус атаки: +").append(character.getSpellAttackBonus()).append("\n\n");

            // Spell slots
            if (!character.getSpellSlots().isEmpty()) {
                sb.append("━━ ЯЧЕЙКИ ЗАКЛИНАНИЙ ━━\n");
                for (Map.Entry<Integer, Integer> entry : character.getSpellSlots().entrySet()) {
                    int level = entry.getKey();
                    int total = entry.getValue();
                    int used = character.getUsedSpellSlots().getOrDefault(level, 0);
                    sb.append("Уровень ").append(level).append(": ")
                      .append(total - used).append("/").append(total).append("\n");
                }
                sb.append("\n");
            }

            // Known spells
            if (!character.getKnownSpells().isEmpty()) {
                sb.append("━━ ИЗВЕСТНЫЕ ЗАКЛИНАНИЯ ━━\n");
                for (Spell spell : character.getKnownSpells()) {
                    sb.append("• ").append(spell.getName());
                    sb.append(" (").append(spell.getLevel() == 0 ? "Заговор" : spell.getLevel() + " ур.").append(")");
                    if (spell.isConcentration()) sb.append(" [К]");
                    if (spell.isRitual()) sb.append(" [Р]");
                    sb.append("\n");
                }
            } else {
                sb.append("Заклинания не добавлены. Мастер добавит их в ходе игры.");
            }

            tvSpells.setText(sb.toString());
        }
        return view;
    }
}
