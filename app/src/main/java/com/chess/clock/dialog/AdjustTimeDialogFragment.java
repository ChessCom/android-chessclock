package com.chess.clock.dialog;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chess.clock.R;
import com.chess.clock.entities.ClockTime;
import com.chess.clock.util.ClockUtils;

public class AdjustTimeDialogFragment extends FullScreenDialogFragment {

    public static final String TAG = "AdjustTimeDialogFragment";
    private static final String ARG_TIME_KEY = "arg_time_key";
    private static final String ARG_FIRST_PLAYER_KEY = "arg_first_player_key";

    @Override
    int layoutRes() {
        return R.layout.dialog_fragment_adjust_time;
    }

    @Override
    public int bgColorRes() {
        return R.color.black_40;
    }

    public static AdjustTimeDialogFragment newInstance(Long time, Boolean firstPlayer) {

        Bundle args = new Bundle();
        args.putBoolean(ARG_FIRST_PLAYER_KEY, firstPlayer);
        args.putLong(ARG_TIME_KEY, time);

        AdjustTimeDialogFragment fragment = new AdjustTimeDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.saveBtn).setOnClickListener(v -> {
            // todo call activity interface
            dismissAllowingStateLoss();
        });
        view.findViewById(R.id.cancelBtn).setOnClickListener(v -> dismissAllowingStateLoss());
        EditText hoursEt = view.findViewById(R.id.hoursEt);
        EditText minutesEt = view.findViewById(R.id.minutesEt);
        EditText secondsEt = view.findViewById(R.id.secondsEt);

        if (savedInstanceState == null) {
            ClockTime clockTime = new ClockTime(requireArguments().getLong(ARG_TIME_KEY));
            hoursEt.setText(ClockUtils.twoDecimalPlacesFormat(clockTime.hours));
            minutesEt.setText(ClockUtils.twoDecimalPlacesFormat(clockTime.minutes));
            secondsEt.setText(ClockUtils.twoDecimalPlacesFormat(clockTime.seconds));
        }

        ClockUtils.clearFocusOnActionDone(hoursEt);
        ClockUtils.setClockTextWatcher(minutesEt);
        ClockUtils.setClockTextWatcher(secondsEt);
    }
}
