package com.tribe.app.presentation.view.widget.chat.model;

/**
 * Created by madaaflak on 07/09/2017.
 */

public class MessagePoke extends Message {
  public static final String INTENT_FUN = "FUN";
  public static final String INTENT_JEALOUS = "JEALOUS";

  private String data;
  private String clientMessageId;
  private String intent;
  private String gameId;

  public MessagePoke() {
  }

  public MessagePoke(String id) {
    super(id);
  }

  public String getData() {
    return data;
  }

  public String getClientMessageId() {
    return clientMessageId;
  }

  public String getIntent() {
    return intent;
  }

  public String getGameId() {
    return gameId;
  }

  public void setData(String data) {
    this.data = data;
  }

  public void setClientMessageId(String clientMessageId) {
    this.clientMessageId = clientMessageId;
  }

  public void setIntent(String intent) {
    this.intent = intent;
  }

  public void setGameId(String gameId) {
    this.gameId = gameId;
  }
}
