package com.chess.clock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.chess.clock.R;
import com.chess.clock.activities.SettingsActivity;
import com.chess.clock.adapters.TimeControlAdapter;
import com.chess.clock.adapters.TimeControlCABAdapter;
import com.chess.clock.engine.time.TimeControl;
import com.chess.clock.engine.time.TimeControlWrapper;
import com.chess.clock.service.ChessClockLocalService;
import com.chess.clock.statics.AppData;
import com.chess.clock.util.MultiSelectionUtil;

import java.util.ArrayList;


public class SettingsFragment extends Fragment implements MultiSelectionUtil.MultiChoiceModeListener {

    private static final String TAG = SettingsFragment.class.getName();

    /**
     * Shared preferences wrapper
     */
    private AppData appData;

    /**
     * FullScreen  menu item flag
     */
    boolean isFullScreen;

    /**
     * This interface must be implemented by activities that contain this fragment to allow interaction.
     */
    public interface OnSettingsListener {

        public ArrayList<TimeControlWrapper> getCurrentTimeControls();

        public int getCheckedTimeControlIndex();

        public void setCheckedTimeControlIndex(int position);

        public void loadTimeControl(int position);

        public void addTimeControl();

        public void removeTimeControl(int[] positions);
    }

    /**
     * Save Instance KEYS
     */
    private static final String KEY_ACTION_MODE = "action_mode";
    private static final String KEY_ITEM_SELECTED = "item_selected";

    /**
     * FRAGMENT TAGS
     */
    private static final String TAG_RESET_DIALOG_FRAGMENT = "ResetDialogFragment";
    private View.OnClickListener mStartBtnListener = v -> {
        SettingsActivity activity = (SettingsActivity) getActivity();

        // Check if current time control selected is the same as the the list selected
        if (activity.isSameTimeControlLoaded()) {
            ResetClockDialogFragment resetClockDialog = new ResetClockDialogFragment();
            resetClockDialog.setTargetFragment(SettingsFragment.this, 0);
            resetClockDialog.show(getActivity().getSupportFragmentManager(), TAG_RESET_DIALOG_FRAGMENT);
        } else {
            startNewClock();
        }
    };
    /**
     * Time Controls List Adapters and ActionMode helper.
     */
    private TimeControlAdapter adapter;
    private TimeControlCABAdapter adapterCAB;
    private MultiSelectionUtil.Controller mMultiSelectionController;

    /**
     * State
     */
    private int mItemChecked = -1;
    private int mTotalItemChecked;
    private boolean isMultiSelectionActive;

    /**
     * Util
     */
    private boolean multiSelectionFinishedByOnDestroyView;

    /**
     * Activity attached.
     */
    private OnSettingsListener mListener;

    /**
     * UI
     */
    private ListView mListView;
    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (mListener != null && mListView != null) {
                mItemChecked = position - mListView.getHeaderViewsCount();
                mListener.setCheckedTimeControlIndex(mItemChecked);
            }
        }
    };
    private View mStartBtn;
    private View.OnClickListener mHeaderClickListener = v -> {
        final String appPackageName = "com.chess";
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SettingsFragment() {
    }

    /**
     * Called when a fragment is first attached to its activity. onCreate(Bundle) will be called after this.
     *
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnSettingsListener) activity;
            appData = new AppData(activity.getApplicationContext());
            isFullScreen = appData.getClockFullScreen();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSettingsListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register the Fragment as a contributor to the options Menu
        setHasOptionsMenu(true);

        multiSelectionFinishedByOnDestroyView = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        // Inflate chess.com logo
        View header = inflater.inflate(R.layout.list_settings_header, null);
        header.setOnClickListener(mHeaderClickListener);

        // Init ListView
        mListView = v.findViewById(R.id.list_time_controls);
        mListView.addHeaderView(header, null, false);
        mListView.setOnItemClickListener(mItemClickListener);

        // Set start button listener
        mStartBtn = v.findViewById(R.id.btn_start);
        mStartBtn.setOnClickListener(mStartBtnListener);

        // Set List Adapter and selection positions.
        setupListView(savedInstanceState);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getString(R.string.title_activity_settings));
        if (mListView != null && !isMultiSelectionActive) {
            setListViewItemChecked();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMultiSelectionController != null && mMultiSelectionController.isActionModeActive()) {
            multiSelectionFinishedByOnDestroyView = true;
            mMultiSelectionController.finish();
        }
        mMultiSelectionController = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.settings_actions, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.action_full_screen).setChecked(isFullScreen);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                mListener.addTimeControl();
                mStartBtn.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                return true;
            case R.id.action_edit:
                startSettingsActionMode();
                mStartBtn.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                return true;
            case R.id.action_full_screen:
                isFullScreen = !isFullScreen;
                appData.setClockFullScreen(isFullScreen);
                int currentApiVersion = android.os.Build.VERSION.SDK_INT;
                isFullScreen = appData.getClockFullScreen();

                if (isFullScreen) {
                    ((SettingsActivity) getActivity()).showFullScreen();
                } else {
                    ((SettingsActivity) getActivity()).hideFullScreen();
                }

                if (currentApiVersion >= Build.VERSION_CODES.HONEYCOMB) {
                    getActivity().invalidateOptionsMenu();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_ACTION_MODE, isMultiSelectionActive);
        outState.putInt(KEY_ITEM_SELECTED, mItemChecked);

        if (mMultiSelectionController != null) {
            mMultiSelectionController.saveInstanceState(outState);
        }

        super.onSaveInstanceState(outState);
    }

    /**
     * Set TimeControl ListView with proper Adapter and item(s) selection positions.
     */
    private void setupListView(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            // Restore list selection mode.
            isMultiSelectionActive = savedInstanceState.getBoolean(KEY_ACTION_MODE);
            // Restore last list item check position
            mItemChecked = savedInstanceState.getInt(KEY_ITEM_SELECTED, 0);
        } else {
            isMultiSelectionActive = false;
            mItemChecked = mListener.getCheckedTimeControlIndex();
        }

        // Init the CAB helper
        mMultiSelectionController = MultiSelectionUtil.attachMultiSelectionController(
                mListView, (AppCompatActivity) getActivity(), this);

        if (isMultiSelectionActive) {
            adapterCAB = new TimeControlCABAdapter(getActivity(), mListener.getCurrentTimeControls(), this);
            mListView.setAdapter(adapterCAB);
            mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            mMultiSelectionController.tryRestoreInstanceState(savedInstanceState);

            // Hide Start Button
            mStartBtn.setVisibility(View.GONE);

        } else {
            adapter = new TimeControlAdapter(getActivity(), mListener.getCurrentTimeControls());
            mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            mListView.setAdapter(adapter);
        }
    }

    /**
     * Change context action mode: time control list set to multi choice mode enabling to delete items.
     * This swaps ListView Adapter, which changes list items layout.
     */
    private void startSettingsActionMode() {

        adapterCAB = new TimeControlCABAdapter(getActivity(), mListener.getCurrentTimeControls(), this);
        mListView.setAdapter(adapterCAB);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        mMultiSelectionController.startActionMode();

        // Hide Start Button
        mStartBtn.setVisibility(View.GONE);

        // Reset number of selected time controls to delete.
        mTotalItemChecked = 0;
    }

    public void refreshTimeControlList() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void loadTimeControl(int position) {

        if (isMultiSelectionActive) {
            mMultiSelectionController.finish();
        }

        mItemChecked = position;
        mListener.setCheckedTimeControlIndex(mItemChecked);

        mListView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);

        mListener.loadTimeControl(mItemChecked);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.settings_cab_actions, menu);
        isMultiSelectionActive = true;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        actionMode.setTitle(mTotalItemChecked + " " + getActivity().getString(R.string.settings_cab_title_time_controls_selected));
        return false; // Return false if nothing is done
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete:
                mStartBtn.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                deleteTimeControls();
                actionMode.finish(); // Action picked, so close the CAB
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        // On configuration change, API level 8 does not handle setting single choice in the middle of process.
        if (!multiSelectionFinishedByOnDestroyView) {
            setListSingleChoiceMode();
        }
        isMultiSelectionActive = false;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, boolean checked) {

        if (checked) {
            mTotalItemChecked++;
        } else {
            mTotalItemChecked--;
        }
        mode.setTitle(mTotalItemChecked + " " + getActivity().getString(R.string.settings_cab_title_time_controls_selected));
    }

    private void setListSingleChoiceMode() {
        if (mListView != null) {

            mTotalItemChecked = 0;

            adapter = new TimeControlAdapter(getActivity(), mListener.getCurrentTimeControls());
            mListView.setAdapter(adapter);
            mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            setListViewItemChecked();

            mListener.setCheckedTimeControlIndex(mItemChecked);
            mStartBtn.setVisibility(View.VISIBLE);
        }
    }

    private void setListViewItemChecked() {
        // Add header offset
        int itemChecked = mItemChecked + mListView.getHeaderViewsCount();
        if (itemChecked <= 0) {
            itemChecked = 1;
            Log.w(TAG, "Caught itemChecked <= 0. Resetting to 1.");
        }
        mListView.setItemChecked(itemChecked, true);
    }

    private void deleteTimeControls() {
        Log.d(TAG, "Requested to delete " + mTotalItemChecked + " time controls.");

        boolean updateList = false;
        int[] positions = new int[mTotalItemChecked];
        SparseBooleanArray checked = mListView.getCheckedItemPositions();

        int tmpItemChecked = mItemChecked;

        // Get position of checked items
        int k = 0;
        for (int i = 0; i < checked.size(); i++) {
            // If checked
            if (checked.valueAt(i)) {
                int position = checked.keyAt(i) - mListView.getHeaderViewsCount();
                Log.d(TAG, "Marking time control " + position + " to remove.");
                positions[k] = position;
                k++;
                updateList = true;

                // Update position of check item
                if (position < mItemChecked) {
                    tmpItemChecked--;
                } else if (position == mItemChecked) {
                    tmpItemChecked = 0;
                }
            }
        }

        mItemChecked = tmpItemChecked;

        // If checked items found request their removal.
        if (updateList) {
            mListener.removeTimeControl(positions);
            // Note: No need to notifyDataSetChanged as mListView will have adapters swap.
        }
    }

    public void startNewClock() {
        int position = mItemChecked + mListView.getHeaderViewsCount();
        Log.d(TAG, "Starting new clock on list position: " + position);

        TimeControlWrapper timeControlWrapper = (TimeControlWrapper) mListView.getAdapter().getItem(position);
        if (timeControlWrapper != null) {
            TimeControl playerOne = timeControlWrapper.getTimeControlPlayerOne();
            TimeControl playerTwo = timeControlWrapper.getTimeControlPlayerTwo();
            Intent startServiceIntent = ChessClockLocalService.getChessClockServiceIntent(getActivity().getApplicationContext(), playerOne, playerTwo);
            getActivity().startService(startServiceIntent);
            getActivity().setResult(getActivity().RESULT_OK);
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.left_to_right_in, R.anim.left_to_right_full);
        } else {
            Log.w(TAG, "time control not available, ignoring start new clock");
            Thread.dumpStack();
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
            builder.setMessage(R.string.dialog_clock_running_reset)
                    .setPositiveButton(R.string.dialog_yes, (dialog, id) -> {
                        SettingsFragment f = (SettingsFragment) getTargetFragment();
                        if (f != null) {
                            f.startNewClock();
                        }
                    })
                    .setNegativeButton(R.string.dialog_no, (dialog, id) -> ((SettingsActivity) getActivity()).dismiss());
            // Create the AlertDialog object and return it
            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
    }
}
