package com.chess.utilities;

import com.bugsense.trace.BugSense;
import com.bugsense.trace.BugSenseHandler;

/**
 * Abstraction layer for distribution tools
 *
 * Created by electrolobzik (electrolobzik@gmail.com) on 02/03/2014.
 */
public class Distribution {

    // class is static
    private Distribution() {
    }

    /**
     * Saves value of flag to send with crash
     *
     * @param flagName  name of the flag, not null
     * @param value  flag value, may be null
     */
    public static void setFlagValue(String flagName, String value) {

        BugSenseHandler.addCrashExtraData(flagName, value);
    }

    /**
     * Logs and sends exception
     *
     * @param exception  logging exception
     */
    public static void logException(Exception exception) {

        BugSenseHandler.sendException(exception);
    }

    /**
     * Enables sending of logs
     */
    public static void enableLogging() {

        BugSenseHandler.setLogging(100);
    }
}
