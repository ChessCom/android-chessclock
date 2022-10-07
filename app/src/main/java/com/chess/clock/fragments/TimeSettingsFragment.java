package com.chess.clock.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.chess.clock.R;
import com.chess.clock.activities.AppSettingsActivity;
import com.chess.clock.activities.TimerSettingsActivity;
import com.chess.clock.adapters.TimeControlAdapter;
import com.chess.clock.adapters.TimeControlCABAdapter;
import com.chess.clock.engine.TimeControl;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.entities.AppTheme;
import com.chess.clock.service.ChessClockLocalService;
import com.chess.clock.util.MultiSelectionUtil;
import com.chess.clock.views.StyledButton;
import com.chess.clock.views.ViewUtils;

import java.util.ArrayList;


public class TimeSettingsFragment extends BaseFragment implements MultiSelectionUtil.MultiChoiceModeListener {

    private static final String TAG = TimeSettingsFragment.class.getName();

    /**
     * This interface must be implemented by activities that contain this fragment to allow interaction.
     */
    public interface OnSettingsListener {

        ArrayList<TimeControlWrapper> getCurrentTimeControls();

        int getCheckedTimeControlIndex();

        void setCheckedTimeControlIndex(int position);

        void loadTimeControl(int position);

        void addTimeControl();

        void removeTimeControl(int[] positions);
    }

    /**
     * Constants
     */
    private static final String KEY_ACTION_MODE = "action_mode";
    private static final String KEY_ITEM_SELECTED = "item_selected";
    private static final String TAG_RESET_DIALOG_FRAGMENT = "ResetDialogFragment";

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
    private ListView timesListView;
    private StyledButton startBtn;
    private ImageView plusImg;

    public TimeSettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mListener = (OnSettingsListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity() + " must implement OnSettingsListener");
        }

        setHasOptionsMenu(true);
        multiSelectionFinishedByOnDestroyView = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        initListViewAndInflateHeaders(inflater, view);
        startBtn = view.findViewById(R.id.startBtn);
        setupListViewAdapter(savedInstanceState);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AdapterView.OnItemClickListener itemClickListener = (parent, view1, position, id) -> {
            if (mListener != null && timesListView != null) {
                mItemChecked = position - timesListView.getHeaderViewsCount();
                mListener.setCheckedTimeControlIndex(mItemChecked);
            }
        };
        timesListView.setOnItemClickListener(itemClickListener);
        startBtn.setOnClickListener(v -> {
            TimerSettingsActivity activity = (TimerSettingsActivity) requireActivity();

            // Check if current time control selected is the same as the the list selected
            if (activity.isSameTimeControlLoaded()) {
                ResetClockDialogFragment resetClockDialog = new ResetClockDialogFragment();
                resetClockDialog.setTargetFragment(TimeSettingsFragment.this, 0);
                resetClockDialog.show(activity.getSupportFragmentManager(), TAG_RESET_DIALOG_FRAGMENT);
            } else {
                startNewClock();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(getString(R.string.time_controls));
        if (timesListView != null && !isMultiSelectionActive) {
            setListViewItemChecked();
        }
    }

    @Override
    void loadTheme(AppTheme theme) {
        startBtn.setCardBackgroundColor(ContextCompat.getColor(requireContext(), theme.primaryColorRes));
        if (adapter != null) {
            adapter.updateTheme(theme);
        }
        if (adapterCAB != null) {
            adapterCAB.updateTheme(theme);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            plusImg.setImageTintList(theme.primaryColorAsStateList(getContext()));
        }
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.settings_actions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                FragmentActivity activity = requireActivity();
                startActivity(new Intent(activity, AppSettingsActivity.class));
                activity.overridePendingTransition(R.anim.right_to_left_full, R.anim.right_to_left_out);
                return true;
            case R.id.action_edit:
                startSettingsActionMode();
                startBtn.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                return true;
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

    @SuppressLint("InflateParams")
    private void initListViewAndInflateHeaders(LayoutInflater inflater, View view) {
        timesListView = view.findViewById(R.id.list_time_controls);
        View headerPresets = inflater.inflate(R.layout.header_presets, null);
        View headerLogo = inflater.inflate(R.layout.header_logo, null);
        headerLogo.setOnClickListener(v -> {
            final String appPackageName = "com.chess";
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        });
        View headerTimeBtn = inflater.inflate(R.layout.header_time_button, null);
        plusImg = headerTimeBtn.findViewById(R.id.plusImg);
        headerTimeBtn
                .findViewById(R.id.timeBtn)
                .setOnClickListener(v -> {
                    mListener.addTimeControl();
                    startBtn.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                });

        timesListView.addHeaderView(headerLogo, null, false);
        timesListView.addHeaderView(headerTimeBtn, null, false);
        timesListView.addHeaderView(headerPresets, null, false);
    }

    /**
     * Set TimeControl ListView with proper Adapter and item(s) selection positions.
     */
    private void setupListViewAdapter(Bundle savedInstanceState) {

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
                timesListView, (AppCompatActivity) getActivity(), this);

        if (isMultiSelectionActive) {
            adapterCAB = new TimeControlCABAdapter(
                    getActivity(),
                    mListener.getCurrentTimeControls(),
                    this,
                    loadedTheme
            );
            timesListView.setAdapter(adapterCAB);
            timesListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            mMultiSelectionController.tryRestoreInstanceState(savedInstanceState);

            // Hide Start Button
            startBtn.setVisibility(View.GONE);

        } else {
            adapter = new TimeControlAdapter(getActivity(), mListener.getCurrentTimeControls(), loadedTheme);
            timesListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            timesListView.setAdapter(adapter);
        }
    }

    /**
     * Change context action mode: time control list set to multi choice mode enabling to delete items.
     * This swaps ListView Adapter, which changes list items layout.
     */
    private void startSettingsActionMode() {

        adapterCAB = new TimeControlCABAdapter(
                getActivity(),
                mListener.getCurrentTimeControls(),
                this,
                loadedTheme
        );
        timesListView.setAdapter(adapterCAB);
        timesListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        mMultiSelectionController.startActionMode();

        // Hide Start Button
        startBtn.setVisibility(View.GONE);

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

        timesListView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
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
        actionMode.setTitle(mTotalItemChecked + " " + requireActivity().getString(R.string.settings_cab_title_time_controls_selected));
        return false; // Return false if nothing is done
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_delete) {
            startBtn.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.ConfirmationDialogTheme);
            builder
                    .setMessage(R.string.delete_custom_time)
                    .setPositiveButton(R.string.action_delete, (dialog, id) -> {
                        deleteTimeControls();
                        actionMode.finish(); // Action picked, so close the CAB
                    })
                    .setNegativeButton(R.string.action_keep, (dialog, id) -> {
                        // Resume the clock
                    });
            Dialog dialog = builder.create();
            ViewUtils.setUpConfirmationPopup(dialog, getResources());
            dialog.show();
            return true;
        }
        return false;
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
        mode.setTitle(mTotalItemChecked + " " + getString(R.string.settings_cab_title_time_controls_selected));
    }

    private void setListSingleChoiceMode() {
        if (timesListView != null) {

            mTotalItemChecked = 0;
            if (adapter == null) {
                adapter = new TimeControlAdapter(getActivity(), mListener.getCurrentTimeControls(), loadedTheme);
            } else {
                adapter.updateTimeControls(mListener.getCurrentTimeControls());
            }
            timesListView.setAdapter(adapter);
            timesListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            setListViewItemChecked();

            mListener.setCheckedTimeControlIndex(mItemChecked);
            startBtn.setVisibility(View.VISIBLE);
        }
    }

    private void setListViewItemChecked() {
        // Add header offset
        int itemChecked = mItemChecked + timesListView.getHeaderViewsCount();
        if (itemChecked <= 0) {
            itemChecked = 1;
            Log.w(TAG, "Caught itemChecked <= 0. Resetting to 1.");
        }
        timesListView.setItemChecked(itemChecked, true);
    }

    private void deleteTimeControls() {
        Log.d(TAG, "Requested to delete " + mTotalItemChecked + " time controls.");

        boolean updateList = false;
        int[] positions = new int[mTotalItemChecked];
        SparseBooleanArray checked = timesListView.getCheckedItemPositions();

        int tmpItemChecked = mItemChecked;

        // Get position of checked items
        int k = 0;
        for (int i = 0; i < checked.size(); i++) {
            // If checked
            if (checked.valueAt(i)) {
                int position = checked.keyAt(i) - timesListView.getHeaderViewsCount();
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
        int position = mItemChecked + timesListView.getHeaderViewsCount();
        Log.d(TAG, "Starting new clock on list position: " + position);

        TimeControlWrapper timeControlWrapper = (TimeControlWrapper) timesListView.getAdapter().getItem(position);
        if (timeControlWrapper != null) {
            TimeControl playerOne = timeControlWrapper.getTimeControlPlayerOne();
            TimeControl playerTwo = timeControlWrapper.getTimeControlPlayerTwo();
            FragmentActivity activity = requireActivity();
            Intent startServiceIntent = ChessClockLocalService.getChessClockServiceIntent(activity.getApplicationContext(), playerOne, playerTwo);
            activity.startService(startServiceIntent);
            activity.setResult(Activity.RESULT_OK);
            activity.finish();
            activity.overridePendingTransition(R.anim.left_to_right_in, R.anim.left_to_right_full);
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

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_clock_running_reset)
                    .setPositiveButton(R.string.dialog_yes, (dialog, id) -> {
                        TimeSettingsFragment f = (TimeSettingsFragment) getTargetFragment();
                        if (f != null) {
                            f.startNewClock();
                        }
                    })
                    .setNegativeButton(R.string.dialog_no, (dialog, id) -> ((TimerSettingsActivity) requireActivity()).dismiss());
            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
    }
}
