package com.chess.clock.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.chess.clock.app.R;
import com.chess.clock.engine.Stage;
import com.chess.clock.engine.StageManager;
import com.chess.clock.fragments.TimeControlFragment;


public class StageAdapter extends ArrayAdapter<Stage> {

	/**
	 * FRAGMENT TAG
	 */
	private static final String TAG_DELETE_STAGE_DIALOG_FRAGMENT = "DeleteDialogFragment";

	/**
	 * DIALOG request code
	 */
	private static final int DELETE_STAGE_DIALOG = 1;

	/**
	 * STATE
	 */
	private Context mContext;
	private int mLayoutResourceId;
	private StageManager mStageManager;
	private Fragment mTargetFragment;

	public StageAdapter(Context context, StageManager stageManager, Fragment targetFragment) {
		super(context, R.layout.list_stage_item, stageManager.getStages());

		mLayoutResourceId = R.layout.list_stage_item;
		mContext = context;
		mStageManager = stageManager;
		mTargetFragment = targetFragment;
	}

	@Override
	public int getCount() {
		return mStageManager.getTotalStages();
	}

	@Override
	public Stage getItem(int position) {
		return mStageManager.getStages()[position];
	}

	@Override
	public long getItemId(int position) {
		return super.getItemId(position);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;
		final StageHolder holder;

		if (row == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			row = inflater.inflate(mLayoutResourceId, parent, false);

			holder = new StageHolder();
			holder.label = (TextView) row.findViewById(R.id.stage_label);
			holder.description = (TextView) row.findViewById(R.id.stage_description);
			holder.deleteBtn = (ImageButton) row.findViewById(R.id.stage_remove_btn);
			holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int listPosition = (Integer) holder.deleteBtn.getTag();
					DeleteDialogFragment resetClockDialog = DeleteDialogFragment.newInstance(listPosition);
					resetClockDialog.setTargetFragment(mTargetFragment, DELETE_STAGE_DIALOG);
					resetClockDialog.show(mTargetFragment.getActivity().getSupportFragmentManager(),
							TAG_DELETE_STAGE_DIALOG_FRAGMENT);
				}
			});

			row.setTag(holder);
		} else {
			holder = (StageHolder) row.getTag();
		}

		Stage stage = mStageManager.getStages()[position];
		holder.label.setText(mContext.getString(R.string.stage_item_list_label) + " " + (stage.getId() + 1));
		holder.description.setText(stage.toString());
		holder.deleteBtn.setTag(position);

		if (stage.getId() > 0) {
			holder.deleteBtn.setVisibility(View.VISIBLE);

			// Img Button must set focusable as false otherwise it will steal clicks from parent.
			holder.deleteBtn.setFocusable(false);

		} else {
			holder.deleteBtn.setVisibility(View.GONE);
		}

		return row;
	}

	/**
	 * Delete dialog to be displayed when user presses the delete widget.
	 */
	public static class DeleteDialogFragment extends DialogFragment {

		public static final String ARG_STAGE_ID = "stageID";

		public DeleteDialogFragment() {
			super();
		}

		public static DeleteDialogFragment newInstance(int stageID) {
			DeleteDialogFragment myFragment = new DeleteDialogFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_STAGE_ID, stageID);
			myFragment.setArguments(args);
			return myFragment;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			// Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.delete_stage_dialog_message)
					.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							TimeControlFragment fragment = (TimeControlFragment) getTargetFragment();
							int stageID = getArguments().getInt(ARG_STAGE_ID, 0);
							fragment.removeStage(stageID);
						}
					})
					.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// Resume
						}
					});
			// Create the AlertDialog object and return it
			Dialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(false);
			return dialog;
		}
	}

	static class StageHolder {
		TextView label;
		TextView description;
		ImageButton deleteBtn;
	}
}
