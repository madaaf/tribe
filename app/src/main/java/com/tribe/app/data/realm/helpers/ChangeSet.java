package com.tribe.app.data.realm.helpers;

import io.realm.RealmResults;
import java.util.ArrayList;
import java.util.List;

/**
 * SHOUT OUT TO MY MAN @remybourgoin
 */

public class ChangeSet<T extends RealmResults<? extends Changeable>> {

  public int insertedItems = 0;
  public int deletedItems = 0;
  public Integer[] updatedItems = new Integer[] {};

  private int[] changeHashCodes;

  public ChangeSet(T t) {
    this.changeHashCodes = computeHashCodes(t);
  }

  public ChangeSet(T t, ChangeSet previousChangeSet) {
    this(t);

    int diff = changeHashCodes.length - previousChangeSet.changeHashCodes.length;
    if (diff > 0) {
      insertedItems = diff;
    } else if (diff < 0) {
      deletedItems = -diff;
    } else {
      List<Integer> updatedItemsList = new ArrayList<>();

      for (int i = 0; i < changeHashCodes.length; i++) {
        if (changeHashCodes[i] != previousChangeSet.changeHashCodes[i]) {
          updatedItemsList.add(i);
        }
      }

      updatedItems = updatedItemsList.toArray(new Integer[updatedItemsList.size()]);
    }
  }

  private int[] computeHashCodes(T t) {
    int[] hashCodes = new int[t.size()];

    for (int i = 0; i < hashCodes.length; i++) {
      hashCodes[i] = t.get(i).getChangeHashCode();
    }

    return hashCodes;
  }
}
