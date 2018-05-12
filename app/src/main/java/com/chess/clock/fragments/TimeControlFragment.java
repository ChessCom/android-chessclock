package com.chess.clock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chess.clock.R;
import com.chess.clock.adapters.StageAdapter;
import com.chess.clock.dialog.StageEditorDialog;
import com.chess.clock.dialog.TimeIncrementEditorDialog;
import com.chess.clock.engine.Stage;
import com.chess.clock.engine.TimeControl;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.engine.TimeIncrement;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * UI fragment to create and edit a TimeControl.
 */
public class TimeControlFragment extends Fragment implements StageEditorDialog.OnStageEditListener,
        TimeIncrementEditorDialog.OnTimeIncrementEditListener {

    /**
     * This interface must be implemented by activities that contain this fragment to allow interaction.
     */
    public interface BottomNavigationActionListener {

        void setVisibility(int visibility);

        void setSelected(int id);

        void setBottomNavigationListener(OnNavigationItemSelectedListener listener);
    }

    /**
     * Save Instance state keys
     */
    private static final String STATE_TIME_CONTROL_SNAPSHOT_KEY = "time_control_snapshot_key";
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
    private OnTimeControlListener mListener;
    private BottomNavigationActionListener mBottomNavigationActionListener;
    /**
     * State.
     */
    private TimeControlWrapper mTimeControlWrapper;
    private TimeControl mSelectedTimeControl;
    private boolean mPlayerOneSelected = false;
    /**
     * Time Control Name Text WATCHER
     */
    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            if (mTimeControlWrapper != null && text.length() != 0 && !text.equals("")) {
                mTimeControlWrapper.getTimeControlPlayerOne().setName(s.toString());
                mTimeControlWrapper.getTimeControlPlayerTwo().setName(s.toString());
            }
        }
    };
    private int mEditableStageIndex;
    /**
     * Listeners
     */
    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mListener != null) {
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
    private EditText mTimeControlNameEditText;
    private ViewGroup mTimeIncrementBtn;
    private TextView mTimeIncrementDescription;
    private FrameLayout mSameAsPlayerOneSwitchContainer;
    private SwitchCompat mSameAsPlayerOneSwtich;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TimeControlFragment() {

    }

    /**
     * Called when a fragment is first attached to its activity.
     * onCreate(Bundle) will be called after this.
     *
     * @param activity Parent Activity.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnTimeControlListener) activity;
            mBottomNavigationActionListener = (BottomNavigationActionListener) activity;

            // Fetch current TimeControl object
            mTimeControlWrapper = mListener.getEditableTimeControl();

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTimeControlListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register the Fragment as a contributor to the options Menu
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update Activity title if it is new time control.
        TimeControl playerOne = mTimeControlWrapper.getTimeControlPlayerOne();
        if (playerOne != null && (playerOne.getName() == null || playerOne.getName().equals(""))) {
            getActivity().setTitle(getString(R.string.title_activity_time_control_new));
        } else {
            getActivity().setTitle(getString(R.string.title_activity_time_control));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_time_control, container, false);
        mStageListView = (ListView) v.findViewById(R.id.list_stages);
        mSameAsPlayerOneSwitchContainer = (FrameLayout) v.findViewById(R.id.switch_same_as_player_one_container);
        mSameAsPlayerOneSwtich = (SwitchCompat) v.findViewById(R.id.switch_same_as_player_one);
        mSameAsPlayerOneSwtich.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTimeControlWrapper.setSameAsPlayerOne(isChecked);
                if (isChecked && !mPlayerOneSelected) {
                    showPlayerOneViews();
                }
            }
        });
        mSameAsPlayerOneSwitchContainer.setVisibility(GONE);
        mBottomNavigationActionListener.setVisibility(VISIBLE);
        mBottomNavigationActionListener.setBottomNavigationListener(
            new OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(
                    @NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nav_player1:
                            mSameAsPlayerOneSwitchContainer.setVisibility(GONE);
                            break;
                        case R.id.nav_player2:
                            mSameAsPlayerOneSwitchContainer.setVisibility(VISIBLE);
                            break;
                    }
                    mPlayerOneSelected = !mPlayerOneSelected;
                    mSelectedTimeControl = mPlayerOneSelected ? mTimeControlWrapper.getTimeControlPlayerOne() : mTimeControlWrapper.getTimeControlPlayerTwo();
                    if (!mPlayerOneSelected && mTimeControlWrapper.isSameAsPlayerOne()) {
                        showPlayerOneViews();
                    } else {
                        StageAdapter stageAdapter = new StageAdapter(getActivity(), mSelectedTimeControl.getStageManager(), TimeControlFragment.this);
                        mStageListView.setAdapter(stageAdapter);
                        mTimeIncrementDescription.setText(mSelectedTimeControl.getTimeIncrement().toString());
                        updateDisplay();
                    }
                    return true;
                }
            });

        if (mStageListView != null) {

            mStageListView.setOnItemClickListener(mItemClickListener);
            if (mTimeControlWrapper != null) {

                if (savedInstanceState != null) {
                    mTimeControlSnapshot = savedInstanceState.getParcelable(STATE_TIME_CONTROL_SNAPSHOT_KEY);
                } else {
                    // Save copy to check modifications before exit.
                    mTimeControlSnapshot = null;
                    try {
                        mTimeControlSnapshot = (TimeControlWrapper) mTimeControlWrapper.clone();
                    } catch(CloneNotSupportedException e) {
                        e.printStackTrace();
                        throw new IllegalStateException("Could not build time control snapshot");
                    }
                }

                mSameAsPlayerOneSwtich.setChecked(mTimeControlWrapper.isSameAsPlayerOne());

                // Setup Time Control Name Edit Text
                mTimeControlNameEditText = (EditText) v.findViewById(R.id.time_control_name);
                mTimeControlNameEditText.addTextChangedListener(mTextWatcher);

                TimeControl tc = mTimeControlWrapper.getTimeControlPlayerOne();
                if (tc.getName() != null && !tc.getName().equals("")) {
                    mTimeControlNameEditText.setText(tc.getName());
                }

                // Setup Stages list
                StageAdapter stageAdapter = new StageAdapter(getActivity(), tc.getStageManager(), this);
                mStageListView.setAdapter(stageAdapter);

                // Load Time Increment item
                mTimeIncrementDescription = (TextView) v.findViewById(R.id.increment_description);
                mTimeIncrementDescription.setText(tc.getTimeIncrement().toString());

                // Setup click listener to Time Increment btn
                mTimeIncrementBtn = (ViewGroup) v.findViewById(R.id.btn_edit_increment);
                mTimeIncrementBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showTimeIncrementEditorDialog();
                    }
                });
            }
        }

        mBottomNavigationActionListener.setSelected(R.id.nav_player1);
        mSelectedTimeControl = mTimeControlWrapper.getTimeControlPlayerOne();

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(STATE_TIME_CONTROL_SNAPSHOT_KEY, mTimeControlSnapshot);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mBottomNavigationActionListener.setVisibility(GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.time_control_actions, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mSelectedTimeControl.getStageManager().getTotalStages() == 3) {
            menu.findItem(R.id.action_new_stage).setVisible(false);
        } else {
            menu.findItem(R.id.action_new_stage).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_new_stage:
                addNewStage();
                return true;
            case R.id.action_save_time_control:
                saveTimeControl();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveTimeControl() {
        if (mTimeControlWrapper != null) {

            // Hide soft keyboard
            mTimeControlNameEditText.clearFocus();
            InputMethodManager imm =
                    (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mTimeControlNameEditText.getWindowToken(), 0);

            if (mTimeControlNameEditText != null &&
                    mTimeControlNameEditText.getText().toString() == null ||
                    mTimeControlNameEditText.getText().toString().equals("")) {
                mTimeControlNameEditText.requestFocus();
                Toast.makeText(getActivity(), getString(R.string.toast_requesting_time_control_name), Toast.LENGTH_LONG).show();
            } else {
                if (mTimeControlWrapper.isSameAsPlayerOne()) {
                    TimeControl playerOneClone = null;
                    try {
                        playerOneClone = (TimeControl) mTimeControlWrapper.getTimeControlPlayerOne().clone();
                    } catch(CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    mTimeControlWrapper.setTimeControlPlayerTwo(playerOneClone);
                }
                mListener.saveTimeControl();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }

    /**
     * Update Stage list and menu actions.
     */
    public void updateDisplay() {
        ((StageAdapter) mStageListView.getAdapter()).notifyDataSetChanged();

        // Remove "add stage" action from menu if total stages is 3
        getActivity().supportInvalidateOptionsMenu();
    }

    public void showConfirmGoBackDialog() {
        if (!(mTimeControlWrapper.isEqual(mTimeControlSnapshot))) {
            DialogFragment newFragment = ExitConfirmationDialogFragment.newInstance();
            newFragment.setTargetFragment(this, REQUEST_EXIT_DIALOG);
            newFragment.show(getFragmentManager(), TAG_EXIT_DIALOG_FRAGMENT);
        } else {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private void addNewStage() {

        // Hide soft keyboard
        mTimeControlNameEditText.clearFocus();
        InputMethodManager imm =
                (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mTimeControlNameEditText.getWindowToken(), 0);


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
        newFragment.show(getActivity().getSupportFragmentManager(), TAG_STAGE_EDITOR_DIALOG_FRAGMENT);
    }

    /**
     * Clone the player one views if the "same as player one" switch is turned on in the player 2 tab
     */
    private void showPlayerOneViews() {
        TimeControl playerOneClone;
        try {
            playerOneClone = (TimeControl) mTimeControlWrapper.getTimeControlPlayerOne().clone();
        } catch(CloneNotSupportedException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not clone player one time control");
        }
        mTimeControlWrapper.setTimeControlPlayerTwo(playerOneClone);
        mTimeIncrementDescription.setText(playerOneClone.getTimeIncrement().toString());
        mSelectedTimeControl = mPlayerOneSelected ? mTimeControlWrapper.getTimeControlPlayerOne() : mTimeControlWrapper.getTimeControlPlayerTwo();

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
        newFragment.show(getActivity().getSupportFragmentManager(), TAG_TIME_INCREMENT_EDITOR_DIALOG_FRAGMENT);
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
    private static class ExitConfirmationDialogFragment extends DialogFragment {

        public static ExitConfirmationDialogFragment newInstance() {
            return new ExitConfirmationDialogFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            return builder
                    .setTitle(getString(R.string.exit_dialog_title))
                    .setMessage(getString(R.string.exit_dialog_message))
                    .setNegativeButton(getString(R.string.exit_dialog_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            Fragment target = getTargetFragment();
                            if (target != null) {
                                target.getActivity().getSupportFragmentManager().popBackStack();
                            }
                        }
                    })
                    .setPositiveButton(getString(R.string.exit_dialog_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            Fragment target = getTargetFragment();
                            if (target != null) {
                                ((TimeControlFragment) target).saveTimeControl();
                            }
                        }
                    })
                    .create();
        }
    }
}