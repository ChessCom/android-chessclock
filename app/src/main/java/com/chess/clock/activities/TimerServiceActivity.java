package com.chess.clock.activities;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.chess.clock.engine.TimeControlParser;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.manager.ChessClockManager;

public abstract class TimerServiceActivity extends BaseActivity implements LifecycleObserver {

    private static final String TAG = TimerServiceActivity.class.getName();

    /**
     * Chess clock local service (clock engine).
     */
    protected ChessClockManager clockManager;


    /**
     * Start clock service with last know time control or default one.
     */
    protected void startLastTimeControlSafely() {
        if (isAtLeastOnResume()) {
            startServiceWithLastTimeControlInternal();
        } else {
            Log.d(TAG, "Starting of service postponed.");
            getLifecycle().addObserver(new DefaultLifecycleObserver() {
                @Override
                public void onResume(@NonNull LifecycleOwner owner) {
                    DefaultLifecycleObserver.super.onResume(owner);
                    startServiceWithLastTimeControlInternal();
                    getLifecycle().removeObserver(this);
                }
            });
        }
    }

    protected void resumeClockSafely() {
        if (isAtLeastOnResume()) {
            clockManager.resumeClock();
        } else {
            Log.d(TAG, "Resuming of clock postponed.");
            getLifecycle().addObserver(new DefaultLifecycleObserver() {
                @Override
                public void onResume(@NonNull LifecycleOwner owner) {
                    DefaultLifecycleObserver.super.onResume(owner);
                    Log.d(TAG, "Resume clock.");
                    clockManager.resumeClock();
                    getLifecycle().removeObserver(this);
                }
            });
        }
    }

    private void startServiceWithLastTimeControlInternal() {
        TimeControlWrapper selectedControl = TimeControlParser.getLastTimeControlOrDefault(this);
        getClockManager().setupClock(selectedControl);
        Log.d(TAG, "Start service.");
    }

    private boolean isAtLeastOnResume() {
        Lifecycle.State currentState = getLifecycle().getCurrentState();
        return currentState.isAtLeast(Lifecycle.State.RESUMED);
    }

    abstract void bindUiOnServiceConnected();
}
