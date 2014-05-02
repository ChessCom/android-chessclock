package com.chess.chessclock.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.chess.chessclock.app.R;
import com.chess.chessclock.dialog.TimeIncrementEditorDialog;
import com.chess.chessclock.dialog.TimePickerDialog;
import com.chess.chessclock.engine.TimeIncrement;

/**
 * Stage Editor Dialog Fragment. This DialogFragment extends TimePickerDialogFragment. Overriding
 * Dialog creation, adds a new layout to Dialog content view which adds
 * the moves number edit text field on top of TimePicker widget.
 */
public class TimeIncrementEditorDialogFragment extends DialogFragment {

	/**
	 * Save Instance keys
	 */
	protected static final String KEY_TIME_INCREMENT = "key_time_increment";
	protected Context mContext;
	/**
	 * State
	 */
	TimeIncrement mTimeIncrement;

	/**
	 * Mandatory constructor
	 */
	public TimeIncrementEditorDialogFragment() {
	}

	/**
	 * Dialog Fragment constructor for Editing Stages.
	 *
	 * @param context       Context for the Dialog.
	 * @param timeIncrement TimeIncrement being edited.
	 */
	public TimeIncrementEditorDialogFragment(Context context, TimeIncrement timeIncrement) {
		mContext = context;
		mTimeIncrement = timeIncrement;
	}

	/**
	 * Override Dialog creation to use StageEditorDialog instead of TimePickerDialog.
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		if (savedInstanceState != null) {
			mContext = getActivity(); // Update activity reference
			mTimeIncrement = savedInstanceState.getParcelable(KEY_TIME_INCREMENT);
		}

		// Get stage details
		int[] time = mTimeIncrement.getDuration();
		int hour = time[0];
		int minute = time[1];
		int second = time[2];
		int incrementType = mTimeIncrement.getType().getValue();

		TimeIncrementEditorDialog.Builder builder = new TimeIncrementEditorDialog.Builder(mContext);
		builder.setTitle(mContext.getString(R.string.time_increment_editor_dialog_title));
		builder.setPositiveButton(mContext.getString(R.string.time_increment_editor_dialog_set));
		builder.setNegativeButton(mContext.getString(R.string.time_increment_editor_dialog_cancel));
		builder.setHour(hour);
		builder.setMinute(minute);
		builder.setSecond(second);
		builder.setTimeIncrementType(incrementType);
		builder.setType(TimePickerDialog.Type.MINUTE_SECOND);
		builder.setOnTimeIncrementEditListener((TimeIncrementEditorDialog.OnTimeIncrementEditListener) getTargetFragment());

		return builder.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_TIME_INCREMENT, mTimeIncrement);
		super.onSaveInstanceState(outState);
	}
}