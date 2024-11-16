package com.spldeolin.allison1875.common.exception;

import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;

/**
 * @author Deolin 2024-11-03
 */
public class PrimaryTypeAbsentException extends Allison1875Exception {

    private static final long serialVersionUID = -2364590836467365501L;

    public PrimaryTypeAbsentException(String message) {
        super(message);
    }

    public PrimaryTypeAbsentException(Throwable cause) {
        super(cause);
    }

    public PrimaryTypeAbsentException(String message, Throwable cause) {
        super(message, cause);
    }

}
