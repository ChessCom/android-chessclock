package com.chess.clock.dialog;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chess.clock.R;

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
    }
}
