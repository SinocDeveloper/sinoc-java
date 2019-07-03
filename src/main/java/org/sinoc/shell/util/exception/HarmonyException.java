package org.sinoc.shell.util.exception;

public class HarmonyException extends RuntimeException {

    /**
     * Might be useful on client side to understand exact cause of exception.
     */
    private int errorCode;

    public HarmonyException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
