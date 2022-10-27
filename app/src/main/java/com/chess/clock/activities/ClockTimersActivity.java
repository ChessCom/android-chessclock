package com.chess.clock.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.chess.clock.R;
import com.chess.clock.dialog.AdjustTimeDialogFragment;
import com.chess.clock.engine.CountDownTimer;
import com.chess.clock.engine.Stage;
import com.chess.clock.views.ClockButton;
import com.chess.clock.views.ClockMenu;
import com.chess.clock.views.ViewUtils;

public class ClockTimersActivity extends TimerServiceActivity implements AdjustTimeDialogFragment.TimeAdjustmentsListener {

    private static final String TAG = ClockTimersActivity.class.getName();
    /**
     * FRAGMENT TAGS
     */
    private static final String TAG_RESET_DIALOG_FRAGMENT = "ResetDialogFragment";
    /**
     * UI saveInstance Bundle Keys.
     */
    private static final String STATE_TIMERS_KEY = "STATE_TIMERS_KEY";
    private static final String STATE_TIMERS_PREVIOUS_PAUSE_KEY = "STATE_TIMERS_PREVIOUS_PAUSE_KEY";
    private static final String STATE_LAST_TIME_PAUSED_ACTIVITY_KEY = "LAST_TIME_PAUSED_ACTIVITY_KEY";
    /**
     * Shared Preferences Keys.
     */
    private static final String SP_KEY_TIMERS_STATE = "timersState";
    private static final String SP_KEY_TIMERS_STATE_PREVIOUS_TO_PAUSE = "timersStatePreviousToPause";
    /**
     * Settings Activity request code
     */
    private final int SETTINGS_REQUEST_CODE = 1;

    private ClockSoundManager soundManager;

    /**
     * UI
     */
    private ClockButton playerOneButton;
    private ClockButton playerTwoButton;
    private ClockMenu clockMenu;
    /**
     * Utils
     */
    private long mTimeStampOnPauseActivity;
    private View mDecorView;
    private TimersState mTimersState;
    private final CountDownTimer.Callback playerOneCallback = new CountDownTimer.Callback() {
        @Override
        public void onClockTimeUpdate(long millisUntilFinished) {
            playerOneButton.setTime(millisUntilFinished);
        }

        @Override
        public void onClockFinish() {
            Log.i(TAG, "Player one loses");
            mTimersState = TimersState.PLAYER_ONE_FINISHED;
            soundManager.playSound(ClockSound.GAME_FINISHED);
            updateUIState();
        }

        @Override
        public void onStageUpdate(Stage stage, String timeControlName) {
            playerOneButton.updateStage(stage.getId(), timeControlName);
        }

        @Override
        public void onMoveCountUpdate(int moves) {
            playerOneButton.setMoves(moves);
        }

        @Override
        public void onTotalStageNumber(int stagesNumber) {
            playerOneButton.setStages(stagesNumber);
        }
    };
    private final CountDownTimer.Callback playerTwoCallback = new CountDownTimer.Callback() {
        @Override
        public void onClockTimeUpdate(long millisUntilFinished) {
            playerTwoButton.setTime(millisUntilFinished);
        }

        @Override
        public void onClockFinish() {
            Log.i(TAG, "Player two loses");
            mTimersState = TimersState.PLAYER_TWO_FINISHED;
            soundManager.playSound(ClockSound.GAME_FINISHED);
            updateUIState();
        }

        @Override
        public void onStageUpdate(Stage stage, String timeControlName) {
            playerTwoButton.updateStage(stage.getId(), timeControlName);
        }

        @Override
        public void onMoveCountUpdate(int moves) {
            playerTwoButton.setMoves(moves);
        }

        @Override
        public void onTotalStageNumber(int stagesNumber) {
            playerTwoButton.setStages(stagesNumber);
        }
    };
    private TimersState mTimersStatePreviousToPause;

    /**
     * Update UI according to Settings Activity return code.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {

            // Both states at pause means it's the beginning of the game.
            mTimersState = TimersState.PAUSED;
            mTimersStatePreviousToPause = TimersState.PAUSED;

            // Resetting timer state on shared preferences is mandatory here. Otherwise, the user
            // would press back now and when returning it would resume the previous deprecated state.
            saveTimersState();

            updateUIState();
        }
    }

    /**
     * Called when Activity is created.
     *
     * @param savedInstanceState UI state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen for pre-kitkat
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion < Build.VERSION_CODES.KITKAT) {
            boolean isFullScreen = appData.getClockFullScreen();
            if (isFullScreen) {
                hideStatusBar();
            } else {
                showStatusBar();
            }
        }

        int layout = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ?
                getProperLandscapeLayout() : R.layout.activity_clock_timers;
        setContentView(layout);

        mDecorView = getWindow().getDecorView();
        soundManager = new ClockSoundManagerImpl();
        soundManager.init(getApplicationContext());

        // Keep screen ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Save reference of UI widgets.
        initWidgetReferences();

        // Check for configuration change to resume timers state.
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else {
            // Reset timers state to PAUSED
            mTimersState = TimersState.PAUSED;
            mTimersStatePreviousToPause = TimersState.PAUSED;
        }
    }

    /**
     * Set to immersive mode since Build.VERSION_CODES.KITKAT
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        boolean isFullScreen = appData.getClockFullScreen();
        if (hasFocus && currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            if (isFullScreen) {
                mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                mDecorView.setSystemUiVisibility(0);
            }
        } else if (hasFocus) {
            if (isFullScreen) {
                hideStatusBar();
            } else {
                showStatusBar();
            }
        }
    }

    /**
     * Get different layouts for left and right landscape rotation.
     *
     * @return Layout to be inflated.
     */
    private int getProperLandscapeLayout() {

        Display display = ((WindowManager)
                getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        switch (display.getRotation()) {
            case Surface.ROTATION_90:
            case Surface.ROTATION_180:
                Log.d(TAG, "Getting Landscape Layout for ROTATION_90 || ROTATION_180");
                return R.layout.activity_clock_timers_reversed;
            case Surface.ROTATION_0:
            case Surface.ROTATION_270:
            default:
                Log.d(TAG, "Getting default Landscape Layout");
                return R.layout.activity_clock_timers;
        }
    }

    /**
     * Restore Clock Timer state from saved Instance State bundle.
     */
    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(STATE_TIMERS_KEY)) {
            int state = savedInstanceState.getInt(STATE_TIMERS_KEY);
            mTimersState = TimersState.fromInteger(state);
        }
        if (savedInstanceState.containsKey(STATE_TIMERS_PREVIOUS_PAUSE_KEY)) {
            int state = savedInstanceState.getInt(STATE_TIMERS_PREVIOUS_PAUSE_KEY);
            mTimersStatePreviousToPause = TimersState.fromInteger(state);
        }

        // Set play/pause toggle btn
        if (mTimersState != TimersState.PAUSED) {
            clockMenu.showPause();
        }

        // Restore time stamp when onPause() was called.
        if (savedInstanceState.containsKey(STATE_LAST_TIME_PAUSED_ACTIVITY_KEY)) {
            mTimeStampOnPauseActivity = savedInstanceState.getLong(STATE_LAST_TIME_PAUSED_ACTIVITY_KEY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        soundManager.setSoundsEnabled(appData.areSoundsEnabled());
        updateUIState();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mTimeStampOnPauseActivity = System.currentTimeMillis();

        // Store current timer state and state previous to pause in case we want to
        // resume the clock if this Activity is bound to a already started Service.
        saveTimersState();

        pauseClock();

        appData.setSoundsEnabled(soundManager.areSoundsEnabled());
    }

    @Override
    public void onBackPressed() {

        if (serviceBound && (mTimersState != TimersState.PAUSED)) {
            pauseClock();
        }

        super.onBackPressed();
    }

    @Override
    void bindUiOnServiceConnected() {
        clockService.setPlayerOneListener(playerOneCallback);
        clockService.setPlayerTwoListener(playerTwoCallback);

			/*
            Check if Service is already started. Started Service means that Time Controls were set in the clock.
			If no time control were set, start a new Service with last used Time Controls.
			*/
        if (!clockService.isServiceStarted()) {
            startLastTimeControlSafely();
        } else {
				/*
				Service is already started. Every time this Activity goes to background, the
				timers state are saved on shared preferences, before the clock is paused. And
				here, they must be restored in order to resume state properly.
				*/
            restoreTimersState();

				/*
				Only update UI if game was finished OR in the case of configuration change the
				game was previously running.
				*/
            if (mTimersState == TimersState.PLAYER_ONE_FINISHED || mTimersState == TimersState.PLAYER_TWO_FINISHED) {
                updateUIState();
            } else {
					/*
					Only resume clock if elapsed time was less than 2 seconds since last pause.
					This will serve to filter orientation changes only.
					*/
                if (mTimeStampOnPauseActivity > 0) {
                    long elapsedTime = System.currentTimeMillis() - mTimeStampOnPauseActivity;
                    Log.v(TAG, "Configuration change lasted " + elapsedTime + " milliseconds.");
                    if (elapsedTime < 2000 && (mTimersState == TimersState.PLAYER_TWO_RUNNING ||
                            mTimersState == TimersState.PLAYER_ONE_RUNNING)) {
                        resumeClockSafely();
                        updateUIState();
                    } else {
                        // If pause took too long, reset state to paused.
                        pauseClock();
                    }
                }
            }
        }
    }

    /**
     * Reset the clock.
     */
    public void resetClock() {
        if (serviceBound) {

            clockService.resetClock();

            // Both states at pause means it's the beginning of the game.
            mTimersState = TimersState.PAUSED;
            mTimersStatePreviousToPause = TimersState.PAUSED;
        }
        updateUIState();
        soundManager.playSound(ClockSound.RESET_CLOCK);
    }

    /**
     * Pause button visibility.
     */
    public void pauseClock() {

        if (serviceBound) {
            if (mTimersState == TimersState.PLAYER_ONE_RUNNING || mTimersState == TimersState.PLAYER_TWO_RUNNING) {
                Log.i(TAG, "Clock paused.");
                mTimersStatePreviousToPause = mTimersState;
                mTimersState = TimersState.PAUSED;
                Log.d(TAG, "Previous state: " + mTimersStatePreviousToPause +
                        " , current state: " + mTimersState);
                clockService.pauseClock();

                updateUIState();
            }
        }
    }

    /**
     * Save widgets state before configuration change.
     *
     * @param saveInstanceState Bundle with current widgets state.
     */
    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        Log.v(TAG, "Saving UI State on instance Bundle ");
        saveInstanceState.putInt(STATE_TIMERS_KEY, mTimersState.getValue());
        saveInstanceState.putInt(STATE_TIMERS_PREVIOUS_PAUSE_KEY, mTimersStatePreviousToPause.getValue());
        saveInstanceState.putLong(STATE_LAST_TIME_PAUSED_ACTIVITY_KEY, mTimeStampOnPauseActivity);
        super.onSaveInstanceState(saveInstanceState);
    }

    class ClockClickListener implements ClockButton.ClockClickListener {
        private final boolean firstPlayer;

        ClockClickListener(boolean firstPlayer) {
            this.firstPlayer = firstPlayer;
        }

        @Override
        public void onClickClock() {
            onPlayerClockClicked(firstPlayer);
        }

        @Override
        public void onClickOptions() {
            long time = firstPlayer ? clockService.firstPlayerTime() : clockService.secondPlayerTime();
            AdjustTimeDialogFragment
                    .newInstance(time, firstPlayer)
                    .show(getSupportFragmentManager(), AdjustTimeDialogFragment.TAG);
        }
    }

    /**
     * Save references and init listeners of inflated widgets.
     */
    protected void initWidgetReferences() {

        // Save references
        playerOneButton = findViewById(R.id.playerOneClockContainer);
        playerTwoButton = findViewById(R.id.playerTwoClockContainer);
        clockMenu = findViewById(R.id.menu_container);

        // Set listeners
        playerOneButton.setClockButtonClickListener(new ClockClickListener(true));
        playerTwoButton.setClockButtonClickListener(new ClockClickListener(false));
        clockMenu.setListener(new ClockMenu.MenuClickListener() {
            @Override
            public void timeSettingsClicked() {
                // Pause clock before going to settings menu
                pauseClock();

                Intent settingsIntent = new Intent(ClockTimersActivity.this, TimerSettingsActivity.class);
                startActivityForResult(settingsIntent, SETTINGS_REQUEST_CODE);
                overridePendingTransition(R.anim.right_to_left_full, R.anim.right_to_left_out);
            }

            @Override
            public void playPauseClicked() {
                if (mTimersState == TimersState.PAUSED) {
                    onPlayerClockClicked(mTimersStatePreviousToPause != TimersState.PLAYER_ONE_RUNNING);
                } else {
                    pauseClock();
                }
            }

            @Override
            public void resetClicked() {
                if (mTimersState == TimersState.PLAYER_ONE_RUNNING
                        || mTimersState == TimersState.PLAYER_TWO_RUNNING) {
                    pauseClock();
                }
                showResetClockDialog();
            }

            @Override
            public void soundClicked() {
                soundManager.toggleSound();
                updateUIState();
            }
        });
    }

    /**
     * Change buttons and timers UI according to TimerState.
     */
    private void updateUIState() {
        Log.d(TAG, "Updating UI state to: " + mTimersState);
        switch (mTimersState) {
            case PAUSED:
                playerOneButton.updateUi(selectedTheme, ClockButton.State.IDLE);
                playerTwoButton.updateUi(selectedTheme, ClockButton.State.IDLE);
                clockMenu.showPlay();
                break;
            case PLAYER_ONE_RUNNING:
                playerOneButton.updateUi(selectedTheme, ClockButton.State.RUNNING);
                playerTwoButton.updateUi(selectedTheme, ClockButton.State.LOCKED);
                clockMenu.showPause();
                break;
            case PLAYER_TWO_RUNNING:
                playerOneButton.updateUi(selectedTheme, ClockButton.State.LOCKED);
                playerTwoButton.updateUi(selectedTheme, ClockButton.State.RUNNING);
                clockMenu.showPause();
                break;
            case PLAYER_ONE_FINISHED:
                playerOneButton.updateUi(selectedTheme, ClockButton.State.FINISHED);
                playerTwoButton.updateUi(selectedTheme, ClockButton.State.IDLE);
                clockMenu.hidePlayPauseBtn();
                break;
            case PLAYER_TWO_FINISHED:
                playerOneButton.updateUi(selectedTheme, ClockButton.State.IDLE);
                playerTwoButton.updateUi(selectedTheme, ClockButton.State.FINISHED);
                clockMenu.hidePlayPauseBtn();
                break;
        }
        clockMenu.updateSoundIcon(soundManager.areSoundsEnabled());
    }

    private void onPlayerClockClicked(boolean firstPlayer) {
        TimersState playerTimerRunning = firstPlayer ? TimersState.PLAYER_ONE_RUNNING : TimersState.PLAYER_TWO_RUNNING;
        TimersState otherPlayerTimerRunning = firstPlayer ? TimersState.PLAYER_TWO_RUNNING : TimersState.PLAYER_ONE_RUNNING;
        TimersState playerTimerFinished = firstPlayer ? TimersState.PLAYER_ONE_FINISHED : TimersState.PLAYER_TWO_FINISHED;
        TimersState otherPlayerTimerFinished = firstPlayer ? TimersState.PLAYER_TWO_FINISHED : TimersState.PLAYER_ONE_FINISHED;
        String logPlayerNumber = firstPlayer ? "one" : "two";

        Log.i(TAG, "Player " + logPlayerNumber + " pressed the clock with state: " + mTimersState + " (previous: " + mTimersStatePreviousToPause + ")");
        // Set pause btn visibility
        if (mTimersState == TimersState.PAUSED && mTimersStatePreviousToPause == TimersState.PAUSED) {
            clockMenu.showPause();
        }
        if (mTimersState == playerTimerRunning || mTimersState == TimersState.PAUSED) {
            // If bound to clock service, press clock and update UI state.
            if (serviceBound) {
                // First or continuation move
                if ((mTimersState == TimersState.PAUSED && mTimersStatePreviousToPause == TimersState.PAUSED) ||
                        (mTimersState == TimersState.PAUSED && mTimersStatePreviousToPause == playerTimerRunning) ||
                        mTimersState == playerTimerRunning) {
                    if (firstPlayer) clockService.pressPlayerOneClock();
                    else clockService.pressPlayerTwoClock();
                    mTimersState = otherPlayerTimerRunning;
                }
                // Resuming clock
                else {
                    resumeClockSafely();
                    mTimersState = mTimersStatePreviousToPause;
                    mTimersStatePreviousToPause = TimersState.PAUSED;
                }
                soundManager.playSound(ClockSound.PLAYER_ONE_MOVE);
                updateUIState();
            }
        } else if (mTimersState == playerTimerFinished ||
                mTimersState == otherPlayerTimerFinished) {
            showResetClockDialog();
        }
    }

    private void showResetClockDialog() {
        ResetClockDialogFragment resetClockDialog = new ResetClockDialogFragment();
        resetClockDialog.show(getSupportFragmentManager(), TAG_RESET_DIALOG_FRAGMENT);
    }

    /**
     * Save Timers State on Shared Preferences
     */
    public void saveTimersState() {

        Log.v(TAG, "Saving timer state: " + mTimersState + ", previous: " + mTimersStatePreviousToPause);

        SharedPreferences mySharedPreferences = getPreferences(Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();

        editor.putInt(SP_KEY_TIMERS_STATE, mTimersState.getValue());
        editor.putInt(SP_KEY_TIMERS_STATE_PREVIOUS_TO_PAUSE, mTimersStatePreviousToPause.getValue());

        editor.apply();
    }

    /**
     * Restore Timers State.
     */
    public void restoreTimersState() {

        SharedPreferences sp = getPreferences(Activity.MODE_PRIVATE);

        mTimersState = TimersState.fromInteger(sp.getInt(SP_KEY_TIMERS_STATE, 0));
        mTimersStatePreviousToPause = TimersState.fromInteger(sp.getInt(SP_KEY_TIMERS_STATE_PREVIOUS_TO_PAUSE, 0));

        Log.v(TAG, "Retrieving timer state: " + mTimersState + ", previous: " + mTimersStatePreviousToPause);
    }

    @Override
    public void onTimeAdjustmentsConfirmed(long timeMs, boolean firstPlayer) {
        if (firstPlayer) {
            clockService.setFirstPlayerTime(timeMs);
            playerOneButton.setTime(timeMs);
        } else {
            clockService.setSecondPlayerTime(timeMs);
            playerTwoButton.setTime(timeMs);
        }
    }

    /**
     * Timers state.
     */
    public enum TimersState {
        PAUSED(0),
        PLAYER_ONE_RUNNING(1),
        PLAYER_TWO_RUNNING(2),
        PLAYER_ONE_FINISHED(3),
        PLAYER_TWO_FINISHED(4);

        private final int value;

        TimersState(int value) {
            this.value = value;
        }

        public static TimersState fromInteger(int type) {
            switch (type) {
                case 0:
                    return PAUSED;
                case 1:
                    return PLAYER_ONE_RUNNING;
                case 2:
                    return PLAYER_TWO_RUNNING;
                case 3:
                    return PLAYER_ONE_FINISHED;
                case 4:
                    return PLAYER_TWO_FINISHED;
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Reset dialog to be displayed when user presses the Reset widget.
     */
    public static class ResetClockDialogFragment extends DialogFragment {

        public ResetClockDialogFragment() {
            super();
        }

        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.WhiteButtonsDialogTheme);
            builder.setMessage(R.string.dialog_reset_clock)
                    .setPositiveButton(R.string.dialog_yes, (dialog, id) -> {
                        // Reset the clock
                        ClockTimersActivity activity = (ClockTimersActivity) getActivity();
                        if (activity != null) {
                            activity.resetClock();
                        }
                    })
                    .setNegativeButton(R.string.dialog_no, (dialog, id) -> {
                        // Resume the clock
                    });
            // Create the AlertDialog object and return it
            Dialog dialog = builder.create();
            ViewUtils.setLargePopupMessageTextSize(dialog, getResources());
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
    }
}