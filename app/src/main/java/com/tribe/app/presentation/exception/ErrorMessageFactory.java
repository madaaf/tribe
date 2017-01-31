package com.tribe.app.presentation.exception;

import android.content.Context;

import com.tribe.app.R;
import com.tribe.app.data.exception.NetworkConnectionException;
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
    exception.printStackTrace();

    String message = context.getString(R.string.error_unknown);

    if (exception instanceof NetworkConnectionException) {
      message = context.getString(R.string.error_no_network);
    }

    message = EmojiParser.demojizedText(message);

    return message;
  }
}
