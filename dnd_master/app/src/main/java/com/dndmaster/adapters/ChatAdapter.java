package com.dndmaster.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dndmaster.R;
import com.dndmaster.models.ChatMessage;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MASTER = 0;
    private static final int TYPE_PLAYER = 1;
    private static final int TYPE_SYSTEM = 2;
    private static final int TYPE_DICE = 3;

    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        switch (msg.getRole()) {
            case ChatMessage.ROLE_MASTER: return TYPE_MASTER;
            case ChatMessage.ROLE_SYSTEM: return TYPE_SYSTEM;
            case ChatMessage.ROLE_DICE: return TYPE_DICE;
            default: return TYPE_PLAYER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        switch (viewType) {
            case TYPE_MASTER:
                view = inflater.inflate(R.layout.item_chat_master, parent, false);
                return new MasterViewHolder(view);
            case TYPE_SYSTEM:
                view = inflater.inflate(R.layout.item_chat_system, parent, false);
                return new SystemViewHolder(view);
            case TYPE_DICE:
                view = inflater.inflate(R.layout.item_chat_dice, parent, false);
                return new DiceViewHolder(view);
            default:
                view = inflater.inflate(R.layout.item_chat_player, parent, false);
                return new PlayerViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        if (holder instanceof MasterViewHolder) {
            ((MasterViewHolder) holder).tvContent.setText(msg.getContent());
        } else if (holder instanceof PlayerViewHolder) {
            PlayerViewHolder pvh = (PlayerViewHolder) holder;
            pvh.tvContent.setText(msg.getContent());
            if (msg.getCharacterName() != null) {
                pvh.tvCharName.setText(msg.getCharacterName());
                pvh.tvCharName.setVisibility(View.VISIBLE);
            }
        } else if (holder instanceof SystemViewHolder) {
            ((SystemViewHolder) holder).tvContent.setText(msg.getContent());
        } else if (holder instanceof DiceViewHolder) {
            DiceViewHolder dvh = (DiceViewHolder) holder;
            dvh.tvContent.setText(msg.getContent());
            if (msg.getCharacterName() != null) {
                dvh.tvCharName.setText(msg.getCharacterName());
            }
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    public void addMessage(ChatMessage msg) {
        messages.add(msg);
        notifyItemInserted(messages.size() - 1);
    }

    public void removeLastMessage() {
        if (!messages.isEmpty()) {
            int last = messages.size() - 1;
            messages.remove(last);
            notifyItemRemoved(last);
        }
    }

    static class MasterViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        MasterViewHolder(View v) { super(v); tvContent = v.findViewById(R.id.tvContent); }
    }

    static class PlayerViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvCharName;
        PlayerViewHolder(View v) { super(v); tvContent = v.findViewById(R.id.tvContent); tvCharName = v.findViewById(R.id.tvCharName); }
    }

    static class SystemViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        SystemViewHolder(View v) { super(v); tvContent = v.findViewById(R.id.tvContent); }
    }

    static class DiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvCharName;
        DiceViewHolder(View v) { super(v); tvContent = v.findViewById(R.id.tvContent); tvCharName = v.findViewById(R.id.tvCharName); }
    }
}
