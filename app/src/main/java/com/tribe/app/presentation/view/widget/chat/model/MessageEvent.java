package com.tribe.app.presentation.view.widget.chat.model;

import android.support.annotation.StringDef;
import com.tribe.app.domain.entity.User;

/**
 * Created by madaaflak on 14/09/2017.
 */

public class MessageEvent extends Message {

  @StringDef({ ACTION_JOIN, ACTION_LEAVE, ACTION_SWITCH }) public @interface ActionType {
  }

  public static final String ACTION_JOIN = "JOIN";
  public static final String ACTION_LEAVE = "LEAVE";
  public static final String ACTION_SWITCH = "SWITCH";

  private @ActionType String action;
  private User user;

  public MessageEvent(String id) {
    super(id);
  }

  public String getContent(String displayName) {
    String content = displayName;
    switch (action) {
      case ACTION_JOIN:
        content += " joined live";
        break;
      case ACTION_LEAVE:
        content += " leaved live";
        break;
      case ACTION_SWITCH:
        content += " switched";
        break;
    }
    return content;
  }

  public String getAction() {
    return action;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
    setAuthor(user);
  }

  public void setAction(@ActionType String action) {
    this.action = action;
  }

  @Override public String toString() {
    return "MessageEvent{" + "action='" + action + '\'' + ", user=" + user + '}';
  }
}
