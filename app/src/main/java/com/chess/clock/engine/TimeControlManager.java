package com.chess.clock.engine;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * Helper for TimeControl list management.
 */
public class TimeControlManager {

    private static final String TAG = TimeControlManager.class.getName();
    /**
     * Save instance state keys
     */
    private final String KEY_SELECTED_TIME_CONTROL_ID = "key_time_control_checked";
    private final String KEY_EDITABLE_TIME_CONTROL = "key_editable_time_control";
    private final String KEY_EDITABLE_STAGE_NEW_FLAG = "key_editable_time_control_new_flag";
    /**
     * State
     */
    private ArrayList<TimeControlWrapper> mTimeControls;   // List of time control wrappers.
    private TimeControlWrapper mEditableTimeControl;       // Copy of a TimeControl for edit purpose.
    private long selectedTimeControlId;     // Id of TimeControl in the list.
    private boolean isNewEditableTimeControl;       // Flag to add new TimeControl in the list after edit.
    /**
     * Listener used to dispatch updates.
     */
    private Callback mCallback;

    /**
     * Time control manager constructor. Should be created before SettingsActivity super.onCreate() method.
     *
     * @param savedInstanceState Bundle instance passed though Activity onCreate method.
     */
    public TimeControlManager(Context context, Bundle savedInstanceState) {

        // Check for configuration change.
        if (savedInstanceState != null) {
            selectedTimeControlId = savedInstanceState.getInt(KEY_SELECTED_TIME_CONTROL_ID, 1);
            mEditableTimeControl = savedInstanceState.getParcelable(KEY_EDITABLE_TIME_CONTROL);
            isNewEditableTimeControl = savedInstanceState.getBoolean(KEY_EDITABLE_STAGE_NEW_FLAG, true);
        } else {
            // First launch, fetch last check position.
            selectedTimeControlId = TimeControlParser.getLastSelectedTimeControlId(context);
            isNewEditableTimeControl = true;
        }

        // Get time controls stored on shared preferences.
        mTimeControls = TimeControlParser.restoreTimeControlsList(context);
        if (mTimeControls != null) {
            Collections.sort(mTimeControls, (o1, o2) -> Integer.compare(o1.getOrder(), o2.getOrder()));
        }

        // Build default List if none was restored from shared preferences.
        if (mTimeControls == null || mTimeControls.isEmpty()) {
            Log.i(TAG, "Time controls list empty. Building and saving default list.");
            mTimeControls = TimeControlDefaults.buildDefaultTimeControlsList(context);
            selectedTimeControlId = TimeControlDefaults.DEFAULT_TIME_ID;
        } else {
            verifySelectedControlExists();
        }
    }

    private void verifySelectedControlExists() {
        boolean selectedControlExists = false;
        for (TimeControlWrapper tc : mTimeControls) {
            if (tc.getId() == selectedTimeControlId) {
                selectedControlExists = true;
                break;
            }
        }
        if (!selectedControlExists) {
            setSelectedTimeControlId(mTimeControls.get(0).getId());
        }
    }

    /**
     * Register a callback to be invoked when state updates.
     *
     * @param listener The callback that will run.
     */
    public void setTimeControlManagerListener(Callback listener) {
        mCallback = listener;
    }

    /**
     * This should be called in onSaveInstanceState method of SettingsActivity.
     *
     * @param outState Bundle where state is stored.
     */
    public void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            outState.putLong(KEY_SELECTED_TIME_CONTROL_ID, selectedTimeControlId);
            outState.putParcelable(KEY_EDITABLE_TIME_CONTROL, mEditableTimeControl);
            outState.putBoolean(KEY_EDITABLE_STAGE_NEW_FLAG, isNewEditableTimeControl);
        }
    }

    /**
     * Get List of TimeControls available.
     *
     * @return TimeControl List
     */
    public ArrayList<TimeControlWrapper> getTimeControls() {
        return mTimeControls;
    }

    /**
     * Save the last time control id.
     */
    public void saveSelectedTimeControlId(Context context) {
        TimeControlParser.saveSelectedTimeControlId(context, selectedTimeControlId);
    }

    /**
     * Add new TimeControl object in the List.
     */
    public void saveTimeControl(Context context) {

        if (mEditableTimeControl != null) {

            if (isNewEditableTimeControl) {
                // Prepend editable time control in the list.
                mTimeControls.add(0, mEditableTimeControl);
                setSelectedTimeControlId(mEditableTimeControl.getId());
            } else {
                // replace time control in the list with the editable time control.
                for (int i = 0; i < mTimeControls.size(); i++) {
                    TimeControlWrapper timeControlWrapper = mTimeControls.get(i);
                    if (timeControlWrapper.getId() == mEditableTimeControl.getId()) {
                        mTimeControls.set(i, mEditableTimeControl);
                    }
                }
            }

            // reset editable time control object
            mEditableTimeControl = null;
        }
        updateItemsOrderAndSave(context);
    }

    /**
     * Remove TimeControl objects from the List.
     *
     * @param ids ids of objects to remove.
     */
    public void removeTimeControls(Context context, Set<Long> ids) {
        Log.v(TAG, "Received time controls remove request");
        ArrayList<TimeControlWrapper> objectBatchToDelete = new ArrayList<>();
        for (TimeControlWrapper tc : mTimeControls) {
            if (ids.contains(tc.getId())) {
                objectBatchToDelete.add(tc);
            }
        }

        mTimeControls.removeAll(objectBatchToDelete);

        if (mTimeControls.size() == 0) {
            restoreDefaultTimeControls(context);
            // Notifies list became empty.
            mCallback.onEmptyTimeControlsListRestored();

        } else {
            Log.v(TAG, "Requesting to save the remaining " + mTimeControls.size() + " time controls.");
            updateItemsOrderAndSave(context);
            verifySelectedControlExists();
        }
    }

    /**
     * Produces a deep copy of the TimeControlWrapper object to edit.
     */
    public void prepareEditableTimeControl(TimeControlWrapper wrapper) {
        isNewEditableTimeControl = false;
        selectedTimeControlId = wrapper.getId();
        try {
            mEditableTimeControl = (TimeControlWrapper) wrapper.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not build editable time control.");
        }
    }

    /**
     * Produces a new blank Editable TimeControl with default 5|5.
     */
    public void prepareNewEditableTimeControl() {

        isNewEditableTimeControl = true;

        // Set default stage and time increment
        Stage stage1 = new Stage(0, 2 * 60 * 60 * 1000L, 40, TimeIncrement.defaultIncrement());
        Stage stage2 = new Stage(1, 60 * 60 * 1000L, TimeIncrement.defaultIncrement());
        TimeControl blank = new TimeControl(null, new Stage[]{stage1, stage2});

        long id = System.currentTimeMillis(); // supported locally, unique enough
        int order = -1; // add item at start, order will be updated before saving
        try {
            // Set current editable time control with a new "blank" time control
            mEditableTimeControl = new TimeControlWrapper(id, order, blank, (TimeControl) blank.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not create Editable time control with blank time control.");
        }
    }

    /**
     * Get current Editable TimeControl.
     *
     * @return current Editable TimeControl object.
     */
    public TimeControlWrapper getEditableTimeControl() {
        return mEditableTimeControl;
    }

    public long getSelectedTimeControlId() {
        return selectedTimeControlId;
    }

    public void setSelectedTimeControlId(long id) {
        selectedTimeControlId = id;
    }

    public void updateOrderOnItemMove(int from, int to, Context context) {
        if (from < to) {
            for (int i = from; i < to; i++) {
                Collections.swap(mTimeControls, i, i + 1);
            }
        } else {
            for (int i = from; i > to; i--) {
                Collections.swap(mTimeControls, i, i - 1);
            }
        }
        updateItemsOrderAndSave(context);
    }

    private void updateItemsOrderAndSave(Context context) {
        for (int i = 0; i < mTimeControls.size(); i++) {
            mTimeControls.get(i).setOrder(i);
        }
        TimeControlParser.saveTimeControls(context, mTimeControls);
    }

    public void restoreDefaultTimeControls(Context context) {
        mTimeControls = TimeControlDefaults.buildDefaultTimeControlsList(context);
        selectedTimeControlId = TimeControlDefaults.DEFAULT_TIME_ID;
    }

    /**
     * Interface definition for a callback to be invoked when state updates.
     */
    public interface Callback {
        /**
         * Called when Time Control list gets empty and was restored.
         */
        void onEmptyTimeControlsListRestored();
    }
}
