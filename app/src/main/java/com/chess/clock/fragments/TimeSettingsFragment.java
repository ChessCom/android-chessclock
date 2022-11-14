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
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.chess.clock.engine.TimeControl;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.entities.AppTheme;
import com.chess.clock.service.ChessClockLocalService;
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

        void loadTimeControl(TimeControlWrapper wrapper);

        void addTimeControl();

        void removeTimeControl(Set<Long> ids);

        void upDateOrderOnItemMove(int from, int to);

        void restoreDefaultTimeControls();
    }

    /**
     * Constants
     */
    private static final String TAG_RESET_DIALOG_FRAGMENT = "ResetDialogFragment";

    /**
     * Activity attached.
     */
    private OnSettingsListener mListener;
    ActivityResultLauncher<Intent> startSettingsForResult;

    /**
     * UI
     */
    private RecyclerView timesRecyclerView;
    private StyledButton startBtn;
    private ImageView plusImg;
    private ActionMode actionMode;

    /**
     * UI management
     */
    private TimesAdapter adapter;
    ItemTouchHelper touchHelper;

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
        startSettingsForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        restoreControls();
                    }
                });
    }

    private void restoreControls() {
        mListener.restoreDefaultTimeControls();
        adapter.updateControls(mListener.getCurrentTimeControls(), mListener.getCheckedTimeControlId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        initRecyclerViewAndHeaders(view);
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
                resetClockDialog.show(getChildFragmentManager(), TAG_RESET_DIALOG_FRAGMENT);
            } else {
                startNewClock();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(getString(R.string.time_controls));
    }

    @Override
    void loadTheme(AppTheme theme) {
        startBtn.setButtonBackground(ContextCompat.getColor(requireContext(), theme.primaryColorRes));
        if (adapter != null) {
            adapter.updateTheme(theme);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            plusImg.setImageTintList(theme.primaryColorAsStateList(getContext()));
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.settings_actions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            openAppSettings();
            return true;
        } else if (itemId == R.id.action_edit) {
            runEditMode(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openAppSettings() {
        FragmentActivity activity = requireActivity();
        startSettingsForResult.launch(new Intent(activity, AppSettingsActivity.class));
        activity.overridePendingTransition(R.anim.right_to_left_full, R.anim.right_to_left_out);
    }

    private void runEditMode(boolean hapticFeedback) {
        ((AppCompatActivity) requireActivity()).startSupportActionMode(this);
        if (hapticFeedback) {
            startBtn.performHapticFeedback(
                    HapticFeedbackConstants.LONG_PRESS,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            );
        }
    }

    private void editModeUiSetup(Boolean editMode) {
        ViewUtils.showView(startBtn, !editMode);
        adapter.setEditMode(editMode);

        if (touchHelper == null) return;
        if (editMode) {
            touchHelper.attachToRecyclerView(timesRecyclerView);
        } else {
            touchHelper.attachToRecyclerView(null);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (adapter != null) {
            adapter.saveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("InflateParams")
    private void initRecyclerViewAndHeaders(View view) {
        timesRecyclerView = view.findViewById(R.id.timesRecycler);
        timesRecyclerView.setHasFixedSize(true);
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
                    quitActionMode();
                    mListener.addTimeControl();
                    startBtn.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                });
    }

    private void quitActionMode() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    /**
     * Set TimeControl RecyclerView with proper Adapter and item(s) selection positions.
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

                    @Override
                    public void onClickEdit(TimeControlWrapper wrapper) {
                        loadTimeControlToEdit(wrapper);
                    }

                    @Override
                    public void onItemsReordered(int from, int to) {
                        mListener.upDateOrderOnItemMove(from, to);
                    }

                    @Override
                    public void onItemLongClick() {
                        runEditMode(true);
                    }
                });
        ItemTouchHelper.Callback callback = new TimeRowMoveCallback(adapter);
        touchHelper = new ItemTouchHelper(callback);

        adapter.restoreInstanceState(savedInstanceState);

        boolean editMode = adapter.inEditMode();
        if (editMode) {
            runEditMode(false);
        }

        ViewUtils.showView(startBtn, !editMode);
        timesRecyclerView.setAdapter(adapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshTimeControlList() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void loadTimeControlToEdit(TimeControlWrapper controlWrapper) {

        quitActionMode();
        mListener.setCheckedTimeControlId(controlWrapper.getId());

        timesRecyclerView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);

        mListener.loadTimeControl(controlWrapper);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.settings_cab_actions, menu);
        this.actionMode = actionMode;
        editModeUiSetup(true);
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
        this.actionMode = null;
        editModeUiSetup(false);
    }

    private void deleteTimeControls(ActionMode mode) {

        Set<Long> idsToRemove = adapter.getIdsToRemove();
        Log.d(TAG, "Requested to delete " + idsToRemove.size() + " time controls.");

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
        TimeControlWrapper wrapper = adapter.getSelectedTimeControlWrapper();
        Log.d(TAG, "Starting new clock: " + wrapper);
        if (wrapper != null) {
            TimeControl playerOne = wrapper.getTimeControlPlayerOne();
            TimeControl playerTwo = wrapper.getTimeControlPlayerTwo();
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
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.WhiteButtonsDialogTheme);
            builder.setMessage(R.string.dialog_clock_running_reset)
                    .setPositiveButton(R.string.dialog_yes, (dialog, id) -> {
                        TimeSettingsFragment f = (TimeSettingsFragment) getParentFragment();
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
