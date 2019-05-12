package com.chess.clock.engine;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

/**
 * Helper for TimeControl list management.
 */
public class TimeControlManager {

    private static final String TAG = TimeControlManager.class.getName();
    /**
     * Save instance state keys
     */
    private final String KEY_EDITABLE_TIME_CONTROL_CHECK_INDEX = "key_time_control_checked";
    private final String KEY_EDITABLE_TIME_CONTROL = "key_editable_time_control";
    private final String KEY_EDITABLE_STAGE_NEW_FLAG = "key_editable_time_control_new_flag";
    /**
     * State
     */
    private ArrayList<TimeControl> mTimeControls;   // List of time controls.
    private TimeControl mEditableTimeControl;       // Copy of a TimeControl for edit purpose.
    private int mEditableTimeControlCheckIndex;     // Position of TimeControl in the list.
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
            mEditableTimeControlCheckIndex = savedInstanceState.getInt(KEY_EDITABLE_TIME_CONTROL_CHECK_INDEX, 1);
            mEditableTimeControl = savedInstanceState.getParcelable(KEY_EDITABLE_TIME_CONTROL);
            isNewEditableTimeControl = savedInstanceState.getBoolean(KEY_EDITABLE_STAGE_NEW_FLAG, true);
        } else {
            // First launch, fetch last check position.
            mEditableTimeControlCheckIndex = TimeControlParser.getLastTimeControlCheckIndex(context);
            isNewEditableTimeControl = true;
        }

        // Get time controls stored on shared preferences.
        mTimeControls = TimeControlParser.restoreTimeControlsList(context);

        // Build default List if none was restored from shared preferences.
        if (mTimeControls == null || mTimeControls.size() == 0) {
            Log.i(TAG, "Time controls list empty. Building and saving default list.");
            mTimeControls = TimeControlParser.buildDefaultTimeControlsList(context);
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
            outState.putInt(KEY_EDITABLE_TIME_CONTROL_CHECK_INDEX, mEditableTimeControlCheckIndex);
            outState.putParcelable(KEY_EDITABLE_TIME_CONTROL, mEditableTimeControl);
            outState.putBoolean(KEY_EDITABLE_STAGE_NEW_FLAG, isNewEditableTimeControl);
        }
    }

    /**
     * Get List of TimeControls available.
     *
     * @return TimeControl List
     */
    public ArrayList<TimeControl> getTimeControls() {
        return mTimeControls;
    }

    /**
     * Save the last time control check position in the list.
     */
    public void saveTimeControlIndex(Context context) {
        TimeControlParser.saveTimeControlCheckIndex(context, mEditableTimeControlCheckIndex);
    }

    /**
     * Add new TimeControl object in the List.
     */
    public void saveTimeControl(Context context) {

        if (mEditableTimeControl != null) {

            if (isNewEditableTimeControl) {
                // Prepend editable time control in the list.
                mTimeControls.add(0, mEditableTimeControl);
                setEditableTimeControlCheckIndex(0);
            } else {
                // replace time control in the list with the editable time control.
                mTimeControls.set(mEditableTimeControlCheckIndex, mEditableTimeControl);
            }

            // reset editable time control object
            mEditableTimeControl = null;
        }

        // Save modified time control list in shared preferences.
        TimeControlParser.saveTimeControls(context, mTimeControls);
    }

    /**
     * Remove TimeControl objects from the List.
     *
     * @param positions array with object to remove index positions in the list.
     */
    public void removeTimeControls(Context context, int[] positions) {
        Log.v(TAG, "Received time controls remove request");

        ArrayList<TimeControl> objectBatchToDelete = new ArrayList<>();
        for (int position : positions) {
            if (position >= 0 && position < mTimeControls.size()) {
                Log.v(TAG, "Removing time control (" + position + "): " + mTimeControls.get(position).getName());
                objectBatchToDelete.add(mTimeControls.get(position));
            }
        }
        mTimeControls.removeAll(objectBatchToDelete);

        if (mTimeControls.size() == 0) {

            // Notifies list became empty.
            mCallback.onTimeControlListEmpty();
            mTimeControls = TimeControlParser.buildDefaultTimeControlsList(context);

        } else {
            Log.v(TAG, "Requesting to save the remaining " + mTimeControls.size() + " time controls.");
            // save modified time control list.
            TimeControlParser.saveTimeControls(context, mTimeControls);
        }
    }

    /**
     * Produces a deep copy of the TimeControl object in position.
     *
     * @param position position of the TimeControl object in the list.
     */
    public void prepareEditableTimeControl(int position) {
        isNewEditableTimeControl = false;
        mEditableTimeControlCheckIndex = position;
        mEditableTimeControl = buildEditableTimeControl(position);
    }

    /**
     * Produces a new blank Editable TimeControl with default 5|5.
     */
    public void prepareNewEditableTimeControl() {

        isNewEditableTimeControl = true;

        // Set default stage and time increment
        Stage stage = new Stage(0, 300000);
        TimeIncrement timeIncrement = new TimeIncrement(TimeIncrement.Type.FISCHER, 5000);

        // Set current editable time control with a new "blank" time control
        mEditableTimeControl = new TimeControl(null, new Stage[]{stage}, timeIncrement);
    }

    /**
     * Get current Editable TimeControl.
     *
     * @return current Editable TimeControl object.
     */
    public TimeControl getEditableTimeControl() {
        return mEditableTimeControl;
    }

    /**
     * Get current checked position of TimeControl in the list.
     *
     * @return current checked position of TimeControl in the list.
     */
    public int getEditableTimeControlCheckIndex() {
        return mEditableTimeControlCheckIndex;
    }

    /**
     * Get current checked position of TimeControl in the list.
     *
     * @param idx current checked position of TimeControl in the list.
     */
    public void setEditableTimeControlCheckIndex(int idx) {
        mEditableTimeControlCheckIndex = idx;
    }

    /**
     * Get Editable copy of selected time control. This copy will replace the original time control
     * if the user presses "Done" on the time control edit menu.
     *
     * @param position Position of time control in the list.
     * @return Copy of TimeControl object.
     */
    private TimeControl buildEditableTimeControl(int position) {

        if (position >= 0 && position < mTimeControls.size()) {

            TimeControl original = mTimeControls.get(position);
            try {
                return (TimeControl) original.clone();
            } catch (CloneNotSupportedException e) {
                Log.e(TAG, "Could not produce editable copy of TimeControl object");
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Interface definition for a callback to be invoked when state updates.
     */
    public interface Callback {
        /**
         * Called when Time Control list gets empty.
         */
        void onTimeControlListEmpty();
    }
}
