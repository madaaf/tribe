package com.tribe.app.presentation.view.utils;

import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import java.util.List;

public class ListUtils {

  public static void addEmptyItemsHome(List<Recipient> items) {
    double minItems = 10;

    if (items.size() < minItems) {
      for (int i = items.size(); i < minItems; i++) {
        items.add(new Shortcut(Recipient.ID_EMPTY));
      }
    } else {
      items.add(new Shortcut(Recipient.ID_EMPTY));
    }
  }

  public static void addEmptyItemsSearch(List<Object> items) {
    double minItems = 10;

    if (items.size() < minItems) {
      for (int i = items.size(); i < minItems; i++) {
        items.add(new Shortcut(Recipient.ID_EMPTY));
      }
    } else {
      items.add(new Shortcut(Recipient.ID_EMPTY));
    }
  }

  public static void addEmptyItemsInvite(List<LiveInviteAdapterSectionInterface> items) {
    double minItems = 5;

    if (items.size() < minItems) {
      for (int i = items.size(); i < minItems; i++) {
        items.add(new Shortcut(Recipient.ID_EMPTY));
      }
    } else {
      items.add(new Shortcut(Recipient.ID_EMPTY));
    }
  }
}