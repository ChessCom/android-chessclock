package com.chess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.chess.clock.util.AutoNameFormatter;

import org.junit.Test;

public class AutoNameFormatterTest {

    private final AutoNameFormatter.NameParametersFormat sampleParametersFormat = new AutoNameFormatter.NameParametersFormat() {
        @Override
        public String getMinutesFormatted(int minutes) {
            return minutes + " min";
        }

        @Override
        public String getSecondsFormatted(int seconds) {
            return seconds + " sec";
        }
    };
    private final AutoNameFormatter formatter = new AutoNameFormatter(sampleParametersFormat);

    @Test
    public void noGameTime_withOrWithoutIncrement_returnsNull() {
        // when
        String noParamsName = formatter.prepareAutoName(0, 0, 0, 0);
        String incrementOnlyName = formatter.prepareAutoName(0, 0, 1, 1);

        // then
        assertNull(noParamsName);
        assertNull(incrementOnlyName);
    }

    @Test
    public void minutesOnly_returnsMinutesString() {
        // given
        int minutes = 10;
        String expectedName = "10 min";

        // when
        String name = formatter.prepareAutoName(minutes, 0, 0, 0);

        // then
        assertEquals(name, expectedName);
    }

    @Test
    public void secondsOnly_returnsSecondsString() {
        // given
        int seconds = 10;
        String expectedName = "10 sec";

        // when
        String name = formatter.prepareAutoName(0, seconds, 0, 0);

        // then
        assertEquals(name, expectedName);
    }

    @Test
    public void minutesGameWithSecondsIncrement_returnsProperFormat() {
        // given
        int gameTimeMinutes = 5;
        int incrementSeconds = 2;
        String expectedName = "5 min | 2 sec";

        // when
        String name = formatter.prepareAutoName(gameTimeMinutes, 0, 0, incrementSeconds);

        // then
        assertEquals(name, expectedName);
    }

    @Test
    public void secondsGameWithMinutesIncrement_returnsProperFormat() {
        // given
        int gameTimeSeconds = 55;
        int incrementMinutes = 1;
        String expectedName = "55 sec | 1 min";

        // when
        String name = formatter.prepareAutoName(0, gameTimeSeconds, incrementMinutes, 0);

        // then
        assertEquals(name, expectedName);
    }

    @Test
    public void gameParamsOnly_returnsFormatWithoutIncrement() {
        // given
        int gameTimeMinutes = 2;
        int gameTimeSeconds = 30;
        String expectedName = "2 min 30 sec";

        // when
        String name = formatter.prepareAutoName(gameTimeMinutes, gameTimeSeconds, 0, 0);

        // then
        assertEquals(name, expectedName);
    }

    @Test
    public void fullGameParamsWithFullIncrement_returnsFullFormat() {
        // given
        int gameTimeMinutes = 2;
        int gameTimeSeconds = 30;
        int incrementMinutes = 1;
        int incrementSeconds = 30;
        String expectedName = "2 min 30 sec | 1 min 30 sec";

        // when
        String name = formatter.prepareAutoName(gameTimeMinutes, gameTimeSeconds, incrementMinutes, incrementSeconds);

        // then
        assertEquals(name, expectedName);
    }

}
