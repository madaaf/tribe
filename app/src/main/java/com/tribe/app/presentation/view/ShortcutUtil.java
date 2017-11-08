package com.tribe.app.presentation.view;

import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.utils.ListUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by madaaflak on 08/11/2017.
 */

public class ShortcutUtil {

  public static Shortcut getRecipientFromId(String userIds, User user) {
    List<String> myList = new ArrayList<>(Arrays.asList(userIds.split(",")));
    removeMyId(myList, user);
    List<String> idslist = new ArrayList<>();
    Shortcut notificationShortcut = null;
    for (Recipient recipient : user.getRecipientList()) {
      if (recipient instanceof Shortcut) {
        for (User member : ((Shortcut) recipient).getMembers()) {
          idslist.add(member.getId());
        }
        removeMyId(idslist, user);
        if (ListUtils.equalLists(myList, idslist)) {
          notificationShortcut = (Shortcut) recipient;
          myList.clear();
          idslist.clear();
          break;
        }
      }
      idslist.clear();
    }
    return notificationShortcut;
  }

  private static void removeMyId(List<String> list, User user) {
    if (list.contains(user.getId())) {
      list.remove(user.getId());
    }
  }
}
