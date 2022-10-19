package com.chess.clock.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.chess.clock.R;
import com.chess.clock.activities.BaseActivity;
import com.chess.clock.engine.Stage;
import com.chess.clock.engine.TimeIncrement;
import com.chess.clock.entities.AppTheme;
import com.chess.clock.entities.ClockTime;
import com.chess.clock.fragments.TimeControlFragment;
import com.chess.clock.util.ClockUtils;
import com.chess.clock.views.ViewUtils;

public class EditStageDialogFragment extends FullScreenDialogFragment
        implements EditTimeIncrementDialogFragment.OnTimeIncrementEditListener {

    public static final String TAG = "EditStageDialogFragment";
    private static final String ARG_STAGE_KEY = "arg_stage_key";

    private Stage stage;

    EditText hoursEt;
    EditText minutesEt;
    EditText secondsEt;
    EditText movesEt;
    TextView timeIncrementDetailsTv;

    @Override
    int layoutRes() {
        return R.layout.dialog_fragment_edit_stage;
    }

    public static EditStageDialogFragment newInstance(Stage stage) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_STAGE_KEY, stage);

        EditStageDialogFragment fragment = new EditStageDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            stage = requireArguments().getParcelable(ARG_STAGE_KEY);
        } else {
            stage = savedInstanceState.getParcelable(ARG_STAGE_KEY);
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int titleRes = R.string.stage_editor_dialog_title;
        setName(view, titleRes);
        view.findViewById(R.id.backBtn).setOnClickListener(v -> dismissAllowingStateLoss());
        view.findViewById(R.id.deleteBtn).setOnClickListener(v ->
                DeleteConfirmationDialogFragment.newInstance(stage.getId())
                        .show(getParentFragmentManager(), DeleteConfirmationDialogFragment.TAG)
        );

        timeIncrementDetailsTv = view.findViewById(R.id.incrementDetailsTv);
        timeIncrementDetailsTv.setText(stage.getTimeIncrement().toString());

        view.findViewById(R.id.incrementLay).setOnClickListener(v -> {
            DialogFragment dialogFragment = EditTimeIncrementDialogFragment.newInstance(stage.getTimeIncrement());
            dialogFragment.show(getChildFragmentManager(), EditTimeIncrementDialogFragment.TAG);
        });

        hoursEt = view.findViewById(R.id.hoursEt);
        minutesEt = view.findViewById(R.id.minutesEt);
        secondsEt = view.findViewById(R.id.secondsEt);
        movesEt = view.findViewById(R.id.movesEt);

        ClockUtils.setClockTextWatcher(minutesEt);
        ClockUtils.setClockTextWatcher(secondsEt);

        ClockUtils.clearFocusOnActionDone(hoursEt);
        ClockUtils.clearFocusOnActionDone(movesEt);

        if (savedInstanceState == null) {
            ClockTime time = stage.getTime();
            hoursEt.setText(ClockUtils.twoDecimalPlacesFormat(time.hours));
            minutesEt.setText(ClockUtils.twoDecimalPlacesFormat(time.minutes));
            secondsEt.setText(ClockUtils.twoDecimalPlacesFormat(time.seconds));
            movesEt.setText(String.valueOf(stage.getTotalMoves()));
        }

        ViewUtils.showView(view.findViewById(R.id.movesLay), stage.getStageType() != Stage.StageType.GAME);
        ViewUtils.showView(view.findViewById(R.id.movesDivider), stage.getStageType() != Stage.StageType.GAME);
    }

    @Override
    public void onResume() {
        super.onResume();
        AppTheme theme = ((BaseActivity) requireActivity()).selectedTheme;
        if (theme != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ColorStateList tintList = theme.colorStateListFocused(requireContext());
                hoursEt.setBackgroundTintList(tintList);
                minutesEt.setBackgroundTintList(tintList);
                secondsEt.setBackgroundTintList(tintList);
                movesEt.setBackgroundTintList(tintList);
            }
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        int hours = ClockUtils.getIntOrZero(hoursEt);
        int minutes = ClockUtils.getIntOrZero(minutesEt);
        int seconds = ClockUtils.getIntOrZero(secondsEt);
        int moves = Math.max(1, ClockUtils.getIntOrZero(movesEt));
        Fragment parentFragment = getParentFragment();
        if (parentFragment != null) {
            ((OnStageEditListener) parentFragment).onStageEditDone(
                    stage.getId(),
                    moves,
                    ClockUtils.durationMillis(hours, minutes, seconds)
            );
        }
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

    @Override
    public void onTimeIncrementEditDone(TimeIncrement.Type type, long time) {
        TimeIncrement increment = new TimeIncrement(type, time);
        stage.setTimeIncrement(increment);
        timeIncrementDetailsTv.setText(increment.toString());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_STAGE_KEY, stage);
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

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.WhiteButtonsDialogTheme);
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
            ViewUtils.setLargePopupMessageTextSize(dialog, getResources());
            return dialog;
        }
    }

    /**
     * The callback interface used to indicate the user is done filling in
     * the stage data (the user clicked on the 'Set' button).
     */
    public interface OnStageEditListener {

        /**
         * @param moves The number of moves that was set.
         * @param time  The time that was set in milliseconds.
         */
        void onStageEditDone(int stageId, int moves, long time);
    }
}
