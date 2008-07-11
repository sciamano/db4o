/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package com.db4o.foundation;

import java.io.*;

/**
 * @exclude
 *
 * Just delegates to the platform chaining mechanism.
 * 
 * @decaf.mixin ChainedRuntimeExceptionMixin
 */
public abstract class ChainedRuntimeException extends RuntimeException {

    public ChainedRuntimeException() {
    }

    /**
     * @decaf.replaceFirst super(msg);
     */
    public ChainedRuntimeException(String msg) {
        super(msg, null);
    }

    /**
     * @decaf.replaceFirst super(msg);
     */
    public ChainedRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

/**
 * Provides jdk11 compatible exception chaining. decaf will mix this class
 * into {@link ChainedRuntimeException}.
 * 
 * @exclude
 */
class ChainedRuntimeExceptionMixin {

    private final ChainedRuntimeException _subject;
    private final Throwable _cause;

    public ChainedRuntimeExceptionMixin(ChainedRuntimeException mixee) {
        _subject = mixee;
        _cause = null;
    }
    
    public ChainedRuntimeExceptionMixin(ChainedRuntimeException mixee, String msg) {
        _subject = mixee;
        _cause = null;
    }
    
    public ChainedRuntimeExceptionMixin(ChainedRuntimeException mixee, String msg, Throwable cause) {
        _subject = mixee;
        _cause = cause;
    }

    /**
    * @return The originating exception, if any
    */
    public final Throwable getCause() {
        return _cause;
    }

    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(PrintStream ps) {
        printStackTrace(new PrintWriter(ps, true));
    }

    public void printStackTrace(PrintWriter pw) {
        _subject.printStackTrace(pw);
        if (_cause != null) {
            pw.println("Nested cause:");
            _cause.printStackTrace(pw);
        }
    }
}
