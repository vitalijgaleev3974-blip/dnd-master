package com.dndmaster.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dndmaster.R;
import com.dndmaster.models.GameSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {

    public interface SessionClickListener {
        void onSessionClick(GameSession session);
        void onSessionDelete(GameSession session);
    }

    private List<GameSession> sessions;
    private SessionClickListener listener;

    public SessionAdapter(List<GameSession> sessions, SessionClickListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameSession session = sessions.get(position);
        holder.tvName.setText(session.getName());
        holder.tvProvider.setText(session.getAiProvider().toUpperCase());
        holder.tvPlayers.setText(session.getCharacters().size() + " персонажей");

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(session.getLastPlayedAt())));

        holder.itemView.setOnClickListener(v -> listener.onSessionClick(session));
        holder.btnDelete.setOnClickListener(v -> listener.onSessionDelete(session));
    }

    @Override
    public int getItemCount() { return sessions.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvProvider, tvPlayers, tvDate;
        ImageButton btnDelete;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvSessionName);
            tvProvider = v.findViewById(R.id.tvProvider);
            tvPlayers = v.findViewById(R.id.tvPlayers);
            tvDate = v.findViewById(R.id.tvDate);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
