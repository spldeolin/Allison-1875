package com.spldeolin.allison1875.common.exception;

/**
 * @author Deolin 2023-12-28
 */
public class Allison1875Exception extends RuntimeException {

    private static final long serialVersionUID = -801498553581107750L;

    public Allison1875Exception(String message) {
        super(message);
    }

    public Allison1875Exception(Throwable cause) {
        super(cause);
    }

    public Allison1875Exception(String message, Throwable cause) {
        super(message, cause);
    }

}
