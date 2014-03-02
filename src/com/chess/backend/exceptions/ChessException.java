package com.chess.backend.exceptions;

import com.chess.utilities.Distribution;

/**
 * Base exception class for chess android application
 *
 * Created by electrolobzik (electrolobzik@gmail.com) on 01/03/2014.
 */
public class ChessException extends Exception {

    public ChessException(String detailMessage) {
        super(detailMessage);
    }

    public ChessException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Throws exception as runtime
     */
    public void throwAsRuntime() {

        throw new RuntimeException(this);
    }

    /**
     * Logs handled exception
     */
    public void logHandled() {

        Distribution.logException(this);
    }
}
