package com.dndmaster.managers;

import android.content.Context;

public class GameDataManager {
    private static GameDataManager instance;
    private Context context;

    public static GameDataManager getInstance() {
        if (instance == null) instance = new GameDataManager();
        return instance;
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
    }
}
