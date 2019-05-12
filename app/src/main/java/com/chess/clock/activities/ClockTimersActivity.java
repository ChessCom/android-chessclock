package com.chess.clock.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;

import com.chess.clock.R;
import com.chess.clock.engine.CountDownTimer;
import com.chess.clock.engine.Stage;
import com.chess.clock.engine.TimeControlParser;
import com.chess.clock.service.ChessClockLocalService;
import com.chess.clock.statics.AppData;

public class ClockTimersActivity extends FragmentActivity {

    private static final String TAG = ClockTimersActivity.class.getName();

    /**
     * Shared preferences wrapper
     */
    private AppData appData;

    private boolean isFullScreen;

    /**
     * FRAGMENT TAGS
     */
    private static final String TAG_RESET_DIALOG_FRAGMENT = "ResetDialogFragment";

    /**
     * UI saveInstance Bundle Keys.
     */
    private static final String STATE_PLAYER_ONE_KEY = "STATE_PLAYER_ONE_KEY";
    private static final String STATE_PLAYER_TWO_KEY = "STATE_PLAYER_TWO_KEY";
    private static final String STATE_TIMERS_KEY = "STATE_TIMERS_KEY";
    private static final String STATE_TIMERS_PREVIOUS_PAUSE_KEY = "STATE_TIMERS_PREVIOUS_PAUSE_KEY";
    private static final String STATE_PLAYER_ONE_MOVES_CONTAINER_VISIBLE_KEY = "PLAYER_ONE_MOVES_VISIBLE_KEY";
    private static final String STATE_PLAYER_TWO_MOVES_CONTAINER_VISIBLE_KEY = "PLAYER_ONE_MOVES_VISIBLE_KEY";
    private static final String STATE_LAST_TIME_PAUSED_ACTIVITY_KEY = "LAST_TIME_PAUSED_ACTIVITY_KEY";

    /**
     * Shared Preferences Keys.
     */
    private static final String SP_KEY_TIMERS_STATE = "timersState";
    private static final String SP_KEY_TIMERS_STATE_PREVIOUS_TO_PAUSE = "timersStatePreviousToPause";

    /**
     * Chess clock local service (clock engine).
     */
    ChessClockLocalService mService;

    /**
     * True when this activity is bound to chess clock service.
     */
    boolean mBound = false;

    /**
     * Settings Activity request code
     */
    private int SETTINGS_REQUEST_CODE = 1;

    /**
     * UI
     */
    private Button mPlayerOneImgButton;
    private Button mPlayerTwoImgButton;
    private Button mSettingsButton;
    private Button mPauseButton;
    private Button mResetButton;
    private TextView mPlayerOneTimerTextView;
    private TextView mPlayerTwoTimerTextView;
    private TextView mPlayerOneMovesTextView;
    private TextView mPlayerTwoMovesTextView;

    /**
     * Utils
     */
    private boolean mPlayerOneMovesContainerVisible = true;
    private boolean mPlayerTwoMovesContainerVisible = true;
    private long mTimeStampOnPauseActivity;
    private View mDecorView;

    /**
     * Clock button sounds.
     */
    private MediaPlayer playerOneMoveSound;
    private MediaPlayer playerTwoMoveSound;
    private MediaPlayer clockFinished;

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

    private TimersState mTimersState;
    private TimersState mTimersStatePreviousToPause;

    private View.OnClickListener mSettingsButtonListener = v -> {

        // Pause clock before going to settings menu
        pauseClock();


        Intent settingsIntent = new Intent(v.getContext(), SettingsActivity.class);
        startActivityForResult(settingsIntent, SETTINGS_REQUEST_CODE);
        overridePendingTransition(R.anim.right_to_left_full, R.anim.right_to_left_out);
    };

    private View.OnClickListener mPauseButtonListener = v -> pauseClock();

    private View.OnClickListener mResetButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mTimersState == TimersState.PLAYER_ONE_RUNNING
                    || mTimersState == TimersState.PLAYER_TWO_RUNNING) {
                pauseClock();
            }
            showResetClockDialog();
        }
    };

    private View.OnClickListener mPlayerOneButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "Player one pressed the clock with state: " + mTimersState + " (previous: " + mTimersStatePreviousToPause + ")");

            // Set pause btn visibility
            if (mTimersState == TimersState.PAUSED
                    && mTimersStatePreviousToPause == TimersState.PAUSED) {
                mPauseButton.setVisibility(View.VISIBLE);
                mPauseButton.setText(getString(R.string.btn_pause_settings));
            }

            if (mTimersState == TimersState.PLAYER_ONE_RUNNING || mTimersState == TimersState.PAUSED) {

                // If bound to clock service, press clock and update UI state.
                if (mBound) {

                    // First or continuation move
                    if ((mTimersState == TimersState.PAUSED && mTimersStatePreviousToPause == TimersState.PAUSED) ||
                            (mTimersState == TimersState.PAUSED && mTimersStatePreviousToPause == TimersState.PLAYER_ONE_RUNNING) ||
                            mTimersState == TimersState.PLAYER_ONE_RUNNING) {

                        mService.pressPlayerOneClock();
                        mTimersState = TimersState.PLAYER_TWO_RUNNING;

                    }
                    // Resuming clock
                    else {
                        mService.resumeClock();
                        mTimersState = mTimersStatePreviousToPause;
                        mTimersStatePreviousToPause = TimersState.PAUSED;
                    }

                    // Start audio
                    playerOneMoveSound.start();

                    updateUIState();
                }
            } else if (mTimersState == TimersState.PLAYER_ONE_FINISHED ||
                    mTimersState == TimersState.PLAYER_TWO_FINISHED) {
                showResetClockDialog();
            }
        }
    };

    private View.OnClickListener mPlayerTwoButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "Player two pressed the clock with state: " + mTimersState + " (previous: " + mTimersStatePreviousToPause + ")");

            // Set pause btn visibility
            if (mTimersState == TimersState.PAUSED
                    && mTimersStatePreviousToPause == TimersState.PAUSED) {
                mPauseButton.setVisibility(View.VISIBLE);
                mPauseButton.setText(getString(R.string.btn_pause_settings));
            }

            if (mTimersState == TimersState.PLAYER_TWO_RUNNING || mTimersState == TimersState.PAUSED) {

                // If bound to clock service, press clock and update UI state.
                if (mBound) {
                    if ((mTimersState == TimersState.PAUSED && mTimersStatePreviousToPause == TimersState.PAUSED) ||
                            (mTimersState == TimersState.PAUSED && mTimersStatePreviousToPause == TimersState.PLAYER_TWO_RUNNING) ||
                            (mTimersState == TimersState.PLAYER_TWO_RUNNING)) {

                        mService.pressPlayerTwoClock();

                        mTimersState = TimersState.PLAYER_ONE_RUNNING;

                    }
                    // Resuming clock
                    else {
                        mService.resumeClock();
                        mTimersState = mTimersStatePreviousToPause;
                        mTimersStatePreviousToPause = TimersState.PAUSED;
                    }

                    // Start audio
                    playerTwoMoveSound.start();

                    updateUIState();
                }
            } else if (mTimersState == TimersState.PLAYER_ONE_FINISHED ||
                    mTimersState == TimersState.PLAYER_TWO_FINISHED) {
                showResetClockDialog();
            }
        }
    };

    private int mPlayerOneTotalStageNumber;
    private CountDownTimer.Callback playerOneCallback = new CountDownTimer.Callback() {
        @Override
        public void onClockTimeUpdate(long millisUntilFinished) {
            setTime(mPlayerOneTimerTextView, millisUntilFinished);
        }

        @Override
        public void onClockFinish() {
            Log.i(TAG, "Player one loses");
            mTimersState = TimersState.PLAYER_ONE_FINISHED;

            // Play finish sound
            clockFinished.start();

            updateUIState();
        }

        @Override
        public void onStageUpdate(Stage stage) {

            // Reset
            if (stage.getId() == 0) {
                findViewById(R.id.playerOneStageTwo).setBackgroundResource(R.drawable.shape_stage_empty);
                findViewById(R.id.playerOneStageThree).setBackgroundResource(R.drawable.shape_stage_empty);
                if (mPlayerOneTotalStageNumber >= 2) {
                    mPlayerOneMovesContainerVisible = true;
                }

            } else if (stage.getId() == 1) {

                // Mark beginning of stage 2
                findViewById(R.id.playerOneStageTwo).setBackgroundResource(R.drawable.shape_stage_fill);

                // Hide move counter when not required.
                if (mPlayerOneTotalStageNumber == 2) {
                    findViewById(R.id.playerOneMovesContainer).setVisibility(View.GONE);
                    mPlayerOneMovesContainerVisible = false;
                } else {
                    mPlayerOneMovesContainerVisible = true;
                }

            } else if (stage.getId() == 2) {
                findViewById(R.id.playerOneStageTwo).setBackgroundResource(R.drawable.shape_stage_fill);
                findViewById(R.id.playerOneStageThree).setBackgroundResource(R.drawable.shape_stage_fill);

                // Hide move counter when not required.
                findViewById(R.id.playerOneMovesContainer).setVisibility(View.GONE);
                mPlayerOneMovesContainerVisible = false;
            }
        }

        @Override
        public void onMoveCountUpdate(int moves) {
            formatMoves(mPlayerOneMovesTextView, moves);
        }

        @Override
        public void onTotalStageNumber(int stagesNumber) {

            mPlayerOneTotalStageNumber = stagesNumber;

            // Hide all stage views
            if (stagesNumber == 1) {
                findViewById(R.id.playerOneStageOne).setVisibility(View.GONE);
                findViewById(R.id.playerOneStageTwo).setVisibility(View.GONE);
                findViewById(R.id.playerOneStageThree).setVisibility(View.GONE);

                // Hide move counter
                findViewById(R.id.playerOneMovesContainer).setVisibility(View.GONE);
                mPlayerOneMovesContainerVisible = false;
            }

            // 3 is the max allowed stages.
            if (stagesNumber >= 2) {
                View stageOne = findViewById(R.id.playerOneStageOne);
                stageOne.setVisibility(View.VISIBLE);
                stageOne.setBackgroundResource(R.drawable.shape_stage_fill);
                findViewById(R.id.playerOneStageTwo).setVisibility(View.VISIBLE);
                findViewById(R.id.playerOneStageThree).setVisibility(View.GONE);

                // Show move counter
                findViewById(R.id.playerOneMovesContainer).setVisibility(View.VISIBLE);
                mPlayerOneMovesContainerVisible = true;
            }
            if (stagesNumber == 3) {
                findViewById(R.id.playerOneStageThree).setVisibility(View.VISIBLE);
                mPlayerOneMovesContainerVisible = true;
            }
        }
    };

    private int mPlayerTwoTotalStageNumber;
    private CountDownTimer.Callback playerTwoCallback = new CountDownTimer.Callback() {
        @Override
        public void onClockTimeUpdate(long millisUntilFinished) {
            setTime(mPlayerTwoTimerTextView, millisUntilFinished);
        }

        @Override
        public void onClockFinish() {
            Log.i(TAG, "Player two loses");
            mTimersState = TimersState.PLAYER_TWO_FINISHED;

            // Play finish sound
            clockFinished.start();

            updateUIState();
        }

        @Override
        public void onStageUpdate(Stage stage) {

            // Reset
            if (stage.getId() == 0) {
                findViewById(R.id.playerTwoStageTwo).setBackgroundResource(R.drawable.shape_stage_empty);
                findViewById(R.id.playerTwoStageThree).setBackgroundResource(R.drawable.shape_stage_empty);

                if (mPlayerTwoTotalStageNumber >= 2) {
                    mPlayerTwoMovesContainerVisible = true;
                }
            } else if (stage.getId() == 1) {
                findViewById(R.id.playerTwoStageTwo).setBackgroundResource(R.drawable.shape_stage_fill);

                // Hide move counter when not required.
                if (mPlayerTwoTotalStageNumber == 2) {
                    findViewById(R.id.playerTwoMovesContainer).setVisibility(View.GONE);
                    mPlayerTwoMovesContainerVisible = false;
                } else {
                    mPlayerTwoMovesContainerVisible = true;
                }

            } else if (stage.getId() == 2) {
                findViewById(R.id.playerTwoStageTwo).setBackgroundResource(R.drawable.shape_stage_fill);
                findViewById(R.id.playerTwoStageThree).setBackgroundResource(R.drawable.shape_stage_fill);

                // Hide move counter when not required.
                findViewById(R.id.playerTwoMovesContainer).setVisibility(View.GONE);
                mPlayerTwoMovesContainerVisible = false;
            }
        }

        @Override
        public void onMoveCountUpdate(int moves) {
            formatMoves(mPlayerTwoMovesTextView, moves);
        }

        @Override
        public void onTotalStageNumber(int stagesNumber) {

            mPlayerTwoTotalStageNumber = stagesNumber;

            // Hide all stage views
            if (stagesNumber == 1) {
                findViewById(R.id.playerTwoStageOne).setVisibility(View.GONE);
                findViewById(R.id.playerTwoStageTwo).setVisibility(View.GONE);
                findViewById(R.id.playerTwoStageThree).setVisibility(View.GONE);

                // Hide move counter
                findViewById(R.id.playerTwoMovesContainer).setVisibility(View.GONE);
                mPlayerTwoMovesContainerVisible = false;
            }

            // 3 is the max allowed stages.
            if (stagesNumber >= 2) {
                View stageOne = findViewById(R.id.playerTwoStageOne);
                stageOne.setVisibility(View.VISIBLE);
                stageOne.setBackgroundResource(R.drawable.shape_stage_fill);
                findViewById(R.id.playerTwoStageTwo).setVisibility(View.VISIBLE);
                findViewById(R.id.playerTwoStageThree).setVisibility(View.GONE);

                // Show move counter
                findViewById(R.id.playerTwoMovesContainer).setVisibility(View.VISIBLE);
                mPlayerTwoMovesContainerVisible = false;
            }
            if (stagesNumber == 3) {
                findViewById(R.id.playerTwoStageThree).setVisibility(View.VISIBLE);
                mPlayerTwoMovesContainerVisible = true;
            }
        }
    };

    /**
     * Defines callbacks for chess clock service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ChessClockLocalService.ChessClockLocalServiceBinder binder
                    = (ChessClockLocalService.ChessClockLocalServiceBinder) service;
            mService = binder.getService();
            mBound = true;

            mService.setPlayerOneListener(playerOneCallback);
            mService.setPlayerTwoListener(playerTwoCallback);

			/*
            Check if Service is already started. Started Service means that Time Controls were set in the clock.
			If no time control were set, start a new Service with last used Time Controls.
			*/
            if (!mService.isServiceStarted()) {
                startServiceWithLastTimeControl();
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
                            mService.resumeClock();
                            updateUIState();
                        } else {
                            // If pause took too long, reset state to paused.
                            pauseClock();
                        }
                    }
                }
            }
            Log.i(TAG, "Service bound connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Log.i(TAG, "Service bound disconnected");
        }
    };

    /**
     * Update UI according to Settings Activity return code.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {

            // Both states at pause means it's the beginning of the game.
            mTimersState = TimersState.PAUSED;
            mTimersStatePreviousToPause = TimersState.PAUSED;

            // Resetting timer state on shared preferences is mandatory here. Otherwise, the user
            // would press back now and when returning it would resume the previous deprecated state.
            saveTimersState();

            // reset play/pause toggle btn
            mPauseButton.setVisibility(View.INVISIBLE);

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

        appData = new AppData(getApplicationContext());

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

        playerOneMoveSound = MediaPlayer.create(getApplicationContext(), R.raw.chess_clock_switch1);
        playerTwoMoveSound = MediaPlayer.create(getApplicationContext(), R.raw.chess_clock_switch2);
        clockFinished = MediaPlayer.create(getApplicationContext(), R.raw.chess_clock_time_ended);

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

        // Set the custom chess fonts. The buttons text contain pause|settings|reset symbols.
        Typeface font = Typeface.createFromAsset(getAssets(), "ChessGlyph-Regular.otf");
        mPauseButton.setTypeface(font);
        mResetButton.setTypeface(font);
        mSettingsButton.setTypeface(font);

        // Update widgets style according to TimerState
        updateUIState();
    }

    /**
     * Set to immersive mode for Build.VERSION_CODES.KITKAT only.
     *
     * @param hasFocus
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        isFullScreen = appData.getClockFullScreen();
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
        } else if (hasFocus && currentApiVersion < Build.VERSION_CODES.KITKAT) {
            if (isFullScreen) {
                hideStatusBar();
            } else {
                showStatusBar();
            }
        }
    }

    public void hideStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    public void showStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
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
            default:
                Log.d(TAG, "Getting default Landscape Layout");
                return R.layout.activity_clock_timers;
        }
    }

    /**
     * Restore Clock Timer state from saved Instance State bundle.
     *
     * @param savedInstanceState
     */
    private void restoreState(Bundle savedInstanceState) {

        if (savedInstanceState.containsKey(STATE_PLAYER_ONE_KEY)) {
            CharSequence text = savedInstanceState.getString(STATE_PLAYER_ONE_KEY);
            mPlayerOneTimerTextView.setText(text);
        }
        if (savedInstanceState.containsKey(STATE_PLAYER_TWO_KEY)) {
            CharSequence text = savedInstanceState.getString(STATE_PLAYER_TWO_KEY);
            mPlayerTwoTimerTextView.setText(text);
        }
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
            mPauseButton.setVisibility(View.VISIBLE);
            mPauseButton.setText(getString(R.string.btn_pause_settings));
        }

        if (savedInstanceState.containsKey(STATE_PLAYER_ONE_MOVES_CONTAINER_VISIBLE_KEY)) {
            View v = findViewById(R.id.playerOneMovesContainer);
            if (savedInstanceState.getBoolean(STATE_PLAYER_ONE_MOVES_CONTAINER_VISIBLE_KEY)) {
                v.setVisibility(View.VISIBLE);
            } else {
                v.setVisibility(View.GONE);
            }
        }

        if (savedInstanceState.containsKey(STATE_PLAYER_TWO_MOVES_CONTAINER_VISIBLE_KEY)) {
            View v = findViewById(R.id.playerTwoMovesContainer);
            if (savedInstanceState.getBoolean(STATE_PLAYER_TWO_MOVES_CONTAINER_VISIBLE_KEY)) {
                v.setVisibility(View.VISIBLE);
            } else {
                v.setVisibility(View.GONE);
            }
        }

        // Restore time stamp when onPause() was called.
        if (savedInstanceState.containsKey(STATE_LAST_TIME_PAUSED_ACTIVITY_KEY)) {
            mTimeStampOnPauseActivity = savedInstanceState.getLong(STATE_LAST_TIME_PAUSED_ACTIVITY_KEY);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mTimeStampOnPauseActivity = System.currentTimeMillis();

        // Store current timer state and state previous to pause in case we want to
        // resume the clock if this Activity is bound to a already started Service.
        saveTimersState();

        pauseClock();
    }

    @Override
    public void onBackPressed() {

        if (mBound && (mTimersState != TimersState.PAUSED)) {
            pauseClock();
        }

        super.onBackPressed();
    }

    /**
     * Called after onCreate — or after onRestart when the activity had been stopped,
     * but is now again being displayed to the user. It will be followed by onResume.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Bind to Local Chess clock Service.
        Intent intent = new Intent(this, ChessClockLocalService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        Log.i(TAG, "Binding UI to Chess Clock Service.");
    }

    /**
     * Called when you are no longer visible to the user. You will next receive either
     * onRestart, onDestroy, or nothing, depending on later user activity.
     */
    @Override
    protected void onStop() {
        super.onStop();

        // Unbind from the chess clock service.
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
            Log.i(TAG, "Unbinding UI from Chess Clock Service.");
        }
    }

    /**
     * Reset the clock.
     */
    public void resetClock() {
        if (mBound) {

            mService.resetClock();

            // reset play/pause toggle btn
            mPauseButton.setVisibility(View.INVISIBLE);

            // Both states at pause means it's the beginning of the game.
            mTimersState = TimersState.PAUSED;
            mTimersStatePreviousToPause = TimersState.PAUSED;
        }
        updateUIState();
    }

    /**
     * Pause button visibility.
     */
    public void pauseClock() {

        if (mBound) {
            if (mTimersState == TimersState.PLAYER_ONE_RUNNING || mTimersState == TimersState.PLAYER_TWO_RUNNING) {
                Log.i(TAG, "Clock paused.");
                mTimersStatePreviousToPause = mTimersState;
                mTimersState = TimersState.PAUSED;
                Log.d(TAG, "Previous state: " + mTimersStatePreviousToPause +
                        " , current state: " + mTimersState);
                mService.pauseClock();

                mPauseButton.setVisibility(View.INVISIBLE);

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
        saveInstanceState.putCharSequence(STATE_PLAYER_ONE_KEY, mPlayerOneTimerTextView.getText());
        saveInstanceState.putCharSequence(STATE_PLAYER_TWO_KEY, mPlayerTwoTimerTextView.getText());
        saveInstanceState.putInt(STATE_TIMERS_KEY, mTimersState.getValue());
        saveInstanceState.putInt(STATE_TIMERS_PREVIOUS_PAUSE_KEY, mTimersStatePreviousToPause.getValue());

        saveInstanceState.putBoolean(STATE_PLAYER_ONE_MOVES_CONTAINER_VISIBLE_KEY, mPlayerOneMovesContainerVisible);
        saveInstanceState.putBoolean(STATE_PLAYER_TWO_MOVES_CONTAINER_VISIBLE_KEY, mPlayerTwoMovesContainerVisible);

        saveInstanceState.putLong(STATE_LAST_TIME_PAUSED_ACTIVITY_KEY, mTimeStampOnPauseActivity);
        super.onSaveInstanceState(saveInstanceState);
    }

    /**
     * Save references and init listeners of inflated widgets.
     */
    protected void initWidgetReferences() {

        // Save references
        mPlayerOneImgButton = findViewById(R.id.playerOneButton);
        mPlayerTwoImgButton = findViewById(R.id.playerTwoButton);
        mSettingsButton = findViewById(R.id.settings);
        mPauseButton = findViewById(R.id.resume_pause_toggle);
        mResetButton = findViewById(R.id.reset);
        mPlayerOneTimerTextView = findViewById(R.id.playerOneClockText);
        mPlayerTwoTimerTextView = findViewById(R.id.playerTwoClockText);
        mPlayerOneMovesTextView = findViewById(R.id.playerOneMovesText);
        mPlayerTwoMovesTextView = findViewById(R.id.playerTwoMovesText);

        // Set listeners
        mPlayerOneImgButton.setOnClickListener(mPlayerOneButtonListener);
        mPlayerTwoImgButton.setOnClickListener(mPlayerTwoButtonListener);
        mSettingsButton.setOnClickListener(mSettingsButtonListener);
        mPauseButton.setOnClickListener(mPauseButtonListener);
        mResetButton.setOnClickListener(mResetButtonListener);
    }

    /**
     * Change buttons and timers UI according to TimerState.
     */
    private void updateUIState() {
        Resources resources = getResources();
        Log.d(TAG, "Updating UI state to: " + mTimersState);
        switch (mTimersState) {
            case PAUSED:
                mPlayerOneImgButton.setBackgroundDrawable(resources.getDrawable(R.drawable.shape_btn_clock_idle_gradient));
                mPlayerTwoImgButton.setBackgroundDrawable(resources.getDrawable(R.drawable.shape_btn_clock_idle_gradient));
                mPlayerOneTimerTextView.setTextColor(resources.getColor(R.color.clock_timer_idle_textColor));
                mPlayerTwoTimerTextView.setTextColor(resources.getColor(R.color.clock_timer_idle_textColor));
                break;
            case PLAYER_ONE_RUNNING:
                mPlayerOneImgButton.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_btn_clock_running));
                mPlayerTwoImgButton.setBackgroundDrawable(resources.getDrawable(R.drawable.shape_btn_clock_idle_gradient));
                mPlayerOneTimerTextView.setTextColor(resources.getColor(R.color.clock_timer_selected_textColor));
                mPlayerTwoTimerTextView.setTextColor(resources.getColor(R.color.clock_timer_idle_textColor));
                mPauseButton.setVisibility(View.VISIBLE);
                mPauseButton.setText(getString(R.string.btn_pause_settings));
                break;
            case PLAYER_TWO_RUNNING:
                mPlayerOneImgButton.setBackgroundDrawable(resources.getDrawable(R.drawable.shape_btn_clock_idle_gradient));
                mPlayerTwoImgButton.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_btn_clock_running));
                mPlayerOneTimerTextView.setTextColor(resources.getColor(R.color.clock_timer_idle_textColor));
                mPlayerTwoTimerTextView.setTextColor(resources.getColor(R.color.clock_timer_selected_textColor));
                mPauseButton.setVisibility(View.VISIBLE);
                mPauseButton.setText(getString(R.string.btn_pause_settings));
                break;
            case PLAYER_ONE_FINISHED:
                mPlayerOneImgButton.setBackgroundDrawable(resources.getDrawable(R.drawable.shape_btn_clock_finished_gradient));
                mPlayerTwoImgButton.setBackgroundDrawable(resources.getDrawable(R.drawable.shape_btn_clock_idle_gradient));
                mPlayerOneTimerTextView.setTextColor(resources.getColor(R.color.clock_timer_selected_textColor));
                mPlayerTwoTimerTextView.setTextColor(resources.getColor(R.color.clock_timer_idle_textColor));
                mPauseButton.setVisibility(View.INVISIBLE);
                break;
            case PLAYER_TWO_FINISHED:
                mPlayerOneImgButton.setBackgroundDrawable(resources.getDrawable(R.drawable.shape_btn_clock_idle_gradient));
                mPlayerTwoImgButton.setBackgroundDrawable(resources.getDrawable(R.drawable.shape_btn_clock_finished_gradient));
                mPlayerOneTimerTextView.setTextColor(resources.getColor(R.color.clock_timer_idle_textColor));
                mPlayerTwoTimerTextView.setTextColor(resources.getColor(R.color.clock_timer_selected_textColor));
                mPauseButton.setVisibility(View.INVISIBLE);
                break;
        }
    }

    /**
     * Start clock service method.
     */
    private void startServiceWithLastTimeControl() {
        TimeControlParser.startClockWithLastTimeControl(this);
    }

    private void showResetClockDialog() {
        ResetClockDialogFragment resetClockDialog = new ResetClockDialogFragment();
        resetClockDialog.show(getSupportFragmentManager(), TAG_RESET_DIALOG_FRAGMENT);
    }

    /**
     * Set stylized time text on TextView.
     *
     * @param timer TextView object which text will be updated with String time.
     * @param time  Player time in milliseconds.
     * @return Readable String format of time.
     */
    private void setTime(TextView timer, long time) {

        int remaining = (int) (time % 1000);

        // Calibrate seconds to +1 if there is remaining ms
        if (remaining > 0 && time > 0) {
            time += 1000;
        }

        int s = (int) (time / 1000) % 60;
        int m = (int) ((time / (1000 * 60)) % 60);
        int h = (int) ((time / (1000 * 60 * 60)) % 24);

        // 1 hour
        if (time >= 3600000) {
            timer.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.clock_timer_textSize_small));
            timer.setText(String.format("%d:%02d:%02d", h, m, s));
        } else {
            timer.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.clock_timer_textSize_normal));
            timer.setText(String.format("%d:%02d", m, s));
        }
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

        editor.commit();
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

    /**
     * Set stylized moves text on TextView
     *
     * @param v     TextView object which text will be updated.
     * @param moves Current move number of player.
     */
    private void formatMoves(TextView v, int moves) {
        v.setText(String.format("%2d", moves));
    }

    /**
     * Removes full screen before.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showSystemUI() {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && isFullScreen) {
            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
    }
}