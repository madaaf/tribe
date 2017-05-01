package com.tribe.tribelivesdk.model.error;

import android.support.annotation.IntDef;

/**
 * Created by tiago on 26/01/2017.
 */

public class WebSocketError {

  @IntDef({
      ERROR_ROOM_FULL
  }) public @interface ErrorType {
  }

  public static final int ERROR_ROOM_FULL = 0x524;

  private String message;
  private int id;

  public WebSocketError(int id, String message) {
    this.id = id;
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public int getId() {
    return id;
  }
}
