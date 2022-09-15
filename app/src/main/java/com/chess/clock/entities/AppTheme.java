package com.chess.clock.entities;

import android.content.Context;
import android.content.res.ColorStateList;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.chess.clock.R;

public enum AppTheme {
    GREEN(R.color.green),
    BLUE(R.color.blue),
    ORANGE(R.color.orange),
    TURQUOISE(R.color.turquoise),
    GOLD(R.color.gold),
    PINK(R.color.pink);

    public final int primaryColorRes;

    AppTheme(@ColorRes int colorRes) {
        this.primaryColorRes = colorRes;
    }

    public static AppTheme fromInt(int position) {
        for (AppTheme theme : AppTheme.values()) {
            if (theme.ordinal() == position) {
                return theme;
            }
        }
        throw new AssertionError("no app theme for position: " + position);
    }

    public ColorStateList colorStateListChecked(Context context) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };

        int[] colors = new int[]{
                ContextCompat.getColor(context, primaryColorRes),
                ContextCompat.getColor(context, R.color.white_20)
        };

        return new ColorStateList(states, colors);
    }

    public ColorStateList primaryColorAsStateList(Context context) {
        int color = ContextCompat.getColor(context, primaryColorRes);
        return ColorStateList.valueOf(color);
    }
}
