package com.smartcampus.api.exceptions;

/**
 * A standardised JSON error body returned by all ExceptionMappers.
 * Ensures no raw stack traces or framework details are ever leaked to clients.
 */
public class ErrorMessage {
    private String message;
    private int status;
    private long timestamp;

    public ErrorMessage() {}

    public ErrorMessage(String message, int status) {
        this.message = message;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
