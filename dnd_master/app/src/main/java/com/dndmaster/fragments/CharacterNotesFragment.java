package com.dndmaster.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.dndmaster.R;
import com.dndmaster.managers.SessionManager;
import com.dndmaster.models.Character;

public class CharacterNotesFragment extends Fragment {
    private static final String ARG_CHAR_ID = "char_id";
    private Character character;

    public static CharacterNotesFragment newInstance(String charId) {
        CharacterNotesFragment f = new CharacterNotesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHAR_ID, charId);
        f.setArguments(args);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_character_notes, container, false);
        String charId = getArguments().getString(ARG_CHAR_ID);
        character = SessionManager.getInstance().getCharacter(charId);
        if (character != null) {
            EditText etBackstory = view.findViewById(R.id.etBackstory);
            EditText etNotes = view.findViewById(R.id.etNotes);
            EditText etAppearance = view.findViewById(R.id.etAppearance);

            etBackstory.setText(character.getBackstory());
            etNotes.setText(character.getNotes());
            etAppearance.setText(character.getAppearance());

            Button btnSave = view.findViewById(R.id.btnSaveNotes);
            btnSave.setOnClickListener(v -> {
                character.setBackstory(etBackstory.getText().toString());
                character.setNotes(etNotes.getText().toString());
                character.setAppearance(etAppearance.getText().toString());
                SessionManager.getInstance().saveCharacter(character);
                Toast.makeText(getContext(), "Сохранено", Toast.LENGTH_SHORT).show();
            });
        }
        return view;
    }
}
