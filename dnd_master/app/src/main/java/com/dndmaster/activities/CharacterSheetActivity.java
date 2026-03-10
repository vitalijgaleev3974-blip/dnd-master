package com.dndmaster.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.dndmaster.R;
import com.dndmaster.adapters.CharacterSheetPagerAdapter;
import com.dndmaster.managers.SessionManager;
import com.dndmaster.models.Character;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CharacterSheetActivity extends AppCompatActivity {

    private Character character;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_sheet);

        String charId = getIntent().getStringExtra("character_id");
        character = SessionManager.getInstance().getCharacter(charId);

        if (character == null) { finish(); return; }

        TextView tvCharName = findViewById(R.id.tvCharName);
        tvCharName.setText(character.getCharacterName());

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        CharacterSheetPagerAdapter adapter = new CharacterSheetPagerAdapter(this, character);
        viewPager.setAdapter(adapter);

        String[] tabs = {"Основное", "Навыки", "Инвентарь", "Заклинания", "Квесты", "Заметки"};
        new TabLayoutMediator(tabLayout, viewPager, (tab, pos) -> tab.setText(tabs[pos])).attach();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
