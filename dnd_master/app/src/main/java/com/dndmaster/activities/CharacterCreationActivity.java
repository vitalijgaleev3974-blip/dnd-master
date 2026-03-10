package com.dndmaster.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.dndmaster.R;
import com.dndmaster.managers.SessionManager;
import com.dndmaster.models.Character;
import com.dndmaster.models.Item;
import com.dndmaster.utils.DnD5eData;
import com.dndmaster.utils.DiceRoller;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CharacterCreationActivity extends AppCompatActivity {

    private String sessionId;
    private Character character;

    // Step 1 - Basic Info
    private EditText etPlayerName, etCharacterName;
    private Spinner spinnerRace, spinnerSubrace, spinnerClass, spinnerBackground, spinnerAlignment;
    private TextView tvSubraceLabel;

    // Step 2 - Ability Scores
    private TextView tvStr, tvDex, tvCon, tvInt, tvWis, tvCha;
    private TextView tvStrMod, tvDexMod, tvConMod, tvIntMod, tvWisMod, tvChaMod;
    private RadioGroup rgScoreMethod;
    private int[] currentScores = {8, 8, 8, 8, 8, 8};

    // Step 3 - Backstory
    private EditText etBackstory, etTraits, etIdeals, etBonds, etFlaws;

    private Button btnPrev, btnNext, btnFinish;
    private TextView tvStepTitle;
    private int currentStep = 0;
    private View[] steps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_creation);

        sessionId = getIntent().getStringExtra("session_id");
        character = new Character();

        initViews();
        setupSpinners();
        showStep(0);
    }

    private void initViews() {
        tvStepTitle = findViewById(R.id.tvStepTitle);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnFinish = findViewById(R.id.btnFinish);

        steps = new View[]{
            findViewById(R.id.stepBasicInfo),
            findViewById(R.id.stepAbilityScores),
            findViewById(R.id.stepBackstory)
        };

        // Basic Info
        etPlayerName = findViewById(R.id.etPlayerName);
        etCharacterName = findViewById(R.id.etCharacterName);
        spinnerRace = findViewById(R.id.spinnerRace);
        spinnerSubrace = findViewById(R.id.spinnerSubrace);
        spinnerClass = findViewById(R.id.spinnerClass);
        spinnerBackground = findViewById(R.id.spinnerBackground);
        spinnerAlignment = findViewById(R.id.spinnerAlignment);
        tvSubraceLabel = findViewById(R.id.tvSubraceLabel);

        // Ability Scores
        tvStr = findViewById(R.id.tvStr); tvStrMod = findViewById(R.id.tvStrMod);
        tvDex = findViewById(R.id.tvDex); tvDexMod = findViewById(R.id.tvDexMod);
        tvCon = findViewById(R.id.tvCon); tvConMod = findViewById(R.id.tvConMod);
        tvInt = findViewById(R.id.tvInt); tvIntMod = findViewById(R.id.tvIntMod);
        tvWis = findViewById(R.id.tvWis); tvWisMod = findViewById(R.id.tvWisMod);
        tvCha = findViewById(R.id.tvCha); tvChaMod = findViewById(R.id.tvChaMod);

        // Backstory
        etBackstory = findViewById(R.id.etBackstory);
        etTraits = findViewById(R.id.etTraits);
        etIdeals = findViewById(R.id.etIdeals);
        etBonds = findViewById(R.id.etBonds);
        etFlaws = findViewById(R.id.etFlaws);

        btnPrev.setOnClickListener(v -> {
            if (currentStep > 0) showStep(currentStep - 1);
        });

        btnNext.setOnClickListener(v -> {
            if (validateStep(currentStep)) {
                if (currentStep < steps.length - 1) showStep(currentStep + 1);
            }
        });

        btnFinish.setOnClickListener(v -> {
            if (validateStep(currentStep)) {
                finishCharacterCreation();
            }
        });

        // Roll dice buttons for ability scores
        View[] rollButtons = {
            findViewById(R.id.btnRollStr), findViewById(R.id.btnRollDex),
            findViewById(R.id.btnRollCon), findViewById(R.id.btnRollInt),
            findViewById(R.id.btnRollWis), findViewById(R.id.btnRollCha)
        };
        int[] scoreIndexes = {0, 1, 2, 3, 4, 5};
        for (int i = 0; i < rollButtons.length; i++) {
            final int idx = i;
            if (rollButtons[i] != null) {
                rollButtons[i].setOnClickListener(v -> {
                    currentScores[idx] = DiceRoller.rollAbilityScore();
                    updateAbilityScoreDisplays();
                });
            }
        }

        View btnRollAll = findViewById(R.id.btnRollAllScores);
        if (btnRollAll != null) {
            btnRollAll.setOnClickListener(v -> {
                currentScores = DiceRoller.rollAllAbilityScores();
                updateAbilityScoreDisplays();
            });
        }

        View btnStandardArray = findViewById(R.id.btnStandardArray);
        if (btnStandardArray != null) {
            btnStandardArray.setOnClickListener(v -> {
                currentScores = DnD5eData.STANDARD_ARRAY.clone();
                updateAbilityScoreDisplays();
            });
        }

        updateAbilityScoreDisplays();
    }

    private void setupSpinners() {
        // Race
        ArrayAdapter<String> raceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, DnD5eData.RACES);
        raceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRace.setAdapter(raceAdapter);
        spinnerRace.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                String race = DnD5eData.RACES.get(pos);
                List<String> subraces = DnD5eData.SUBRACES.get(race);
                if (subraces != null && !subraces.isEmpty()) {
                    tvSubraceLabel.setVisibility(View.VISIBLE);
                    spinnerSubrace.setVisibility(View.VISIBLE);
                    ArrayAdapter<String> subAdapter = new ArrayAdapter<>(CharacterCreationActivity.this, android.R.layout.simple_spinner_item, subraces);
                    subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSubrace.setAdapter(subAdapter);
                } else {
                    tvSubraceLabel.setVisibility(View.GONE);
                    spinnerSubrace.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> p) {}
        });

        // Class
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, DnD5eData.CLASSES);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(classAdapter);

        // Background
        ArrayAdapter<String> bgAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, DnD5eData.BACKGROUNDS);
        bgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBackground.setAdapter(bgAdapter);

        // Alignment
        ArrayAdapter<String> alignAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, DnD5eData.ALIGNMENTS);
        alignAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAlignment.setAdapter(alignAdapter);
    }

    private void showStep(int step) {
        currentStep = step;
        for (View s : steps) s.setVisibility(View.GONE);
        steps[step].setVisibility(View.VISIBLE);

        String[] titles = {"Основная информация", "Характеристики", "Предыстория"};
        tvStepTitle.setText(titles[step] + " (" + (step + 1) + "/" + steps.length + ")");

        btnPrev.setVisibility(step > 0 ? View.VISIBLE : View.GONE);
        btnNext.setVisibility(step < steps.length - 1 ? View.VISIBLE : View.GONE);
        btnFinish.setVisibility(step == steps.length - 1 ? View.VISIBLE : View.GONE);
    }

    private boolean validateStep(int step) {
        switch (step) {
            case 0:
                if (etCharacterName.getText().toString().trim().isEmpty()) {
                    etCharacterName.setError("Введите имя персонажа");
                    return false;
                }
                if (etPlayerName.getText().toString().trim().isEmpty()) {
                    etPlayerName.setError("Введите имя игрока");
                    return false;
                }
                // Save basic info
                character.setCharacterName(etCharacterName.getText().toString().trim());
                character.setPlayerName(etPlayerName.getText().toString().trim());
                character.setRace(DnD5eData.RACES.get(spinnerRace.getSelectedItemPosition()));
                character.setCharacterClass(DnD5eData.CLASSES.get(spinnerClass.getSelectedItemPosition()));
                character.setBackground(DnD5eData.BACKGROUNDS.get(spinnerBackground.getSelectedItemPosition()));
                character.setAlignment(DnD5eData.ALIGNMENTS.get(spinnerAlignment.getSelectedItemPosition()));
                if (spinnerSubrace.getVisibility() == View.VISIBLE && spinnerSubrace.getAdapter() != null) {
                    character.setSubrace(spinnerSubrace.getSelectedItem().toString());
                }
                applyClassDefaults();
                return true;

            case 1:
                character.setStrength(currentScores[0]);
                character.setDexterity(currentScores[1]);
                character.setConstitution(currentScores[2]);
                character.setIntelligence(currentScores[3]);
                character.setWisdom(currentScores[4]);
                character.setCharisma(currentScores[5]);
                calculateDerivedStats();
                return true;

            case 2:
                character.setBackstory(etBackstory.getText().toString());
                if (!etTraits.getText().toString().isEmpty())
                    character.getTraits().add(etTraits.getText().toString());
                if (!etIdeals.getText().toString().isEmpty())
                    character.getIdeals().add(etIdeals.getText().toString());
                if (!etBonds.getText().toString().isEmpty())
                    character.getBonds().add(etBonds.getText().toString());
                if (!etFlaws.getText().toString().isEmpty())
                    character.getFlaws().add(etFlaws.getText().toString());
                return true;
        }
        return true;
    }

    private void applyClassDefaults() {
        String cls = character.getCharacterClass();
        String hitDice = DnD5eData.CLASS_HIT_DICE.getOrDefault(cls, "d8");
        character.setHitDice(hitDice);
        character.setHitDiceTotal(1);
        character.setSpeed(30);

        // Set spellcasters
        List<String> spellcasters = Arrays.asList("Бард", "Жрец", "Друид", "Паладин",
                "Следопыт", "Чародей", "Колдун", "Волшебник", "Изобретатель");
        character.setSpellcaster(spellcasters.contains(cls));

        // Add starting equipment
        List<String> equipment = DnD5eData.CLASS_STARTING_EQUIPMENT.get(cls);
        if (equipment != null) {
            for (String eq : equipment) {
                Item item = new Item(eq, "equipment", 1);
                character.getInventory().add(item);
            }
        }

        // Default proficiencies by class
        applyClassProficiencies(cls);
    }

    private void applyClassProficiencies(String cls) {
        switch (cls) {
            case "Варвар":
                character.getSavingThrowProficiencies().add("Strength");
                character.getSavingThrowProficiencies().add("Constitution");
                character.getArmorProficiencies().add("Лёгкие доспехи");
                character.getArmorProficiencies().add("Средние доспехи");
                character.getArmorProficiencies().add("Щиты");
                character.getClassResourcesMax().put("Ярость", 2);
                break;
            case "Бард":
                character.getSavingThrowProficiencies().add("Dexterity");
                character.getSavingThrowProficiencies().add("Charisma");
                character.getClassResourcesMax().put("Вдохновение Барда", 1);
                break;
            case "Жрец":
                character.getSavingThrowProficiencies().add("Wisdom");
                character.getSavingThrowProficiencies().add("Charisma");
                character.getArmorProficiencies().add("Лёгкие доспехи");
                character.getArmorProficiencies().add("Средние доспехи");
                character.getArmorProficiencies().add("Щиты");
                break;
            case "Боец":
                character.getSavingThrowProficiencies().add("Strength");
                character.getSavingThrowProficiencies().add("Constitution");
                character.getClassResourcesMax().put("Второе дыхание", 1);
                break;
            case "Монах":
                character.getSavingThrowProficiencies().add("Strength");
                character.getSavingThrowProficiencies().add("Dexterity");
                character.getClassResourcesMax().put("Очки ки", 1);
                break;
            case "Паладин":
                character.getSavingThrowProficiencies().add("Wisdom");
                character.getSavingThrowProficiencies().add("Charisma");
                character.getArmorProficiencies().add("Все доспехи");
                character.getArmorProficiencies().add("Щиты");
                character.getClassResourcesMax().put("Божественная кара", 1);
                break;
            case "Плут":
                character.getSavingThrowProficiencies().add("Dexterity");
                character.getSavingThrowProficiencies().add("Intelligence");
                break;
            case "Волшебник":
                character.getSavingThrowProficiencies().add("Intelligence");
                character.getSavingThrowProficiencies().add("Wisdom");
                break;
            case "Колдун":
                character.getSavingThrowProficiencies().add("Wisdom");
                character.getSavingThrowProficiencies().add("Charisma");
                break;
            case "Чародей":
                character.getSavingThrowProficiencies().add("Constitution");
                character.getSavingThrowProficiencies().add("Charisma");
                character.getClassResourcesMax().put("Очки чародейства", 1);
                break;
        }
    }

    private void calculateDerivedStats() {
        int conMod = character.getConstitutionModifier();
        int hitDieSides = Integer.parseInt(character.getHitDice().replace("d", ""));
        int maxHp = hitDieSides + conMod;
        character.setMaxHitPoints(Math.max(1, maxHp));
        character.setCurrentHitPoints(character.getMaxHitPoints());

        // Initiative = DEX modifier
        character.setInitiative(character.getDexterityModifier());

        // AC = 10 + DEX (no armor default)
        character.setArmorClass(10 + character.getDexterityModifier());

        // Spell stats if spellcaster
        if (character.isSpellcaster()) {
            String ability = getSpellcastingAbility(character.getCharacterClass());
            character.setSpellcastingAbility(ability);
            int abilityMod = getModByAbility(ability);
            character.setSpellSaveDC(8 + character.getProficiencyBonus() + abilityMod);
            character.setSpellAttackBonus(character.getProficiencyBonus() + abilityMod);
        }
    }

    private String getSpellcastingAbility(String cls) {
        switch (cls) {
            case "Волшебник": case "Изобретатель": return "Intelligence";
            case "Жрец": case "Друид": case "Следопыт": return "Wisdom";
            default: return "Charisma";
        }
    }

    private int getModByAbility(String ability) {
        switch (ability) {
            case "Intelligence": return character.getIntelligenceModifier();
            case "Wisdom": return character.getWisdomModifier();
            default: return character.getCharismaModifier();
        }
    }

    private void updateAbilityScoreDisplays() {
        String[] names = {"СИЛ", "ЛОВ", "ТЕЛ", "ИНТ", "МДР", "ХАР"};
        TextView[] scoreViews = {tvStr, tvDex, tvCon, tvInt, tvWis, tvCha};
        TextView[] modViews = {tvStrMod, tvDexMod, tvConMod, tvIntMod, tvWisMod, tvChaMod};

        for (int i = 0; i < 6; i++) {
            if (scoreViews[i] != null) scoreViews[i].setText(String.valueOf(currentScores[i]));
            if (modViews[i] != null) {
                int mod = (int) Math.floor((currentScores[i] - 10) / 2.0);
                modViews[i].setText(mod >= 0 ? "+" + mod : String.valueOf(mod));
            }
        }
    }

    private void finishCharacterCreation() {
        SessionManager.getInstance().saveCharacter(character);

        Intent result = new Intent();
        result.putExtra("character_id", character.getId());
        setResult(RESULT_OK, result);
        finish();

        Toast.makeText(this, character.getCharacterName() + " создан!", Toast.LENGTH_SHORT).show();
    }
}
