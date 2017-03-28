package com.tribe.app.data.exception;

/**
 * Exception throw by the application when a there is a join room exception.
 */
public class JoinRoomException extends Exception {

  public JoinRoomException() {
    super();
  }

  public JoinRoomException(final String message) {
    super(message);
  }

  public JoinRoomException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public JoinRoomException(final Throwable cause) {
    super(cause);
  }
}
