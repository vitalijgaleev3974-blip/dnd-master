package com.dndmaster.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.dndmaster.R;
import com.dndmaster.managers.SessionManager;
import com.dndmaster.models.Character;
import com.dndmaster.models.Item;

public class CharacterInventoryFragment extends Fragment {
    private static final String ARG_CHAR_ID = "char_id";

    public static CharacterInventoryFragment newInstance(String charId) {
        CharacterInventoryFragment f = new CharacterInventoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHAR_ID, charId);
        f.setArguments(args);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_character_inventory, container, false);
        String charId = getArguments().getString(ARG_CHAR_ID);
        Character character = SessionManager.getInstance().getCharacter(charId);
        if (character != null) {
            // Currency
            TextView tvCurrency = view.findViewById(R.id.tvCurrency);
            tvCurrency.setText("🪙 " + character.getGoldPieces() + " зм  " +
                    character.getSilverPieces() + " см  " +
                    character.getCopperPieces() + " мм  " +
                    character.getPlatinumPieces() + " пм");

            // Items
            StringBuilder sb = new StringBuilder();
            if (character.getInventory().isEmpty()) {
                sb.append("Инвентарь пуст");
            } else {
                for (Item item : character.getInventory()) {
                    sb.append(item.isEquipped() ? "⚔️ " : "• ");
                    sb.append(item.getName());
                    if (item.getQuantity() > 1) sb.append(" x").append(item.getQuantity());
                    if (item.isMagical()) sb.append(" ✨");
                    if (item.getDamage() != null && !item.getDamage().isEmpty()) {
                        sb.append(" (").append(item.getDamage()).append(" ").append(item.getDamageType()).append(")");
                    }
                    if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                        sb.append("\n  ").append(item.getDescription());
                    }
                    sb.append("\n");
                }
            }
            ((TextView) view.findViewById(R.id.tvInventoryList)).setText(sb.toString());
        }
        return view;
    }
}
