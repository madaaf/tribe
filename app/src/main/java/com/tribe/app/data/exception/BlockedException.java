package com.tribe.app.data.exception;

/**
 * Exception throw by the application when a there is a join room exception.
 */
public class BlockedException extends Exception {

  public BlockedException() {
    super();
  }

  public BlockedException(final String message) {
    super(message);
  }

  public BlockedException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public BlockedException(final Throwable cause) {
    super(cause);
  }
}
