package com.tribe.app.presentation.view.utils;

import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.view.adapter.interfaces.HomeAdapterInterface;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListUtils {

  public static void addEmptyItemsHome(List<HomeAdapterInterface> items) {
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

  private String listToStringArray(List<String> ids) {
    StringBuilder result = new StringBuilder();

    for (String string : ids) {
      result.append("" + string + "\"");
      result.append(",");
    }

    return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
  }
  
  public static boolean equalLists(List<String> one, List<String> two) {
    if (one == null && two == null) {
      return true;
    }

    if ((one == null && two != null) || one != null && two == null || one.size() != two.size()) {
      return false;
    }

    one = new ArrayList<String>(one);
    two = new ArrayList<String>(two);

    Collections.sort(one);
    Collections.sort(two);
    return one.equals(two);
  }
}