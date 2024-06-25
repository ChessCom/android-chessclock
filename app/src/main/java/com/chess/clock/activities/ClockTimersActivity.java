package com.chess.clock.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.chess.clock.R;
import com.chess.clock.dialog.AdjustTimeDialogFragment;
import com.chess.clock.engine.ClockPlayer;
import com.chess.clock.engine.CountDownTimer;
import com.chess.clock.engine.Stage;
import com.chess.clock.engine.TimeControlParser;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.views.ClockButton;
import com.chess.clock.views.ClockMenu;
import com.chess.clock.views.ViewUtils;


public class ClockTimersActivity extends BaseActivity implements AdjustTimeDialogFragment.TimeAdjustmentsListener {

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

    private AudioManager audioManager;
    private ClockSoundManager soundManager;

    /**
     * UI
     */
    private ClockButton playerOneButton;
    private ClockButton playerTwoButton;
    private ClockMenu clockMenu;
    private View clocksDivider;

    /**
     * Utils
     */
    private View windowDecorView;
    private TimersState timerState;
    private TimersState timersStatePreviousToPause;
    private final CountDownTimer.Callback playerOneCallback = new CountDownTimer.Callback() {
        @Override
        public void onClockTimeUpdate(long millisUntilFinished) {
            playerOneButton.setTime(millisUntilFinished);
        }

        @Override
        public void onClockFinish() {
            Log.i(TAG, "Player one loses");
            timerState = TimersState.PLAYER_ONE_FINISHED;
            soundManager.playSound(ClockSound.GAME_FINISHED, audioManager);
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
            timerState = TimersState.PLAYER_TWO_FINISHED;
            soundManager.playSound(ClockSound.GAME_FINISHED, audioManager);
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


    private final ActivityResultLauncher<Intent> settingsResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Both states at pause means it's the beginning of the game.
                    timerState = TimersState.PAUSED;
                    timersStatePreviousToPause = TimersState.PAUSED;
                    updateUIState();
                }
            });

    /**
     * Called when Activity is created.
     *
     * @param savedInstanceState UI state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
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

        windowDecorView = getWindow().getDecorView();
        soundManager = new ClockSoundManagerImpl();
        soundManager.init(getApplicationContext());

        // Keep screen ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Save reference of UI widgets.
        initWidgetReferences();

        // Check for configuration change to restore timers state.
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else {
            // Reset timers state to PAUSED
            timerState = TimersState.PAUSED;
            timersStatePreviousToPause = TimersState.PAUSED;
        }

        initClock(savedInstanceState == null);
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
                windowDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                windowDecorView.setSystemUiVisibility(0);
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
            timerState = TimersState.fromInteger(state);
        }
        if (savedInstanceState.containsKey(STATE_TIMERS_PREVIOUS_PAUSE_KEY)) {
            int state = savedInstanceState.getInt(STATE_TIMERS_PREVIOUS_PAUSE_KEY);
            timersStatePreviousToPause = TimersState.fromInteger(state);
        }

        // Set play/pause toggle btn
        if (timerState != TimersState.PAUSED) {
            clockMenu.showPause();
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
        appData.setSoundsEnabled(soundManager.areSoundsEnabled());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundManager.releaseSounds();
    }

    @Override
    public void onBackPressed() {
        if (timerState != TimersState.PAUSED) {
            pauseClock();
        }
        super.onBackPressed();
    }

    void initClock(boolean initializeClockSettings) {
        getClockManager().setListeners(playerOneCallback, playerTwoCallback);
        if (initializeClockSettings) {
            TimeControlWrapper selectedControl = TimeControlParser.getLastTimeControlOrDefault(this);
            getClockManager().setupClock(selectedControl);
            Log.d(TAG, "Last controls set.");
        }
    }

    /**
     * Reset the clock.
     */
    public void resetClock() {

        getClockManager().resetClock();

        // Both states at pause means it's the beginning of the game.
        timerState = TimersState.PAUSED;
        timersStatePreviousToPause = TimersState.PAUSED;
        updateUIState();
        soundManager.playSound(ClockSound.RESET_CLOCK, audioManager);
    }

    public void pauseClock() {
        if (timerState == TimersState.PLAYER_ONE_RUNNING || timerState == TimersState.PLAYER_TWO_RUNNING) {
            Log.i(TAG, "Clock paused.");
            timersStatePreviousToPause = timerState;
            timerState = TimersState.PAUSED;
            Log.d(TAG, "Previous state: " + timersStatePreviousToPause +
                    " , current state: " + timerState);
            getClockManager().pauseClock();

            updateUIState();
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
        saveInstanceState.putInt(STATE_TIMERS_KEY, timerState.getValue());
        saveInstanceState.putInt(STATE_TIMERS_PREVIOUS_PAUSE_KEY, timersStatePreviousToPause.getValue());
        super.onSaveInstanceState(saveInstanceState);
    }

    class ClockClickListener implements ClockButton.ClockClickListener {
        private final ClockPlayer player;

        ClockClickListener(ClockPlayer player) {
            this.player = player;
        }

        @Override
        public void onClickClock() {
            onPlayerClockClicked(player.isFirstPlayer());
        }

        @Override
        public void onClickOptions() {
            long time = getClockManager().getTimeForPlayer(player);
            AdjustTimeDialogFragment
                    .newInstance(time, player.isFirstPlayer())
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
        clocksDivider = findViewById(R.id.divider);

        // Set listeners
        playerOneButton.setClockButtonClickListener(new ClockClickListener(ClockPlayer.ONE));
        playerTwoButton.setClockButtonClickListener(new ClockClickListener(ClockPlayer.TWO));
        clockMenu.setListener(new ClockMenu.MenuClickListener() {
            @Override
            public void timeSettingsClicked() {
                // Pause clock before going to settings menu
                pauseClock();

                Intent settingsIntent = new Intent(ClockTimersActivity.this, TimerSettingsActivity.class);
                settingsResultLauncher.launch(settingsIntent);
                overridePendingTransition(R.anim.right_to_left_full, R.anim.right_to_left_out);
            }

            @Override
            public void playPauseClicked() {
                if (timerState == TimersState.PAUSED) {
                    onPlayerClockClicked(timersStatePreviousToPause != TimersState.PLAYER_ONE_RUNNING);
                } else {
                    pauseClock();
                    soundManager.playSound(ClockSound.MENU_ACTION, audioManager);
                }
            }

            @Override
            public void resetClicked() {
                if (timerState == TimersState.PLAYER_ONE_RUNNING
                        || timerState == TimersState.PLAYER_TWO_RUNNING) {
                    pauseClock();
                }
                showResetClockDialog();
            }

            @Override
            public void soundClicked() {
                soundManager.toggleSound();
                updateUIState();
                if (soundManager.areSoundsEnabled()) {
                    soundManager.playSound(ClockSound.MENU_ACTION, audioManager);
                }
            }
        });
    }

    /**
     * Change buttons and timers UI according to TimerState.
     */
    private void updateUIState() {
        Log.d(TAG, "Updating UI state to: " + timerState);
        switch (timerState) {
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
        if (clocksDivider != null) {
            ViewUtils.showView(clocksDivider, timerState == TimersState.PAUSED);
        }
    }

    private void onPlayerClockClicked(boolean firstPlayer) {
        ClockPlayer player = ClockPlayer.ofBoolean(firstPlayer);
        TimersState playerTimerRunning = firstPlayer ? TimersState.PLAYER_ONE_RUNNING : TimersState.PLAYER_TWO_RUNNING;

        Log.i(TAG, "Player " + player.name() + " pressed the clock with state: " + timerState + " (previous: " + timersStatePreviousToPause + ")");
        if (timerState == TimersState.PAUSED && timersStatePreviousToPause == TimersState.PAUSED) {
            clockMenu.showPause();
        }
        if (timerState == playerTimerRunning || timerState == TimersState.PAUSED) {
            getClockManager().pressClock(player);
            timerState = firstPlayer ? TimersState.PLAYER_TWO_RUNNING : TimersState.PLAYER_ONE_RUNNING;
            soundManager.playSound(firstPlayer ? ClockSound.PLAYER_ONE_MOVE : ClockSound.PLAYER_TWO_MOVE, audioManager);
            updateUIState();
        } else if (timerState == TimersState.PLAYER_ONE_FINISHED || timerState == TimersState.PLAYER_TWO_FINISHED) {
            showResetClockDialog();
        }
    }

    private void showResetClockDialog() {
        ResetClockDialogFragment resetClockDialog = new ResetClockDialogFragment();
        resetClockDialog.show(getSupportFragmentManager(), TAG_RESET_DIALOG_FRAGMENT);
    }

    @Override
    public void onTimeAdjustmentsConfirmed(long timeMs, boolean firstPlayer) {
        getClockManager().setPlayerTime(
                ClockPlayer.ofBoolean(firstPlayer),
                timeMs
        );
        if (firstPlayer) {
            playerOneButton.setTime(timeMs);
        } else {
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