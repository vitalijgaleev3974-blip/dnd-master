package com.dndmaster;

import android.app.Application;
import com.dndmaster.managers.GameDataManager;
import com.dndmaster.managers.SessionManager;
import com.dndmaster.ai.AIManager;

public class DnDApplication extends Application {

    private static DnDApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        GameDataManager.getInstance().init(this);
        SessionManager.getInstance().init(this);
        AIManager.getInstance().init(this);
    }

    public static DnDApplication getInstance() {
        return instance;
    }
}
