package com.dndmaster.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.dndmaster.R;
import com.dndmaster.managers.SessionManager;
import com.dndmaster.models.Character;
import com.dndmaster.utils.DiceRoller;

public class CharacterMainFragment extends Fragment {

    private static final String ARG_CHAR_ID = "char_id";
    private Character character;

    public static CharacterMainFragment newInstance(String charId) {
        CharacterMainFragment f = new CharacterMainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHAR_ID, charId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_character_main, container, false);
        String charId = getArguments().getString(ARG_CHAR_ID);
        character = SessionManager.getInstance().getCharacter(charId);
        if (character != null) bindData(view);
        return view;
    }

    private void bindData(View view) {
        // Header
        ((TextView) view.findViewById(R.id.tvCharClass)).setText(
            character.getRace() + " " + character.getCharacterClass() + " " + character.getLevel() + " ур."
        );
        ((TextView) view.findViewById(R.id.tvAlignment)).setText(character.getAlignment());
        ((TextView) view.findViewById(R.id.tvBackground)).setText(character.getBackground());
        ((TextView) view.findViewById(R.id.tvXP)).setText("XP: " + character.getExperiencePoints());

        // HP
        TextView tvHp = view.findViewById(R.id.tvHp);
        tvHp.setText(character.getCurrentHitPoints() + " / " + character.getMaxHitPoints());
        if (character.getTemporaryHitPoints() > 0) {
            tvHp.setText(tvHp.getText() + " (+" + character.getTemporaryHitPoints() + " вр.)");
        }

        // Combat stats
        ((TextView) view.findViewById(R.id.tvAC)).setText(String.valueOf(character.getArmorClass()));
        ((TextView) view.findViewById(R.id.tvInitiative)).setText(
            (character.getInitiative() >= 0 ? "+" : "") + character.getInitiative()
        );
        ((TextView) view.findViewById(R.id.tvSpeed)).setText(character.getSpeed() + " фт.");
        ((TextView) view.findViewById(R.id.tvProf)).setText("+" + character.getProficiencyBonus());
        ((TextView) view.findViewById(R.id.tvHitDice)).setText(
            (character.getHitDiceTotal() - character.getHitDiceUsed()) + "/" + character.getHitDiceTotal() + " " + character.getHitDice()
        );

        // Ability scores
        bindAbilityScore(view, R.id.tvStrScore, R.id.tvStrMod, character.getStrength(), character.getStrengthModifier());
        bindAbilityScore(view, R.id.tvDexScore, R.id.tvDexMod, character.getDexterity(), character.getDexterityModifier());
        bindAbilityScore(view, R.id.tvConScore, R.id.tvConMod, character.getConstitution(), character.getConstitutionModifier());
        bindAbilityScore(view, R.id.tvIntScore, R.id.tvIntMod, character.getIntelligence(), character.getIntelligenceModifier());
        bindAbilityScore(view, R.id.tvWisScore, R.id.tvWisMod, character.getWisdom(), character.getWisdomModifier());
        bindAbilityScore(view, R.id.tvChaScore, R.id.tvChaMod, character.getCharisma(), character.getCharismaModifier());

        // Conditions
        if (!character.getConditions().isEmpty()) {
            ((TextView) view.findViewById(R.id.tvConditions)).setText(
                "⚠️ " + String.join(", ", character.getConditions())
            );
        } else {
            ((TextView) view.findViewById(R.id.tvConditions)).setText("Нет состояний");
        }

        // Inspiration
        view.findViewById(R.id.btnInspiration).setAlpha(character.isHasInspiration() ? 1f : 0.4f);

        // HP buttons
        view.findViewById(R.id.btnDamage).setOnClickListener(v -> showHpDialog(view, false));
        view.findViewById(R.id.btnHeal).setOnClickListener(v -> showHpDialog(view, true));

        // Death saves
        TextView tvDeathSaves = view.findViewById(R.id.tvDeathSaves);
        if (character.isUnconscious()) {
            tvDeathSaves.setVisibility(View.VISIBLE);
            tvDeathSaves.setText("Успехи смерти: " + character.getDeathSaveSuccesses() + "/3  Провалы: " + character.getDeathSaveFailures() + "/3");
        } else {
            tvDeathSaves.setVisibility(View.GONE);
        }

        // Rest buttons
        view.findViewById(R.id.btnShortRest).setOnClickListener(v -> {
            DiceRoller.RollResult result = DiceRoller.parseDiceNotation(character.getHitDice());
            int healing = Math.max(1, result.getTotal() + character.getConstitutionModifier());
            character.heal(healing);
            SessionManager.getInstance().saveCharacter(character);
            bindData(view);
            Toast.makeText(getContext(), "Короткий отдых: восстановлено " + healing + " HP", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btnLongRest).setOnClickListener(v -> {
            character.takeLongRest();
            SessionManager.getInstance().saveCharacter(character);
            bindData(view);
            Toast.makeText(getContext(), "Долгий отдых: все восстановлено!", Toast.LENGTH_SHORT).show();
        });
    }

    private void bindAbilityScore(View root, int scoreId, int modId, int score, int mod) {
        ((TextView) root.findViewById(scoreId)).setText(String.valueOf(score));
        ((TextView) root.findViewById(modId)).setText((mod >= 0 ? "+" : "") + mod);
    }

    private void showHpDialog(View rootView, boolean isHeal) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle(isHeal ? "Восстановление HP" : "Получить урон");
        EditText et = new EditText(getContext());
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        et.setHint("Количество");
        builder.setView(et);
        builder.setPositiveButton("OK", (d, w) -> {
            try {
                int amount = Integer.parseInt(et.getText().toString());
                if (isHeal) character.heal(amount);
                else character.takeDamage(amount);
                SessionManager.getInstance().saveCharacter(character);
                bindData(rootView);
            } catch (NumberFormatException ignored) {}
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }
}
