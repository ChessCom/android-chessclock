package com.chess.clock.dialog;

import com.chess.clock.R;

public class AdjustTimeDialogFragment extends FullScreenDialogFragment {

    public static final String TAG = "AdjustTimeDialogFragment";
    private static final String ARG_TIME_KEY = "arg_time_key";
    private static final String ARG_FIRST_PLAYER_KEY = "arg_first_player_key";

    @Override
    int layoutRes() {
        return R.layout.dialog_fragment_adjust_time;
    }
}
