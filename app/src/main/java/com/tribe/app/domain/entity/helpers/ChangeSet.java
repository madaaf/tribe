package com.tribe.app.domain.entity.helpers;

import java.util.List;

/**
 * SHOUT OUT TO MY MAN @remybourgoin
 */

public class ChangeSet<T extends List<? extends Changeable>> {

  public int insertedItems = 0;
  public int deletedItems = 0;

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
