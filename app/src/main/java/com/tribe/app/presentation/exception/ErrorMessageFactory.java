package com.tribe.app.presentation.exception;

import android.content.Context;
import com.tribe.app.R;
import com.tribe.app.data.exception.BlockedException;
import com.tribe.app.data.exception.JoinRoomException;
import com.tribe.app.data.exception.NetworkConnectionException;
import com.tribe.app.data.exception.RoomFullException;
import com.tribe.app.presentation.utils.EmojiParser;

/**
 * Factory used to create error messages from an Exception as a condition.
 */
public class ErrorMessageFactory {

  private ErrorMessageFactory() {
    //empty
  }

  /**
   * Creates a String representing an error message.
   *
   * @param context Context needed to retrieve string resources.
   * @param exception An exception used as a condition to retrieve the correct error message.
   * @return {@link String} an error message.
   */
  public static String create(Context context, Exception exception) {
    String message = context.getString(R.string.error_unknown);

    if (exception instanceof NetworkConnectionException) {
      message = context.getString(R.string.error_no_network);
    } else if (exception instanceof JoinRoomException) {
      message = context.getString(R.string.error_join_room);
    } else if (exception instanceof BlockedException) {
      message = EmojiParser.demojizedText(context.getString(R.string.live_notification_blocked));
    } else if (exception instanceof RoomFullException) {
      message = EmojiParser.demojizedText(context.getString(R.string.live_join_impossible));
    }

    message = EmojiParser.demojizedText(message);

    return message;
  }
}
