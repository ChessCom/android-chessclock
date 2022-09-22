package com.chess.clock.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.chess.clock.R;
import com.chess.clock.engine.Stage;
import com.chess.clock.engine.TimeIncrement;
import com.chess.clock.fragments.TimeControlFragment;

public class EditStageDialogFragment extends FullScreenDialogFragment {

    public static final String TAG = "EditStageDialogFragment";
    private static final String ARG_STAGE_KEY = "arg_stage_key";
    private static final String ARG_TIME_INCREMENT_KEY = "arg_time_increment_key";

    private Stage stage;
    private TimeIncrement timeIncrement;

    @Override
    int layoutRes() {
        return R.layout.dialog_fragment_edit_stage;
    }

    public static EditStageDialogFragment newInstance(Stage stage, TimeIncrement timeIncrement) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_STAGE_KEY, stage);
        args.putParcelable(ARG_TIME_INCREMENT_KEY, timeIncrement);

        EditStageDialogFragment fragment = new EditStageDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        stage = requireArguments().getParcelable(ARG_STAGE_KEY);
        timeIncrement = requireArguments().getParcelable(ARG_TIME_INCREMENT_KEY);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int titleRes = R.string.stage_editor_dialog_title;
        setName(view, titleRes);
        ((View) view.findViewById(R.id.backBtn)).setOnClickListener(v -> {
            // todo save and finish
            dismissAllowingStateLoss();
        });
        ((View) view.findViewById(R.id.deleteBtn)).setOnClickListener(v -> {
            DeleteConfirmationDialogFragment.newInstance(stage.getId())
                    .show(getParentFragmentManager(), DeleteConfirmationDialogFragment.TAG);
        });
    }

    private void setName(@NonNull View view, int titleRes) {
        switch (stage.getId()) {
            case Stage.STAGE_ONE_ID:
                titleRes = R.string.stage_one;
                break;
            case Stage.STAGE_TWO_ID:
                titleRes = R.string.stage_two;
                break;
            case Stage.STAGE_THREE_ID:
                titleRes = R.string.stage_three;
                break;

        }
        ((TextView) view.findViewById(R.id.titleTv)).setText(titleRes);
    }

    /**
     * Delete dialog to be displayed when user presses the delete widget.
     */
    public static class DeleteConfirmationDialogFragment extends DialogFragment {

        public static final String TAG = "DeleteConfirmationDialogFragment";
        private static final String ARG_STAGE_ID = "stageID";

        public DeleteConfirmationDialogFragment() {
            super();
        }

        public static DeleteConfirmationDialogFragment newInstance(int stageID) {
            DeleteConfirmationDialogFragment myFragment = new DeleteConfirmationDialogFragment();
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
                    .setPositiveButton(R.string.dialog_yes, (dialog, id) -> {
                        TimeControlFragment fragment = (TimeControlFragment) getParentFragment();
                        int stageID = requireArguments().getInt(ARG_STAGE_ID, 0);
                        if (fragment != null) {
                            fragment.removeStage(stageID);
                        }
                    })
                    .setNegativeButton(R.string.dialog_no, (dialog, id) -> {
                        // Resume
                    });
            // Create the AlertDialog object and return it
            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
    }
}
