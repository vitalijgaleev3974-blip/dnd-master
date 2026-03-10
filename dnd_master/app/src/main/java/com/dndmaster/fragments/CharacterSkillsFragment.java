package com.dndmaster.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.dndmaster.R;
import com.dndmaster.managers.SessionManager;
import com.dndmaster.models.Character;
import com.dndmaster.utils.DnD5eData;

public class CharacterSkillsFragment extends Fragment {
    private static final String ARG_CHAR_ID = "char_id";

    public static CharacterSkillsFragment newInstance(String charId) {
        CharacterSkillsFragment f = new CharacterSkillsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHAR_ID, charId);
        f.setArguments(args);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_character_skills, container, false);
        String charId = getArguments().getString(ARG_CHAR_ID);
        Character character = SessionManager.getInstance().getCharacter(charId);
        if (character != null) {
            StringBuilder sb = new StringBuilder();

            sb.append("━━ СПАСБРОСКИ ━━\n");
            String[] abilities = {"Strength", "Dexterity", "Constitution", "Intelligence", "Wisdom", "Charisma"};
            String[] abilityNames = {"Сила", "Ловкость", "Телосложение", "Интеллект", "Мудрость", "Харизма"};
            for (int i = 0; i < abilities.length; i++) {
                int bonus = character.getSavingThrow(abilities[i]);
                boolean prof = character.getSavingThrowProficiencies().contains(abilities[i]);
                sb.append(prof ? "● " : "○ ").append(abilityNames[i]).append(": ")
                  .append(bonus >= 0 ? "+" : "").append(bonus).append("\n");
            }

            sb.append("\n━━ НАВЫКИ ━━\n");
            String[] skills = {"Акробатика", "Анализ", "Атлетика", "Выживание", "Внимание",
                    "Выступление", "Запугивание", "История", "Ловкость рук",
                    "Магия", "Медицина", "Обман", "Природа", "Религия",
                    "Скрытность", "Убеждение", "Уход за животными"};
            String[] skillsEng = {"Acrobatics", "Investigation", "Athletics", "Survival", "Perception",
                    "Performance", "Intimidation", "History", "Sleight of Hand",
                    "Arcana", "Medicine", "Deception", "Nature", "Religion",
                    "Stealth", "Persuasion", "Animal Handling"};
            for (int i = 0; i < skills.length; i++) {
                int bonus = character.getSkillBonus(skillsEng[i]);
                boolean prof = character.getSkillProficiencies().contains(skillsEng[i]);
                sb.append(prof ? "● " : "○ ").append(skills[i]).append(": ")
                  .append(bonus >= 0 ? "+" : "").append(bonus).append("\n");
            }

            if (!character.getLanguages().isEmpty()) {
                sb.append("\n━━ ЯЗЫКИ ━━\n").append(String.join(", ", character.getLanguages()));
            }

            ((TextView) view.findViewById(R.id.tvSkillsList)).setText(sb.toString());
        }
        return view;
    }
}
