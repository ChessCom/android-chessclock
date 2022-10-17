package com.chess.clock.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.chess.clock.R;
import com.chess.clock.activities.AppSettingsActivity;
import com.chess.clock.activities.TimerSettingsActivity;
import com.chess.clock.adapters.TimeRowMoveCallback;
import com.chess.clock.adapters.TimesAdapter;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.entities.AppTheme;
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
     * Time Controls List Adapters.
     */
    private TimesAdapter adapter;

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
    private RecyclerView timesRecyclerView;
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
        initListViewAndHeaders(inflater, view);
        startBtn = view.findViewById(R.id.startBtn);
        setupListViewAdapter(savedInstanceState);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        AdapterView.OnItemClickListener itemClickListener = (parent, view1, position, id) -> {
//            if (mListener != null && timesRecyclerView != null) {
//                mItemChecked = position - timesRecyclerView.getHeaderViewsCount();
//                mListener.setCheckedTimeControlIndex(mItemChecked);
//            }
//        };
//        timesRecyclerView.setOnItemClickListener(itemClickListener);
        startBtn.setOnClickListener(v -> {
            TimerSettingsActivity activity = (TimerSettingsActivity) requireActivity();

            // Check if current time control selected is the same as the the list selected
            if (activity.showResetWarning()) {
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
        if (timesRecyclerView != null && !isMultiSelectionActive) {
            setListViewItemChecked();
        }
    }

    @Override
    void loadTheme(AppTheme theme) {
        startBtn.setCardBackgroundColor(ContextCompat.getColor(requireContext(), theme.primaryColorRes));
        if (adapter != null) {
//            adapter.updateTheme(theme);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            plusImg.setImageTintList(theme.primaryColorAsStateList(getContext()));
        }
    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        if (mMultiSelectionController != null && mMultiSelectionController.isActionModeActive()) {
//            multiSelectionFinishedByOnDestroyView = true;
//            mMultiSelectionController.finish();
//        }
//        mMultiSelectionController = null;
//    }

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

//        if (mMultiSelectionController != null) {
//            mMultiSelectionController.saveInstanceState(outState);
//        }

        super.onSaveInstanceState(outState);
    }

    @SuppressLint("InflateParams")
    private void initListViewAndHeaders(LayoutInflater inflater, View view) {
        timesRecyclerView = view.findViewById(R.id.list_time_controls);
        plusImg = view.findViewById(R.id.plusImg);
        View headerLogo = view.findViewById(R.id.logo);
        View headerTimeBtn = view.findViewById(R.id.timeBtn);

        headerLogo.setOnClickListener(v -> {
            final String appPackageName = "com.chess";
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        });

        headerTimeBtn
                .findViewById(R.id.timeBtn)
                .setOnClickListener(v -> {
                    mListener.addTimeControl();
                    startBtn.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                });
    }

    /**
     * Set TimeControl ListView with proper Adapter and item(s) selection positions.
     */
    private void setupListViewAdapter(Bundle savedInstanceState) {

//        if (savedInstanceState != null) {
//            // Restore list selection mode.
//            isMultiSelectionActive = savedInstanceState.getBoolean(KEY_ACTION_MODE);
//            // Restore last list item check position
//            mItemChecked = savedInstanceState.getInt(KEY_ITEM_SELECTED, 0);
//        } else {
//            isMultiSelectionActive = false;
//            mItemChecked = mListener.getCheckedTimeControlIndex();
//        }

        // Init the CAB helper
//        mMultiSelectionController = MultiSelectionUtil.attachMultiSelectionController(
//                timesRecyclerView, (AppCompatActivity) getActivity(), this);
//
//        if (isMultiSelectionActive) {
//            adapterCAB = new TimeControlCABAdapter(
//                    getActivity(),
//                    mListener.getCurrentTimeControls(),
//                    this,
//                    loadedTheme
//            );
//            timesRecyclerView.setAdapter(adapterCAB);
//            timesRecyclerView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
//            mMultiSelectionController.tryRestoreInstanceState(savedInstanceState);
//
//            // Hide Start Button
//            startBtn.setVisibility(View.GONE);
//
//        } else {
//            adapter = new TimeControlAdapter(getActivity(), mListener.getCurrentTimeControls(), loadedTheme);
        adapter = new TimesAdapter(mListener.getCurrentTimeControls(), loadedTheme);
//            timesRecyclerView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        timesRecyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new TimeRowMoveCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(timesRecyclerView);
//    }
    }

    /**
     * Change context action mode: time control list set to multi choice mode enabling to delete items.
     * This swaps ListView Adapter, which changes list items layout.
     */
    private void startSettingsActionMode() {

//        adapterCAB = new TimeControlCABAdapter(
//                getActivity(),
//                mListener.getCurrentTimeControls(),
//                this,
//                loadedTheme
//        );
//        timesRecyclerView.setAdapter(adapterCAB);
//        timesRecyclerView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
//        mMultiSelectionController.startActionMode();
//
//        // Hide Start Button
//        startBtn.setVisibility(View.GONE);
//
//        // Reset number of selected time controls to delete.
//        mTotalItemChecked = 0;
    }

    public void refreshTimeControlList() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void loadTimeControl(int position) {

//        if (isMultiSelectionActive) {
//            mMultiSelectionController.finish();
//        }

        mItemChecked = position;
        mListener.setCheckedTimeControlIndex(mItemChecked);

        timesRecyclerView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
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
//            deleteTimeControls(actionMode);
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        // On configuration change, API level 8 does not handle setting single choice in the middle of process.
//        if (!multiSelectionFinishedByOnDestroyView) {
//            setListSingleChoiceMode();
//        }
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

//    private void setListSingleChoiceMode() {
//        if (timesRecyclerView != null) {
//
//            mTotalItemChecked = 0;
//            if (adapter == null) {
//                adapter = new TimeControlAdapter(getActivity(), mListener.getCurrentTimeControls(), loadedTheme);
//            } else {
//                adapter.updateTimeControls(mListener.getCurrentTimeControls());
//            }
//            timesRecyclerView.setAdapter(adapter);
//            timesRecyclerView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
//            setListViewItemChecked();
//
//            mListener.setCheckedTimeControlIndex(mItemChecked);
//            startBtn.setVisibility(View.VISIBLE);
//        }
//    }

    private void setListViewItemChecked() {
//        // Add header offset
//        int itemChecked = mItemChecked + timesRecyclerView.getHeaderViewsCount();
//        if (itemChecked <= 0) {
//            itemChecked = 1;
//            Log.w(TAG, "Caught itemChecked <= 0. Resetting to 1.");
//        }
//        timesRecyclerView.setItemChecked(itemChecked, true);
    }

//    private void deleteTimeControls(ActionMode actionMode) {
//        Log.d(TAG, "Requested to delete " + mTotalItemChecked + " time controls.");
//
//        boolean updateList = false;
//        int[] positions = new int[mTotalItemChecked];
//        SparseBooleanArray checked = timesRecyclerView.getCheckedItemPositions();
//
//        int tmpItemChecked = mItemChecked;
//
//        // Get position of checked items
//        int k = 0;
//        for (int i = 0; i < checked.size(); i++) {
//            // If checked
//            if (checked.valueAt(i)) {
//                int position = checked.keyAt(i) - timesRecyclerView.getHeaderViewsCount();
//                Log.d(TAG, "Marking time control " + position + " to remove.");
//                positions[k] = position;
//                k++;
//                updateList = true;
//
//                // Update position of check item
//                if (position < mItemChecked) {
//                    tmpItemChecked--;
//                } else if (position == mItemChecked) {
//                    tmpItemChecked = 0;
//                }
//            }
//        }
//
//        mItemChecked = tmpItemChecked;
//
//        // If checked items found request their removal.
//        if (updateList) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.WhiteButtonsDialogTheme);
//            builder
//                    .setMessage(R.string.delete_custom_time)
//                    .setPositiveButton(R.string.action_delete, (dialog, id) -> {
//                        mListener.removeTimeControl(positions);
//                        actionMode.finish();
//                    })
//                    .setNegativeButton(R.string.action_keep, (dialog, id) -> {
//                        // Resume the clock
//                    });
//            Dialog dialog = builder.create();
//            ViewUtils.setLargePopupMessageTextSize(dialog, getResources());
//            dialog.show();
//            // Note: No need to notifyDataSetChanged as mListView will have adapters swap.
//        } else {
//            actionMode.finish();
//        }
//    }

    public void startNewClock() {
//        int position = mItemChecked + timesRecyclerView.getHeaderViewsCount();
//        Log.d(TAG, "Starting new clock on list position: " + position);
//
//        TimeControlWrapper timeControlWrapper = (TimeControlWrapper) timesRecyclerView.getAdapter().getItem(position);
//        if (timeControlWrapper != null) {
//            TimeControl playerOne = timeControlWrapper.getTimeControlPlayerOne();
//            TimeControl playerTwo = timeControlWrapper.getTimeControlPlayerTwo();
//            FragmentActivity activity = requireActivity();
//            Intent startServiceIntent = ChessClockLocalService.getChessClockServiceIntent(activity.getApplicationContext(), playerOne, playerTwo);
//            activity.startService(startServiceIntent);
//            activity.setResult(Activity.RESULT_OK);
//            activity.finish();
//            activity.overridePendingTransition(R.anim.left_to_right_in, R.anim.left_to_right_full);
//        } else {
//            Log.w(TAG, "time control not available, ignoring start new clock");
//            Thread.dumpStack();
//        }
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
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.WhiteButtonsDialogTheme);
            builder.setMessage(R.string.dialog_clock_running_reset)
                    .setPositiveButton(R.string.dialog_yes, (dialog, id) -> {
                        TimeSettingsFragment f = (TimeSettingsFragment) getTargetFragment();
                        if (f != null) {
                            f.startNewClock();
                        }
                    })
                    .setNegativeButton(R.string.dialog_no, (dialog, id) -> ((TimerSettingsActivity) requireActivity()).dismiss());
            Dialog dialog = builder.create();
            ViewUtils.setLargePopupMessageTextSize(dialog, getResources());
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
    }
}
