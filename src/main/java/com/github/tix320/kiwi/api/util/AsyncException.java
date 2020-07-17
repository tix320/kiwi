package com.github.tix320.kiwi.api.util;

public class AsyncException extends Exception {

    public AsyncException(Throwable cause, StackTraceElement[] originalStackTrace ) {

        // No message, given cause, no supression, stack trace writable
        super( null, cause, false, true );

        setStackTrace( originalStackTrace );
    }
}
