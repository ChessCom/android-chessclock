package com.chess.clock.views;

import android.view.View;

public class ViewUtils {
    public static void showView(View v, Boolean show) {
        if (show) {
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }
}
