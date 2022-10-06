package com.chess.clock.dialog;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chess.clock.R;
import com.chess.clock.activities.BaseActivity;
import com.chess.clock.entities.AppTheme;
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
        boolean firstPlayer = requireArguments().getBoolean(ARG_FIRST_PLAYER_KEY);
        if (!firstPlayer && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            view.setRotation(180);
        }
        EditText hoursEt = view.findViewById(R.id.hoursEt);
        EditText minutesEt = view.findViewById(R.id.minutesEt);
        EditText secondsEt = view.findViewById(R.id.secondsEt);

        view.findViewById(R.id.saveBtn).setOnClickListener(v -> {
            int hours = ClockUtils.getIntOrZero(hoursEt);
            int minutes = ClockUtils.getIntOrZero(minutesEt);
            int seconds = ClockUtils.getIntOrZero(secondsEt);
            ((TimeAdjustmentsListener) requireActivity()).onTimeAdjustmentsConfirmed(
                    ClockUtils.durationMillis(hours, minutes, seconds),
                    firstPlayer
            );
            dismissAllowingStateLoss();
        });
        view.findViewById(R.id.cancelBtn).setOnClickListener(v -> dismissAllowingStateLoss());

        if (savedInstanceState == null) {
            ClockTime clockTime = ClockTime.calibrated(requireArguments().getLong(ARG_TIME_KEY));
            hoursEt.setText(ClockUtils.twoDecimalPlacesFormat(Math.min(clockTime.hours, 99)));
            minutesEt.setText(ClockUtils.twoDecimalPlacesFormat(clockTime.minutes));
            secondsEt.setText(ClockUtils.twoDecimalPlacesFormat(clockTime.seconds));
        }

        ClockUtils.clearFocusOnActionDone(hoursEt);
        ClockUtils.setClockTextWatcher(minutesEt);
        ClockUtils.setClockTextWatcher(secondsEt);

        AppTheme theme = ((BaseActivity) requireActivity()).selectedTheme;
        if (theme != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ColorStateList tintList = theme.colorStateListFocused(requireContext());
                hoursEt.setBackgroundTintList(tintList);
                minutesEt.setBackgroundTintList(tintList);
                secondsEt.setBackgroundTintList(tintList);
            }
        }
    }

    public interface TimeAdjustmentsListener {
        void onTimeAdjustmentsConfirmed(long timeMs, Boolean firstPlayer);
    }
}
