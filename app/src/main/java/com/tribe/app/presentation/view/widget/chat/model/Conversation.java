package com.tribe.app.presentation.view.widget.chat.model;

import java.util.List;

/**
 * Created by madaaflak on 01/12/2017.
 */

public class Conversation {

  private final String ID_HOME = "home";
  private final String ID_SUGGEST_GAME = "suggest_game";

  private String id;
  private List<Message> messages;

  public Conversation(String id, List<Message> messages) {
    this.id = id;
    this.messages = messages;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<Message> getMessages() {
    return messages;
  }

  public void setMessages(List<Message> messages) {
    this.messages = messages;
  }
}
