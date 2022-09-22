package com.chess.clock.dialog;

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
import androidx.fragment.app.Fragment;

import com.chess.clock.R;
import com.chess.clock.engine.TimeIncrement;
import com.chess.clock.entities.AppTheme;
import com.chess.clock.fragments.BaseFragment;
import com.chess.clock.util.ClockUtils;

public class EditTimeIncrementDialogFragment extends FullScreenDialogFragment {

    public static final String TAG = "EditTimeIncrementDialogFragment";
    private static final String ARG_TIME_INCREMENT_KEY = "arg_time_increment_key";

    private TimeIncrement timeIncrement;

    EditText secondsEt;
    EditText minutesEt;

    @Override
    int layoutRes() {
        return R.layout.dialog_fragment_edit_time_increment;
    }

    public static EditTimeIncrementDialogFragment newInstance(TimeIncrement timeIncrement) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_TIME_INCREMENT_KEY, timeIncrement);

        EditTimeIncrementDialogFragment fragment = new EditTimeIncrementDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        timeIncrement = requireArguments().getParcelable(ARG_TIME_INCREMENT_KEY);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) view.findViewById(R.id.titleTv)).setText(R.string.increment);
        ((View) view.findViewById(R.id.backBtn)).setOnClickListener(v -> {
            dismissAllowingStateLoss();
        });

        secondsEt = view.findViewById(R.id.secondsEt);
        minutesEt = view.findViewById(R.id.minutesEt);

        ClockUtils.clearFocusOnActionDone(minutesEt);
        ClockUtils.setClockTextWatcher(secondsEt);

        if (savedInstanceState == null) {
            int[] duration = timeIncrement.getDuration();
            int minutes = duration[0] * 60 + duration[1];
            minutesEt.setText(ClockUtils.twoDecimalPlacesFormat(minutes));
            secondsEt.setText(ClockUtils.twoDecimalPlacesFormat(duration[2]));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AppTheme theme = ((BaseFragment) requireParentFragment()).loadedTheme;
        if (theme != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ColorStateList tintList = theme.colorStateListFocused(requireContext());
                minutesEt.setBackgroundTintList(tintList);
                secondsEt.setBackgroundTintList(tintList);
            }
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        int minutes = ClockUtils.getIntOrZero(minutesEt);
        int seconds = ClockUtils.getIntOrZero(secondsEt);
        long duration = minutes * 60 * 1000L + seconds * 1000L;

        Fragment parentFragment = getParentFragment();
        if (parentFragment != null) {
            ((OnTimeIncrementEditListener) parentFragment)
                    .onTimeIncrementEditDone(timeIncrement.getType(), duration);
        }
    }

    /**
     * The callback interface used to indicate the user is done filling in
     * the stage data (the user clicked on the 'Set' button).
     */
    public interface OnTimeIncrementEditListener {

        /**
         * @param type The increment Type that was set.
         * @param time The time that was set in milliseconds.
         */
        void onTimeIncrementEditDone(TimeIncrement.Type type, long time);
    }
}
