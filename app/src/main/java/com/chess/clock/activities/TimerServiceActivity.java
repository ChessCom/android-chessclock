package com.chess.clock.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.chess.clock.engine.TimeControl;
import com.chess.clock.engine.TimeControlParser;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.service.ChessClockLocalService;

public abstract class TimerServiceActivity extends BaseActivity implements LifecycleObserver {

    private static final String TAG = TimerServiceActivity.class.getName();

    /**
     * Chess clock local service (clock engine).
     */
    protected ChessClockLocalService clockService;

    /**
     * State
     */
    protected boolean serviceBound = false;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ChessClockLocalService.ChessClockLocalServiceBinder binder
                    = (ChessClockLocalService.ChessClockLocalServiceBinder) service;
            clockService = binder.getService();
            serviceBound = true;

            Log.i(TAG, "Service bound connected");

            bindUiOnServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
            Log.i(TAG, "Service bound disconnected");
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        // Bind to Local Chess clock Service.
        Intent intent = new Intent(this, ChessClockLocalService.class);

        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        Log.i(TAG, "Binding UI to Chess Clock Service.");
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unbind from the chess clock service.
        if (serviceBound) {
            unbindService(mConnection);
            serviceBound = false;
            Log.i(TAG, "Unbinding UI from Chess Clock Service.");
        }
    }

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
            clockService.resumeClock();
        } else {
            Log.d(TAG, "Resuming of clock postponed.");
            getLifecycle().addObserver(new DefaultLifecycleObserver() {
                @Override
                public void onResume(@NonNull LifecycleOwner owner) {
                    DefaultLifecycleObserver.super.onResume(owner);
                    Log.d(TAG, "Resume clock.");
                    clockService.resumeClock();
                    getLifecycle().removeObserver(this);
                }
            });
        }
    }

    private void startServiceWithLastTimeControlInternal() {
        Context ctx = this;
        TimeControlWrapper selectedControl = TimeControlParser.getLastTimeControlOrDefault(ctx);
        TimeControl playerOne = selectedControl.getTimeControlPlayerOne();
        TimeControl playerTwo = selectedControl.getTimeControlPlayerTwo();

        Intent startServiceIntent =
                ChessClockLocalService.getChessClockServiceIntent(ctx, playerOne, playerTwo);
        ctx.startService(startServiceIntent);
        Log.d(TAG, "Start service.");
    }

    private boolean isAtLeastOnResume() {
        Lifecycle.State currentState = getLifecycle().getCurrentState();
        return currentState.isAtLeast(Lifecycle.State.RESUMED);
    }

    abstract void bindUiOnServiceConnected();
}
