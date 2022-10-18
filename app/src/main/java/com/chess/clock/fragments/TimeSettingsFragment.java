package com.chess.clock.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import androidx.appcompat.app.AppCompatActivity;
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
import com.chess.clock.views.StyledButton;
import com.chess.clock.views.ViewUtils;

import java.util.ArrayList;
import java.util.Set;


public class TimeSettingsFragment extends BaseFragment implements ActionMode.Callback {

    private static final String TAG = TimeSettingsFragment.class.getName();

    /**
     * This interface must be implemented by activities that contain this fragment to allow interaction.
     */
    public interface OnSettingsListener {

        ArrayList<TimeControlWrapper> getCurrentTimeControls();

        long getCheckedTimeControlId();

        void setCheckedTimeControlId(long id);

        void loadTimeControl(int position);

        void addTimeControl();

        void removeTimeControl(Set<Long> ids);
    }

    /**
     * Constants
     */
    private static final String TAG_RESET_DIALOG_FRAGMENT = "ResetDialogFragment";

    /**
     * Time Controls List Adapters.
     */
    private TimesAdapter adapter;

    /**
     * State
     */
    private int mTotalItemChecked;

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
    private ActionMode actionMode;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        initListViewAndHeaders(view);
        startBtn = view.findViewById(R.id.startBtn);
        setupRecyclerView(savedInstanceState);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        if (timesRecyclerView != null) {
            // todo restore selected item on back ftom "new time control"
        }
    }

    @Override
    void loadTheme(AppTheme theme) {
        startBtn.setCardBackgroundColor(ContextCompat.getColor(requireContext(), theme.primaryColorRes));
        if (adapter != null) {
            adapter.updateTheme(theme);
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
                runEditMode();
                startBtn.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void runEditMode() {
        ((AppCompatActivity) requireActivity()).startSupportActionMode(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        adapter.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("InflateParams")
    private void initListViewAndHeaders(View view) {
        timesRecyclerView = view.findViewById(R.id.timesRecycler);
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
    private void setupRecyclerView(Bundle savedInstanceState) {
        adapter = new TimesAdapter(
                mListener.getCurrentTimeControls(),
                mListener.getCheckedTimeControlId(),
                loadedTheme,
                new TimesAdapter.SelectedItemListener() {
                    @Override
                    public void onSelectedItemChange(long itemId) {
                        mListener.setCheckedTimeControlId(itemId);
                    }

                    @Override
                    public void onMarkItemToRemove(int removeItemsCount) {
                        updateEditModeTitle(actionMode, removeItemsCount);
                    }
                });
        adapter.restoreInstanceState(savedInstanceState);

        boolean editMode = adapter.inEditMode();
        if (editMode) {
            runEditMode();
        }

        ViewUtils.showView(startBtn, !editMode);
        timesRecyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new TimeRowMoveCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(timesRecyclerView);
    }

    public void refreshTimeControlList() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void loadTimeControl(int position) {

//        if (adapter.isEditMode()) {
//            mMultiSelectionController.finish();
//        }

//        checkedItemId = position;
//        mListener.setCheckedTimeControlIndex(checkedItemId);
//
//        timesRecyclerView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
//                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
//
//        mListener.loadTimeControl(checkedItemId);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.settings_cab_actions, menu);
        this.actionMode = actionMode;
        adapter.setEditMode(true);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        updateEditModeTitle(actionMode, adapter.getIdsToRemove().size());
        return false;
    }

    private void updateEditModeTitle(ActionMode actionMode, int removeItemsCount) {
        if (actionMode == null) return;
        String title = getResources().getQuantityString(R.plurals.x_selected, removeItemsCount, removeItemsCount);
        actionMode.setTitle(title);
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_delete) {
            startBtn.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            deleteTimeControls(actionMode);
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        adapter.setEditMode(false);
        this.actionMode = null;
    }

    private void deleteTimeControls(ActionMode mode) {
        Log.d(TAG, "Requested to delete " + mTotalItemChecked + " time controls.");

        Set<Long> idsToRemove = adapter.getIdsToRemove();

        // If checked items found request their removal.
        if (!idsToRemove.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.WhiteButtonsDialogTheme);
            builder
                    .setMessage(R.string.delete_custom_time)
                    .setPositiveButton(R.string.action_delete, (dialog, id) -> {
                        mListener.removeTimeControl(idsToRemove);
                        adapter.clearRemoveIds();
                        mode.finish();
                    })
                    .setNegativeButton(R.string.action_keep, (dialog, id) -> {
                        // Resume the clock
                    });
            Dialog dialog = builder.create();
            ViewUtils.setLargePopupMessageTextSize(dialog, getResources());
            dialog.show();
        } else {
            mode.finish();
        }
    }

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
