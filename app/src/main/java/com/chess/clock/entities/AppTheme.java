package com.chess.clock.entities;

import androidx.annotation.ColorRes;

import com.chess.clock.R;

public enum AppTheme {
    GREEN(R.color.green),
    BLUE(R.color.blue),
    ORANGE(R.color.orange),
    TURQUOISE(R.color.turquoise),
    GOLD(R.color.gold),
    PINK(R.color.pink);

    public final int colorRes;

    AppTheme(@ColorRes int colorRes) {
        this.colorRes = colorRes;
    }
}
