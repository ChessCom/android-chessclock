package com.chess.clock.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.chess.clock.app.R;
import com.chess.clock.dialog.StageEditorDialog;
import com.chess.clock.dialog.TimePickerDialog;
import com.chess.clock.engine.Stage;

/**
 * Stage Editor Dialog Fragment. This DialogFragment extends TimePickerDialogFragment. Overriding
 * Dialog creation, adds a new layout to Dialog content view which adds
 * the moves number edit text field on top of TimePicker widget.
 */
public class StageEditorDialogFragment extends DialogFragment {

	/**
	 * Save Instance keys
	 */
	protected static final String KEY_STAGE = "key_stage";
	protected Context mContext;
	/**
	 * State
	 */
	Stage mStage;

	/**
	 * Mandatory constructor
	 */
	public StageEditorDialogFragment() {
	}

	/**
	 * Dialog Fragment constructor for Editing Stages.
	 *
	 * @param context Context for the Dialog.
	 * @param stage   Stage being edited.
	 */
	public StageEditorDialogFragment(Context context, Stage stage) {
		mContext = context;
		mStage = stage;
	}

	/**
	 * Override Dialog creation to use StageEditorDialog instead of TimePickerDialog.
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		if (savedInstanceState != null) {
			mContext = getActivity(); // Update activity reference
			mStage = savedInstanceState.getParcelable(KEY_STAGE);
		}

		// Get stage details
		int[] time = mStage.getTime();
		int hour = time[0];
		int minute = time[1];
		int second = time[2];
		boolean movesVisible = mStage.getStageType() == Stage.StageType.MOVES;
		int moves = mStage.getTotalMoves();

		StageEditorDialog.Builder builder = new StageEditorDialog.Builder(mContext);
		builder.setTitle(mContext.getString(R.string.stage_editor_dialog_title));
		builder.setPositiveButton(mContext.getString(R.string.tstage_editor_dialog_set));
		builder.setNegativeButton(mContext.getString(R.string.stage_editor_dialog_cancel));
		builder.setHour(hour);
		builder.setMinute(minute);
		builder.setSecond(second);
		builder.setMovesVisible(movesVisible);
		builder.setMoves(moves);
		builder.setType(TimePickerDialog.Type.HOUR_MINUTE_SECOND);
		builder.setOnStageEditListener((StageEditorDialog.OnStageEditListener) getTargetFragment());

		return builder.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_STAGE, mStage);
		super.onSaveInstanceState(outState);
	}
}