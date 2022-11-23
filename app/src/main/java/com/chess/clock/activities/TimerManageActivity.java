package com.chess.clock.activities;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;

import com.chess.clock.ClockApplication;
import com.chess.clock.engine.TimeControlParser;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.manager.ChessClockManager;

public abstract class TimerManageActivity extends BaseActivity implements LifecycleObserver {

    private static final String TAG = TimerManageActivity.class.getName();

    public ChessClockManager getClockManager() {
        return ClockApplication.getClockManager();
    }
}
