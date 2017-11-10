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
    if (userIds == null || user == null) {
      return null;
    }
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

  public static boolean equalShortcutMembers(List<User> one, List<User> two, User user) {
    if ((one == null) || (two == null) || (user == null)) {
      return false;
    }
    List<String> oneIds = new ArrayList<String>();
    List<String> twoIds = new ArrayList<String>();

    for (User u1 : one) {
      oneIds.add(u1.getId());
    }
    for (User u2 : two) {
      twoIds.add(u2.getId());
    }
    if (oneIds.contains(user.getId())) {
      oneIds.remove(user.getId());
    }
    if (twoIds.contains(user.getId())) {
      twoIds.remove(user.getId());
    }

    return ListUtils.equalLists(oneIds, twoIds);
  }

  public static List<User> removeMe(List<User> one, User me) {
    List<String> tempId = new ArrayList<>();
    List<User> tempUser = new ArrayList<>();
    tempUser.addAll(one);

    for (User u : one) {
      tempId.add(u.getId());
    }

    if (tempId.contains(me.getId())) {
      tempUser.remove(me);
    }
    return tempUser;
  }

  public static boolean equalShortcutMembersIds(List<String> one, List<String> two, User user) {
    if (one.contains(user.getId())) {
      one.remove(user.getId());
    }
    if (two.contains(user.getId())) {
      two.remove(user.getId());
    }
    return ListUtils.equalLists(one, two);
  }

  private static void removeMyId(List<String> list, User user) {
    if (list.contains(user.getId())) {
      list.remove(user.getId());
    }
  }
}
