package com.tribe.tribelivesdk.game.trivia;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by tiago on 12/12/2017.
 */

public enum TriviaCategoryEnum {

  MOVIES("movie", 11), MUSIC("music", 12), TV("tv", 14), CELEBS("celebs", 26), SPORTS("sports", 21), GEEKS(
      "geeks", 18), GENERAL("general", 9), WORLD("world", 22), GAMES("games", 15);

  private String id;
  private int category;

  TriviaCategoryEnum(String id, int category) {
    this.id = id;
    this.category = category;
  }

  public List<TriviaCategoryEnum> getCategories() {
    return new ArrayList<>(EnumSet.allOf(TriviaCategoryEnum.class));
  }

  public String getId() {
    return id;
  }

  public int getCategory() {
    return category;
  }
}
