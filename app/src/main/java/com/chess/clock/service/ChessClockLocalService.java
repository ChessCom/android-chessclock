package com.chess.clock.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.chess.clock.R;
import com.chess.clock.activities.ClockTimersActivity;
import com.chess.clock.engine.CountDownTimer;
import com.chess.clock.engine.time.TimeControl;
import com.chess.clock.util.Args;

// Get access to the app resources, since this class is in a sub-package.

/**
 * Background Service used to manage a Chess Clock.
 */
public class ChessClockLocalService extends Service {

    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    /**
     * Logs stuff
     */
    private static final String TAG = ChessClockLocalService.class.getName();
    private static final String WAKE_LOCK_TAG = ChessClockLocalService.class.getName() + "WakeLock";
    private static final boolean VERBOSE = true;

    /**
     * Action that starts the ChessClockLocalService.
     */
    private static final String ACTION_START_CHESS_CLOCK = "com.chess.clock.service.startclock";

    /**
     * Optional extras supplied in ACTION_START_CHESS_CLOCK intent.
     */
    private static final String EXTRA_PLAYER_ONE_TIME_CONTROL = "player_one_time_control";
    private static final String EXTRA_PLAYER_TWO_TIME_CONTROL = "player_two_time_control";

    /**
     * Local Service Binder.
     */
    private final IBinder mBinder = new ChessClockLocalServiceBinder();

    /**
     * The interval along the way for each clock tick in milliseconds.
     */
    private final long DEFAULT_COUNT_DOWN_INTERVAL = 100;

    /**
     * True if the game is on-going. Note: pause state still counts as game running.
     */
    private boolean mChessGameRunning;

    /**
     * Count down timer callback implementation to run when a timer is finished.
     * Removes foreground status and notification since the a player's clock is stopped.
     * Also releases wake lock acquired when the game started.
     */
    private CountDownTimer.FinishCallback mFinishListener = new CountDownTimer.FinishCallback() {
        @Override
        public void onClockFinish() {
            stopForeground(true);
            mChessGameRunning = false;

            mPlayerOneTimer.stop();
            mPlayerTwoTimer.stop();

            if (VERBOSE) Log.i(TAG, "#" + this.hashCode() + " Service and Game finished.");
        }
    };

    /**
     * True if the Service was started already.
     */
    private boolean mIsServiceStarted;

    /**
     * Count down timers for both players.
     */
    private CountDownTimer mPlayerOneTimer;
    private CountDownTimer mPlayerTwoTimer;

    /**
     * Builds the Intent to be supplied to Context.startService(Intent).
     *
     * @param playerOneTimeControl Time Control of player One.
     * @param playerTwoTimeControl Time Control of player Two.
     * @return The Intent used to start this service.
     */
    public static Intent getChessClockServiceIntent(Context context,
                                                    TimeControl playerOneTimeControl,
                                                    TimeControl playerTwoTimeControl) {

        Intent intent = new Intent(context, ChessClockLocalService.class);
        intent.setAction(ACTION_START_CHESS_CLOCK);

        if (playerOneTimeControl != null && playerTwoTimeControl != null) {
            intent.putExtra(EXTRA_PLAYER_ONE_TIME_CONTROL, playerOneTimeControl);
            intent.putExtra(EXTRA_PLAYER_TWO_TIME_CONTROL, playerTwoTimeControl);
        }

        return intent;
    }

    /**
     * The Local Service is being created.
     */
    @Override
    public void onCreate() {
        if (VERBOSE) Log.d(TAG, "#" + this.hashCode() + " created. ");

        mChessGameRunning = false;
        mPlayerOneTimer = new CountDownTimer(DEFAULT_COUNT_DOWN_INTERVAL);
        mPlayerTwoTimer = new CountDownTimer(DEFAULT_COUNT_DOWN_INTERVAL);
        mPlayerOneTimer.setFinishListener(mFinishListener);
        mPlayerTwoTimer.setFinishListener(mFinishListener);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (VERBOSE) Log.d(TAG, "#" + this.hashCode() + " destroyed.");
    }

    /**
     * Return the communication channel to the Service.
     *
     * @param intent The Intent that was used to bind to this service.
     * @return IBinder through which clients can call on to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling startService(Intent)
     *
     * @param intent  The Intent supplied to startService(Intent), as given.
     * @param flags   Additional data about this start request.
     * @param startId A unique integer representing this specific request to start.
     * @return The return value indicates what semantics the system should use for the service's current started state.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            if (VERBOSE) Log.d(TAG, "#" + this.hashCode() + " started. Id:" + startId);

            String action = intent.getAction();
            TimeControl playerOneTimeControlExtra = intent.getParcelableExtra(EXTRA_PLAYER_ONE_TIME_CONTROL);
            TimeControl playerTwoTimeControlExtra = intent.getParcelableExtra(EXTRA_PLAYER_TWO_TIME_CONTROL);

            if (ACTION_START_CHESS_CLOCK.equals(action)) {

                // Supply time controls if provided on starting the Service.
                if (playerOneTimeControlExtra != null && playerTwoTimeControlExtra != null) {
                    setupTimeControl(playerOneTimeControlExtra, playerTwoTimeControlExtra);
                }
            }

            mIsServiceStarted = true;

        } else {
            if (VERBOSE) Log.w(TAG, "Restarted. New #" + this.hashCode());
        }

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    /**
     * Set the Service with Foreground priority (harder to get killed by OS).
     * Add a status bar notification of the clock running.
     */
    private void startServiceInForeground() {

        Intent notificationIntent = new Intent(getApplicationContext(), ClockTimersActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW; // No sound is needed
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }


        Notification notification =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setContentIntent(contentIntent)
                        .setContentTitle(getText(R.string.foreground_service_started))
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .build();

        Log.v(TAG, "Acquiring wake lock");

        startForeground(R.string.foreground_service_started, notification);
    }

    /**
     * *************************************
     * Bound Service public methods.
     * /****************************************
     * <p/>
     * /**
     * Registers a callback to be invoked when player One status updates.
     *
     * @param listener The callback that will run
     */
    public void setPlayerOneListener(CountDownTimer.Callback listener) {
        if (listener != null) {
            if (VERBOSE) Log.d(TAG, "#" + this.hashCode()
                    + " (1) registered listener: #" + listener.hashCode() + ".");
        }
        mPlayerOneTimer.setClockTimerListener(listener);
    }

    /**
     * Registers a callback to be invoked when player One status updates.
     *
     * @param listener The callback that will run
     */
    public void setPlayerTwoListener(CountDownTimer.Callback listener) {
        if (listener != null) {
            if (VERBOSE) Log.d(TAG, "#" + this.hashCode()
                    + " (2) registered listener: #" + listener.hashCode() + ".");
        }
        mPlayerTwoTimer.setClockTimerListener(listener);
    }

    /**
     * Add new time controls to Chess Clock and resetTimeControl timer. The only way to provide
     * time controls to the service is through starting command with ACTION_START_CHESS_CLOCK.
     *
     * @param playerOneTimeControl Time Control of player One.
     * @param playerTwoTimeControl Time Control of player Two.
     * @throws java.lang.NullPointerException If playerOneTimeControl is not provided.
     * @throws java.lang.NullPointerException If playerTwoTimeControl is not provided.
     * @see #getChessClockServiceIntent(Context, TimeControl, TimeControl)
     */
    private void setupTimeControl(TimeControl playerOneTimeControl, TimeControl playerTwoTimeControl) {
        Args.checkForNull(playerOneTimeControl);
        Args.checkForNull(playerTwoTimeControl);

        // Sanity check..
        if (mPlayerOneTimer == null || mPlayerTwoTimer == null) {
            mPlayerOneTimer = new CountDownTimer(DEFAULT_COUNT_DOWN_INTERVAL);
            mPlayerTwoTimer = new CountDownTimer(DEFAULT_COUNT_DOWN_INTERVAL);
        }

        // Finish running game
        if (mChessGameRunning) {
            if (VERBOSE) Log.d(TAG, "Finishing current timers.");
            // Reset the clock to new time controls
            resetClock();
            mChessGameRunning = false;
        }

        mPlayerOneTimer.setTimeControl(playerOneTimeControl);
        mPlayerTwoTimer.setTimeControl(playerTwoTimeControl);

        if (VERBOSE) Log.d(TAG, "#" + this.hashCode() + " Time Control set.");

        // Remove foreground status and notification since the clocks
        // are stopped due to new time control added.
        stopForeground(true);
    }

    public boolean isServiceStarted() {
        return mIsServiceStarted;
    }

    /**
     * Notifies Chess Clock Service that Player One pressed the clock.
     */
    public void pressPlayerOneClock() {
        if (VERBOSE) Log.v(TAG, "#" + this.hashCode() + " (1) pressed the clock.");
        pressPlayerClock(mPlayerOneTimer, mPlayerTwoTimer);
    }

    /**
     * Notifies Chess Clock Service that Player Two pressed the clock.
     */
    public void pressPlayerTwoClock() {
        if (VERBOSE) Log.v(TAG, "#" + this.hashCode() + " (2) pressed the clock.");
        pressPlayerClock(mPlayerTwoTimer, mPlayerOneTimer);
    }

    /**
     * Pauses the global state of the chess clock.
     */
    public void pauseClock() {
        // The pause instruction only affects the time control which has
        // the count down timer in the state CountDownTimer.TimerState.RUNNING.
        if (mPlayerOneTimer != null && mPlayerTwoTimer != null) {

            mPlayerOneTimer.pause();
            mPlayerTwoTimer.pause();

            if (VERBOSE) Log.v(TAG, "#" + this.hashCode() + " paused the clock timers.");

            // Remove this service from foreground state, allowing it to be killed if
            // more memory is needed. The notification previously provided to
            // startForeground(int, Notification) will be removed.
            stopForeground(true);
        }
    }

    /**
     * Resumes the global state of the chess clock.
     */
    public void resumeClock() {
        if (mPlayerOneTimer != null && mPlayerTwoTimer != null) {
            mPlayerOneTimer.resume();
            mPlayerTwoTimer.resume();

            startServiceInForeground();

            if (VERBOSE) Log.v(TAG, "#" + this.hashCode() + " resumed the clock timers.");
        }
    }

    /**
     * Resets the timer and time control state of both players.
     */
    public void resetClock() {
        if (mPlayerOneTimer != null && mPlayerTwoTimer != null) {

            mPlayerOneTimer.resetTimeControl();
            mPlayerTwoTimer.resetTimeControl();

            mChessGameRunning = false;

            // Remove this service from foreground state, allowing it to be killed if
            // more memory is needed. The notification previously provided to
            // startForeground(int, Notification) will be removed.
            stopForeground(true);
        }
    }


    public String getNameOfTimeControlRunning() {
        // Note: this service considers that time control is always the same for both players.
        if (mPlayerOneTimer != null) {
            return mPlayerOneTimer.getTimeControlTitle();
        }
        return null;
    }

    /****************************************
     * Callbacks Definition.
     ***************************************/

    /**
     * Notifies the Chess Clock Service that a player made a move.
     *
     * @param playerTimer   The player timer which made a move.
     * @param opponentTimer The opponent timer.
     */
    private void pressPlayerClock(CountDownTimer playerTimer, CountDownTimer opponentTimer) {

        // Ignore clock press if timers are not initiated or opponent timer already finished.
        if (playerTimer != null && opponentTimer != null
                && !opponentTimer.isFinished() && !playerTimer.isFinished()) {

            // Game already running? stop player timer and start the opponent timer.
            if (mChessGameRunning) {
                playerTimer.stop();
                opponentTimer.start();

                if (VERBOSE) Log.d(TAG, "Move number: " + playerTimer.getTotalMoveCount()
                        + ", time left: " + formatTime(playerTimer.getTime()));
            }
            // First move: do not stop player clock to avoid invalid initial time increment.
            else {
                opponentTimer.start();
                mChessGameRunning = true;

                // As game started, set service in foreground.
                startServiceInForeground();

                if (VERBOSE) Log.i(TAG, "#" + this.hashCode() + " Game started.");
            }
        } else {
            if (VERBOSE) Log.w(TAG, "Discarded clock press due to Time Controls"
                    + " not available or game finished already.");
        }
    }

    /****************************************
     * Private stuff..
     ****************************************/

    /**
     * @param time Player time in milliseconds.
     * @return Readable String format of time.
     */
    private String formatTime(long time) {
        int s = (int) (time / 1000) % 60;
        int m = (int) ((time / (1000 * 60)) % 60);
        int h = (int) ((time / (1000 * 60 * 60)) % 24);
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    /**
     * Class used for the Service client Binder. Because we know this is a local service
     * (runs in the same process as its clients) we don't need to deal with IPC.
     */
    public class ChessClockLocalServiceBinder extends Binder {
        public ChessClockLocalService getService() {
            // Return the Service instance so clients can call Service public methods directly.
            return ChessClockLocalService.this;
        }
    }
}
