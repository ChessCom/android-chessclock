package com.chess.clock.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.chess.clock.service.ChessClockLocalService;

public abstract class TimerServiceActivity extends BaseActivity {

    private static final String TAG = TimerServiceActivity.class.getName();

    /**
     * Chess clock local service (clock engine).
     */
    protected ChessClockLocalService clockService;

    /**
     * True when this activity is bound to chess clock service.
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

    abstract void bindUiOnServiceConnected();
}
