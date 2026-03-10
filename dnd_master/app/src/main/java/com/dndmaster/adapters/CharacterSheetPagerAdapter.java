package com.dndmaster.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.dndmaster.fragments.CharacterMainFragment;
import com.dndmaster.fragments.CharacterSkillsFragment;
import com.dndmaster.fragments.CharacterInventoryFragment;
import com.dndmaster.fragments.CharacterSpellsFragment;
import com.dndmaster.fragments.CharacterQuestsFragment;
import com.dndmaster.fragments.CharacterNotesFragment;
import com.dndmaster.models.Character;

public class CharacterSheetPagerAdapter extends FragmentStateAdapter {

    private Character character;

    public CharacterSheetPagerAdapter(@NonNull FragmentActivity fa, Character character) {
        super(fa);
        this.character = character;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return CharacterMainFragment.newInstance(character.getId());
            case 1: return CharacterSkillsFragment.newInstance(character.getId());
            case 2: return CharacterInventoryFragment.newInstance(character.getId());
            case 3: return CharacterSpellsFragment.newInstance(character.getId());
            case 4: return CharacterQuestsFragment.newInstance(character.getId());
            case 5: return CharacterNotesFragment.newInstance(character.getId());
            default: return CharacterMainFragment.newInstance(character.getId());
        }
    }

    @Override
    public int getItemCount() { return 6; }
}
