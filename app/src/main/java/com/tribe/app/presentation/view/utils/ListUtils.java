package com.tribe.app.presentation.view.utils;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import java.util.List;

/**
 * Created by tiago on 29/09/2016.
 */
public class ListUtils {

  public static void addEmptyItems(ScreenUtils screenUtils, List<Recipient> items) {
    double minItems =
        Math.ceil((float) screenUtils.getHeightPx() / (screenUtils.getWidthPx() >> 1)) * 2;
    if (minItems % 2 == 0) minItems++;

    if (items.size() < minItems) {
      for (int i = items.size(); i < minItems; i++) {
        items.add(new Friendship(Recipient.ID_EMPTY));
      }
    } else {
      items.add(new Friendship(Recipient.ID_EMPTY));
      items.add(new Friendship(Recipient.ID_EMPTY));

      if (items.size() % 2 == 0) {
        items.add(new Friendship(Recipient.ID_EMPTY));
      }
    }
  }
}
