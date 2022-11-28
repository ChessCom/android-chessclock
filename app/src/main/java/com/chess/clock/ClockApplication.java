package com.chess.clock;

import android.app.Application;

import com.chess.clock.manager.ChessClockManager;
import com.chess.clock.manager.ChessClockManagerImpl;

public class ClockApplication extends Application {
    private static ChessClockManager clockManager;

    public static ChessClockManager getClockManager() {
        if (clockManager == null) {
            clockManager = new ChessClockManagerImpl();
        }
        return clockManager;
    }
}
