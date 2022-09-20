package com.chess.clock.fragments;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.chess.clock.R;
import com.chess.clock.adapters.StageAdapter;
import com.chess.clock.dialog.StageEditorDialog;
import com.chess.clock.dialog.TimeIncrementEditorDialog;
import com.chess.clock.engine.Stage;
import com.chess.clock.engine.TimeControl;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.engine.TimeIncrement;
import com.chess.clock.entities.AppTheme;
import com.chess.clock.views.ViewUtils;

/**
 * UI fragment to create and edit a TimeControl.
 */
//todo max stages 3(update controls visibility)
public class TimeControlFragment extends BaseFragment implements StageEditorDialog.OnStageEditListener,
        TimeIncrementEditorDialog.OnTimeIncrementEditListener {


    /**
     * Save Instance state keys
     */
    private static final String STATE_TIME_CONTROL_SNAPSHOT_KEY = "time_control_snapshot_key";
    private static final String STATE_ADVANCED_MODE_KEY = "advanced_mode_key";
    /**
     * Dialog Fragment TAGS
     */
    private static final String TAG_STAGE_EDITOR_DIALOG_FRAGMENT = "StageEditorDialog";
    private static final String TAG_TIME_INCREMENT_EDITOR_DIALOG_FRAGMENT = "TimeIncrementEditorDialog";
    private static final String TAG_EXIT_DIALOG_FRAGMENT = "ExitDialogFragment";
    /**
     * DIALOG request code
     */
    private static final int REQUEST_STAGE_DIALOG = 1;
    private static final int REQUEST_TIME_INCREMENT_DIALOG = 2;
    private static final int REQUEST_EXIT_DIALOG = 3;
    /**
     * Activity attached.
     */
    private OnTimeControlListener timeControlListener;
    /**
     * State.
     */
    private TimeControlWrapper timeControlWrapper;
    private TimeControl mSelectedTimeControl;
    private boolean mPlayerOneSelected = false;
    private boolean advancedMode = false;

    /**
     * Time Control Name Text WATCHER
     */
    private int mEditableStageIndex;
    /**
     * Listeners
     */
    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (timeControlListener != null) {
                mEditableStageIndex = position;
                showStageEditorDialog();
            }
        }
    };
    /**
     * This is used to check for modifications before exiting.
     */
    private TimeControlWrapper mTimeControlSnapshot;
    /**
     * UI
     */
    private ListView mStageListView;
    private EditText nameEt;
    private EditText minutesEt;
    private EditText secondsEt;
    private EditText incrementMinutesEt;
    private EditText incrementSecondsEt;
    private ViewGroup mTimeIncrementBtn;
    private TextView mTimeIncrementDescription;
    private FrameLayout mSameAsPlayerOneSwitchContainer;
    private SwitchCompat sameAsPlayerOneSwitch;
    private SwitchCompat advancedModeSwitch;
    private Button saveBtn;
    private View baseView;
    private View advancedView;


    public TimeControlFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            timeControlListener = (OnTimeControlListener) requireActivity();
            // Fetch current TimeControl object
            timeControlWrapper = timeControlListener.getEditableTimeControl();

        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity() + " must implement OnTimeControlListener");
        }
    }

    @Override
    void loadTheme(AppTheme theme) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ColorStateList tintList = theme.colorStateListFocused(requireContext());
            minutesEt.setBackgroundTintList(tintList);
            secondsEt.setBackgroundTintList(tintList);
            incrementMinutesEt.setBackgroundTintList(tintList);
            incrementSecondsEt.setBackgroundTintList(tintList);
        }
        DrawableCompat.setTintList(advancedModeSwitch.getThumbDrawable(), theme.colorStateListChecked(requireContext()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        requireActivity().setTitle(R.string.custom_time);
        View v = inflater.inflate(R.layout.fragment_time_control, container, false);
        mStageListView = v.findViewById(R.id.list_stages);
        advancedModeSwitch = v.findViewById(R.id.advancedModeSwitch);
        saveBtn = v.findViewById(R.id.saveBtn);
        baseView = v.findViewById(R.id.baseLay);
        advancedView = v.findViewById(R.id.advancedLay);
        mSameAsPlayerOneSwitchContainer = v.findViewById(R.id.switch_same_as_player_one_container);
        sameAsPlayerOneSwitch = v.findViewById(R.id.switch_same_as_player_one);
        sameAsPlayerOneSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            timeControlWrapper.setSameAsPlayerOne(isChecked);
            if (isChecked && !mPlayerOneSelected) {
                showPlayerOneViews();
            }
        });
        mSameAsPlayerOneSwitchContainer.setVisibility(GONE);
        // todo tab actions in advanced mode
//        mBottomNavigationActionListener.setBottomNavigationListener(item -> {
//                    switch (item.getItemId()) {
//                        case R.id.nav_player1:
//                            mSameAsPlayerOneSwitchContainer.setVisibility(GONE);
//                            break;
//                        case R.id.nav_player2:
//                            mSameAsPlayerOneSwitchContainer.setVisibility(VISIBLE);
//                            break;
//                    }
//                    mPlayerOneSelected = !mPlayerOneSelected;
//                    mSelectedTimeControl = mPlayerOneSelected ? mTimeControlWrapper.getTimeControlPlayerOne() : mTimeControlWrapper.getTimeControlPlayerTwo();
//                    if (!mPlayerOneSelected && mTimeControlWrapper.isSameAsPlayerOne()) {
//                        showPlayerOneViews();
//                    } else {
//                        StageAdapter stageAdapter = new StageAdapter(getActivity(), mSelectedTimeControl.getStageManager(), TimeControlFragment.this);
//                        mStageListView.setAdapter(stageAdapter);
//                        mTimeIncrementDescription.setText(mSelectedTimeControl.getTimeIncrement().toString());
//                        updateDisplay();
//                    }
//                    return true;
//                });

        if (mStageListView != null) {

            mStageListView.setOnItemClickListener(mItemClickListener);
            if (timeControlWrapper != null) {

                if (savedInstanceState != null) {
                    mTimeControlSnapshot = savedInstanceState.getParcelable(STATE_TIME_CONTROL_SNAPSHOT_KEY);
                    advancedMode = savedInstanceState.getBoolean(STATE_ADVANCED_MODE_KEY);
                } else {
                    // Save copy to check modifications before exit.
                    mTimeControlSnapshot = null;
                    try {
                        mTimeControlSnapshot = (TimeControlWrapper) timeControlWrapper.clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                        throw new IllegalStateException("Could not build time control snapshot");
                    }
                }

                sameAsPlayerOneSwitch.setChecked(timeControlWrapper.isSameAsPlayerOne());

                nameEt = v.findViewById(R.id.time_control_name);
                minutesEt = v.findViewById(R.id.baseMinEt);
                secondsEt = v.findViewById(R.id.baseSecEt);
                incrementMinutesEt = v.findViewById(R.id.baseIncrementMinEt);
                incrementSecondsEt = v.findViewById(R.id.baseIncrementSecEt);

                TimeControl tc = timeControlWrapper.getTimeControlPlayerOne();
                if (tc.getName() != null && !tc.getName().equals("")) {
                    nameEt.setText(tc.getName());
                }

                // Setup Stages list
                StageAdapter stageAdapter = new StageAdapter(getActivity(), tc.getStageManager(), this);
                mStageListView.setAdapter(stageAdapter);

                // Load Time Increment item
                mTimeIncrementDescription = v.findViewById(R.id.increment_description);
                mTimeIncrementDescription.setText(tc.getTimeIncrement().toString());

                // Setup click listener to Time Increment btn
                mTimeIncrementBtn = v.findViewById(R.id.btn_edit_increment);
                mTimeIncrementBtn.setOnClickListener(v1 -> showTimeIncrementEditorDialog());
            }
        }

        mSelectedTimeControl = timeControlWrapper.getTimeControlPlayerOne();

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        saveBtn.setOnClickListener(v -> saveTimeControl());
        nameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (timeControlWrapper != null && !text.isEmpty()) {
                    timeControlWrapper.getTimeControlPlayerOne().setName(s.toString());
                    timeControlWrapper.getTimeControlPlayerTwo().setName(s.toString());
                }
            }
        });
        setMinutesTextWatcher(secondsEt);
        setMinutesTextWatcher(incrementSecondsEt);
        advancedModeSwitch.setChecked(advancedMode);
        advancedModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            advancedMode = isChecked;
            updateUi();
        });
        if (savedInstanceState == null) {
            String hint = twoDecimalPlacesFormat(0);
            minutesEt.setHint(hint);
            incrementMinutesEt.setHint(hint);
            secondsEt.setHint(hint);
            incrementSecondsEt.setHint(hint);
        }
        updateUi();
    }

    private void updateUi() {
        ViewUtils.showView(baseView, !advancedMode);
        ViewUtils.showView(advancedView, advancedMode);
    }

    private void setMinutesTextWatcher(EditText editText) {
        TextWatcher minutesTextWatcher = new TextWatcher() {
            final int MAX = 59;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                editText.removeTextChangedListener(this);
                String minutesAsString = s.toString();
                int minutes = minutesAsString.isEmpty() ? 0 : Integer.parseInt(minutesAsString);
                if (minutes > MAX) {
                    s.clear();
                    s.append(twoDecimalPlacesFormat(MAX));
                }
                editText.addTextChangedListener(this);
            }
        };
        editText.addTextChangedListener(minutesTextWatcher);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                String minutesAsString = v.getText().toString();
                int minutes = minutesAsString.isEmpty() ? 0 : Integer.parseInt(minutesAsString);
                v.setText(twoDecimalPlacesFormat(minutes));
                v.clearFocus();
            }
            return false;
        });
    }

    @SuppressLint("DefaultLocale")
    private String twoDecimalPlacesFormat(int value) {
        return String.format("%02d", value);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(STATE_TIME_CONTROL_SNAPSHOT_KEY, mTimeControlSnapshot);
        outState.putBoolean(STATE_ADVANCED_MODE_KEY, advancedMode);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        timeControlListener = null;
    }

    private void saveTimeControl() {
        if (timeControlWrapper == null) return;

        // Hide soft keyboard
        nameEt.clearFocus();
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(nameEt.getWindowToken(), 0);

        String newControlName = nameEt.getText().toString();
        if (newControlName.equals("")) {
            nameEt.requestFocus();
            Toast.makeText(getActivity(), getString(R.string.toast_requesting_time_control_name), Toast.LENGTH_LONG).show();
        } else {
            if (advancedMode) {
                if (timeControlWrapper.isSameAsPlayerOne()) {
                    TimeControl playerOneClone = null;
                    try {
                        playerOneClone = (TimeControl) timeControlWrapper.getTimeControlPlayerOne().clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    timeControlWrapper.setTimeControlPlayerTwo(playerOneClone);
                }
            } else {
                int minutes = getIntOrZero(minutesEt);
                int seconds = getIntOrZero(secondsEt);
                int incrementMinutes = getIntOrZero(incrementMinutesEt);
                int incrementSeconds = getIntOrZero(incrementSecondsEt);

                long gameDurationMs = minutes * 60 * 1000L + seconds * 1000L;
                long incrementMs = incrementMinutes * 60 * 1000L + incrementSeconds * 1000L;

                if (gameDurationMs == 0) {
                    Toast.makeText(getActivity(), getString(R.string.please_set_time), Toast.LENGTH_LONG).show();
                    return;
                }

                Stage stage = new Stage(0, gameDurationMs);
                TimeIncrement timeIncrement = new TimeIncrement(TimeIncrement.Type.FISCHER, incrementMs);
                TimeControl simpleControl = new TimeControl(newControlName, new Stage[]{stage}, timeIncrement);
                timeControlWrapper.setTimeControlPlayerOne(simpleControl);
                timeControlWrapper.setTimeControlPlayerTwo(simpleControl);
            }

            timeControlListener.saveTimeControl();
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private int getIntOrZero(EditText et) {
        String textValue = et.getText().toString();
        return textValue.isEmpty() ? 0 : Integer.parseInt(textValue);
    }

    /**
     * Update Stage list and menu actions.
     */
    public void updateDisplay() {
        ((StageAdapter) mStageListView.getAdapter()).notifyDataSetChanged();
    }

    public void showConfirmGoBackDialog() {
        if (!(timeControlWrapper.isEqual(mTimeControlSnapshot))) {
            DialogFragment newFragment = ExitConfirmationDialogFragment.newInstance();
            newFragment.setTargetFragment(this, REQUEST_EXIT_DIALOG);
            newFragment.show(getParentFragmentManager(), TAG_EXIT_DIALOG_FRAGMENT);
        } else {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }

    // todo on add stage
    private void addNewStage() {

        // Hide soft keyboard
        nameEt.clearFocus();
        InputMethodManager imm =
                (InputMethodManager) requireActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(nameEt.getWindowToken(), 0);


        if (mSelectedTimeControl.getStageManager().getTotalStages() < 3) {
            mSelectedTimeControl.getStageManager().addNewStage();
            updateDisplay();
        }
    }

    public void removeStage(int stageIndex) {
        mSelectedTimeControl.getStageManager().removeStage(stageIndex);
        updateDisplay();
    }

    /**
     * Launch Stage Editor Dialog where the user can manipulate the Stage's properties.
     */
    private void showStageEditorDialog() {
        // Get correct Stage.
        Stage stage = mSelectedTimeControl.getStageManager().getStages()[mEditableStageIndex];

        // Setup Stage Editor Dialog.
        DialogFragment newFragment = new StageEditorDialogFragment(getActivity(), stage);
        newFragment.setTargetFragment(this, REQUEST_STAGE_DIALOG);

        // Launch Stage Editor Dialog.
        newFragment.show(requireActivity().getSupportFragmentManager(), TAG_STAGE_EDITOR_DIALOG_FRAGMENT);
    }

    /**
     * Clone the player one views if the "same as player one" switch is turned on in the player 2 tab
     */
    private void showPlayerOneViews() {
        TimeControl playerOneClone;
        try {
            playerOneClone = (TimeControl) timeControlWrapper.getTimeControlPlayerOne().clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not clone player one time control");
        }
        timeControlWrapper.setTimeControlPlayerTwo(playerOneClone);
        mTimeIncrementDescription.setText(playerOneClone.getTimeIncrement().toString());
        mSelectedTimeControl = mPlayerOneSelected ? timeControlWrapper.getTimeControlPlayerOne() : timeControlWrapper.getTimeControlPlayerTwo();

        StageAdapter stageAdapter = new StageAdapter(getActivity(), mSelectedTimeControl.getStageManager(), TimeControlFragment.this);
        mStageListView.setAdapter(stageAdapter);
    }

    @Override
    public void onStageEditDone(int moves, long timeValue) {
        Stage stage = mSelectedTimeControl.getStageManager().getStages()[mEditableStageIndex];

        // Save new moves number
        stage.setMoves(moves);

        // Save new stage duration
        if (stage.getDuration() != timeValue) {
            stage.setDuration(timeValue);
        }

        // Update stage list
        updateDisplay();
    }

    public void showTimeIncrementEditorDialog() {
        // Get Time Increment
        TimeIncrement timeIncrement = mSelectedTimeControl.getTimeIncrement();

        // Setup Time Increment Editor Dialog
        DialogFragment newFragment = new TimeIncrementEditorDialogFragment(getActivity(), timeIncrement);
        newFragment.setTargetFragment(this, REQUEST_TIME_INCREMENT_DIALOG);

        // Launch Time Increment Editor Dialog.
        newFragment.show(requireActivity().getSupportFragmentManager(), TAG_TIME_INCREMENT_EDITOR_DIALOG_FRAGMENT);
    }

    @Override
    public void onTimeIncrementEditDone(TimeIncrement.Type type, long time) {
        // Get Time Increment
        TimeIncrement timeIncrement = mSelectedTimeControl.getTimeIncrement();

        timeIncrement.setType(type);
        timeIncrement.setValue(time);

        // Update Time Increment description
        mTimeIncrementDescription.setText(timeIncrement.toString());
    }

    /**
     * This interface must be implemented by activities that contain this fragment to allow interaction.
     */
    public interface OnTimeControlListener {
        public TimeControlWrapper getEditableTimeControl();

        public void saveTimeControl();
    }

    /**
     * DIALOG
     */
    public static class ExitConfirmationDialogFragment extends DialogFragment {

        public static ExitConfirmationDialogFragment newInstance() {
            return new ExitConfirmationDialogFragment();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            return builder
                    .setTitle(getString(R.string.exit_dialog_title))
                    .setMessage(getString(R.string.exit_dialog_message))
                    .setNegativeButton(getString(R.string.exit_dialog_cancel), (arg0, arg1) -> {
                        Fragment target = getTargetFragment();
                        if (target != null) {
                            target.getActivity().getSupportFragmentManager().popBackStack();
                        }
                    })
                    .setPositiveButton(getString(R.string.exit_dialog_ok), (arg0, arg1) -> {
                        Fragment target = getTargetFragment();
                        if (target != null) {
                            ((TimeControlFragment) target).saveTimeControl();
                        }
                    })
                    .create();
        }
    }
}