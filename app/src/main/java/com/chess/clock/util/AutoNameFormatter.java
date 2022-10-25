package com.chess.clock.util;

import androidx.annotation.Nullable;

/**
 * SCHEME: “GAME_MIN(ifExist) GAME_SEC(ifExist) | INCREMENT_MIN(ifExist) INCREMENT_SEC(ifExist)”
 * <p>
 * If there is no game time(only increment), the result should be null.
 * You need to provide a formatter for minutes and seconds depending on your needs.
 */
public class AutoNameFormatter {

    private final NameParametersFormat paramsFormat;

    public AutoNameFormatter(NameParametersFormat format) {
        this.paramsFormat = format;
    }

    @Nullable
    public String prepareAutoName(
            int minutes,
            int seconds,
            int incrementMinutes,
            int incrementSeconds) {
        if (minutes == 0 && seconds == 0) return null;

        StringBuilder builder = new StringBuilder();
        if (minutes > 0) {
            builder.append(paramsFormat.getMinutesFormatted(minutes));
        }
        if (seconds > 0) {
            builder.append(" ");
            builder.append(paramsFormat.getSecondsFormatted(seconds));
        }
        if (incrementMinutes > 0 || incrementSeconds > 0) {
            builder.append(" |");
        }
        if (incrementMinutes > 0) {
            builder.append(" ");
            builder.append(paramsFormat.getMinutesFormatted(incrementMinutes));
        }
        if (incrementSeconds > 0) {
            builder.append(" ");
            builder.append(paramsFormat.getSecondsFormatted(incrementSeconds));
        }

        return builder.toString().trim();
    }

    public interface NameParametersFormat {
        String getMinutesFormatted(int minutes);

        String getSecondsFormatted(int seconds);
    }
}
