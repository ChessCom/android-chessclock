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
                color(context),
                ContextCompat.getColor(context, R.color.gray)
        };

        return new ColorStateList(states, colors);
    }

    public ColorStateList colorStateListFocused(Context context) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled, android.R.attr.state_focused},
                new int[]{}
        };

        int[] colors = new int[]{
                color(context),
                ContextCompat.getColor(context, R.color.gray_controls)
        };

        return new ColorStateList(states, colors);
    }

    public ColorStateList primaryColorAsStateList(Context context) {
        return ColorStateList.valueOf(color(context));
    }

    public int color(Context context) {
        return ContextCompat.getColor(context, primaryColorRes);
    }
}
