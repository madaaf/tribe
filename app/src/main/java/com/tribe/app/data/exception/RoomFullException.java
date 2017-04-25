package com.tribe.app.data.exception;

/**
 * Exception throw by the application when a there is a join room exception.
 */
public class RoomFullException extends Exception {

  public RoomFullException() {
    super();
  }

  public RoomFullException(final String message) {
    super(message);
  }

  public RoomFullException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public RoomFullException(final Throwable cause) {
    super(cause);
  }
}
