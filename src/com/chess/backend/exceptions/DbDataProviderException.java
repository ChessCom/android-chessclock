package com.chess.backend.exceptions;

/**
 * Db data provider exception
 *
 * Created by electrolobzik (electrolobzik@gmail.com) on 01/03/2014.
 */
public class DbDataProviderException extends ChessException {

    public DbDataProviderException(String detailMessage) {
        super(detailMessage);
    }

    public DbDataProviderException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
